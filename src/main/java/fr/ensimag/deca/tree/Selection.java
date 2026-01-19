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
            if (!compiler.environmentType.subType(typeExpr, currentClass.getType())) {
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
        // 1. Évaluer l'objet dans le registre
        expr.codeGenExpr(compiler, register);

        // 2. Vérifier Null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), register));
            compiler.getIrqController().triggerInterrupt(compiler, InterruptVector.IRQ_NULL_PTR);
        }

        // 3. Charger le champ (LOAD offset(Reg), Reg)
        int index = fieldName.getFieldDefinition().getIndex();
        compiler.addInstruction(new LOAD(new RegisterOffset(index, register), register));
    }

    /**
     * Génère le code pour stocker une valeur DANS ce champ (pour Assign).
     * @param compiler Le compilateur
     * @param sourceRegister Le registre contenant la valeur à écrire
     */
    public void codeGenStore(DecacCompiler compiler, GPRegister sourceRegister) {
        // Pour faire object.field = val, on a besoin de l'adresse de object.
        // sourceRegister contient déjà 'val'.
        // Il nous faut un autre registre pour calculer 'object'.

        GPRegister addrReg = compiler.getRegisterManager().prendreRegistre();

        // 1. Calculer l'adresse de l'objet
        expr.codeGenExpr(compiler, addrReg);

        // 2. Vérifier Null
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new CMP(new NullOperand(), addrReg));
            compiler.getIrqController().triggerInterrupt(compiler, InterruptVector.IRQ_NULL_PTR);
        }

        // 3. Stocker la valeur (STORE source, offset(addrReg))
        int index = fieldName.getFieldDefinition().getIndex();
        compiler.addInstruction(new STORE(sourceRegister, new RegisterOffset(index, addrReg)));

        // 4. Libérer le registre d'adresse
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