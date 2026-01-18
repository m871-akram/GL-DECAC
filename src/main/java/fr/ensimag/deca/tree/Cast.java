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

        if (!compiler.environmentType.assignCompatible(exprType, classType)) {
            throw new ContextualError(
                "cast est incompatible entre :" + classType.getName()+ " et " + exprType.getName(),
                expr.getLocation());
        }

        setType(classType);
        return classType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Évaluer l'expression
        expr.codeGenExpr(compiler, register);

        // 2. Conversion float → int
        if (expr.getType().isFloat() && getType().isInt()) {
            compiler.addInstruction(new INT(register, register));
            return;
        }

        // 3. Conversion int → float (normalement déjà gérée par ConvFloat)
        if (expr.getType().isInt() && getType().isFloat()) {
            compiler.addInstruction(new FLOAT(register, register));
            return;
        }

        // 4. Si c'est un cast vers Float/Int, gérer ici (ConvFloat)
        // Mais Cast est souvent utilisé pour les objets.
        if (getType().isClass()) {
            ClassDefinition targetClassDef = (ClassDefinition) cast.getDefinition();

            Label endLabel = compiler.getSequencer().genSignal("cast_end");
            Label loopLabel = compiler.getSequencer().genSignal("cast_loop");

            // Si null, cast réussi (null est instance de tout)
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.addInstruction(new BEQ(endLabel));

            // Vérification dynamique (instanceof)
            // On utilise R0 et R1 pour parcourir la hiérarchie
            // R0 : VTable de l'objet courant
            // R1 : Adresse VTable cible

            // Charger VTable de l'objet (offset 0)
            compiler.addInstruction(new LOAD(new RegisterOffset(0, register), Register.R0));

            // Charger adresse VTable cible
            RegisterOffset targetVTableAddr = (RegisterOffset) targetClassDef.getOperand();
            compiler.addInstruction(new LEA(targetVTableAddr, Register.R1));

            compiler.addLabel(loopLabel);
            // Si VTable courante == VTable cible -> OK
            compiler.addInstruction(new CMP(Register.R1, Register.R0));
            compiler.addInstruction(new BEQ(endLabel));

            // Sinon, remonter au parent (VTable[0] contient le pointeur super)
            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));

            // Si parent est null (on est arrivé en haut sans trouver) -> Erreur
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



