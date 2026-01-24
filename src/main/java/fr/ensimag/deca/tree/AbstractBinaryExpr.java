package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
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

    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    /**
     * Vérifie si une expression peut modifier les registres (appel de méthode, etc.)
     * Dans ce cas, il faut sauvegarder le registre de l'opérande gauche sur la pile.
     */
    private boolean canModifyRegisters(AbstractExpr expr) {
        if (expr instanceof MethodCall) {
            return true;
        }
        if (expr instanceof AbstractBinaryExpr) {
            AbstractBinaryExpr binExpr = (AbstractBinaryExpr) expr;
            return canModifyRegisters(binExpr.getLeftOperand()) || canModifyRegisters(binExpr.getRightOperand());
        }
        if (expr instanceof UnaryMinus) {
            return canModifyRegisters(((UnaryMinus) expr).getOperand());
        }
        if (expr instanceof Not) {
            return canModifyRegisters(((Not) expr).getOperand());
        }
        return false;
    }

    /**
     * Méthode template pour générer le code binaire standard.
     * Gère automatiquement l'allocation de registre et le SPILL.
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // calcul gauche
        getLeftOperand().codeGenExpr(compiler, register);

        // Si l'opérande droit peut modifier les registres (ex: appel de méthode),
        // on doit sauvegarder l'opérande gauche sur la pile pour éviter qu'elle soit écrasée
        boolean needSpill = !compiler.getRegisterManager().registreLibre() || canModifyRegisters(getRightOperand());

        if (!needSpill) {
            // cas normal : reg dispo et pas d'appel de méthode dans l'opérande droit
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();
            getRightOperand().codeGenExpr(compiler, rRight);
            if (getType().isFloat() && this instanceof AbstractOpArith && !compiler.getCompilerOptions().getNoCheck() && !(this instanceof Divide)) {
                compiler.getIrqController().triggerInterrupt(compiler,
                        InterruptVector.IRQ_FLOAT_OVERFLOW);
            }

            // operation
            codeGenInst(compiler, rRight, register);

            compiler.getRegisterManager().libererRegistre();
        } else {
            // spill : plus de reg -> pile, ou l'opérande droit peut modifier les registres
            GPRegister rRight = fr.ensimag.ima.pseudocode.Register.R0;
            GPRegister rTemp = fr.ensimag.ima.pseudocode.Register.R1;  // registre temporaire pour éviter l'écrasement

            // sauvegarde gauche
            compiler.addInstruction(new PUSH(register));
            compiler.getMMU().notifyPush(1);

            // calcul droite
            getRightOperand().codeGenExpr(compiler, register);
            if (getType().isFloat() && this instanceof AbstractOpArith && !compiler.getCompilerOptions().getNoCheck() && !(this instanceof Divide)) {
                compiler.getIrqController().triggerInterrupt(compiler,
                        InterruptVector.IRQ_FLOAT_OVERFLOW);
            }

            // Cas spécial : si register est R0, utiliser R1 comme intermédiaire pour éviter l'écrasement
            if (register == rRight) {
                // sauvegarder droite dans R1
                compiler.addInstruction(new LOAD(register, rTemp));
                // restaure gauche dans R0
                compiler.addInstruction(new POP(register));
                compiler.getMMU().notifyPop(1);
                // operation : R0 (gauche) op= R1 (droite)
                codeGenInst(compiler, rTemp, register);
            } else {
                // sauvegarder temporairement droite dans R0
                compiler.addInstruction(new LOAD(register, rRight));
                // restaure gauche dans register
                compiler.addInstruction(new POP(register));
                compiler.getMMU().notifyPop(1);
                // operation : register (gauche) op= R0 (droite)
                codeGenInst(compiler, rRight, register);
            }
        }
    }

    /**
     * À implémenter par les opérateurs (Plus, Minus, Equals, etc.)
     *
     * @param opSource L'opérande de droite (soit un Registre temporaire, soit R0)
     * @param opDest   L'opérande de gauche (le registre de résultat)
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
