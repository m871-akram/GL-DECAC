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

    /**
     * Passe 2 de la vérification contextuelle.
     * Vérifie la déclaration du champ (type, visibilité) et l'ajoute à l'environnement.
     */
    protected abstract void verifyDeclField(DecacCompiler compiler,
                                            EnvironmentExp superClassEnv,
                                            EnvironmentExp localEnv,
                                            ClassDefinition currentClassDef) throws ContextualError;


    /**
     * Génération de code pour l'initialisation par défaut et explicite du champ.
     * Utilisé dans le sous-programme init.A [5].
     */
    protected abstract void codeGenInitField(DecacCompiler compiler);

    protected abstract void verifyDeclFieldInit(DecacCompiler compiler, ClassDefinition currentClassDef,
                                                EnvironmentExp localEnv) throws ContextualError;

}
