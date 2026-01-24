package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class MethodCall extends AbstractExpr {
    private final ListExpr args;
    private final AbstractExpr object;
    private final AbstractIdentifier methode;

    public MethodCall(AbstractExpr object, AbstractIdentifier methode, ListExpr args) {
        Validate.notNull(object);
        Validate.notNull(methode);
        Validate.notNull(args);
        this.object = object;
        this.methode = methode;
        this.args = args;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type callType = verifyExpr(compiler, localEnv, currentClass);
        if (!returnType.isVoid() && !callType.sameType(returnType)) {
            throw new ContextualError(
                    "Type de retour de la methode incorrect : attendu "
                            + returnType + ", trouve " + callType,
                    getLocation()
            );
        }
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type objectType = object.verifyExpr(compiler, localEnv, currentClass);

        if (!objectType.isClass()) {
            throw new ContextualError(
                    "Appel de methode sur un type non-classe",
                    getLocation()
            );
        }
        ClassDefinition classDef = objectType.asClassType(
                "Type non-classe dans un appel de methode",
                getLocation()
        ).getDefinition();

        var def = classDef.getMembers().get(methode.getName());
        if (def == null) {
            throw new ContextualError(
                    "Méthode '" + methode.getName().getName() + "' inexistante",
                    getLocation()
            );
        }

        if (!def.isMethod()) {
            throw new ContextualError(
                    "'" + methode.getName().getName() + "' n'est pas une méthode",
                    getLocation()
            );
        }

        var methodDef = def.asMethodDefinition(
                "Ce n'est pas une méthode",
                getLocation()
        );
        args.verifyRValue(compiler, localEnv, currentClass, methodDef.getSignature(), getLocation());

        methode.setDefinition(methodDef);

        Type returnType = methodDef.getType();
        setType(returnType);
        return returnType;
    }


    public AbstractExpr getObject() {
        return object;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // calc adresse objet (this)
        object.codeGenExpr(compiler, register);

        // verif null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptVector.IRQ_NULL_PTR);
        }

        // reserve place params + this
        compiler.addInstruction(new ADDSP(args.size() + 1));
        compiler.getMMU().notifyPush(args.size() + 1);

        // stocke this
        compiler.addInstruction(new STORE(register, new RegisterOffset(0, Register.SP)));

        // calc et stocke arguments
        int index = -1;
        for (AbstractExpr arg : args.getList()) {
            arg.codeGenExpr(compiler, register);
            compiler.addInstruction(new STORE(register, new RegisterOffset(index, Register.SP)));
            index--;
        }

        // Récupérer le paramètre implicite (this) pour l'appel virtuel
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R1));

        // Vérification null (comme dans le compilateur de référence)
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), Register.R1));
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptVector.IRQ_NULL_PTR);
        }

        // Charger l'adresse de la VTable depuis l'objet (offset 0)
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));

        // Charger l'adresse de la méthode depuis la VTable dans un registre
        int methodIndex = methode.getMethodDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(methodIndex, Register.R1), Register.R1));

        // Appel virtuel : BSR R1 (registre contenant l'adresse de la méthode)
        compiler.addInstruction(new BSR(Register.R1));

        // nettoyage pile
        compiler.addInstruction(new SUBSP(args.size() + 1));
        compiler.getMMU().notifyPop(args.size() + 1);

        // resultat dans r0
        if (getType() != compiler.environmentType.VOID) {
            compiler.addInstruction(new LOAD(Register.R0, register));
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // appel comme instruction, alloue reg temp
        GPRegister reg = compiler.getRegisterManager().prendreRegistre();
        codeGenExpr(compiler, reg);
        compiler.getRegisterManager().libererRegistre();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (!object.isImplicit()) {
            object.decompile(s);
            s.print(".");
        }
        methode.decompile(s);
        s.print("(");
        args.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, true);
        methode.prettyPrint(s, prefix, true);
        args.prettyPrint(s, prefix, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        methode.iter(f);
        args.iterChildren(f);
    }

}