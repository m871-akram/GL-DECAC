package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
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
        Type condType = condition.verifyExpr(compiler, localEnv, currentClass);
        if (!condType.isBoolean()) {
            throw new ContextualError(
                "La condition d'un if ou else doit être de type booléen: " + condType,
                condition.getLocation());
        }
        
        thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
        
        elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    // if_else.1, if_else.2...
    private static int c = 0;

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        c++;
        String a = "." + c;
        
        Label elseLabel = new Label("else" + a);
        Label endLabel = new Label("end_if" + a);
    
        // <Code(Condition, faux, E_Sinon)>
        condition.codeGenBool(compiler, false, elseLabel);

        // (Then)
        thenBranch.codeGenListInst(compiler);

        // on saute à la fin pour ne pas exécuter le Sinon après le Alors
       
        compiler.addInstruction(new BRA(endLabel));

        compiler.addLabel(elseLabel);
        elseBranch.codeGenListInst(compiler);
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
    protected
    void iterChildren(TreeFunction f) {
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
