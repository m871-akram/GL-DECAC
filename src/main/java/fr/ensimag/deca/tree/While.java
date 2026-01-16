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

   
    private static int c = 0;

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // throw new UnsupportedOperationException("not yet implemented");

        // <Code(while (C) { I })> =
        // BRA E_Cond.n
        // E_Debut.n:
        // <Code(I)>
        // E_Cond.n:
        // <Code(C, vrai, E_Debut.n)>        
        String w = "." + c;
        Label conditionLabel = new Label("while_cond" + w);
        Label startLabel = new Label("while_start" + w);
        compiler.addInstruction(new BRA(conditionLabel));
        compiler.addLabel(startLabel);
        body.codeGenListInst(compiler);
        compiler.addLabel(conditionLabel);
        condition.codeGenBool(compiler, true, startLabel);
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
