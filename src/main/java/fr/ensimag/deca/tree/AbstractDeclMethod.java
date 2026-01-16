package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.context.EnvironmentType;


public abstract class AbstractDeclMethod extends Tree {

    /**
     * Passe 2 de la vérification contextuelle.
     * Vérifie la signature de la méthode et gère la redéfinition [7].
     */
    protected abstract void verifyMethodMembers(DecacCompiler compiler,
                                                Symbol superClass) throws ContextualError;

    /**
     * Passe 3 de la vérification contextuelle.
     * Vérifie le corps de la méthode (variables locales et instructions) [9].
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler,
                                             EnvironmentType envTypes, ClassDefinition nameClass) throws ContextualError;

    /**
     * Génération de code pour le corps de la méthode (étiquette code.A.m) [8].
     */
    protected abstract void codeGenDeclMethod(DecacCompiler compiler);
}
