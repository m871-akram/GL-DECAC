package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.REM;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        //throw new UnsupportedOperationException("not yet implemented");
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

       // Le modulo nécessite deux entiers  et return un int
        if (!t1.isInt() || !t2.isInt()) {
            throw new ContextualError(
                "Modulo nécessite deux entiers, pas " + t1 + " et " + t2,
                this.getLocation());
        }

        setType(compiler.environmentType.INT);
        return getType();
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        compiler.addInstruction(new REM(opSource, opDest));

        if (!compiler.getCompilerOptions().getNoCheck()) {
        compiler.getIrqController().triggerInterrupt(compiler,
                InterruptVector.IRQ_DIV_BY_ZERO);
    }
}


    @Override
    protected String getOperatorName() {
        return "%";
    }

}
