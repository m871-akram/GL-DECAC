package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;

import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;


import fr.ensimag.deca.context.*;


public class ListDeclParam extends TreeList<AbstractDeclParam> {

    @Override
    public void decompile(IndentPrintStream s) {
        int count = 0;
        for (AbstractDeclParam p : getList()) {
            if (count > 0) s.print(", ");
            p.decompile(s);
            count++;
        }
    }


//    /**
//     * Pass 2: Construct the method signature from parameter types [9, 17].
//     */
//    Signature verifyListDeclParam(DecacCompiler compiler) throws ContextualError {
//        Signature sig = new Signature();
//        for (AbstractDeclParam p : getList()) {
//            Type t = p.verifyParamMembers(compiler);
//            sig.add(t);
//        }
//        return sig;
//    }
//
//    /**
//     * Pass 3: Declare parameters in the local environment of the method [11, 18].
//     */
//    void verifyListDeclParamBody(DecacCompiler compiler, EnvironmentExp localEnv)
//            throws ContextualError {
//        for (AbstractDeclParam p : getList()) {
//            p.verifyParamBody(compiler, localEnv);
//        }
//    }
}


