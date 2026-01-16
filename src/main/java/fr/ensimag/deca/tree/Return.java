package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


public class Return extends AbstractInst {

    private AbstractExpr expr;

    public Return(AbstractExpr expr) {
        this.expr = expr;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expr.decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
    }

//    @Override
//    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
//                              ClassDefinition currentClass, Type returnType) throws ContextualError {
//        // Rule (3.24): The method must not return void [1].
//        if (returnType.isVoid()) {
//            throw new ContextualError("Cannot return a value from a void method", getLocation());
//        }
//
//        // Rule (3.24): Verify that the expression matches the expected return type (rvalue check) [1].
//        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
//        if (!compiler.environmentType.assignCompatible(returnType, exprType)) {
//            throw new ContextualError("Return type " + exprType + " is not compatible with " + returnType,
//                    expr.getLocation());
//        }
//    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Step C: Evaluate expression into R0 and branch to method end [2].
        expr.codeGenExpr(compiler, fr.ensimag.ima.pseudocode.Register.R0);
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.BRA(
                compiler.getCurrentMethodEndLabel()));
    }
}
