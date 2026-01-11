package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

       // Le modulo nécessite deux entiers  et return un int
        if (!t1.isInt() || !t2.isInt()) {
            throw new ContextualError(
                "Modulo nécessite deux entiers, pas " + t1 + " et " + t2,
                this.getLocation());
        }

        setType(compiler.environmentType.INT);
        return getType();
    }


    @Override
    protected Instruction getInstruction(GPRegister op1, GPRegister op2) {
        return new REM(op1, op2);
        
    }


    @Override
    protected String getOperatorName() {
        return "%";
    }

}
