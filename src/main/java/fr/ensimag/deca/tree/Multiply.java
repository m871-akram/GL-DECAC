package fr.ensimag.deca.tree;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.MUL;


/**
 * @author gl51
 * @date 01/01/2026
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    

    @Override
    protected String getOperatorName() {
        return "*";
    }

    @Override
    protected Instruction getInstruction(GPRegister op1, GPRegister op2) {
        return new MUL(op1, op2);
    }

}
