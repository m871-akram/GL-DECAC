package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Selection, i.e. object.field
 * @author gl51
 * @date 15/01/2026
 */
public class Selection extends AbstractLValue {

    private final AbstractExpr expr;
    private final AbstractIdentifier fieldName;

    public Selection(AbstractExpr expr, AbstractIdentifier fieldName) {
        Validate.notNull(expr);
        Validate.notNull(fieldName);
        this.expr = expr;
        this.fieldName = fieldName;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

        // 1. Vérifier que la partie gauche est une classe
        Type typeExpr = expr.verifyExpr(compiler, localEnv, currentClass);
        if (!typeExpr.isClass()) {
            throw new ContextualError("La sélection s'applique uniquement aux classes", getLocation());
        }

        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(typeExpr.getName());

        // 2. Vérifier que le champ existe dans la classe
        ExpDefinition def = classDef.getMembers().get(fieldName.getName());
        if (def == null || !def.isField()) {
            throw new ContextualError("Champ " + fieldName.getName() + " inexistant dans " + classDef.getType(), getLocation());
        }

        FieldDefinition fieldDef = (FieldDefinition) def;

        // 3. Vérification de la visibilité (PROTECTED)
        if (fieldDef.getVisibility() == Visibility.PROTECTED) {
            // Si on est dans le main (currentClass == null) ou si la classe courante n'est pas sous-classe
            // CORRECTION ICI : On utilise .getType() pour passer de Definition à Type
            if (currentClass == null || !currentClass.getType().isSubClassOf(fieldDef.getContainingClass().getType())) {
                throw new ContextualError("Accès impossible au champ protégé " + fieldName.getName(), getLocation());
            }

            // Condition supplémentaire : le type de l'objet (expr) doit être sous-type de la classe courante
            // On vérifie que typeExpr (le type de l'expression) est un sous-type de currentClass
            // subType(T1, T2) retourne vrai si T2 <: T1, donc on passe currentClass en premier
            if (!compiler.environmentType.subType(currentClass.getType(), typeExpr)) {
                throw new ContextualError("Accès protégé invalide : le type de l'expression n'est pas un sous-type de la classe courante", getLocation());
            }
        }

        // 4. Décoration
        fieldName.setDefinition(fieldDef);
        setType(fieldDef.getType());
        return fieldDef.getType();
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // evalue l'objet
        expr.codeGenExpr(compiler, register);

        // verif null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.getIrqController().triggerInterrupt(compiler, InterruptVector.IRQ_NULL_PTR);
        }

        // charge le champ
        int index = fieldName.getFieldDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(index, register), register));
    }

    /**
     * gen code pour stocker dans ce champ (pour assign)
     */
    public void codeGenStore(DecacCompiler compiler, GPRegister sourceRegister) {
        // besoin reg pour adresse objet
        GPRegister addrReg = compiler.getRegisterManager().prendreRegistre();

        // calc adresse objet
        expr.codeGenExpr(compiler, addrReg);

        // verif null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), addrReg));
            compiler.getIrqController().triggerInterrupt(compiler, InterruptVector.IRQ_NULL_PTR);
        }

        // store la valeur
        int index = fieldName.getFieldDefinition().getIndex();
        compiler.addInstruction(new STORE(sourceRegister, new RegisterOffset(index, addrReg)));

        // libere reg adresse
        compiler.getRegisterManager().libererRegistre();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expr.decompile(s);
        s.print(".");
        fieldName.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        fieldName.iter(f);
    }
}