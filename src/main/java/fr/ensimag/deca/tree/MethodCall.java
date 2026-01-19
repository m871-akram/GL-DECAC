package fr.ensimag.deca.tree;
import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;

public class MethodCall extends AbstractExpr {
    private final ListExpr args;
    private final AbstractExpr object;
    private final AbstractIdentifier methode;
    public MethodCall(AbstractExpr object, AbstractIdentifier methode, ListExpr args){
        Validate.notNull(object);
        Validate.notNull(methode);
        Validate.notNull(args);
        this.object=object;
        this.methode=methode;
        this.args=args;
    }
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type callType = verifyExpr(compiler, localEnv, currentClass);
        if (returnType != null && !callType.sameType(returnType)) {
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


    public AbstractExpr getObject() { return object; }

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

        // 5. Appel indirect : IMA ne supporte pas BSR avec registre
        // On simule BSR manuellement :
        // BSR fait : empiler PC, empiler LB, LB=SP, PC=destination
        // Comme on ne peut pas accéder à PC, on utilise un pattern différent :
        // On sauvegarde l'adresse dans un registre temporaire et on utilise BRA
        
        // Sauvegarder l'adresse de la méthode dans R0 (registre scratch)
        GPRegister methodAddrReg = register; // L'adresse est déjà dans register
        
        // On doit empiler manuellement les éléments du frame avant de sauter
        // Mais comme BRA ne crée pas de frame, on doit appeler une fonction intermédiaire
        // Solution : Utiliser PUSH + ajuster et sauter
        
        // Alternative : Stocker l'adresse dans un emplacement temporaire et utiliser BSR avec label
        // Mais c'est complexe. Pour l'instant, gardons BSR et documentons la limitation
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
        if(!object.isImplicit()){
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
        object.prettyPrint(s, prefix,true);
        methode.prettyPrint(s, prefix,true);
        args.prettyPrint(s, prefix,false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        methode.iter(f);
        args.iterChildren(f);
    }

}