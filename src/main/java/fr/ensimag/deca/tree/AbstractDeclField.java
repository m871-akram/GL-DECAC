package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.context.EnvironmentType;


public abstract class AbstractDeclField extends Tree {

    /**
     * Passe 2 de la vérification contextuelle.
     * Vérifie la déclaration du champ (type, visibilité) et l'ajoute à l'environnement.
     */
    protected abstract void verifyFieldMembers(DecacCompiler compiler,
                                               Symbol superClass, Symbol nameClass) throws ContextualError;

    /**
     * Passe 3 de la vérification contextuelle.
     * Vérifie le corps de l'initialisation du champ.
     */
    protected abstract void verifyFieldBody(DecacCompiler compiler,
                                            EnvironmentType envTypes, ClassDefinition nameClass) throws ContextualError;

    /**
     * Génération de code pour l'initialisation par défaut et explicite du champ.
     * Utilisé dans le sous-programme init.A [5].
     */
    protected abstract void codeGenInitField(DecacCompiler compiler);


}

