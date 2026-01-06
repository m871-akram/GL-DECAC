//version pour les programmes sans objet (pas de classes ..)
//TODO , a completer apres!

package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.*;




public class Passe3 {
    private final DecacCompiler compiler;
    private EnvironmentType envTypes;

    public Passe3(DecacCompiler compiler) {
        this.compiler = compiler;
    }

    
    public void verifie3(Program program) throws ContextualError {
        if (!program.getClasses().getList().isEmpty()) {
            //sans objet ; TODO prochainement pour les objets....
            throw new ContextualError("Classes non supportées pour le moment", program.getLocation());
        }
        
        this.envTypes = new EnvironmentType(compiler);
        EnvironmentExp envExp = new EnvironmentExp(null);
        
        verifieMain(program.getMain());
    }
    


    private void verifieMain(AbstractMain main) throws ContextualError {
        if (main instanceof EmptyMain) {
            return;
        }
        
        if (main instanceof Main) {
            Main mainnNde = (Main) main;
            verifieBloc(mainNode.getDeclVariables(), mainNode.getInstructions());
        }
    }



    private void verifieBloc(ListDeclVar declVars, ListInst instructions) throws ContextualError {
        EnvironmentExp envExp = new EnvironmentExp(null);
        
        verifieListDeclVar(declVars , envExp);//verifier la liste Declaration des variables 
        verifieListInst(instructions, envExp); // et apres la liste des instructions
    }


    // a completer les autres foncrtions ....
}