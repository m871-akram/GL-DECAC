package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.io.PrintStream;

/**
 * @author gl51
 * @date 01/01/2026
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify Main: start");
        // A FAIRE: Appeler méthodes "verify*" de ListDeclVarSet et ListInst.
        // Vous avez le droit de changer le profil fourni pour ces méthodes
        // (mais ce n'est à priori pas nécessaire).
        EnvironmentExp localEnv = new EnvironmentExp(null);
        this.declVariables.verifyListDeclVariable(compiler,localEnv , null);
        this.insts.verifyListInst(compiler, localEnv, null, compiler.environmentType.VOID);
        LOG.debug("verify Main: end");
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        // 1. Initialiser une nouvelle Frame pour le Main
        compiler.getMMU().enterNewMethodFrame();

        // 2. Déclarations de variables (Allocation MMU interne)
        compiler.addComment("Beginning of main declarations:");
        declVariables.codeGenListDeclVar(compiler);

        // 3. EN-TÊTE DU MAIN : Gestion Pile et Globales
        // Récupérer les infos de la MMU
        int nbLocales = declVariables.size();
        compiler.getMMU().notifyLocalBlockAllocation(nbLocales);
        int maxStack = compiler.getMMU().getStackRequirements();
        int nbGlobals = compiler.getMMU().getGlobalUsage(); // Si tu gères les globales dans MMU

        // A. TSTO : Pile Max + Globales (si elles n'ont pas leur propre zone réservée)
        // Note: Dans IMA, GB est à part, mais si on empile beaucoup, TSTO doit couvrir.
        // Souvent TSTO = maxStack
        compiler.addInstruction(new TSTO(maxStack));

        // B. BOV : Vérification Stack Overflow
        compiler.getIrqController().triggerInterrupt(compiler,
                InterruptVector.IRQ_STACK_OVERFLOW);

        // C. ADDSP : Réservation pour les variables locales
        compiler.addInstruction(new ADDSP(nbLocales));

        // A FAIRE: traiter les déclarations de variables.
        compiler.addComment("Beginning of main instructions:");
        insts.codeGenListInst(compiler);
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
