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
    public void verifyDeclMethodPrototype2(DecacCompiler compiler,EnvironmentExp localEnv, ClassDefinition currentClassDef) throws ContextualError{
        for (AbstractDeclMethod method : getList()) {
            method.verifyDeclMethodPrototype2(compiler, currentClassDef, localEnv);
        }
    }
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod methode : getList()) {
                methode.decompile(s);
                s.println();
        }
    }

//    /**
//     * Pass 2: Verify method signatures and handle inheritance/overriding [9, 14].
//     */
//    void verifyListDeclMethod(DecacCompiler compiler, Symbol superClass)
//            throws ContextualError {
//        for (AbstractDeclMethod m : getList()) {
//            m.verifyMethodMembers(compiler, superClass);
//        }
//    }
//
//    /**
//     * Pass 3: Verify method bodies (instructions and local variables) [11, 15].
//     */
//    void verifyListDeclMethodBody(DecacCompiler compiler, EnvironmentType envTypes, ClassDefinition nameClass)
//            throws ContextualError {
//        for (AbstractDeclMethod m : getList()) {
//            m.verifyMethodBody(compiler, envTypes, nameClass);
//        }
//    }

}


