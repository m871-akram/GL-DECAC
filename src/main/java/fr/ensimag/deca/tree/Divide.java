package fr.ensimag.deca.tree;


import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;
import fr.ensimag.ima.pseudocode.GPRegister;



import fr.ensimag.deca.DecacCompiler;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.Label;

import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;




/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        getLeftOperand().codeGenExpr(compiler, register);



        if (compiler.getRegisterManager().registreLibre()) {
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();
            getRightOperand().codeGenExpr(compiler, rRight);

            // Choix de l'instruction
            if (this.getType().isFloat()) {
                compiler.addInstruction(new DIV(rRight, register));
            } else {
                compiler.addInstruction(new QUO(rRight, register));
            }

            compiler.getRegisterManager().libererRegistre();
        } else {
            // Cas Spill (Pile pleine)
            compiler.addInstruction(new PUSH(register));
            compiler.getRegisterManager().empiler();

            getRightOperand().codeGenExpr(compiler, register);

            compiler.addInstruction(new LOAD(register, Register.R0));
            compiler.addInstruction(new POP(register));
            compiler.getRegisterManager().depiler();

            if (this.getType().isFloat()) {
                compiler.addInstruction(new DIV(Register.R0, register));
            } else {
                compiler.addInstruction(new QUO(Register.R0, register));
            }
        }

        // Vérification Erreur
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(new Label("division_par_0")));
            if (this.getType().isFloat()) {
                compiler.addInstruction(new BOV(new Label("erreur de pile_OV")));
            }
        }
    }
    


    @Override
    protected String getOperatorName() {
        return "/";
    }

}
