package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.*;
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



        // ===== PASSE 2 : Programme Principal =====

        //  passe 2 :Partie "Programme Principal"
        compiler.addComment("Main program");
        main.codeGenMain(compiler); // reg manager compte les variables globales via declvar et les spill
        compiler.addInstruction(new HALT()); // fin normale du programme

        // gestion des erreurs

        // erreur de pile_OV
        compiler.addLabel(new Label("erreur_pile_OV"));
        compiler.addInstruction(new WSTR("Error: pile_OV"));
    
        compiler.addInstruction(new WNL());

        compiler.addInstruction(new ERROR());

        // erreur_io

        compiler.addLabel(new Label("erreur_io"));
        compiler.addInstruction(new WSTR("Error: erreur I/O"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());

        // erreur de division_par_0
         compiler.addLabel(new Label("division_par_0"));
         compiler.addInstruction(new WSTR("Error: division par 0"));
         compiler.addInstruction(new WNL());
         compiler.addInstruction(new ERROR());

//        // 5. Null Dereference - Pour Selection/MethodCall
//        compiler.addLabel(new Label("null_dereference"));
//        compiler.addInstruction(new WSTR("Error: Null dereference"));
//        compiler.addInstruction(new WNL());
//        compiler.addInstruction(new ERROR());


        // Partie En-tête du programme (TSTO / ADDSP) en ordre LIFO
        int maxTemp = compiler.getRegisterManager().getTaillePileMax();
        int nbGlob = compiler.getRegisterManager().getNbGlobales();

        // 3 ADDSP #nbGlob
        if (nbGlob > 0) {
            compiler.addFirstInstruction(new ADDSP(nbGlob));
        }

        // 2 BOV erreur de pile_OV
        compiler.addFirstInstruction(new BOV(new Label("erreur_pile_OV")));

        // 1 TSTO #(maxTemp + nbGlob)
        compiler.addFirstInstruction(new TSTO(maxTemp + nbGlob));

//        // 2. Vérification débordement pile initiale
//        if (!compiler.getCompilerOptions().getNoCheck()) {
//            compiler.addFirstInstruction(new BOV(new Label("stack_overflow_error")));
//            // 1. TSTO
//            compiler.addFirstInstruction(new TSTO(new ImmediateInteger(maxTemp + nbGlob)));
//        }


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
