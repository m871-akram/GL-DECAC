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


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Cast extends AbstractExpr {
    private AbstractExpr expr;
    private AbstractIdentifier cast;
    public Cast(AbstractIdentifier ident, AbstractExpr expr) {
        Validate.notNull(expr, "left operand cannot be null");
        Validate.notNull(ident, "right operand cannot be null");
        this.expr = expr;
        this.cast = ident;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        Type classType = cast.verifyType(compiler);
    
        if (!compiler.environmentType.assignCompatible(classType, exprType)) {
            throw new ContextualError(
                "cast est incompatible entre :" + classType.getName()+ " et " + exprType.getName(),
                expr.getLocation());
        }
    
        setType(classType);
        return classType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenExpr'");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        cast.decompile(s);
        s.print(")");
        s.print("(");
        expr.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        cast.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        cast.iter(f);
    }

}



