package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

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


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
 
        getLeftOperand().codeGenExpr(compiler, register);

       
        if (compiler.getRegisterManager().registreLibre()) {
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre(register);
            getRightOperand().codeGenExpr(compiler, rRight);
            
            //  CMP Val, Reg => Codes conditions basés sur (Reg - Val)
            compiler.addInstruction(new CMP(rRight, register));
            
            compiler.getRegisterManager().libererRegistre();
        } else {
            // gestion du Spill 
            GPRegister rRight = Register.R0; 
            
            // sauvegard gauche
            compiler.addInstruction(new PUSH(register));
            

            getRightOperand().codeGenExpr(compiler, register);
           
            compiler.addInstruction(new LOAD(register, rRight)); 
            compiler.addInstruction(new POP(register));         
            
            //  CMP Droite, Gauche
            compiler.addInstruction(new CMP(rRight, register));
        }

        // si (cc = vrai) alors Rm <- 1 sinon Rm <- 0"
        
        compiler.addInstruction(getSccInstruction(register));
    }
    
    // Retourne l'instruction Scc (Set on Condition Code) correspondant à l'opérateur
    protected abstract Instruction getSccInstruction(GPRegister register);


}
