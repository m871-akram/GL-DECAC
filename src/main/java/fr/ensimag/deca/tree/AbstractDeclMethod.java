package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;

/**
 * Class Abstraite DeclMethod.
 *
 * @author G51
 * @date 15/01/2026
 */
public abstract class AbstractDeclMethod extends Tree {

    protected abstract void verifyDeclMethodPrototype(DecacCompiler compiler,
                                         EnvironmentExp superClassEnv, ClassDefinition currentClassDef, EnvironmentExp localEnv)
        throws ContextualError;

    protected abstract void verifyDeclMethodBody(DecacCompiler compiler,EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError ;

    protected abstract void verifyDeclMethodContent(DecacCompiler compiler, ClassDefinition currentClassDef,
            EnvironmentExp localEnv) throws ContextualError;

    /**
     * Génération du code assembleur de la méthode
     */
    protected abstract void codeGenDeclMethod(DecacCompiler compiler);
}
