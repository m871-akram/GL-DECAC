package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Deca Identifier
 *
 * @author gl51
 * @date 01/01/2026
 */
public class Identifier extends AbstractIdentifier {

    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try { return (ClassDefinition) definition; }
        catch (ClassCastException e) {
            throw new DecacInternalError("Identifier " + getName() + " is not a class identifier");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        ExpDefinition def = localEnv.get(this.name);

        if (def == null) {
            throw new ContextualError(
                "Expresion inconnu : " + this.name.getName(),
                this.getLocation()
            );
        }

        // lier la definition à l'AST
        this.setDefinition(def);

        Type type = def.getType();
        this.setType(type);
        this.setDefinition(def);
        return type;
    }

    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        TypeDefinition def = compiler.environmentType.defOfType(this.name);

        if (def == null) {
            throw new ContextualError(
                "Type inconnu : " + this.name.getName(),
                this.getLocation()
            );
        }

        this.setDefinition(def);

        Type type = def.getType();
        this.setType(type);
        return type;
    }


    private Definition definition;


    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        Definition def = getDefinition();

        if (def.isField()) {
            // Accès Champ (implicite sur 'this' (-2(LB)))
            // 1. Charger 'this' dans le registre
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), register));

            // 2. Charger le champ depuis l'objet (Offset par rapport au début de l'objet)
            FieldDefinition fieldDef = (FieldDefinition) def;
            // ATTENTION: index champ commence à 1 (après VTable)
            compiler.addInstruction(new LOAD(new RegisterOffset(fieldDef.getIndex(), register), register));

        } else {
            // Accès Variable Locale / Paramètre / Globale
            // L'opérande (adresse) a été stockée dans la définition par DeclVar ou DeclParam
            compiler.addInstruction(new LOAD(def.getOperand(), register));
        }
    }

    /**
     * Stocke la valeur d'un registre dans l'identifiant (Écriture)
     * Utile pour Assign (gauche = source)
     */
    protected void codeGenStore(DecacCompiler compiler, GPRegister source) {
        Definition def = getDefinition();

        if (def.isField()) {
            // Accès Champ (implicite sur 'this')
            // Il faut un registre temporaire pour calculer l'adresse de 'this'
            // On utilise R1 (jamais alloué par RegisterManager, toujours dispo comme scratch)
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));

            FieldDefinition fieldDef = (FieldDefinition) def;
            compiler.addInstruction(new STORE(source, new RegisterOffset(fieldDef.getIndex(), Register.R1)));

        } else {
            // Variable standard
            compiler.addInstruction(new STORE(source, def.getOperand()));
        }
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        // On charge la valeur dans R1 (convention d'affichage)
        codeGenExpr(compiler, Register.R1);

        if (getDefinition().getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getDefinition().getType().isFloat()) {
            compiler.addInstruction(new WFLOAT());
        }
    }


    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }

}
