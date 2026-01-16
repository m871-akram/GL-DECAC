package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclParam extends TreeList<AbstractDeclParam>{
    protected Signature verifyListDeclParam(DecacCompiler compiler) throws ContextualError {
        Signature sign = new Signature();
        for(AbstractDeclParam param : getList()){
            sign.add(param.verifyDeclParam(compiler));
        }
        return sign;
    }
    protected void verifyListDeclParam2(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        for(AbstractDeclParam param : getList()){
            param.verifyDeclParam2(compiler, localEnv);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Unimplemented method 'decompile'");
        boolean first = true;  //pour gerer la virgule apres le premier param
        for (AbstractDeclParam param : getList()) {
            if (!first) {
                s.print(", ");
            }
            param.decompile(s);
            first = false;
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


