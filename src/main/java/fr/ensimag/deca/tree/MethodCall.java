package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


public class MethodCall extends AbstractExpr {

    private AbstractExpr target;
    private AbstractIdentifier method;
    private ListExpr args;

    public MethodCall(AbstractExpr target, AbstractIdentifier method, ListExpr args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (target != null) {
            target.decompile(s);
            s.print(".");
        }
        method.decompile(s);
        s.print("(");
        args.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (target != null) target.prettyPrint(s, prefix, false);
        method.prettyPrint(s, prefix, false);
        args.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        if (target != null) target.iter(f);
        method.iter(f);
        args.iter(f);
    }

//    @Override
//    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
//                           ClassDefinition currentClass) throws ContextualError {
//        // Rule (3.71): Verify the target is a class and the method exists in its environment [9].
//        Type targetType = target.verifyExpr(compiler, localEnv, currentClass);
//        ClassType classType = targetType.asClassType("Method call on non-class type", getLocation());
//
//        // Find method definition in the class members
//        ExpDefinition def = classType.getDefinition().getMembers().get(method.getName());
//        if (def == null || !def.isMethod()) {
//            throw new ContextualError("Method " + method.getName() + " not found in class " + classType.getName(),
//                    method.getLocation());
//        }
//        MethodDefinition methodDef = (MethodDefinition) def;
//
//        // Verify arguments against the method signature [9].
//        args.verifyListExpr(compiler, localEnv, currentClass, methodDef.getSignature());
//
//        this.setType(methodDef.getType());
//        method.setDefinition(methodDef);
//        return methodDef.getType();
//    }
}

