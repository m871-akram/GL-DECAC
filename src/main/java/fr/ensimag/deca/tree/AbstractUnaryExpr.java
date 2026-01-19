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

    public AbstractExpr getOperand() {
        return operand;
    }
    private AbstractExpr operand;
    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected abstract String getOperatorName();

    /**
     * Méthode template pour les opérations unaires.
     * 1. Evalue l'opérande dans le registre.
     * 2. Applique l'opération (codeGenUnary).
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        getOperand().codeGenExpr(compiler, register);
        codeGenUnary(compiler, register);
    }

    /**
     * À implémenter par les sous-classes (Not, UnaryMinus, ConvFloat).
     * Génère juste l'instruction atomique (ex: OPP R2, R2).
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
