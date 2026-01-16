package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl51
 * @date 01/01/2026
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        boolean first = true; 
        for(AbstractExpr exp : getList()){
            if (!first) {
                s.print(", ");
            }
            exp.decompile(s);

            first = false;
        }
    }
}
