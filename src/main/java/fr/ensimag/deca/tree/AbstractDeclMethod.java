package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;


public abstract class AbstractDeclMethod extends Tree {


    /**
     * Passe 3 de la vérification contextuelle.
     * Vérifie le corps de la méthode (variables locales et instructions) [9].
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler,
                                             EnvironmentType envTypes, ClassDefinition nameClass) throws ContextualError;


    protected abstract void verifyDeclMethodPrototype(DecacCompiler compiler,
                                                      EnvironmentExp superClassEnv, ClassDefinition currentClassDef, EnvironmentExp localEnv)
            throws ContextualError;


    /**
     * Génération du code assembleur de la méthode
     */
    protected abstract void codeGenDeclMethod(DecacCompiler compiler);
}