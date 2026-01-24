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
 * Full if/else if/else statement.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class IfThenElse extends AbstractInst {

    private final AbstractExpr condition;
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }


    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        condition.verifyCondition(compiler, localEnv, currentClass);

        thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);

        elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // labels uniques via sequencer
        Label elseLabel = compiler.getSequencer().genSignal("else");
        Label endLabel = compiler.getSequencer().genSignal("end_if");

        // saut conditionnel si faux -> else
        condition.codeGenBool(compiler, false, elseLabel);

        // bloc alors
        thenBranch.codeGenListInst(compiler);

        // saut vers fin
        compiler.addInstruction(new BRA(endLabel));

        // bloc sinon
        compiler.addLabel(elseLabel);
        elseBranch.codeGenListInst(compiler);

        // fin
        compiler.addLabel(endLabel);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        s.print("if (");
        condition.decompile(s);
        s.println(") {");

        thenBranch.decompile(s);

        s.println("} else {");

        elseBranch.decompile(s);

        s.println("}");

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
