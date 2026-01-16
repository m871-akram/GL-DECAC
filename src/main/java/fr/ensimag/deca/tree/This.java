package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;

public class This extends AbstractExpr {



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
        // this est toujours à -2(LB) dans les méthodes
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), register));
    }
}
