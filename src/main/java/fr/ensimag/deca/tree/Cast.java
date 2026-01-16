package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

public class Cast extends AbstractExpr {

    private final AbstractIdentifier type;
    private final AbstractExpr expr;

    public Cast(AbstractIdentifier type, AbstractExpr expr) {
        this.type = type;
        this.expr = expr;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(") (");
        expr.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        expr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        expr.iter(f);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        Type classType = cast.verifyType(compiler);

        if (!compiler.environmentType.assignCompatible(classType, exprType)) {
            throw new ContextualError(
                    "cast est incompatible entre :" + classType.getName()+ " et " + exprType.getName(),
                    expr.getLocation());
        }

        setType(classType);
        return classType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        ClassType targetType = getCastType().getType().asClassType("Not a class", getLocation());

        // 1. Évaluer l'objet
        getObject().codeGenExpr(compiler, register);

        // 2. Si null, cast OK (null peut être casté en n'importe quoi)
        compiler.addInstruction(new CMP(new NullOperand(), register));
        Label end = new Label("cast_end_" + getCastCounter());
        compiler.addInstruction(new BEQ(end));

        // 3. Vérifier instanceof (réutiliser la logique ci-dessus)
        // Si instanceof échoue, sauter vers cast_error
        // (Pour simplifier, on peut copier la logique instanceof ici)

        // Version simplifiée : dupliquer le code instanceof
        ClassDefinition targetClassDef = targetType.getDefinition();
        GPRegister tempReg = Register.R0;
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), tempReg)); // vTable

        int targetVTableAddr = targetClassDef.getVTableAddr();
        Label loop = new Label("cast_loop_" + getCastCounter());
        Label success = new Label("cast_success_" + getCastCounter());

        compiler.addLabel(loop);
        compiler.addInstruction(new LOAD(new RegisterOffset(targetVTableAddr, Register.GB), Register.R1));
        compiler.addInstruction(new CMP(Register.R1, tempReg));
        compiler.addInstruction(new BEQ(success));

        compiler.addInstruction(new LOAD(new RegisterOffset(0, tempReg), tempReg)); // super
        compiler.addInstruction(new CMP(new NullOperand(), tempReg));
        compiler.addInstruction(new BNE(loop));

        // Échec du cast
        compiler.addInstruction(new BRA(new Label("cast_error")));

        compiler.addLabel(success);
        compiler.addLabel(end);
    }

    private static int castCounter = 0;
    private static int getCastCounter() { return castCounter++; }



}