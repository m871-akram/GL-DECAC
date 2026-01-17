package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
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
    protected void verifyListDeclParamEnv(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        for(AbstractDeclParam param : getList()){
            param.verifyDeclParamEnv(compiler, localEnv);
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

}
