package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
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
    private ListDeclClass classes;
    private AbstractMain main;

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
        // jump pour sauter code classes
        Label mainLabel = new Label("main_start");
        Label vtableInitLabel = new Label("vtable_init");

        // Sauter vers l'initialisation des VTables (qui sautera ensuite vers main)
        compiler.addInstruction(new BRA(vtableInitLabel));

        // 1. Générer le code de la méthode Object.equals (une méthode, pas exécutée au démarrage)
        compiler.addComment("Méthode Object.equals (comparaison référentielle)");
        compiler.addLabel(new Label("equals"));
        // Compare this (-2(LB)) et param (-3(LB))
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R0));
        compiler.addInstruction(new LOAD(new RegisterOffset(-3, Register.LB), Register.R1));
        compiler.addInstruction(new CMP(Register.R1, Register.R0));
        compiler.addInstruction(new SEQ(Register.R0));
        compiler.addInstruction(new RTS());

        // Code d'initialisation des VTables (exécuté au démarrage)
        compiler.addLabel(vtableInitLabel);
        compiler.addComment("Initialisation des tables de méthodes");

        // Calculer la taille totale pour les VTables :
        // Object : 2 mots (parent null + equals)
        // Chaque classe : 1 + numberOfMethods (parent ptr + méthodes)
        int totalVTableSize = 2; // Object
        for (AbstractDeclClass declClass : classes.getList()) {
            DeclClass dc = (DeclClass) declClass;
            fr.ensimag.deca.context.ClassDefinition classDef =
                    (fr.ensimag.deca.context.ClassDefinition) compiler.environmentType.defOfType(
                            dc.getClassName().getName());
            totalVTableSize += 1 + classDef.getNumberOfMethods();
        }

        // Allouer l'espace pour toutes les VTables en une seule fois
        compiler.addInstruction(new ADDSP(totalVTableSize));

        compiler.addComment("Table des méthodes de Object");
        // Alloc 2 mots : 1 pour parent (null) + 1 pour equals
        RegisterOffset objectVTableAddr = compiler.getMMU().allocGlobal(2);

        // Sauvegarder l'adresse pour les classes filles
        fr.ensimag.deca.context.ClassDefinition objectDef = (fr.ensimag.deca.context.ClassDefinition) compiler.environmentType.defOfType(compiler.createSymbol("Object"));
        objectDef.setOperand(objectVTableAddr);

        // Remplir VTable Object : Parent = null
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, objectVTableAddr));

        // Remplir VTable Object : Méthode equals à l'index 1
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("equals")), Register.R0));
        compiler.addInstruction(new STORE(fr.ensimag.ima.pseudocode.Register.R0, new RegisterOffset(objectVTableAddr.getOffset() + 1, Register.GB)));

        // passe 1 : tables methodes
        compiler.addComment("Construction des tables des methodes");
        classes.codeGenListDeclClass(compiler);

        // Sauter au main après l'initialisation des VTables
        compiler.addInstruction(new BRA(mainLabel));

        // passe 2a : init objets (code des constructeurs - appelé dynamiquement)
        compiler.addComment("init objets");
        // Générer l'initialisateur vide pour Object (classe prédéfinie)
        compiler.addLabel(new Label("init.Object"));
        compiler.addInstruction(new RTS());

        classes.codeGenListInit(compiler);

        // passe 2b : code methodes
        classes.codeGenListMethods(compiler);

        // programme principal
        compiler.addLabel(mainLabel);
        compiler.addComment("Main program");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());

        // gestion erreurs
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
