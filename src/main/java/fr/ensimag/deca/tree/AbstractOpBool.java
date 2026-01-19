package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import fr.ensimag.ima.pseudocode.GPRegister;
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


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {

        // CORRECTION : Utilisation du Sequencer propre à cette compilation
        // Cela garantit l'unicité des labels même en parallèle (-P)
        Label trueLabel = compiler.getSequencer().genSignal("bool_true");
        Label endLabel  = compiler.getSequencer().genSignal("bool_end");

        // Si l'expression booléenne est VRAIE, on saute à trueLabel
        this.codeGenBool(compiler, true, trueLabel);

        // Cas FAUX (On n'a pas sauté) -> On charge 0 (False)
        compiler.addInstruction(new LOAD(0, register));
        compiler.addInstruction(new BRA(endLabel));

        // Cas VRAI (On a sauté ici) -> On charge 1 (True)
        compiler.addLabel(trueLabel);
        compiler.addInstruction(new LOAD(1, register));

        compiler.addLabel(endLabel);
    }
}