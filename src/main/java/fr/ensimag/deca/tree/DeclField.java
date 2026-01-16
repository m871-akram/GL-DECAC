package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;


/**
 * Declaration of a Field
 * @author G51
 * @date 15/01/2026
 */

public class DeclField extends AbstractDeclField {

    private final Visibility visibility;
    private final AbstractIdentifier type;
    private final AbstractIdentifier fieldName;
    private final AbstractInitialization initialization;

    public DeclField(Visibility visibility, AbstractIdentifier type,
                     AbstractIdentifier fieldName, AbstractInitialization initialization) {
        this.visibility = visibility;
        this.type = type;
        this.fieldName = fieldName;
        this.initialization = initialization;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (visibility == Visibility.PROTECTED) {//pas besoin si ce n'est pas protected
            s.print("protected ");
        }
        type.decompile(s);
        s.print(" ");
        fieldName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        initialization.iter(f);
    }

    protected void verifyDeclField(DecacCompiler compiler, Symbol currentClass, Symbol superClass)
            throws ContextualError {
        // on vérifier le type du champ
        Type fieldType = this.type.verifyType(compiler);
        if (fieldType.isVoid()) {
            throw new ContextualError("Un champ ne peut pas être de type void", getLocation());
        }

        // on vérifier si le champ existe déjà dans la super classe
        Symbol fieldName = this.name.getName();
        if (superClassEnv != null && superClassEnv.get(fieldName) != null) {
            // on vérifier que c'est bien un champ (et pas une méthode par exemple)
            ExpDefinition def = superClassEnv.get(fieldName);
            if (!def.isField()) {
                throw new ContextualError(
                    fieldName.getName() + ":existe déjà dans la super classe mais n'est pas un champ",
                    getLocation()
                );
            }
        }



        FieldDefinition fieldDef = new FieldDefinition(
            fieldType,
            getLocation(),
            this.visibility, currentClassDef, currentClassDef.getNumberOfFields()
        );

        try {
            localEnv.declare(fieldName, fieldDef);
        } catch (DoubleDefException e) {
            throw new ContextualError(e.getMessage(), getLocation());
        }
    }
//
//    @Override
//    protected void verifyFieldMembers(DecacCompiler compiler, Symbol superClass, Symbol nameClass)
//            throws ContextualError {
//        // Règle (2.5) : Vérifier que le type n'est pas void
//        Type t = type.verifyType(compiler);
//        if (t.isVoid()) {
//            throw new ContextualError("Un champ ne peut pas être de type void", type.getLocation());
//        }
//
//        ClassDefinition currentClass = (ClassDefinition) compiler.environmentType.defOfType(nameClass);
//        // Calcul de l'index du champ (nombre de champs de la superclasse + 1)
//        int index = currentClass.getSuperClass().getNumberOfFields() + 1;
//
//        FieldDefinition fieldDef = new FieldDefinition(t, fieldName.getLocation(), visibility, currentClass, index);
//
//        // Tentative de déclaration dans l'environnement de la classe
//        try {
//            currentClass.getMembers().declare(fieldName.getName(), fieldDef);
//        } catch (EnvironmentExp.DoubleDefException e) {
//            throw new ContextualError("Le champ " + fieldName.getName() + " est déjà défini dans cette classe",
//                    fieldName.getLocation());
//        }
//
//        fieldName.setDefinition(fieldDef);
//        fieldName.setType(t);
//    }
//
//    @Override
//    protected void verifyFieldBody(DecacCompiler compiler, EnvironmentType envTypes, ClassDefinition nameClass)
//            throws ContextualError {
//        // Règle (3.7) : Vérifier l'initialisation par rapport au type du champ
//        Type t = type.getType();
//        initialization.verifyInitialization(compiler, t, nameClass.getMembers(), nameClass);
//    }


    @Override
    protected void codeGenInitField(DecacCompiler compiler) {
        // Étape C (4.3) : Générer le code pour l'initialisation par défaut ou explicite
        initialization.codeGenInit(compiler, fieldName.getFieldDefinition());
    }
}


