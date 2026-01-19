package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;


/**
 *
 * @author gl51
 * @date 15/01/2026
 */
public class Cast extends AbstractExpr {
    private AbstractExpr expr;
    private AbstractIdentifier cast;
    public Cast(AbstractIdentifier ident, AbstractExpr expr) {
        Validate.notNull(expr, "left operand cannot be null");
        Validate.notNull(ident, "right operand cannot be null");
        this.expr = expr;
        this.cast = ident;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);
        Type classType = cast.verifyType(compiler);

        if (!compiler.environmentType.castCompatible(exprType, classType)) {
            throw new ContextualError(
                "cast est incompatible entre :" + classType.getName()+ " et " + exprType.getName(),
                expr.getLocation());
        }

        setType(classType);
        return classType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // evalue expression
        expr.codeGenExpr(compiler, register);

        // conversion float -> int
        if (expr.getType().isFloat() && getType().isInt()) {
            compiler.addInstruction(new INT(register, register));
            return;
        }

        // conversion int -> float
        if (expr.getType().isInt() && getType().isFloat()) {
            compiler.addInstruction(new FLOAT(register, register));
            return;
        }

        // cast objet
        if (getType().isClass()) {
            ClassDefinition targetClassDef = (ClassDefinition) cast.getDefinition();

            Label endLabel = compiler.getSequencer().genSignal("cast_end");
            Label loopLabel = compiler.getSequencer().genSignal("cast_loop");

            // si null, cast ok
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.addInstruction(new BEQ(endLabel));

            // verif dynamique avec r0 et r1
            // charge vtable objet
            compiler.addInstruction(new LOAD(new RegisterOffset(0, register), Register.R0));

            // charge vtable cible
            RegisterOffset targetVTableAddr = (RegisterOffset) targetClassDef.getOperand();
            compiler.addInstruction(new LEA(targetVTableAddr, Register.R1));

            compiler.addLabel(loopLabel);
            // si vtable courante == cible -> ok
            compiler.addInstruction(new CMP(Register.R1, Register.R0));
            compiler.addInstruction(new BEQ(endLabel));

            // remonte au parent
            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));

            // si parent null -> erreur
            compiler.addInstruction(new CMP(new NullOperand(), Register.R0));
            compiler.addInstruction(new BNE(loopLabel));

            // Erreur Cast
            if (!compiler.getCompilerOptions().getNoCheck()) {
                compiler.getIrqController().triggerInterrupt(compiler,
                        InterruptVector.IRQ_CAST_ERROR);
            }

            compiler.addLabel(endLabel);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        cast.decompile(s);
        s.print(")");
        s.print("(");
        expr.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        cast.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        cast.iter(f);
    }

}



