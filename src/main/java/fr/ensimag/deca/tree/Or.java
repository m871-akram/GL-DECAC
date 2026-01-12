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



   // compteur  pour générer des labels uniques 
    private static int count = 0;

    @Override
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        
        if (branchOn) {
            // sauter si (A || B) est Vrai
            
            
            getLeftOperand().codeGenBool(compiler, true, target);
            getRightOperand().codeGenBool(compiler, true, target);
            
        } else {
            // sauter si (A || B) est Faux
        
            count++;
            Label endOrLabel = new Label("end_or." + count);
            
            // Si A est Vrai, on saute à la fin 
            getLeftOperand().codeGenBool(compiler, true, endOrLabel);
            // Si B est Faux, alors tout est Faux -> on saute à target.
            getRightOperand().codeGenBool(compiler, false, target);
            
            compiler.addLabel(endOrLabel);
        }

        
    }






}


