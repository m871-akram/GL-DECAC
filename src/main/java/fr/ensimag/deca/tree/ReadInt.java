package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptController;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RINT;

import java.io.PrintStream;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class ReadInt extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.INT);
        return compiler.environmentType.INT;
    }
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Lire entier (Résultat dans R1)
        compiler.addInstruction(new RINT());

        // 2. Gestion Erreur IO via InterruptController
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptVector.IRQ_IO_ERROR); // Ajoute ce vecteur s'il manque
        }

        // 3. Charger le résultat dans le registre cible
        compiler.addInstruction(new LOAD(Register.R1, register));
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readInt()");
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
