package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

public abstract class AbstractDeclParam extends Tree{
    protected abstract Type verifyDeclParam(DecacCompiler compiler)
            throws ContextualError;

    protected abstract void codeGenDeclVar(DecacCompiler compiler);

    protected abstract void verifyDeclParamEnv(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError;
}
