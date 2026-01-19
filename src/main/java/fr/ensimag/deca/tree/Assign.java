package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
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
        AbstractExpr t2 = this.getRightOperand().verifyRValue(compiler, localEnv, currentClass,t1);
        setRightOperand(t2);
        setType(t1);
        return t1;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition classCourante, Type returnType)
            throws ContextualError {Type exprType = this.verifyExpr(compiler, localEnv, classCourante);
            this.setType(exprType);
    }



    /**
     * gen code assignation en expr
     * permet le chainage : x = y = 2;
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // calc valeur droite
        getRightOperand().codeGenExpr(compiler, register);

        // identifier la lvalue
        AbstractLValue lValue = getLeftOperand();

        // stocker a l'adresse
        if (lValue instanceof Identifier) {
            ((Identifier) lValue).codeGenStore(compiler, register);
        } else if (lValue instanceof Selection) {
            ((Selection) lValue).codeGenStore(compiler, register);
        } else {
            throw new UnsupportedOperationException("Type de LValue non supporté pour assignation: " + lValue.getClass().getSimpleName());
        }

        // registre contient toujours la valeur assignee
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // alloue reg temporaire
        GPRegister reg = compiler.getRegisterManager().prendreRegistre();

        // gen code complet
        codeGenExpr(compiler, reg);

        // libere reg
        compiler.getRegisterManager().libererRegistre();
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        // vide
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

}
