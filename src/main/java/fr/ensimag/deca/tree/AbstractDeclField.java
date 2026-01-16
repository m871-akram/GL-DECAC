package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Class abstarite Field.
 *
 * @author G51
 * @date 15/01/2026
 */
public abstract class AbstractDeclField extends Tree {

    protected abstract void verifyDeclField(DecacCompiler compiler, 
                               EnvironmentExp superClassEnv,
                               EnvironmentExp localEnv, 
                               ClassDefinition currentClassDef) throws ContextualError;

    protected abstract void codeGenDeclVar(DecacCompiler compiler);

    protected abstract void verifyDeclField2(DecacCompiler compiler, ClassDefinition currentClassDef,
            EnvironmentExp localEnv) throws ContextualError;
    

}
