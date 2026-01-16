package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

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
        // Rule (3.65/3.66): The receiver must be a class type [3].
        Type receiverType = receiver.verifyExpr(compiler, localEnv, currentClass);
        ClassType classType = receiverType.asClassType("Selection target must be an object", receiver.getLocation());

        // Look up the field in the class members [3].
        ExpDefinition def = classType.getDefinition().getMembers().get(field.getName());
        if (def == null || !def.isField()) {
            throw new ContextualError("Field " + field.getName() + " not found in class " + classType.getName(),
                    field.getLocation());
        }
        FieldDefinition fieldDef = (FieldDefinition) def;

        // Rule (3.66): Visibility rules for protected fields [4, 5].
        if (fieldDef.getVisibility() == Visibility.PROTECTED) {
            // (1) Receiver type must be a subtype of current class.
            // (2) Current class must be a subtype of the class declaring the field.
            if (currentClass == null ||
                    !compiler.environmentType.subType(classType, currentClass.getType()) ||
                    !compiler.environmentType.subType(currentClass.getType(), fieldDef.getContainingClass().getType())) {
                throw new ContextualError("Access to protected field " + field.getName() + " is forbidden here",
                        getLocation());
            }
        }

        field.setDefinition(fieldDef);
        this.setType(fieldDef.getType());
        return fieldDef.getType();
    }
}
}