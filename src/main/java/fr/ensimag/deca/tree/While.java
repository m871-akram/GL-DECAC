package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
            Type condType = condition.verifyExpr(compiler, localEnv, currentClass);           
            if (!condType.isBoolean()) {
                throw new ContextualError("Condition de while doit être booléenne", condition.getLocation());
            }
        
            body.verifyListInst(compiler, localEnv, currentClass, returnType);    
        }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // 1. Initialiser la séquence de boucle (gestion contextuelle des labels)
        compiler.getSequencer().enterLoopSequence();

        // Récupérer les labels générés par le Sequencer
        Label startLabel = compiler.getSequencer().getCurrentLoopStart(); // Début du corps
        Label exitLabel = compiler.getSequencer().getCurrentLoopExit();   // Fin de la boucle

        // Pour l'optimisation "Test à la fin", il nous faut un label pour le test
        // Le Sequencer ne le donne pas par défaut, on le demande manuellement
        Label condLabel = compiler.getSequencer().genSignal("while_cond");

        // Structure optimisée :
        //    BRA condLabel
        // startLabel:
        //    CORPS
        // condLabel:
        //    Code(Condition, Vrai -> startLabel, Faux -> Fallthrough/Exit)

        compiler.addInstruction(new BRA(condLabel));
        compiler.addLabel(startLabel);

        body.codeGenListInst(compiler);

        compiler.addLabel(condLabel);
        // Si condition VRAIE, on remonte à startLabel. Sinon on continue (sortie).
        condition.codeGenBool(compiler, true, startLabel);

        // Label de fin (utile si un 'break' est généré dans le corps)
        compiler.addLabel(exitLabel);

        // 2. Fermer le contexte
        compiler.getSequencer().exitLoopSequence();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
