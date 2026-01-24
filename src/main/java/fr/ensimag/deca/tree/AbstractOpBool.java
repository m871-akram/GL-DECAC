package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {

        getLeftOperand().verifyCondition(compiler, localEnv, currentClass);
        getRightOperand().verifyCondition(compiler, localEnv, currentClass);

        Type resultType = compiler.environmentType.BOOLEAN;
        setType(resultType);
        return resultType;
    }


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {

        // sequencer unique pour eviter pb en parallele
        Label trueLabel = compiler.getSequencer().genSignal("bool_true");
        Label endLabel = compiler.getSequencer().genSignal("bool_end");

        // si vrai -> saut
        this.codeGenBool(compiler, true, trueLabel);

        // cas faux -> charge 0
        compiler.addInstruction(new LOAD(0, register));
        compiler.addInstruction(new BRA(endLabel));

        // cas vrai -> charge 1
        compiler.addLabel(trueLabel);
        compiler.addInstruction(new LOAD(1, register));

        compiler.addLabel(endLabel);
    }
}