package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;

public class Selection extends AbstractLValue {

    private AbstractExpr receiver;
    private AbstractIdentifier field;

    public Selection(AbstractExpr receiver, AbstractIdentifier field) {
        this.receiver = receiver;
        this.field = field;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        receiver.decompile(s);
        s.print(".");
        field.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        receiver.prettyPrint(s, prefix, false);
        field.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        receiver.iter(f);
        field.iter(f);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Évaluer l'objet (partie gauche)
        getObject().codeGenExpr(compiler, register);

        // 2. Vérifier null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.addInstruction(new BEQ(new Label("dereferencement_null")));
        }

        // 3. Accéder au champ
        int fieldOffset = getFieldName().getFieldDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(fieldOffset, register), register));
    }

    // Pour les affectations (objet.champ = expr)
    protected DAddr codeGenLValue(DecacCompiler compiler) {
        GPRegister regObject = compiler.getRegisterManager().prendreRegistre();
        getObject().codeGenExpr(compiler, regObject);

        // Vérifier null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), regObject));
            compiler.addInstruction(new BEQ(new Label("dereferencement_null")));
        }

        int fieldOffset = getFieldName().getFieldDefinition().getIndex();
        return new RegisterOffset(fieldOffset, regObject);
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type objectType = getObject().verifyExpr(compiler, localEnv, currentClass);
        ClassDefinition classDef = objectType.asClassType("Sélection impossible sur un type non-classe", getLocation()).getDefinition();
        var def = classDef.getMembers().get(getItem().getName());

        if (def == null) {
            throw new ContextualError(
                "Champ " + getItem().getName().getName() + " inexistant",
                getLocation()
            );
        }

        if (!def.isField()) {
            throw new ContextualError(
                getItem().getName().getName() + " n'est pas un champ",
                getLocation()
            );
        }

        FieldDefinition fieldDef = def.asFieldDefinition("Ce n'est pas un champ", getLocation());

        if (fieldDef.getVisibility()== Visibility.PROTECTED && !currentClass.isSubClassOf(fieldDef.getContainingClass())) {
            throw new ContextualError(
                "Champ PROTECTED '" + getItem().getName().getName() + "' non accessible dans cette classe",
                getLocation()
            );
        }

        getItem().setDefinition(fieldDef);
        Type fieldType = fieldDef.getType();
        setType(fieldType);
        return fieldType;
    }
}