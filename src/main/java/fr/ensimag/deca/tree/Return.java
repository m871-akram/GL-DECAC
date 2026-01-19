package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;

import java.io.PrintStream;

/**
 * @author gl51
 * @date 15/01/2026
 */
public class Return extends AbstractInst {

    private AbstractExpr value;

    public Return(AbstractExpr value) {
        this.value = value;
    }

    public AbstractExpr getValue() {
        return value;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler,  EnvironmentExp localEnv,
                            ClassDefinition currentClass, Type returnType) throws ContextualError {

        if (returnType.isVoid()) {
            throw new ContextualError(
                "return avec un void interdit", getLocation()
            );
        }
        AbstractExpr convValue = value.verifyRValue(compiler, localEnv, currentClass,returnType);
        this.value = convValue;
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // 1. Évaluer l'expression de retour dans R0 (Registre de retour conventionnel)
        value.codeGenExpr(compiler, Register.R0);

        // 2. Retourner de la méthode (le résultat est dans R0)
        // RTS va dépiler et retourner à l'appelant
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.RTS());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        value.decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        value.prettyPrintChildren(s, prefix);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        value.iterChildren(f);
    }
}


