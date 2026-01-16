package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.GPRegister;


/**
 * Integer literal
 *
 * @author gl51
 * @date 01/01/2026
 */
public class nullLiteral extends AbstractExpr {
    


    public nullLiteral() {
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.NULL);
        return compiler.environmentType.NULL;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister dest) {

        //  LOAD #12, R2
        //compiler.addInstruction(new LOAD(new ImmediateInteger(value), dest));
        
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {

        
        //compiler.addInstruction(new LOAD(new ImmediateInteger(value), Register.R1));
        
        //compiler.addInstruction(new WINT());
        
    }


    @Override
    String prettyPrintNode() {
        return "null()";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
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


