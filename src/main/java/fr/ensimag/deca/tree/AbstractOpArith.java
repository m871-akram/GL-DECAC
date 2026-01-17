package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
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
            setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        }
        if( t2.isFloat() && t1.isInt()){
            setLeftOperand(new ConvFloat(getLeftOperand()));
            this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        }
        
        if (t1.isFloat() || t2.isFloat()) {
            setType(compiler.environmentType.FLOAT);
        }else{
            setType(compiler.environmentType.INT);
        }
        
        return getType();
    }

}
