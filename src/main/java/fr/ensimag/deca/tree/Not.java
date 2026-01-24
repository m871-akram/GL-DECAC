package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        Type operand = this.getOperand().verifyExpr(compiler, localEnv, currentClass);
        if (!operand.isBoolean()) {
            throw new ContextualError("operation not " + operand + " invalide", this.getOperand().getLocation());
        }
        setType(operand);
        return operand;
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }


    @Override
    protected void codeGenUnary(DecacCompiler compiler, GPRegister register) {
        // si reg=0 -> seq met a 1, si reg=1 -> seq met a 0
        compiler.addInstruction(new CMP(0, register));
        compiler.addInstruction(new SEQ(register));
    }

    /**
     * optim flux controle - inverse condition
     */
    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        // inverse branchon
        getOperand().codeGenBool(compiler, !branchOn, target);
    }
}
