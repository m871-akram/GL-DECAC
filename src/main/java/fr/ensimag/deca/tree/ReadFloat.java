package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptController;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;

import java.io.PrintStream;


/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class ReadFloat extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
    }


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Lecture du flottant (résultat dans R1)
        compiler.addInstruction(new RFLOAT());

        // 2. Gestion des erreurs I/O (Overflow ou format invalide)
        // Utilisation de l'architecture Hardware
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptController.Vector.IRQ_IO_ERROR);
        }

        // 3. Déplacer R1 vers le registre cible
        compiler.addInstruction(new LOAD(Register.R1, register));
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
