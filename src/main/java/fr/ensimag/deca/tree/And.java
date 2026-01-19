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
            // Sauter à 'target' si (A && B) est Vrai

            // On a besoin d'un label pour sortir si A est faux (Court-circuit)
            Label endAndLabel = compiler.getSequencer().genSignal("end_and");

            // 1. Si A est Faux, on arrête tout (on saute à la fin de ce bloc)
            getLeftOperand().codeGenBool(compiler, false, endAndLabel);

            // 2. Si on est ici, A est Vrai. Donc le résultat dépend de B.
            // Si B est Vrai, on saute à la cible.
            getRightOperand().codeGenBool(compiler, true, target);

            // 3. Point de sortie si A était faux
            compiler.addLabel(endAndLabel);

        } else {
            // Sauter à 'target' si (A && B) est Faux

            // 1. Si A est Faux, tout est Faux -> On saute à target
            getLeftOperand().codeGenBool(compiler, false, target);

            // 2. Si A est Vrai, on teste B. Si B est Faux -> On saute à target
            getRightOperand().codeGenBool(compiler, false, target);
        }
    }


}



