package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;

import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;


public abstract class AbstractDeclParam extends Tree {


    /**
     * Passe 2 de la vérification contextuelle.
     * Vérifie le type du paramètre et retourne son type pour construire la signature [10].
     */
    protected abstract Type verifyParamMembers(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Passe 3 de la vérification contextuelle.
     * Déclare le paramètre dans l'environnement local de la méthode [11].
     */
    protected abstract void verifyParamBody(DecacCompiler compiler,
                                            EnvironmentExp envExp) throws ContextualError;
}

