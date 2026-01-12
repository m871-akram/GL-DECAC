package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (t1.isFloat() && t2.isInt()) {
            ConvFloat conv = new ConvFloat(getRightOperand());
            conv.verifyExpr(compiler, localEnv, currentClass); // ConvFloat
            this.setRightOperand(conv);
            t2 = conv.getType(); // t2 devient float
        }

        if(!compiler.environmentType.assignCompatible(t1, t2)){
            throw new ContextualError(
                "Assignment entre des types invalides: " + t1 + " et " + t2,
                this.getLocation());
        }
        setType(t1);
        return t1;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition classCourante, Type returnType)
            throws ContextualError {Type exprType = this.verifyExpr(compiler, localEnv, classCourante);
            this.setType(exprType);
    }


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        getRightOperand().codeGenExpr(compiler, register);
        
        //  stockage du résultat a gauche 
        getLeftOperand().codeGenStore(compiler, register);
    }


    @Override
    protected String getOperatorName() {
        return "=";
    }

}
