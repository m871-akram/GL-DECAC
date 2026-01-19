package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.io.PrintStream;



/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");
        
        classes.verifyListClass(compiler);
        classes.verifyListClassMembers(compiler);
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
        LOG.debug("verify program: end");
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        //  passe 1 : Partie "Déclarations de classes" (Table des méthodes)
        compiler.addComment("Construction des tables des methodes");
        classes.codeGenListDeclClass(compiler);

        // PASSE 2A : Init des objets
        compiler.addComment("===== Sous-programmes d'initialisation =====");
        classes.codeGenListInit(compiler);

        // PASSE 2B : Code des méthodes
        classes.codeGenListMethods(compiler);

        //  passe 2 :Partie "Programme Principal"
        compiler.addComment("Main program");
        main.codeGenMain(compiler); // reg manager compte les variables globales via declvar et les spill
        compiler.addInstruction(new HALT()); // fin normale du programme

        // gestion des erreurs

        compiler.getIrqController().flashServiceRoutines(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
