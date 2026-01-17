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
    public void verifyDeclMethodContent(DecacCompiler compiler,EnvironmentExp localEnv, ClassDefinition currentClassDef) throws ContextualError{
        for (AbstractDeclMethod method : getList()) {
            method.verifyDeclMethodContent(compiler, currentClassDef, localEnv);
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
