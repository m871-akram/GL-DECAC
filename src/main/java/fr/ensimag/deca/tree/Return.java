package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;

public class Return extends AbstractInst {
    private final AbstractExpr value;

    public Return(AbstractExpr value) {
        this.value = value;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return");
        s.print(" ");
        value.decompile(s);
        s.print(";");
    }


    @Override
    protected void verifyInst(DecacCompiler compiler,  EnvironmentExp localEnv,
                            ClassDefinition currentClass, Type returnType) throws ContextualError {

        if (returnType.isVoid()) {
            throw new ContextualError(
                "return avec un void interdit", getLocation()
            );
        }

        // on vérifier que la valeur est compatible
        Type valueType = value.verifyExpr(compiler, localEnv, currentClass);

        if (!compiler.environmentType.assignCompatible(returnType, valueType)) {
            throw new ContextualError(
                "erreur dans le type de return : expected " + returnType + ", y on a: " + valueType, getLocation()
            );
        }
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenInst'");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        value.prettyPrintChildren(s, prefix);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        value.iterChildren(f);
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
        value.codeGenExpr(compiler, fr.ensimag.ima.pseudocode.Register.R0);
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.BRA(
                compiler.getCurrentMethodEndLabel()));
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // 1. Évaluer l'expression de retour dans R0
        getReturnExpr().codeGenExpr(compiler, Register.R0);

        // 2. Sauter vers le label de fin de la méthode
        String className = getCurrentClass().getName().getName();
        String methodName = getCurrentMethod().getName().getName();
        compiler.addInstruction(new BRA(new Label("end." + className + "." + methodName)));
    }
}
