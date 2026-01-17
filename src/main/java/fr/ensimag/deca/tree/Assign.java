package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (t1.isFloat() && t2.isInt()) {
            ConvFloat conv = new ConvFloat(getRightOperand());
            conv.verifyExpr(compiler, localEnv, currentClass); // ConvFloat
            this.setRightOperand(conv);
            t2 = conv.getType(); // t2 devient float
        }

        if(!compiler.environmentType.assignCompatible(t1, t2)){
            throw new ContextualError(
                "Assignment entre des types invalides: " + t1 + " et " + t2,
                this.getLocation());
        }
        setType(t1);
        return t1;
    }



    /**
     * Génère le code pour l'instruction d'assignation
     * (Ne retourne pas de registre, stocke juste la valeur)
     */
    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // 1. Allouer un registre pour le calcul de droite
        GPRegister reg = compiler.getRegisterManager().prendreRegistre();

        // 2. Calculer l'expression de droite dans ce registre
        getRightOperand().codeGenExpr(compiler, reg);

        // 3. Demander à la partie gauche (LValue) de stocker ce registre
        AbstractLValue lValue = getLeftOperand();

        if (lValue instanceof Identifier) {
            ((Identifier) lValue).codeGenStore(compiler, reg);
        }
        else if (lValue instanceof Selection) {
            // Pour la partie Objet : ((Selection) lValue).codeGenStore(compiler, reg);
            // En attendant l'implémentation de Selection :
            throw new UnsupportedOperationException("Selection assignment not implemented yet");
        }

        // 4. Libérer le registre
        compiler.getRegisterManager().libererRegistre();
    }

    /**
     * Génère le code si l'assignation est utilisée comme expression
     * (ex: if ((x = 3) > 0) ...)
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Calculer droite dans le registre cible 'register'
        getRightOperand().codeGenExpr(compiler, register);

        // 2. Stocker la valeur (effet de bord)
        AbstractLValue lValue = getLeftOperand();

        if (lValue instanceof Identifier) {
            ((Identifier) lValue).codeGenStore(compiler, register);
        } else {
            // Selection (à faire)
        }

        // La valeur reste dans 'register', ce qui permet le chaînage
    }

    /**
     * Implémentation du contrat AbstractBinaryExpr (ne devrait pas être appelée
     * car on override codeGenExpr, mais obligatoire pour compiler).
     */
    @Override
    protected void codeGenInst(DecacCompiler compiler, DVal opSource, GPRegister opDest) {
        // Vide : Assign a sa propre logique dans codeGenExpr
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

}
