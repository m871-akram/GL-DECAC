package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Declaration of a Field
 * @author gl51
 * @date 15/01/2026
 */
public class DeclField extends AbstractDeclField {

    private final Visibility visibility;
    private final AbstractIdentifier type;
    private final AbstractIdentifier fieldName;
    private final AbstractInitialization initialization;

    public DeclField(Visibility visibility, AbstractIdentifier type,
                     AbstractIdentifier fieldName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(fieldName);
        Validate.notNull(initialization);
        this.visibility = visibility;
        this.type = type;
        this.fieldName = fieldName;
        this.initialization = initialization;
    }

    @Override
    public void verifyDeclField(DecacCompiler compiler, EnvironmentExp superClassEnv,
                                EnvironmentExp localEnv, ClassDefinition currentClassDef) throws ContextualError {

        // 1. Vérification du type
        Type fieldType = this.type.verifyType(compiler);
        if (fieldType.isVoid()) {
            throw new ContextualError("Un champ ne peut pas être de type void", getLocation());
        }

        // 2. Vérification héritage (Redéfinition)
        Symbol nameSym = fieldName.getName();
        if (superClassEnv != null) {
            ExpDefinition superDef = superClassEnv.get(nameSym);
            if (superDef != null && !superDef.isField()) {
                throw new ContextualError("Le champ " + nameSym + " masque un membre qui n'est pas un champ", getLocation());
            }
        }

        // 3. Déclaration
        // Index = index précédent + 1
        int index = currentClassDef.getNumberOfFields() + 1; // +1 car on commence à 1 (vtable à 0)
        currentClassDef.incNumberOfFields(); // Incrémente le compteur de la classe

        FieldDefinition fieldDef = new FieldDefinition(fieldType, getLocation(), visibility, currentClassDef, index);

        try {
            localEnv.declare(nameSym, fieldDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("Champ " + nameSym + " déjà déclaré dans cette classe", getLocation());
        }

        fieldName.setDefinition(fieldDef);
        fieldName.setType(fieldType);

        // 4. Vérification Initialisation
        initialization.verifyInitialization(compiler, fieldType, localEnv, currentClassDef);
    }

    @Override
    protected void verifyFieldBody(DecacCompiler compiler, EnvironmentType envTypes, 
                                    ClassDefinition nameClass) throws ContextualError {
        // Vérifier l'initialisation du champ
        Type fieldType = fieldName.getType();
        initialization.verifyInitialization(compiler, fieldType, nameClass.getMembers(), nameClass);
    }

    @Override
    protected void codeGenInitField(DecacCompiler compiler) {
        // Initialisation explicite : field = expr;
        // Si Initialization est NoInitialization, codeGenInit ne fera rien, c'est parfait.

        // 1. Récupérer l'adresse de 'this' dans R1 (convention appel init)
        // 'this' est passé en paramètre implicite (-2(LB))
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));

        // 2. Calculer l'adresse du champ : index(R1)
        FieldDefinition fieldDef = fieldName.getFieldDefinition();
        RegisterOffset fieldAddr = new RegisterOffset(fieldDef.getIndex(), Register.R1);

        // 3. Générer le code d'initialisation
        // On passe l'adresse où stocker le résultat
        initialization.codeGenInit(compiler, fieldAddr, fieldDef.getType());
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
}