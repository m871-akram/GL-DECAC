package fr.ensimag.deca.tree;


import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;




/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected Instruction getInstruction(GPRegister op1, GPRegister op2) {
        if (this.getType().isInt()) {

            return new QUO(op1, op2);
        } else {
            return new DIV(op1, op2);
        }
    }
    


    @Override
    protected String getOperatorName() {
        return "/";
    }

}
