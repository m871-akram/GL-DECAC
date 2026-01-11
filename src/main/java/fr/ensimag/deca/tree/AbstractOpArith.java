package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl51
 * @date 01/01/2026
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type t1 = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type t2 = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // on verifier que les deux opérandes sont soit int soit float (en deca ,pas de string)
        if (!compiler.environmentType.castCompatible(t1,t2)) {
            throw new ContextualError(
                "Opérandes arithmétiques sont de types  invalides: " + t1 + " et " + t2,
                this.getLocation());
        }

        if( t1.isFloat() && t2.isInt()){
            setRightOperand(new ConvFloat(getRightOperand()));
            this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
            setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        }
        if( t2.isFloat() && t1.isInt()){
            setLeftOperand(new ConvFloat(getLeftOperand()));
            this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
            setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        }
        
        if (t1.isFloat() || t2.isFloat()) {
            setType(compiler.environmentType.FLOAT);
        }else{
            setType(compiler.environmentType.INT);
        }
        
        return getType();
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {

        getLeftOperand().codeGenExpr(compiler, register);

        //  Évaluation de l'opérande droit
        boolean hasRegister = compiler.getRegisterManager().registreLibre();

        if (hasRegister) {
            // SI il reste des registres disponibles
            GPRegister rRight = compiler.getRegisterManager().prendreRegistre();

            getRightOperand().codeGenExpr(compiler, rRight);

            
            // IMA: OP Source, Dest 
            compiler.addInstruction(getInstruction(rRight, register));

            // Libération du registre temporaire
            compiler.getRegisterManager().libererRegistre();
        } else {
            // SINON Plus de registres (Spill)
            
            //  Sauvegarder le résultat gauche sur la pile
            compiler.addInstruction(new PUSH(register));
            compiler.getRegisterManager().empiler(); // Pour TSTO

            //  Calculer l'opérande droit dans le même registre 'register' 
            getRightOperand().codeGenExpr(compiler, register);

            //  Charger le résultat gauche (pile) dans R0 
            compiler.addInstruction(new LOAD(register, Register.R0)); 
            compiler.addInstruction(new POP(register));              
            compiler.getRegisterManager().depiler();

            // register = register OP R0
            compiler.addInstruction(getInstruction(Register.R0, register));
        }

        // //  Gestion des erreurs d'exécution (Débordement / Division par zéro)
        // if (!compiler.getCompilerOptions().getNoCheck()) {
        //     if (this.getType().isFloat()) {
        //         // Débordement flottant ou division par 0.0
        //         compiler.addInstruction(new BOV(new Label("overflow_error")));
        //     } else if (this instanceof AbstractOpExactCmp) {
        //         // Pas de check pour les comparaisons
        //     } else if (this.getInstruction(register, register).isDivOrMod()) {
        //         // Pour DIV/REM entiers, IMA lève OV si division par 0
        //         compiler.addInstruction(new BOV(new Label("overflow_error")));
        //     }
        // }
    }

    // Méthode abstraite que chaque sous-classe (Plus, Minus...) devra implémenter
    // pour retourner l'instruction IMA correspondante (ADD, SUB...).
    protected abstract Instruction getInstruction(GPRegister op1, GPRegister op2);

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        // Calcule l'expression dans R1 et affiche
        codeGenExpr(compiler, Register.R1);
        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else {
            compiler.addInstruction(new WFLOAT());
        }
    }
}
