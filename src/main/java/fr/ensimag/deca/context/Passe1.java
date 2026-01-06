//La passe1 est  presque inutile en langage sans objet

package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.*;

public class Passe1 {
    private EnvironmentType envTypes;
    private final DecacCompiler compiler;
    
    public Passe1(DecacCompiler compiler) {
        this.compiler = compiler;
    }

    
    public void verifie1(Program program) throws ContextualError {
        return ; //tous est bon en langage sans objet
        //Je reviens vers vous !
    }
}
     