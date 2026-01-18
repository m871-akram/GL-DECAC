package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.BRA;

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

                getLeftOperand().verifyCondition(compiler, localEnv, currentClass);
                getRightOperand().verifyCondition(compiler, localEnv, currentClass);
                
                Type resultType = compiler.environmentType.BOOLEAN;
                setType(resultType);
                return resultType;
    }


    // Compteur pour générer des étiquettes uniques
    private static int boolSeq = 0;

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {

        
        boolSeq++;
        String suffix = "." + boolSeq;
        
        Label trueLabel = new Label("bool_true" + suffix);
        Label endLabel = new Label("bool_end" + suffix);

        // Si VRAI, on saute à trueLabel
        this.codeGenBool(compiler, true, trueLabel);

        // (On n'a pas sauté) -> On charge 0
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), register));
        compiler.addInstruction(new BRA(endLabel));

        //  (On a sauté ici) -> On charge 1
        compiler.addLabel(trueLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), register));

        compiler.addLabel(endLabel);
    }
}