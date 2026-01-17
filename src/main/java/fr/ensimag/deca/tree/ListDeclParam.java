package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclParam extends TreeList<AbstractDeclParam> {

    /**
     * Passe 2 : Construit la signature de la méthode
     */
    protected Signature verifyListDeclParam(DecacCompiler compiler) throws ContextualError {
        Signature sign = new Signature();
        for (AbstractDeclParam param : getList()) {
            sign.add(param.verifyDeclParam(compiler));
        }
        return sign;
    }

    /**
     * Passe 3 : Vérifie et déclare les paramètres dans le corps
     */
    protected void verifyListDeclParamBody(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        for (AbstractDeclParam param : getList()) {
            param.verifyDeclParamBody(compiler, localEnv);
        }
    }

    /**
     * Génération de code : Associe chaque paramètre à son offset dans la pile
     * Appelé au début de codeGenMethod
     */
    public void codeGenListDeclParam(DecacCompiler compiler) {
        int index = 0;
        for (AbstractDeclParam param : getList()) {
            param.codeGenDeclParam(compiler, index);
            index++;
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