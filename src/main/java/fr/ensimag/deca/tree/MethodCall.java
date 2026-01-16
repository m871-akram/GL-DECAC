package fr.ensimag.deca.tree;
import java.io.PrintStream;

import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;

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
        if (args.size() != methodDef.getSignature().size()) {
            throw new ContextualError(
                "Nombre d'arguments incorrect pour la méthode '" + methode.getName().getName() + "'",
                getLocation()
            );
        }
        int i =0;
        for (AbstractExpr arg : args.getList()) {
            Type argType = arg.verifyExpr(compiler, localEnv, currentClass);
            Type paramType = methodDef.getSignature().paramNumber(i);

            if (!argType.sameType(paramType)) {
                throw new ContextualError(
                    "Type de l'argument " + arg.prettyPrint() + " incorrect : attendu "
                    + paramType + ", trouvé " + argType,
                    arg.getLocation()
                );
            }
            i++;
        }

        methode.setDefinition(methodDef);

        Type returnType = methodDef.getType();
        setType(returnType);
        return returnType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        ClassType classType = getObject().getType().asClassType("Not a class", getLocation());
        ClassDefinition classDef = classType.getDefinition();
        MethodDefinition methodDef = getMethodName().getMethodDefinition();

        // 1. Empiler les paramètres en ordre inverse
        ListExpr params = getArguments();
        for (int i = params.size() - 1; i >= 0; i--) {
            GPRegister paramReg = compiler.getRegisterManager().prendreRegistre();
            params.getList().get(i).codeGenExpr(compiler, paramReg);
            compiler.addInstruction(new PUSH(paramReg));
            compiler.getRegisterManager().empiler();
            compiler.getRegisterManager().libererRegistre();
        }

        // 2. Empiler this (l'objet)
        getObject().codeGenExpr(compiler, register);

        // Vérifier null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.addInstruction(new BEQ(new Label("dereferencement_null")));
        }

        compiler.addInstruction(new PUSH(register));
        compiler.getRegisterManager().empiler();

        // 3. Liaison dynamique : récupérer l'adresse de la méthode depuis la vTable
        // vTable = [objet + 0]
        compiler.addInstruction(new LOAD(new RegisterOffset(0, register), register)); // Adresse vTable

        // Adresse méthode = vTable[index+1] (offset +1 car vTable[0] = super)
        int methodIndex = methodDef.getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(methodIndex + 1, register), register));

        // 4. Appel indirect
        compiler.addInstruction(new BSR(register)); // BSR Rm (IMA 2.9)

        // 5. Nettoyer la pile (this + params)
        int nbParams = params.size();
        compiler.addInstruction(new SUBSP(new ImmediateInteger(nbParams + 1)));
        for (int i = 0; i < nbParams + 1; i++) {
            compiler.getRegisterManager().depiler();
        }

        // 6. Résultat dans R0, le copier dans register
        if (!register.equals(Register.R0)) {
            compiler.addInstruction(new LOAD(Register.R0, register));
        }
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

//    @Override
//    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
//                           ClassDefinition currentClass) throws ContextualError {
//        // Rule (3.71): Verify the target is a class and the method exists in its environment [9].
//        Type targetType = target.verifyExpr(compiler, localEnv, currentClass);
//        ClassType classType = targetType.asClassType("Method call on non-class type", getLocation());
//
//        // Find method definition in the class members
//        ExpDefinition def = classType.getDefinition().getMembers().get(method.getName());
//        if (def == null || !def.isMethod()) {
//            throw new ContextualError("Method " + method.getName() + " not found in class " + classType.getName(),
//                    method.getLocation());
//        }
//        MethodDefinition methodDef = (MethodDefinition) def;
//
//        // Verify arguments against the method signature [9].
//        args.verifyListExpr(compiler, localEnv, currentClass, methodDef.getSignature());
//
//        this.setType(methodDef.getType());
//        method.setDefinition(methodDef);
//        return methodDef.getType();
//    }
}

