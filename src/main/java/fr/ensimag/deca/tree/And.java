package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
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
    protected String getOperatorName() {
        return "&&";
    }


    // compteur  pour générer des labels uniques 
    private static int count = 0;

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        
        if (branchOn) {
            // sauter à 'target' si (A && B) est Vrai

            count++;
            Label endAndLabel = new Label("end_and." + count);
            
            // Si A est faux, on saute à la fin 
            getLeftOperand().codeGenBool(compiler, false, endAndLabel);
            
           
            // Si B est vrai, on saute à target.
            getRightOperand().codeGenBool(compiler, true, target);
            
            // Point de sortie si A était faux
            compiler.addLabel(endAndLabel);
            
        } else {
            // Sauter à 'target' si (A && B) est Faux
            
            getLeftOperand().codeGenBool(compiler, false, target);
            getRightOperand().codeGenBool(compiler, false, target);
            
        }
    }


}



