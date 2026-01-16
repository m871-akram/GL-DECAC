package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class InstanceOf extends AbstractExpr {
    private AbstractExpr leftOperand;
    private AbstractIdentifier rightOperand;
    public InstanceOf(AbstractExpr leftOperand, AbstractIdentifier rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = leftOperand.verifyExpr(compiler, localEnv, currentClass);
        Type classType = rightOperand.verifyType(compiler);
    
        if (!exprType.isClassOrNull()) {
            throw new ContextualError(
                "instanceof ne s'applique qu'à un objet",
                leftOperand.getLocation());
        }
    
        if (!classType.isClass()) {
            throw new ContextualError(
                "instanceof attend un type classe",
                rightOperand.getLocation());
        }
    
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenExpr'");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        leftOperand.decompile(s);
        s.print(" instanceof ");
        rightOperand.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

}



