package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.MUL;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;


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
        compiler.addInstruction(new MUL(opSource, opDest));
    }

    

    @Override
    protected String getOperatorName() {
        return "*";
    }

}
