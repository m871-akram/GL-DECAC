package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type operand = this.getOperand().verifyExpr(compiler, localEnv, currentClass);
        if(!operand.isBoolean()){
            throw new ContextualError("operation not " + operand + " invalide", this.getOperand().getLocation());
        }
        setType(operand);
        return operand;
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }



    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
    
        getOperand().codeGenExpr(compiler, register);
        
        // On compare à 0 
        compiler.addInstruction(new CMP(new ImmediateInteger(0), register));
        compiler.addInstruction(new SEQ(register));
    }
    
}
