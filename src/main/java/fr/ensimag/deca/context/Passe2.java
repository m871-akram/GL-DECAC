//La passe2 est inutile en langage sans objet

package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.*;

public class Passe2 {
    private EnvironmentType envTypes;
    private final DecacCompiler compiler;
    
    public Passe2(DecacCompiler compiler) {
        this.compiler = compiler;
    }

    
    public void verifie2(Program program) throws ContextualError {
        return ; //tous est bon en langage sans objet
    }
}
     