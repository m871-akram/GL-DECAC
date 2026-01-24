package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * String literal
 *
 * @author gl51
 * @date 01/01/2026
 */
public class StringLiteral extends AbstractStringLiteral {

    private String value;

    public StringLiteral(String value) {
        Validate.notNull(value);
        if (value.startsWith("\"") && value.endsWith("\"")) {
            this.value = value.substring(1, value.length() - 1);
        } else {
            this.value = value;
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        throw new UnsupportedOperationException("on evalue pas un stringliteral dans un registre ");
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        this.setType(compiler.environmentType.STRING);
        return compiler.environmentType.STRING;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR(value));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("\"");
        s.print(value);
        s.print("\"");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

}
