package fr.ensimag.deca.tree;


import fr.ensimag.deca.tools.IndentPrintStream;


public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod m : getList()) {
            m.decompile(s);
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


