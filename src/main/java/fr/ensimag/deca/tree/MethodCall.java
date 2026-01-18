package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptController;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.*;
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

    public MethodCall(AbstractExpr object, AbstractIdentifier methode, ListExpr args){
        Validate.notNull(object);
        Validate.notNull(methode);
        Validate.notNull(args);
        this.object = object;
        this.methode = methode;
        this.args = args;
    }

    public AbstractExpr getObject() { return object; }
    public AbstractIdentifier getMethodName() { return methode; }
    public ListExpr getArguments() { return args; }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // 1. Vérification de l'objet
        Type objectType = object.verifyExpr(compiler, localEnv, currentClass);

        if (!objectType.isClass()) {
            throw new ContextualError("Appel de méthode sur un type non-classe : " + objectType, getLocation());
        }

        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(objectType.getName());

        // 2. Recherche de la méthode dans la classe
        ExpDefinition def = classDef.getMembers().get(methode.getName());
        if (def == null) {
            throw new ContextualError("Méthode " + methode.getName() + " inexistante dans " + classDef.getType(), getLocation());
        }
        if (!def.isMethod()) {
            throw new ContextualError(methode.getName() + " n'est pas une méthode", getLocation());
        }

        MethodDefinition methodDef = (MethodDefinition) def;

        // 3. Vérification de la signature (Arguments)
        Signature sig = methodDef.getSignature();
        if (args.size() != sig.size()) {
            throw new ContextualError("Nombre d'arguments incorrect", getLocation());
        }

        int i = 0;
        for (AbstractExpr arg : args.getList()) {
            // Vérification de chaque argument (avec conversion implicite potentielle)
            Type paramType = sig.paramNumber(i);

            // On utilise verifyRValue pour gérer la compatibilité + conversion float
            AbstractExpr verifiedArg = arg.verifyRValue(compiler, localEnv, currentClass, paramType);
            args.set(i, verifiedArg); // Mise à jour de l'argument converti
            i++;
        }

        // 4. Liaison
        methode.setDefinition(methodDef);
        setType(methodDef.getType());
        return methodDef.getType();
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        // Un appel de méthode peut être utilisé comme instruction (statement)
        // C'est valide uniquement si la méthode retourne void
        verifyExpr(compiler, localEnv, currentClass);
        // Pas besoin de vérifier si c'est void - un appel de méthode peut être une instruction
        // même s'il retourne une valeur (la valeur est simplement ignorée)
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Calculer l'adresse de l'objet (this)
        // On utilise 'register' pour stocker l'adresse de l'objet
        object.codeGenExpr(compiler, register);

        // 2. Vérification null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptVector.IRQ_NULL_PTR);
        }

        // 3. Empiler les paramètres (Convention : empiler le résultat de l'évaluation)
        // On a besoin d'un registre temporaire pour calculer les params si 'register' tient 'this'
        // ASTUCE : On peut calculer les params AVANT de calculer 'this' pour libérer les registres,
        // puis empiler. Ou alors utiliser la pile.
        // Pour simplifier ici : On empile directement le résultat de l'évaluation.

        // ADDSP pour réserver la place des paramètres + this
        compiler.addInstruction(new ADDSP(args.size() + 1));
        compiler.getMMU().notifyPush(args.size() + 1);

        // Stocker 'this' (qui est dans 'register') à 0(SP)
        compiler.addInstruction(new STORE(register, new RegisterOffset(0, Register.SP)));

        // Calculer et stocker les arguments
        int index = -1; // -1(SP), -2(SP)...
        for (AbstractExpr arg : args.getList()) {
            // On réutilise 'register' car on a déjà sauvegardé 'this' sur la pile
            arg.codeGenExpr(compiler, register);
            compiler.addInstruction(new STORE(register, new RegisterOffset(index, Register.SP)));
            index--;
        }

        // 4. Récupérer l'adresse de la méthode (Liaison Dynamique)
        // On recharge 'this' dans 'register' depuis la pile pour accéder à la vTable
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), register));

        // Charger adresse VTable (0(this))
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), register));

        // Charger adresse méthode (index + 1 dans VTable, car 0 = super)
        int methodIndex = methode.getMethodDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(methodIndex, register), register));

        // 5. Appel (BSR sur registre)
        compiler.addInstruction(new BSR(register));

        // 6. Nettoyage Pile
        // On a fait ADDSP, on doit faire SUBSP
        compiler.addInstruction(new SUBSP(args.size() + 1));
        compiler.getMMU().notifyPop(args.size() + 1);

        // 7. Résultat
        // Le résultat est dans R0. On le copie dans le registre cible si besoin
        if (getType() != compiler.environmentType.VOID) {
            compiler.addInstruction(new LOAD(Register.R0, register));
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Pour un appel de méthode utilisé comme instruction,
        // on alloue un registre temporaire et on appelle codeGenExpr
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
        args.iter(f);
    }
}