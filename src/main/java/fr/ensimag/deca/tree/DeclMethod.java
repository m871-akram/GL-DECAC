package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

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
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(") ");
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


