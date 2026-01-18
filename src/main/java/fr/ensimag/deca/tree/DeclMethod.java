package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.RTS;

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

    @Override
    protected void verifyDeclMethodPrototype(DecacCompiler compiler, EnvironmentExp superClassEnv,
                                             ClassDefinition currentClassDef, EnvironmentExp localEnv) throws ContextualError {
        // 1. Vérifier le type de retour
        Type returnType = type.verifyType(compiler);
        
        // 2. Créer la signature de la méthode
        Signature signature = params.verifyListDeclParam(compiler);
        
        // 3. Vérifier si la méthode est une redéfinition (override)
        int methodIndex;
        if (superClassEnv != null) {
            ExpDefinition superMethodDef = superClassEnv.get(name.getName());
            if (superMethodDef != null && superMethodDef.isMethod()) {
                // C'est une redéfinition
                MethodDefinition superMethod = (MethodDefinition) superMethodDef;
                
                // Vérifier que la signature est compatible
                if (!returnType.sameType(superMethod.getType())) {
                    throw new ContextualError("Le type de retour de la méthode redéfinie doit être identique", getLocation());
                }
                
                Signature superSig = superMethod.getSignature();
                if (signature.size() != superSig.size()) {
                    throw new ContextualError("Le nombre de paramètres doit être identique lors de la redéfinition", getLocation());
                }
                
                for (int i = 0; i < signature.size(); i++) {
                    if (!signature.paramNumber(i).sameType(superSig.paramNumber(i))) {
                        throw new ContextualError("Les types des paramètres doivent être identiques lors de la redéfinition", getLocation());
                    }
                }
                
                // Utiliser le même index que la méthode parente
                methodIndex = superMethod.getIndex();
            } else {
                // Nouvelle méthode
                methodIndex = currentClassDef.getNumberOfMethods();
                currentClassDef.incNumberOfMethods();
            }
        } else {
            // Pas de super-classe (Object) - nouvelle méthode
            methodIndex = currentClassDef.getNumberOfMethods();
            currentClassDef.incNumberOfMethods();
        }
        
        // 4. Créer la définition de la méthode
        MethodDefinition methodDef = new MethodDefinition(returnType, getLocation(), signature, methodIndex);
        
        // 5. Créer et assigner le label de la méthode (code.ClassName.methodName)
        String className = currentClassDef.getType().getName().getName();
        String methodName = name.getName().getName();
        Label methodLabel = new Label("code." + className + "." + methodName);
        methodDef.setLabel(methodLabel);
        
        // 6. Déclarer la méthode dans l'environnement de la classe
        try {
            localEnv.declare(name.getName(), methodDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("Méthode " + name.getName() + " déjà définie dans cette classe", getLocation());
        }
        
        // 7. Lier l'identifiant à sa définition
        name.setDefinition(methodDef);
        name.setType(returnType);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentType envTypes, 
                                    ClassDefinition nameClass) throws ContextualError {
        // Créer un environnement local pour cette méthode
        EnvironmentExp localEnv = new EnvironmentExp(null);
        
        // Déclarer les paramètres dans l'environnement local
        params.verifyListDeclParamBody(compiler, localEnv);
        
        // Vérifier le corps de la méthode
        Type returnType = type.getType();
        body.verifyMethodBody(compiler, localEnv, nameClass, returnType);
    }

    public AbstractIdentifier getMethodName() {
        return name;
    }

    @Override
    protected void codeGenDeclMethod(DecacCompiler compiler) {
        // 1. Label de la méthode  
        MethodDefinition methodDef = (MethodDefinition) name.getDefinition();
        Label methodLabel = methodDef.getLabel();
        compiler.addLabel(methodLabel);
        compiler.addComment("Méthode " + name.getName().getName());

        // 2. Nouveau Contexte Mémoire (Reset LB et compteurs pile)
        compiler.getMMU().enterNewMethodFrame();
        compiler.getRegisterManager().reset();

        // 3. Gestion des paramètres (Liaison -3(LB)...)
        params.codeGenListDeclParam(compiler);

        // 4. Créer un programme temporaire pour le corps de la méthode
        fr.ensimag.ima.pseudocode.IMAProgram bodyProgram = new fr.ensimag.ima.pseudocode.IMAProgram();
        fr.ensimag.ima.pseudocode.IMAProgram originalProgram = compiler.swapProgram(bodyProgram);
        
        // 5. Générer le corps dans le programme temporaire (ceci va appeler notifyPush/notifyPop)
        body.codeGenMethodBody(compiler);
        
        // 6. Restaurer le programme original
        compiler.swapProgram(originalProgram);
        
        // 7. Maintenant on connaît le maxStack, générer le prologue
        int nbLocales = ((MethodBody) body).getLocalVarsCount();
        int maxStack = compiler.getMMU().getStackRequirements();
        
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.TSTO(maxStack));
        compiler.getIrqController().triggerInterrupt(compiler,
                fr.ensimag.deca.codegen.InterruptVector.IRQ_STACK_OVERFLOW);
        compiler.addInstruction(new fr.ensimag.ima.pseudocode.instructions.ADDSP(nbLocales));
        
        // 8. Ajouter le corps après le prologue
        compiler.appendProgram(bodyProgram);

        // 9. Retour par défaut (RTS) si pas de return explicite
        compiler.addInstruction(new RTS());
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
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        params.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }
}