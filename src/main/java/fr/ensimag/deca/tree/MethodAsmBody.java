package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.InlinePortion;
import fr.ensimag.ima.pseudocode.instructions.RTS;

import java.io.PrintStream;


public class MethodAsmBody extends AbstractMethodBody {

    private StringLiteral asmCode;

    public MethodAsmBody(StringLiteral asmCode) {
        this.asmCode = asmCode;
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
                                    ClassDefinition currentClass, Type returnType) throws ContextualError {
        // Le corps ASM n'est pas vérifié sémantiquement, juste le type de retour ?
        // En Deca, asm(...) est valide partout.
        // On peut vérifier les expressions inside si nécessaire, mais ici c'est une string littérale.
        asmCode.verifyExpr(compiler, localEnv, currentClass);
        setType(returnType);
    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        // Injection directe du code assembleur
        compiler.add(new InlinePortion(asmCode.getValue()));
        if (getType().isVoid()) compiler.addInstruction(new RTS());
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
}