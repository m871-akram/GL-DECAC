package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WINT;

import java.io.PrintStream;


/**
 * Integer literal
 *
 * @author gl51
 * @date 01/01/2026
 */
public class IntLiteral extends AbstractExpr {
    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.INT);
        return compiler.environmentType.INT;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister dest) {
        compiler.addInstruction(new LOAD(value, dest));

    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {


        compiler.addInstruction(new LOAD(value, Register.R1));

        compiler.addInstruction(new WINT());

    }


    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}


