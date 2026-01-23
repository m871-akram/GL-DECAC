package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl51
 * @date 01/01/2026
 */
public class NoInitialization extends AbstractInitialization {

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
//        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    protected void codeGenInit(DecacCompiler compiler, Operand target, Type type) {
        boolean isField = false;
        if (target instanceof RegisterOffset) {
            // Astuce : R1 est utilisé pour 'this' lors de l'init des champs
            isField = ((RegisterOffset) target).getRegister() == Register.R1;
        }

        if (isField) {
            // Charger la valeur par défaut (0, 0.0, null) dans R0
            if (type.isFloat()) {
                compiler.addInstruction(new LOAD(new ImmediateFloat(0.0f), Register.R0));
            } else if (type.isClass()) {
                compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
            } else {
                // int, boolean -> 0
                compiler.addInstruction(new LOAD(0, Register.R0));
            }

            // Stocker R0 dans le champ
            compiler.addInstruction(new STORE(Register.R0, (DAddr) target));
        }
    }

    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}
