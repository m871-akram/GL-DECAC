package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Binary expressions.
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractBinaryExpr extends AbstractExpr {

    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    private AbstractExpr leftOperand;
    private AbstractExpr rightOperand;

    public AbstractBinaryExpr(AbstractExpr leftOperand,
            AbstractExpr rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    /**
     * Méthode template pour générer le code binaire standard.
     * Gère automatiquement l'allocation de registre et le SPILL.
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // calcul gauche
        getLeftOperand().codeGenExpr(compiler, register);

        // calcul droite + gestion reg
        if (compiler.getRegisterManager().registreLibre()) {
            // cas normal : reg dispo
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();
            getRightOperand().codeGenExpr(compiler, rRight);

            // operation
            codeGenInst(compiler, rRight, register);

            compiler.getRegisterManager().libererRegistre();
        } else {
            // spill : plus de reg -> pile
            GPRegister rRight = fr.ensimag.ima.pseudocode.Register.R0;

            // sauvegarde gauche
            compiler.addInstruction(new PUSH(register));
            compiler.getMMU().notifyPush(1);

            // calcul droite
            getRightOperand().codeGenExpr(compiler, register);

            // charge droite dans r0
            compiler.addInstruction(new LOAD(register, rRight));

            // restaure gauche
            compiler.addInstruction(new POP(register));
            compiler.getMMU().notifyPop(1);

            // operation avec r0
            codeGenInst(compiler, rRight, register);
        }
    }

    /**
     * À implémenter par les opérateurs (Plus, Minus, Equals, etc.)
     * @param opSource L'opérande de droite (soit un Registre temporaire, soit R0)
     * @param opDest L'opérande de gauche (le registre de résultat)
     */
    protected abstract void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest);

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(" " + getOperatorName() + " ");
        getRightOperand().decompile(s);
        s.print(")");
    }

    abstract protected String getOperatorName();

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

}
