package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;


public abstract class AbstractDeclParam extends Tree {

    /**
     * Vérifie le type du paramètre (Passe 2)
     */
    protected abstract Type verifyDeclParam(DecacCompiler compiler) throws ContextualError;

    /**
     * Déclare le paramètre dans l'environnement local (Passe 3)
     */
    protected abstract void verifyDeclParamBody(DecacCompiler compiler, EnvironmentExp envExp) throws ContextualError;

    /**
     * Génère le code pour lier le paramètre à son adresse mémoire (Pile LB)
     */
    protected abstract void codeGenDeclParam(DecacCompiler compiler, int index);

}