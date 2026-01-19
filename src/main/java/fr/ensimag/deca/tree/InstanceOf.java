package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;


/**
 *
 * @author gl51
 * @date 15/01/2026
 */
public class InstanceOf extends AbstractExpr {
    private AbstractExpr leftOperand;
    private AbstractIdentifier rightOperand;
    public InstanceOf(AbstractExpr leftOperand, AbstractIdentifier rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = leftOperand.verifyExpr(compiler, localEnv, currentClass);
        Type classType = rightOperand.verifyType(compiler);

        if (!exprType.isClassOrNull()) {
            throw new ContextualError(
                "instanceof ne s'applique qu'à un objet",
                leftOperand.getLocation());
        }

        if (!classType.isClass()) {
            throw new ContextualError(
                "instanceof attend un type classe",
                rightOperand.getLocation());
        }

        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        ClassDefinition targetClassDef = (ClassDefinition) rightOperand.getDefinition();

        Label endLabel = compiler.getSequencer().genSignal("instanceof_end");
        Label loopLabel = compiler.getSequencer().genSignal("instanceof_loop");
        Label trueLabel = compiler.getSequencer().genSignal("instanceof_true");

        // evalue objet
        leftOperand.codeGenExpr(compiler, register);

        // si null -> false
        compiler.addInstruction(new CMP(new NullOperand(), register));
        compiler.addInstruction(new BEQ(endLabel));

        // charge vtable objet
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), Register.R0));

        // charge vtable cible
        RegisterOffset targetVTableAddr = (RegisterOffset) targetClassDef.getOperand();
        compiler.addInstruction(new LEA(targetVTableAddr, Register.R1));

        compiler.addLabel(loopLabel);
        compiler.addInstruction(new CMP(Register.R1, Register.R0));
        compiler.addInstruction(new BEQ(trueLabel));

        // Remonter au parent
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));
        compiler.addInstruction(new CMP(new NullOperand(), Register.R0));
        compiler.addInstruction(new BNE(loopLabel));

        // Pas trouvé -> False (0)
        compiler.addInstruction(new LOAD(0, register));
        compiler.addInstruction(new BRA(endLabel));

        // Trouvé -> True (1)
        compiler.addLabel(trueLabel);
        compiler.addInstruction(new LOAD(1, register));

        compiler.addLabel(endLabel);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        leftOperand.decompile(s);
        s.print(" instanceof ");
        rightOperand.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }
}