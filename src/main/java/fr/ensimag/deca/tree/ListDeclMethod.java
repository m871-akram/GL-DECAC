package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclMethod extends TreeList<AbstractDeclMethod>{
    public void verifyDeclMethodPrototype(DecacCompiler compiler, EnvironmentExp superClassEnv, ClassDefinition currentClassDef,EnvironmentExp localEnv) throws ContextualError{
        for (AbstractDeclMethod method : getList()) {
            method.verifyDeclMethodPrototype(compiler, superClassEnv,currentClassDef,localEnv);
        }
    }

    public void verifyListMethodBody(DecacCompiler compiler, ClassDefinition currentClassDef)
            throws ContextualError {
        for (AbstractDeclMethod method : getList()) {
            // Création d'un environnement local pour chaque méthode ?
            // Normalement verifyDeclMethodBody s'en charge.
            // method.verifyDeclMethodBody(compiler, ..., currentClassDef);
        }
    }

    /**
     * Génération du code des méthodes (Code complet)
     */
    public void codeGenListDeclMethod(DecacCompiler compiler) {
        for (AbstractDeclMethod method : getList()) {
            method.codeGenDeclMethod(compiler);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod methode : getList()) {
                methode.decompile(s);
                s.println();
        }
    }
}