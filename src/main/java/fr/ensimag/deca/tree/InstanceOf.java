package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;


public class InstanceOf extends AbstractExpr {

    private AbstractExpr expr;
    private AbstractIdentifier type;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier type) {
        this.expr = expr;
        this.type = type;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        Type typeType = type.verifyType(compiler);

        if (!exprType.isClassOrNull()) {
            throw new ContextualError("instanceof attend un objet à gauche", getLocation());
        }
        if (!typeType.isClass()) {
            throw new ContextualError("instanceof attend une classe à droite", getLocation());
        }

        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        ClassDefinition targetClassDef = (ClassDefinition) type.getDefinition();

        Label endLabel = compiler.getSequencer().genSignal("instanceof_end");
        Label loopLabel = compiler.getSequencer().genSignal("instanceof_loop");
        Label trueLabel = compiler.getSequencer().genSignal("instanceof_true");

        // 1. Evaluer l'objet
        expr.codeGenExpr(compiler, register);

        // 2. Si null -> False
        compiler.addInstruction(new CMP(new NullOperand(), register));
        compiler.addInstruction(new BEQ(endLabel)); // Sauter avec 0 (déjà chargé si null, ou charger 0 avant ?)
        // Attention : si register contient null (0), c'est bon, on a déjà 0 (False).
        // Mais pour être sûr, on devrait charger 0 explicitement si on saute.
        // Ici on suppose que null est représenté par 0.

        // 3. Charger VTable objet
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), Register.R0)); // R0 = VTable courante

        // Charger VTable cible
        RegisterOffset targetVTableAddr = (RegisterOffset) targetClassDef.getOperand();
        compiler.addInstruction(new LEA(targetVTableAddr, Register.R1)); // R1 = VTable Cible

        compiler.addLabel(loopLabel);
        compiler.addInstruction(new CMP(Register.R1, Register.R0));
        compiler.addInstruction(new BEQ(trueLabel));

        // Remonter au parent
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));
        compiler.addInstruction(new CMP(new NullOperand(), Register.R0));
        compiler.addInstruction(new BNE(loopLabel));

        // Pas trouvé -> False (0)
        compiler.addInstruction(new LOAD(0, register));
        compiler.addInstruction(new BRA(endLabel));

        // Trouvé -> True (1)
        compiler.addLabel(trueLabel);
        compiler.addInstruction(new LOAD(1, register));

        compiler.addLabel(endLabel);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        expr.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        type.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        type.iter(f);
    }
}