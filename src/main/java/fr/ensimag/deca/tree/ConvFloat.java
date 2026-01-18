package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl51
 * @date 01/01/2026
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) {
        setType(compiler.environmentType.FLOAT);
        return getType();
    }


    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }


    /**
     * Génération de l'instruction de conversion.
     * L'opérande a déjà été chargé dans le registre par AbstractUnaryExpr.codeGenExpr
     */
    @Override
    protected void codeGenUnary(DecacCompiler compiler, GPRegister register) {
        // Instruction IMA : FLOAT Rn, Rn (Convertit entier en flottant)
        compiler.addInstruction(new FLOAT(register, register));
    }

}
