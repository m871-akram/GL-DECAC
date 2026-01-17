package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

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
        if (!compiler.environmentType.castCompatible(t1, t2)) {
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
     * Implémentation générique de la comparaison.
     * Cette méthode est appelée par AbstractBinaryExpr après avoir chargé les opérandes.
     */
    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        // 1. Comparaison (opDest - opSource)
        // Note: CMP op1, op2 fait (op2 - op1) et set les flags.
        compiler.addInstruction(new CMP(opSource, opDest));

        // 2. Set Condition Code (Transformation en booléen 0/1)
        // ex: SEQ R2 (Met R2 à 1 si égal, 0 sinon)
        compiler.addInstruction(getSccInstruction(opDest));
    }

    /**
     * Retourne l'instruction de saut conditionnel ou de set (ex: SEQ, SLT)
     */
    protected abstract Instruction getSccInstruction(GPRegister register);
}