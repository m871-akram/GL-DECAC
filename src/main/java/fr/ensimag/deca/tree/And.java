package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {

    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        if (branchOn) {
            // saut si (A && B) vrai

            // label pour sortir si a faux
            Label endAndLabel = compiler.getSequencer().genSignal("end_and");

            // si a faux -> fin
            getLeftOperand().codeGenBool(compiler, false, endAndLabel);

            // si a vrai, teste b
            getRightOperand().codeGenBool(compiler, true, target);

            // point de sortie
            compiler.addLabel(endAndLabel);

        } else {
            // saut si (A && B) faux

            // si a faux -> saut target
            getLeftOperand().codeGenBool(compiler, false, target);

            // si a vrai, teste b faux
            getRightOperand().codeGenBool(compiler, false, target);
        }
    }


}



