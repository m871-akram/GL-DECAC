package fr.ensimag.deca.tree;
import fr.ensimag.ima.pseudocode.instructions.SGE;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;



/**
 * Operator "x >= y"
 *
 * @author gl51
 * @date 01/01/2026
 */
public class GreaterOrEqual extends AbstractOpIneq {

    public GreaterOrEqual(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return ">=";
    }

    @Override
    protected Instruction getAsmCode(GPRegister register) { return new SGE(register); }

    }
