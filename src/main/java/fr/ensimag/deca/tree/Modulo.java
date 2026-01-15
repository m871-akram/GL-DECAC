package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.ima.pseudocode.GPRegister;


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
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

       // Le modulo nécessite deux entiers  et return un int
        if (!t1.isInt() || !t2.isInt()) {
            throw new ContextualError(
                "Modulo nécessite deux entiers, pas " + t1 + " et " + t2,
                this.getLocation());
        }

        setType(compiler.environmentType.INT);
        return getType();
    }


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        getLeftOperand().codeGenExpr(compiler, register);

        if (compiler.getRegisterManager().registreLibre()) {
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();
            getRightOperand().codeGenExpr(compiler, rRight);

            compiler.addInstruction(new REM(rRight, register));

            compiler.getRegisterManager().libererRegistre();
        } else {
            compiler.addInstruction(new PUSH(register));
            compiler.getRegisterManager().empiler();

            getRightOperand().codeGenExpr(compiler, register);

            compiler.addInstruction(new LOAD(register, Register.R0));
            compiler.addInstruction(new POP(register));
            compiler.getRegisterManager().depiler();

            compiler.addInstruction(new REM(Register.R0, register));
        }

        // Vérification Erreur
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(new Label("division_par_0")));
        }
    }


    @Override
    protected String getOperatorName() {
        return "%";
    }

}
