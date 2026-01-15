package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.STORE;

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


    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" = ");
        getExpression().decompile(s);
    }


    @Override
    protected void codeGenInit(DecacCompiler compiler, Type type, VariableDefinition varDef) {

        fr.ensimag.deca.codegen.RegisterManager regMa = compiler.getRegisterManager();
        GPRegister register = regMa.prendreRegistre(null);

        // code de  expression dans ce registre
        getExpression().codeGenExpr(compiler, register);

        // stocker la valeur à l'adresse de la variable
        compiler.addInstruction(new STORE(register, varDef.getOperand()));

   
        regMa.libererRegistre();
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
