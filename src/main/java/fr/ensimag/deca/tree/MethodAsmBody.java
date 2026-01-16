package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;


import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.InlinePortion;


public class MethodAsmBody extends AbstractMethodBody {

    private StringLiteral asmCode;

    public MethodAsmBody(StringLiteral asmCode) {
        this.asmCode = asmCode;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("asm(");
        asmCode.decompile(s);
        s.print(");");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        asmCode.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        asmCode.iter(f);
    }

    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
                             ClassDefinition currentClass, Type returnType) throws ContextualError {
        
    }


    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        compiler.add(new InlinePortion(asmCode.getValue()));
    }

}