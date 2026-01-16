package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


public class InstanceOf extends AbstractExpr {

    private AbstractExpr expr;
    private AbstractIdentifier type;

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

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = leftOperand.verifyExpr(compiler, localEnv, currentClass);
        Type classType = rightOperand.verifyType(compiler);

        if (!exprType.isClassOrNull()) {
            throw new ContextualError(
                "instanceof ne s'applique qu'à un objet",
                leftOperand.getLocation());
        }

        if (!classType.isClass()) {
            throw new ContextualError(
                "instanceof attend un type classe",
                rightOperand.getLocation());
        }

        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        ClassType targetType = getTargetType().getType().asClassType("Not a class", getLocation());
        ClassDefinition targetClassDef = targetType.getDefinition();

        // 1. Évaluer l'objet
        getObject().codeGenExpr(compiler, register);

        // 2. Si null, retourner false
        compiler.addInstruction(new CMP(new NullOperand(), register));
        Label notNull = new Label("instanceof_not_null_" + getInstanceOfCounter()); // Compteur statique
        compiler.addInstruction(new BNE(notNull));
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), register)); // false
        Label end = new Label("instanceof_end_" + getInstanceOfCounter());
        compiler.addInstruction(new BRA(end));

        // 3. Remonter la chaîne des vTables
        compiler.addLabel(notNull);
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), register)); // vTable actuelle

        int targetVTableAddr = targetClassDef.getVTableAddr();
        Label loop = new Label("instanceof_loop_" + getInstanceOfCounter());
        Label found = new Label("instanceof_found_" + getInstanceOfCounter());

        compiler.addLabel(loop);
        // Comparer vTable actuelle avec vTable cible
        compiler.addInstruction(new LOAD(new RegisterOffset(targetVTableAddr, Register.GB), Register.R0));
        compiler.addInstruction(new CMP(Register.R0, register));
        compiler.addInstruction(new BEQ(found));

        // Remonter à la super-classe
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), register)); // vTable[0] = super
        compiler.addInstruction(new CMP(new NullOperand(), register));
        compiler.addInstruction(new BNE(loop)); // Si pas null, continuer

        // Pas trouvé : retourner false
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), register));
        compiler.addInstruction(new BRA(end));

        // Trouvé : retourner true
        compiler.addLabel(found);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), register));

        compiler.addLabel(end);
    }

    // Compteur statique pour labels uniques
    private static int instanceOfCounter = 0;
    private static int getInstanceOfCounter() { return instanceOfCounter++; }

}
