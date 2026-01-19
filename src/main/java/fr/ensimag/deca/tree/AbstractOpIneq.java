package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractOpIneq extends AbstractOpCmp {

    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

//    @Override
//    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
//            ClassDefinition currentClass) throws ContextualError {
//        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
//        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
//
//        // Les opérateurs d'inégalité (<, <=, >, >=) ne fonctionnent QUE sur les types numériques
//        if (!t1.isInt() && !t1.isFloat()) {
//            throw new ContextualError(
//                "L'opérande gauche de " + getOperatorName() + " doit être int ou float, pas " + t1,
//                getLeftOperand().getLocation());
//        }
//        if (!t2.isInt() && !t2.isFloat()) {
//            throw new ContextualError(
//                "L'opérande droit de " + getOperatorName() + " doit être int ou float, pas " + t2,
//                getRightOperand().getLocation());
//        }
//
//        // Conversion implicite int -> float si nécessaire
//        if (t1.isFloat() && t2.isInt()) {
//            setRightOperand(new ConvFloat(getRightOperand()));
//            this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
//        } else if (t2.isFloat() && t1.isInt()) {
//            setLeftOperand(new ConvFloat(getLeftOperand()));
//            this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
//        }
//
//        setType(compiler.environmentType.BOOLEAN);
//        return compiler.environmentType.BOOLEAN;
//    }
}
