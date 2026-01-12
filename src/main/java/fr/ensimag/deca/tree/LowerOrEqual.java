package fr.ensimag.deca.tree;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.SLE;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class LowerOrEqual extends AbstractOpIneq {
    public LowerOrEqual(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override protected Instruction getSccInstruction(GPRegister register) { return new SLE(register); }


    @Override
    protected String getOperatorName() {
        return "<=";
    }

}
