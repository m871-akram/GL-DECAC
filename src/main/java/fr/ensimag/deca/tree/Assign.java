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
        AbstractExpr t2 = this.getRightOperand().verifyRValue(compiler, localEnv, currentClass,t1);
        setRightOperand(t2);
        setType(t1);
        return t1;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition classCourante, Type returnType)
            throws ContextualError {Type exprType = this.verifyExpr(compiler, localEnv, classCourante);
            this.setType(exprType);
    }



    /**
     * Génère le code de l'assignation en tant qu'expression.
     * Le résultat (la valeur assignée) reste dans le registre cible.
     * Permet le chaînage : x = y = 2;
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // 1. Calculer la valeur de droite (RHS) dans le registre cible
        getRightOperand().codeGenExpr(compiler, register);

        // 2. Identifier la L-Value (Variable ou Champ)
        AbstractLValue lValue = getLeftOperand();

        // 3. Stocker la valeur à l'adresse de la L-Value
        if (lValue instanceof Identifier) {
            ((Identifier) lValue).codeGenStore(compiler, register);
        } else if (lValue instanceof Selection) {
            ((Selection) lValue).codeGenStore(compiler, register);
        } else {
            throw new UnsupportedOperationException("Type de LValue non supporté pour assignation: " + lValue.getClass().getSimpleName());
        }

        // Le registre 'register' contient toujours la valeur assignée, prêt pour la suite.
    }

    /**
     * Génère le code de l'assignation en tant qu'instruction.
     * (ex: "x = 3;")
     * On alloue un registre temporaire pour le calcul, puis on le libère car la valeur de retour est ignorée.
     */
    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // 1. Allouer un registre temporaire
        GPRegister reg = compiler.getRegisterManager().prendreRegistre();

        // 2. Générer le code complet (Calcul + Store)
        codeGenExpr(compiler, reg);

        // 3. Libérer le registre (la valeur ne sert plus à rien)
        compiler.getRegisterManager().libererRegistre();
    }

    /**
     * Implémentation vide du contrat AbstractBinaryExpr.
     * Assign surcharge directement codeGenExpr, donc cette méthode ne sera jamais appelée par la logique standard.
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
