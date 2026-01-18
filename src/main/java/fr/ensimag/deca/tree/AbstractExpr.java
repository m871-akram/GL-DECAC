package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;

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
                this.getLocation());
        }
}


    protected abstract void codeGenExpr(DecacCompiler compiler, GPRegister register);



    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler) {
        
        //  Calculer la valeur de l'expression dans un registre temporaire
        fr.ensimag.deca.codegen.RegisterManager regMa = compiler.getRegisterManager();
        fr.ensimag.ima.pseudocode.GPRegister register = regMa.prendreRegistre(null);
        
        codeGenExpr(compiler, register); // Évaluation
        
        //  Charger le résultat dans R1 pour l'instruction WINT/WFLOAT
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.LOAD(register, fr.ensimag.ima.pseudocode.Register.R1));
        
        //  Appeler l'instruction d'affichage selon le type
        if (getType().isInt()) {
            compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.WINT());
        } else if (getType().isFloat()) {
            compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.WFLOAT());
        }
        
        //  Libérer le registre temporaire
        regMa.libererRegistre();
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // throw new UnsupportedOperationException("not yet implemented");

        //  allouer un registre temporaire pour stocker le résultat 
        fr.ensimag.deca.codegen.RegisterManager regMa = compiler.getRegisterManager();
        fr.ensimag.ima.pseudocode.GPRegister register = regMa.prendreRegistre(null);
        
        //  code de l'expression
        codeGenExpr(compiler, register);
        
        regMa.libererRegistre(); 
    }

    protected void codeGenBool(DecacCompiler compiler, boolean branchOn, Label target) {
        
        fr.ensimag.deca.codegen.RegisterManager regMgr = compiler.getRegisterManager();
        GPRegister reg = regMgr.prendreRegistre(null);

        this.codeGenExpr(compiler, reg);
        
    
        compiler.addInstruction(new CMP(new ImmediateInteger(0), reg));
        
        // le saut conditionnel
        if (branchOn) {
            // si (reg != 0) -> Saut
            compiler.addInstruction(new BNE(target));
        } else {
            // si (reg == 0) -> Saut
            compiler.addInstruction(new BEQ(target));
        }
        
   
        regMgr.libererRegistre();
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
