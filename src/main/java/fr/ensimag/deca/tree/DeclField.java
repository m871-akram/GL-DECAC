package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.io.PrintStream;

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
        if (visibility == Visibility.PROTECTED) {
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

//    protected void verifyDeclField(DecacCompiler compiler, Symbol currentClass, Symbol superClass)
//            throws ContextualError {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
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


