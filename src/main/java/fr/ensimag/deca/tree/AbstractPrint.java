package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Print statement (print, println, ...).
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    abstract String getSuffix();

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        for (AbstractExpr arg : getArguments().getList()) {

            Type t = arg.verifyExpr(compiler, localEnv, currentClass);

            if (!(t.isString() || t.isInt() || t.isFloat())) {
                throw new ContextualError(
                        "Type non imprimable dans une instruction print : " + t,
                        arg.getLocation()
                );
            }
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList()) {
            if (getPrintHex() && a.getType().isFloat()) {
                // printx/printlnx
                a.codeGenPrintHex(compiler);
            } else {
                // methode standard
                a.codeGenPrint(compiler);
            }
        }
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print");
        s.print(getSuffix());
        if (printHex) {
            s.print("x");
        }
        s.print("(");
        getArguments().decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
