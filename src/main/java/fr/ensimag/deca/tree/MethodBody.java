package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


import fr.ensimag.deca.context.*;


public class MethodBody extends AbstractMethodBody {

    private ListDeclVar locals;
    private ListInst insts;

    public MethodBody(ListDeclVar locals, ListInst insts) {
        this.locals = locals;
        this.insts = insts;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        locals.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        locals.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        locals.iter(f);
        insts.iter(f);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
                                    ClassDefinition currentClass, Type returnType) throws ContextualError {
        // Rule (3.14): Verify local variables and then instructions in the local environment [5, 6].
        locals.verifyListDeclVariable(compiler, localEnv, currentClass);
        insts.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        // Step C: Generate code for locals and instructions [7].
        locals.codeGenListDeclVar(compiler);
        insts.codeGenListInst(compiler);
    }
}
