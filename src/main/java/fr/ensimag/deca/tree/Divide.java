package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptController;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;




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
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        if (getType().isInt()) {
            compiler.addInstruction(new QUO(opSource, opDest));
        } else {
            compiler.addInstruction(new DIV(opSource, opDest));
        }

        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptController.Vector.IRQ_DIV_BY_ZERO);
        }
    }
    


    @Override
    protected String getOperatorName() {
        return "/";
    }

}
