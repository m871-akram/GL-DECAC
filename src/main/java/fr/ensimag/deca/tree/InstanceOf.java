package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.deca.context.*;


public class InstanceOf extends AbstractExpr {

    private AbstractExpr expr;
    private AbstractIdentifier type;

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        expr.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        type.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        type.iter(f);
    }

//    @Override
//    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
//            throws ContextualError {
//        // Rule (3.40): Verify both the expression and the target type [5].
//        Type type1 = expr.verifyExpr(compiler, localEnv, currentClass);
//        Type type2 = type.verifyType(compiler);
//
//        // Check if the types are compatible with the instanceof operator [4].
//        Type resultType = compiler.environmentType.instanceOfCompatible(type1, type2);
//
//        // Result is always a boolean [4, 5].
//        this.setType(resultType);
//        return resultType;
//    }
}
