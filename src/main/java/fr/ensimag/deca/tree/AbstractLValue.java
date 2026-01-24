package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Left-hand side value of an assignment.
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractLValue extends AbstractExpr {

    protected abstract void codeGenStore(DecacCompiler compiler, GPRegister register);
}
