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
import org.apache.commons.lang.Validate;

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
        Type t2 = this.verifyExpr(compiler, localEnv, currentClass);
        if (expectedType.isFloat() && t2.isInt()) {
            ConvFloat conv = new ConvFloat(this);
            conv.verifyExpr(compiler, localEnv, currentClass);
            t2 = conv.getType(); // t2 devient float

            return conv;
        }

        if(!compiler.environmentType.assignCompatible(expectedType, t2)){
            throw new ContextualError(
                "Assignment entre des types invalides: expect" + expectedType + " is " + t2,
                this.getLocation());
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
        Type condType = this.verifyExpr(compiler, localEnv, currentClass);
        if (!condType.isBoolean()) {
            throw new ContextualError(
                "La condition d'un if ou else doit être de type booléen: " + condType,
                getLocation());
        }
}


    protected abstract void codeGenExpr(DecacCompiler compiler, GPRegister register);



    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler) {
        // eval dans reg temp
        GPRegister register = compiler.getRegisterManager().prendreRegistre();

        codeGenExpr(compiler, register);

        // charge dans r1
        compiler.addInstruction(new LOAD(register, Register.R1));

        // affiche
        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            compiler.addInstruction(new WFLOAT());
        }

        // libere registre
        compiler.getRegisterManager().libererRegistre();
    }

    protected void codeGenPrintHex(DecacCompiler compiler) {
        // eval
        GPRegister register = compiler.getRegisterManager().prendreRegistre();
        codeGenExpr(compiler, register);

        // chargement r1
        compiler.addInstruction(new LOAD(register, Register.R1));

        // affichage
        if (getType().isInt()) {
            // printx entier = wint
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            // wfloatx
            compiler.addInstruction(new WFLOATX());
        }

        // liberation
        compiler.getRegisterManager().libererRegistre();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // fallback pour expr utilisees comme inst
        GPRegister register = compiler.getRegisterManager().prendreRegistre();
        codeGenExpr(compiler, register);
        compiler.getRegisterManager().libererRegistre();
    }

    /**
     * gen saut conditionnel
     * impl par defaut pour expr non-bool pures
     */
    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {

        GPRegister reg = compiler.getRegisterManager().prendreRegistre();

        // calc valeur (0 ou 1)
        this.codeGenExpr(compiler, reg);

        // compare a 0
        compiler.addInstruction(new CMP(0, reg));

        // saut
        if (branchOn) {
            // saut si vrai (reg != 0)
            compiler.addInstruction(new BNE(target));
        } else {
            // saut si faux (reg == 0)
            compiler.addInstruction(new BEQ(target));
        }

        compiler.getRegisterManager().libererRegistre();
    }
    public int isPowerOftow(){
        if(!(this instanceof IntLiteral)){
            return -1;
        }
        int n = ((IntLiteral) this).getValue();
        if( n > 0 && (n & (n - 1)) != 0){
            return -1;
        }
        return  Integer.numberOfTrailingZeros(n);
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
