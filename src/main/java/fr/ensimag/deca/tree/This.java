package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;


/**
 * Integer literal
 *
 * @author gl51
 * @date 01/01/2026
 */
public class This extends AbstractExpr {

    boolean value;

    public This(boolean value) {
        Validate.notNull(value);
        this.value=value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        if (currentClass == null) {
            throw new ContextualError(
                "this interdit dans le programme principal",
                getLocation()
            );
        }

        setType(currentClass.getType());
        return currentClass.getType();
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 'this' est toujours passé en paramètre caché à l'adresse -2(LB)
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), register));
    }

    @Override
    boolean isImplicit() {
        return value;
    }
    @Override
    String prettyPrintNode() {
        return "This(" + value + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if(!value){

            s.print("this");
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}


