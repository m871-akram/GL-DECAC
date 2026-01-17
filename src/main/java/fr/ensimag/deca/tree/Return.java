package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;

import java.io.PrintStream;

/**
 * @author gl51
 * @date 15/01/2026
 */
public class Return extends AbstractInst {

    private AbstractExpr value;

    public Return(AbstractExpr value) {
        this.value = value;
    }

    public AbstractExpr getValue() {
        return value;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType) throws ContextualError {

        // 1. Vérifier qu'on n'est pas dans une méthode void
        if (returnType.isVoid()) {
            throw new ContextualError("Interdit de retourner une valeur dans une méthode void", getLocation());
        }

        // 2. Vérifier la compatibilité du type et gérer la conversion implicite (Int -> Float)
        // verifyRValue s'occupe de vérifier assignCompatible et d'ajouter ConvFloat si nécessaire
        this.value = this.value.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // 1. Évaluer l'expression de retour dans R0 (Registre de retour conventionnel)
        value.codeGenExpr(compiler, Register.R0);

        // 2. Sauter vers la fin de la méthode pour la restauration du contexte (RTS)
        // Le label doit être reconstruit ou fourni par le compilateur.
        // On suppose ici qu'on peut récupérer les noms via les définitions ou que le compilateur stocke le label de fin courant.

        // Approche robuste : Le compilateur devrait avoir une méthode getCurrentMethodLabel()
        // Si ce n'est pas le cas, on doit reconstruire le nom :
        // String endLabel = "end." + currentClass.getName() + "." + currentMethod.getName();

        // Pour cet exemple, je suppose que tu as ajouté une méthode helper ou que tu passes le label via une stack dans le compilateur.
        // Faute de mieux, je génère le saut vers un label supposé connu.

        // ASTUCE : Si tu n'as pas de gestionnaire de contexte dans DecacCompiler,
        // tu devras peut-être stocker ce label dans une variable statique ou un champ du compilateur lors du visit de DeclMethod.
        // compiler.addInstruction(new BRA(compiler.getCurrentMethodEndLabel()));

        // Placeholder en attendant ton intégration contexte :
        // (Tu devras adapter cette ligne selon comment tu stockes le contexte courant)
        throw new UnsupportedOperationException("Il faut définir le label de fin de méthode dans Return.java");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        value.decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        value.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        value.iter(f);
    }
}