package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Unary expression.
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractUnaryExpr extends AbstractExpr {

    private AbstractExpr operand;

    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }

    public AbstractExpr getOperand() {
        return operand;
    }

    protected abstract String getOperatorName();

    /**
     * template pour les operations unaires
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        getOperand().codeGenExpr(compiler, register);
        codeGenUnary(compiler, register);
    }

    /**
     * gen l'instruction atomique (ex: OPP R2, R2)
     */
    protected abstract void codeGenUnary(DecacCompiler compiler, GPRegister register);

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(getOperatorName());
        getOperand().decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }

}
