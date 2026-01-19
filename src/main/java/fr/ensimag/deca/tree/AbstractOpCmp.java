package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.CMP;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // on verifier que les deux opérandes sont soit int soit float (en deca ,pas de string)
        if (this instanceof AbstractOpExactCmp) {
            boolean leftOk  = t1.isClassOrNull();
            boolean rightOk = t2.isClassOrNull();
        
            if (leftOk && rightOk) {
                this.setType(compiler.environmentType.BOOLEAN);
                return this.getType();
            }
        }
        if (!compiler.environmentType.aritCompatible(t1, t2)) {
            throw new ContextualError(
                "Opérandes arithmétiques doivent être int ou float, pas " + 
                t1 + " et " + t2,
                this.getLocation());
        }
        if( t1.isFloat() && t2.isInt()){
            setRightOperand(new ConvFloat(getRightOperand()));
            this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }
        if( t2.isFloat() && t1.isInt()){
            setLeftOperand(new ConvFloat(getLeftOperand()));
            this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }
        
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }


    /**
     * impl generique comparaison
     */
    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        // comparaison (opdest - opsource)
        compiler.addInstruction(new CMP(opSource, opDest));

        // set condition code
        compiler.addInstruction(getAsmCode(opDest));
    }

    /**
     * retourne instruction saut conditionnel ou set
     */
    protected abstract Instruction getAsmCode(GPRegister register);
}