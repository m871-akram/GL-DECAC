package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


public class New extends AbstractExpr {

    private final AbstractIdentifier className;

    public New(AbstractIdentifier className) {
        this.className = className;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        className.decompile(s);
        s.print("()");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
    }

//    @Override
//    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
//            throws ContextualError {
//        // Rule (3.42): Verify that the identifier represents a valid class type [2].
//        Type type = className.verifyType(compiler);
//
//        if (!type.isClass()) {
//            throw new ContextualError("The 'new' operator can only be applied to class types.",
//                    className.getLocation());
//        }
//
//        this.setType(type);
//        return type;
//    }
}

