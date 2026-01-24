package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler, fr.ensimag.ima.pseudocode.DVal opSource,
                               fr.ensimag.ima.pseudocode.GPRegister opDest) {
        throw new UnsupportedOperationException("on fait pas Or dans ce contexte la !!");
    }

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        if (branchOn) {
            // saut si (A || B) vrai

            // si a vrai -> saut target
            getLeftOperand().codeGenBool(compiler, true, target);

            // si a faux, teste b vrai
            getRightOperand().codeGenBool(compiler, true, target);

        } else {
            // saut si (A || B) faux

            // label pour sortir si a vrai
            Label endOrLabel = compiler.getSequencer().genSignal("end_or");

            // si a vrai -> fin
            getLeftOperand().codeGenBool(compiler, true, endOrLabel);

            // si a faux, teste b faux
            getRightOperand().codeGenBool(compiler, false, target);

            // point sortie
            compiler.addLabel(endOrLabel);
        }


    }


}


