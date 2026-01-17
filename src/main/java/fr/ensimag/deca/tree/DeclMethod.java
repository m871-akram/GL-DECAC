package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

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
        // ... (Ton code de vérification était correct) ...
        Type returnType = type.verifyType(compiler);
        // Signature signature = params.verifyListDeclParam(compiler);
        // ... Vérification redéfinition ...
        // ... Déclaration ...
    }

    @Override
    protected void verifyDeclMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        // Créer un environnement pour les paramètres
        // params.verifyListDeclParamBody(compiler, localEnv);
        // body.verifyMethodBody(...)
    }

    @Override
    protected void codeGenDeclMethod(DecacCompiler compiler) {
        // 1. Label de la méthode
        // ex: code.Object.equals
        // Label methodLabel = new Label("code." + currentClass.getName() + "." + name.getName());
        // compiler.addLabel(methodLabel);

        // 2. Nouveau Contexte Mémoire (Reset LB et compteurs pile)
        compiler.getMMU().enterNewMethodFrame();
        compiler.getRegisterManager().reset();

        // 3. Gestion des paramètres (Liaison -3(LB)...)
        // params.codeGenListDeclParam(compiler);

        // 4. Génération du corps (TSTO sera géré ici via la MMU)
        // body.codeGenMethodBody(compiler);

        // 5. Retour par défaut (RTS) et restauration registres
        // Géré par Return ou fin de méthode
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