package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;


public abstract class AbstractMethodBody extends Tree {

    /**
     * Passe 3 de la vérification contextuelle (Règle 3.11).
     */
    protected abstract void verifyMethodBody(DecacCompiler compiler,
                                             EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType)
            throws ContextualError;

    /**
     * Génération de code pour le corps de la méthode (Passe 2 de l'étape C).
     */
    protected abstract void codeGenMethodBody(DecacCompiler compiler);
    Type returnType;
    protected void setType(Type returnType) {
        this.returnType = returnType;
    };
    protected Type getType() {
        return returnType;
    };
}


