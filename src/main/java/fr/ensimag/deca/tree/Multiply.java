package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.SHL;
import fr.ensimag.ima.pseudocode.instructions.STORE;


/**
 * @author gl51
 * @date 01/01/2026
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
    
    // Implémente juste l'instruction spécifique
    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        if(getType().isInt()){
            int shiftL = getLeftOperand().isPowerOftow();
            int shiftR = getRightOperand().isPowerOftow();
            if (shiftL != -1){
                compiler.addInstruction(new LOAD(opSource, opDest));
                for(int i =0;i<shiftL;i++){
                    compiler.addInstruction(new SHL(opDest));
                }
            } else if(shiftR != -1){
                for(int i =0;i<shiftR;i++){
                    compiler.addInstruction(new SHL(opDest));
                }
            }
            
        } else {
            compiler.addInstruction(new MUL(opSource, opDest));
        }
    }

    

    @Override
    protected String getOperatorName() {
        return "*";
    }

}
