package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Déclaration de variable ( int x = 3;)
 * @author gl51
 * @date 01/01/2026
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass) 
        throws ContextualError {
        Symbol name = this.varName.getName();

        // pour la partie c 
        VariableDefinition varDef = new VariableDefinition(this.type.verifyType(compiler), getLocation());
        
        // pas de doublon
        if (localEnv.get(name) != null) {
            throw new ContextualError("Variable"+ name +  "déjà déclarée", getLocation());
        }
        
        Type type = this.type.verifyType(compiler);
        if (type == compiler.environmentType.VOID) {
            throw new ContextualError("Type void interdit pour les variables", getLocation());
        }
        initialization.verifyInitialization(compiler, type, localEnv, currentClass);
        
        // Declarer nouveau variable
        try {
            localEnv.declare(name, varDef);
        } catch (DoubleDefException e) {
            throw new ContextualError("nouveau variable est un doublon", getLocation());
        }

        // lier la def a l AST 
        this.varName.setDefinition(varDef);
    }

    @Override
    protected void codeGenDeclVar(DecacCompiler compiler) {
        // 1. Allocation mémoire via MMU (Pile locale)
        // La MMU renvoie un RegisterOffset (ex: 3(LB))
        RegisterOffset addr = compiler.getMMU().allocLocal();

        // 2. Stockage de l'adresse dans la définition pour usage ultérieur (Identifier)
        this.varName.getVariableDefinition().setOperand(addr);

        // 3. Génération du code d'initialisation
        // On passe l'adresse directement à l'initialisation pour qu'elle STORE le résultat
        this.initialization.codeGenInit(compiler, addr, this.varName.getVariableDefinition().getType());
    }


    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        varName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }
    

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
