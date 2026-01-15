package fr.ensimag.deca.tree;


import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.deca.DecacCompiler;

import fr.ensimag.ima.pseudocode.Label;

import fr.ensimag.ima.pseudocode.instructions.BOV;




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
    protected Instruction getInstruction(GPRegister op1, GPRegister op2) {
        if (this.getType().isFloat()) {
            return new DIV(op1, op2);
        } else {
            return new QUO(op1, op2);
        }
    }
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        super.codeGenExpr(compiler, register);
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
