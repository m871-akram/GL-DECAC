package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue" 
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments 
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute            
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {

        // 1. Vérification standard
        Type type = this.verifyExpr(compiler, localEnv, currentClass);

        // 2. Compatibilité d'assignation
        if (!compiler.environmentType.assignCompatible(expectedType, type)) {
            throw new ContextualError("Type incompatible pour l'initialisation ou l'affectation. Attendu: "
                    + expectedType + ", Trouvé: " + type, this.getLocation());
        }

        // 3. Conversion implicite (ConvFloat)
        if (expectedType.isFloat() && type.isInt()) {
            ConvFloat conv = new ConvFloat(this);
            // On vérifie la conversion (ce qui va définir son type à Float)
            conv.verifyExpr(compiler, localEnv, currentClass);
            return conv;
        }

        return this;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        throw new ContextualError(
            " on ne peut pas mettre une expression comme instruction !! ",
            this.getLocation());
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
                         ClassDefinition currentClass) throws ContextualError {
        Type type = verifyExpr(compiler, localEnv, currentClass);
        if (!type.isBoolean()) {
            throw new ContextualError("Condition booléenne attendue, trouvé: " + type, getLocation());
        }
    }


    protected abstract void codeGenExpr(DecacCompiler compiler, GPRegister register);



    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler) {
        // 1. Évaluation dans un registre temporaire
        // Utilisation de takeRegister (Hardware Architecture)
        GPRegister register = compiler.getRegisterManager().prendreRegistre();

        codeGenExpr(compiler, register);

        // 2. Charger dans R1 pour WINT/WFLOAT
        compiler.addInstruction(new LOAD(register, Register.R1));

        // 3. Afficher
        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            compiler.addInstruction(new WFLOAT());
        }

        // 4. Libérer le registre
        compiler.getRegisterManager().libererRegistre();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Fallback pour les expressions utilisées comme instructions (ex: Assign hérite de ça)
        // Mais Assign override cette méthode.
        // Si on est ici, on calcule juste pour l'effet de bord (rare en sans-objet pur hors Assign)

        GPRegister register = compiler.getRegisterManager().prendreRegistre();
        codeGenExpr(compiler, register);
        compiler.getRegisterManager().libererRegistre();
    }

    /**
     * Génère un saut conditionnel basé sur la valeur de l'expression.
     * Implémentation par défaut pour les expressions non-booléennes pures (ex: variables)
     */
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {

        GPRegister reg = compiler.getRegisterManager().prendreRegistre();

        // 1. Calculer la valeur (0 ou 1)
        this.codeGenExpr(compiler, reg);

        // 2. Comparer à 0 (Faux)
        compiler.addInstruction(new CMP(0, reg));

        // 3. Saut
        if (branchOn) {
            // Si on veut sauter quand c'est Vrai (donc reg != 0)
            compiler.addInstruction(new BNE(target));
        } else {
            // Si on veut sauter quand c'est Faux (donc reg == 0)
            compiler.addInstruction(new BEQ(target));
        }

        compiler.getRegisterManager().libererRegistre();
    }


    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }
}
