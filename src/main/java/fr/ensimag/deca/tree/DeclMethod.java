package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

public class DeclMethod extends AbstractDeclMethod {


    private final AbstractIdentifier type;
    private final AbstractIdentifier name;
    private final ListDeclParam params;
    private final AbstractMethodBody body;

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier name,
                      ListDeclParam params, AbstractMethodBody body) {
        this.type = type;
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public void verifyDeclMethodPrototype(DecacCompiler compiler,
                                         EnvironmentExp superClassEnv, ClassDefinition currentClassDef, EnvironmentExp localEnv)
        throws ContextualError {
        ExpDefinition superDef = superClassEnv.get(name.getName());
        Signature signature = params.verifyListDeclParam(compiler);
        Type returnType = type.verifyType(compiler);
        if (superDef != null) {

            MethodDefinition superMethod = superDef.asMethodDefinition("Le nom existe dans la super-classe mais ce n'est pas une methode", getLocation());

            Signature sigSuper = superMethod.getSignature();

            if (signature.size() != sigSuper.size()) {
                throw new ContextualError(
                    "La signature de la methode redefinie doit avoir le meme nombre de parametres",
                    getLocation()
                );
            }

            for (int i = 0; i < signature.size(); i++) {
                if (!signature.paramNumber(i).sameType(sigSuper.paramNumber(i))) {
                    throw new ContextualError(
                        "Les types des parametres doivent correspondre à ceux de la methode heritee",
                        getLocation()
                    );
                }
            }

            Type typeSuper = superMethod.getType();

            if (!compiler.environmentType.subType(returnType,typeSuper)) {
                throw new ContextualError(
                    "Le type de retour de la methode redefinie doit etre un sous-type du type de la super-classe",
                    getLocation()
                );
            }
        }
        MethodDefinition methodDef = new MethodDefinition(returnType, getLocation(), signature, currentClassDef.incNumberOfMethods());
        try {
            localEnv.declare(name.getName(), methodDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(e.getMessage(), getLocation());
        }
    }

    public void verifyDeclMethodBody(DecacCompiler compiler) throws ContextualError {
        // Vérification de body passe 3
        throw new UnsupportedOperationException("not yet implemented");

    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");

        name.decompile(s);

        s.print("(");
        params.decompile(s);
        s.print(")");


        body.decompile(s);

    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        params.iter(f);
        body.iter(f);
    }

    @Override
    protected void codeGenDeclMethod(DecacCompiler compiler) {
        // Génération du label code.Classe.Methode [7]
        compiler.addLabel(name.getMethodDefinition().getLabel());
        body.codeGenMethodBody(compiler);
    }

    protected void codeGenMethod(DecacCompiler compiler) {
        String className = getCurrentClass().getName().getName();
        String methodName = getMethodName().getName().getName();

        compiler.addLabel(new Label("code." + className + "." + methodName));
        compiler.addComment("===== Méthode " + className + "." + methodName + " =====");

        // 1. Réinitialiser le RegisterManager pour ce nouveau bloc
        compiler.getRegisterManager().resetForNewBlock();

        // 2. Générer le corps (pour calculer TSTO)
        // On génère d'abord dans un buffer temporaire
        IMAProgram tempProgram = compiler.swapProgram(new IMAProgram()); // NOUVELLE méthode utilitaire

        // Sauvegarder R2-R15 (on va calculer lesquels sont utilisés)
        List<GPRegister> usedRegisters = new ArrayList<>();
        for (int i = 2; i <= compiler.getRegisterManager().getRegistreMax(); i++) {
            usedRegisters.add(Register.getR(i));
        }

        for (GPRegister reg : usedRegisters) {
            compiler.addInstruction(new PUSH(reg));
            compiler.getRegisterManager().empiler();
        }

        // Générer le code du corps
        getMethodBody().codeGenInst(compiler);

        // Label de fin (pour les return)
        compiler.addLabel(new Label("end." + className + "." + methodName));

        // Restaurer R2-R15
        for (int i = usedRegisters.size() - 1; i >= 0; i--) {
            compiler.addInstruction(new POP(usedRegisters.get(i)));
            compiler.getRegisterManager().depiler();
        }

        compiler.addInstruction(new RTS());

        // 3. Récupérer le TSTO calculé
        int tstoValue = compiler.getRegisterManager().getTSTOValue();
        IMAProgram bodyProgram = compiler.swapProgram(tempProgram);

        // 4. Insérer TSTO en tête
        compiler.addInstruction(new TSTO(new ImmediateInteger(tstoValue)));
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(new Label("erreur_pile_OV")));
        }

        // 5. ADDSP pour les variables locales
        int nbLocals = getMethodBody().countLocalVariables(); // NOUVELLE méthode
        if (nbLocals > 0) {
            compiler.addInstruction(new ADDSP(new ImmediateInteger(nbLocals)));
        }

        // 6. Réinsérer le code du corps
        compiler.appendProgram(bodyProgram); // NOUVELLE méthode
    }


//    protected void verifyDeclMethod(DecacCompiler compiler, Symbol currentClass, Symbol superClass)
//            throws ContextualError {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    @Override
//    protected void verifyMethodMembers(DecacCompiler compiler, Symbol superClass)
//            throws ContextualError {
//        // Règle (2.7) : Vérification de la signature et du type de retour [1]
//        Type returnType = type.verifyType(compiler);
//        Signature sig = params.verifyListDeclParam(compiler);
//
//        ClassDefinition superDef = (ClassDefinition) compiler.environmentType.defOfType(superClass);
//        MethodDefinition methodDef;
//
//        // Vérifier si la méthode existe déjà dans la super-classe (redéfinition)
//        ExpDefinition inheritedDef = superDef.getMembers().get(name.getName());
//        if (inheritedDef != null && inheritedDef.isMethod()) {
//            MethodDefinition inheritedMethod = inheritedDef.asMethodDefinition("Not a method", getLocation());
//
//            // Doit avoir la même signature [1]
//            if (!inheritedMethod.getSignature().equals(sig)) {
//                throw new ContextualError("Redéfinition de méthode avec une signature différente", getLocation());
//            }
//            // Le type de retour doit être un sous-type du type de retour hérité [3]
//            if (!compiler.environmentType.subType(returnType, inheritedMethod.getType())) {
//                throw new ContextualError("Type de retour incompatible pour la redéfinition", getLocation());
//            }
//            // On conserve le même index pour la liaison dynamique [2]
//            methodDef = new MethodDefinition(returnType, getLocation(), sig, inheritedMethod.getIndex());
//        } else {
//            // Nouvelle méthode : index = max(Object.equals) + nombre de méthodes de la classe [2, 4]
//            int index = superDef.getNumberOfMethods() + 1;
//            methodDef = new MethodDefinition(returnType, getLocation(), sig, index);
//        }
//
//        try {
//            // Déclaration dans l'environnement de la classe courante [1]
//            compiler.getContext().getMembers().declare(name.getName(), methodDef);
//        } catch (EnvironmentExp.DoubleDefException e) {
//            throw new ContextualError("Méthode " + name.getName() + " déjà définie dans cette classe", getLocation());
//        }
//
//        name.setDefinition(methodDef);
//    }
//
//    @Override
//    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentType envTypes, ClassDefinition nameClass)
//            throws ContextualError {
//        // Passe 3 : Vérifier le corps (instructions et paramètres) [5, 6]
//        EnvironmentExp localEnv = new EnvironmentExp(nameClass.getMembers());
//        params.verifyListDeclParamBody(compiler, localEnv);
//        body.verifyMethodBody(compiler, localEnv, nameClass, type.getType());
//    }

}


