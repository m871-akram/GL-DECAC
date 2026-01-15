package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;


/**
 * @author gl51
 * @date 01/01/2026
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Calcul gauche
        getLeftOperand().codeGenExpr(compiler, register);

        // 2. Calcul droite


        if (compiler.getRegisterManager().registreLibre()) {
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();
            getRightOperand().codeGenExpr(compiler, rRight);

            // Instruction spécifique : ADD
            compiler.addInstruction(new ADD(rRight, register));

            compiler.getRegisterManager().libererRegistre();
        } else {
            // Spill
            compiler.addInstruction(new PUSH(register));
            compiler.getRegisterManager().empiler();

            getRightOperand().codeGenExpr(compiler, register);

            compiler.addInstruction(new LOAD(register, Register.R0));
            compiler.addInstruction(new POP(register));
            compiler.getRegisterManager().depiler();

            // Instruction spécifique : ADD avec R0
            compiler.addInstruction(new ADD(Register.R0, register));
        }
    }

    
}
