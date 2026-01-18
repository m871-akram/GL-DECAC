package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

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
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(params);
        Validate.notNull(body);
        this.type = type;
        this.name = name;
        this.params = params;
        this.body=body;
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
        } catch (DoubleDefException e) {
            throw new ContextualError(e.getMessage(), getLocation());
        }
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
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, true);
        name.prettyPrint(s, prefix, true);
        params.prettyPrint(s, prefix, true);
        body.prettyPrint(s, prefix, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        params.iterChildren(f);
        body.iter(f);
    }

    @Override
    protected void verifyDeclMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp methodEnv = new EnvironmentExp(localEnv);
        params.verifyListDeclParamEnv(compiler,methodEnv);
        body.verifyMethodBody(compiler, methodEnv, currentClass, type.getType());
    }

    @Override
    protected void verifyDeclMethodContent(DecacCompiler compiler, ClassDefinition currentClassDef,
            EnvironmentExp localEnv) throws ContextualError {
        EnvironmentExp methodEnv = new EnvironmentExp(localEnv);

        this.params.verifyListDeclParamEnv(compiler, methodEnv);
        this.body.verifyMethodBody(compiler, methodEnv, currentClassDef, type.getType());
        MethodDefinition methodDef = (MethodDefinition) localEnv.get(name.getName()); // récupérée en passe 2
        this.name.setDefinition(methodDef);
        this.name.setType(type.getType());
    }

}