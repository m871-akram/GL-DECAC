package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Operand;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * @author gl51
 * @date 01/01/2026
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type t2 = getExpression().verifyExpr(compiler, localEnv, currentClass);


        // ConvFloat
        if (t.isFloat() && t2.isInt()) {
            ConvFloat conv = new ConvFloat(getExpression());
            conv.verifyExpr(compiler, localEnv, currentClass); //  noeud ConvFloat
            this.setExpression(conv);
            t2 = conv.getType(); // mettre a jour t2
        }



        if (!compiler.environmentType.assignCompatible(t, t2)) {
            throw new ContextualError(
                "Initialization dois etre compatible " + 
                t + " et " + t2,
                this.getLocation());
        }

    }

//    @Override
//    protected void verifyInitialization(DecacCompiler compiler, Type t,
//                                        EnvironmentExp localEnv, ClassDefinition currentClass)
//            throws ContextualError {
//
//        // Utilisation de verifyRValue pour gérer la compatibilité ET la conversion implicite Float
//        AbstractExpr verifiedExpr = this.expression.verifyRValue(compiler, localEnv, currentClass, t);
//
//        // Mise à jour de l'expression (au cas où un ConvFloat a été ajouté)
//        this.setExpression(verifiedExpr);
//    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" = ");
        getExpression().decompile(s);
    }


    @Override
    protected void codeGenInit(DecacCompiler compiler, Operand target, Type type) {
        // 1. Allouer un registre temporaire
        GPRegister register = compiler.getRegisterManager().prendreRegistre();

        // 2. Calculer l'expression dans ce registre
        getExpression().codeGenExpr(compiler, register);

        // 3. Stocker le résultat à l'adresse cible (target)
        // STORE attend un DAddr, on cast l'Operand
        compiler.addInstruction(new STORE(register, (fr.ensimag.ima.pseudocode.DAddr) target));

        // 4. Libérer le registre
        compiler.getRegisterManager().libererRegistre();
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }
}
