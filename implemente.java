package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;

/**
 * Operator "||" (ou logique)
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
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        if (branchOn) {
            // Sauter à 'target' si (A || B) est Vrai

            // 1. Si A est Vrai, tout est Vrai -> On saute à target
            getLeftOperand().codeGenBool(compiler, true, target);

            // 2. Si A est Faux, on teste B. Si B est Vrai -> On saute à target
            getRightOperand().codeGenBool(compiler, true, target);

        } else {
            // Sauter à 'target' si (A || B) est Faux

            // On a besoin d'un label pour sortir si A est Vrai (Court-circuit)
            // Car si A est Vrai, l'expression est Vraie, donc on ne doit PAS sauter au target (Faux).
            Label endOrLabel = compiler.getSequencer().genSignal("end_or");

            // 1. Si A est Vrai, on arrête (on saute à la fin de ce bloc)
            getLeftOperand().codeGenBool(compiler, true, endOrLabel);

            // 2. Si on est ici, A est Faux. Le résultat dépend de B.
            // Si B est Faux, on saute à target.
            getRightOperand().codeGenBool(compiler, false, target);

            // 3. Point de sortie si A était Vrai
            compiler.addLabel(endOrLabel);
        }
    }
}