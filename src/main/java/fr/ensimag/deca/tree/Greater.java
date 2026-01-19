package fr.ensimag.deca.tree;




import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.SGT;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Greater extends AbstractOpIneq {

    public Greater(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected Instruction getAsmCode(GPRegister register) { return new SGT(register); }


    @Override
    protected String getOperatorName() {
        return ">";
    }

}
