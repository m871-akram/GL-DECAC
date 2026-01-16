package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
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

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        getLeftOperand().codeGenExpr(compiler, register);

        if (compiler.getRegisterManager().registreLibre()) {
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();
            getRightOperand().codeGenExpr(compiler, rRight);

            compiler.addInstruction(new MUL(rRight, register));

            compiler.getRegisterManager().libererRegistre();
        } else {
            compiler.addInstruction(new PUSH(register));
            compiler.getRegisterManager().empiler();

            getRightOperand().codeGenExpr(compiler, register);

            compiler.addInstruction(new LOAD(register, Register.R0));
            compiler.addInstruction(new POP(register));
            compiler.getRegisterManager().depiler();

            compiler.addInstruction(new MUL(Register.R0, register));
        }
    }

    

    @Override
    protected String getOperatorName() {
        return "*";
    }

}
