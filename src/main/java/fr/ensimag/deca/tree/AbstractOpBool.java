package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

                Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
                Type rightType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
                
                if (leftType != compiler.environmentType.BOOLEAN) {
                    throw new ContextualError(
                        "Op gauche d'une opération booléenne doit être booléen" + leftType,
                        getLeftOperand().getLocation());
                }
                
                if (rightType != compiler.environmentType.BOOLEAN) {
                    throw new ContextualError(
                        "Op droit d'une opération booléenne doit être booléen" + rightType,
                        getRightOperand().getLocation());
                }
                
                Type resultType = compiler.environmentType.BOOLEAN;
                setType(resultType);
                return resultType;
    }
}