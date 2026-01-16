package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.InlinePortion;

import java.io.PrintStream;


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

//    @Override
//    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
//                                    ClassDefinition currentClass, Type returnType) throws ContextualError {
//        // Rule (3.15): Assembly bodies are accepted without further semantic checks [3].
//        asmCode.verifyExpr(compiler, localEnv, currentClass);
//    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        // The generated code is simply the assembly string [2].
        compiler.add(new InlinePortion(asmCode.getValue()));
    }

}


