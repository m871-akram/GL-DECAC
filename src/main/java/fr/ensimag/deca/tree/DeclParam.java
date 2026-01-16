package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

public class DeclParam extends AbstractDeclParam {
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;
    

    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        this.type = type;
        this.name = name;
    }

    
    @Override
    protected Type verifyDeclParam(DecacCompiler compiler) throws ContextualError {
        
        // on vérifie que le type existe
        Symbol typeName = type.getName();
        TypeDefinition typeDef = compiler.environmentType.defOfType(typeName);
        
        if (typeDef == null) {
            throw new ContextualError(
                "Type :" + typeName.getName() + "est inconnu pour le paramètre", getLocation()
            );
        }
    
        Type paramType = typeDef.getType();
        
        // Un paramètre peut pas etre void
        if (paramType.isVoid()) {
            throw new ContextualError("Un paramètre ne peut pas être de type void",
                getLocation()
            );
        }
        this.type.setDefinition(typeDef);
        this.type.setType(paramType);
        return paramType;
    }
    @Override
    protected void verifyDeclParam2(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        VariableDefinition paramDef = new VariableDefinition(this.type.getType(), getLocation());
        this.name.setDefinition(paramDef);  
        this.name.setType(this.type.getType());
        try {
            localEnv.declare(name.getName(), paramDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(
                e.getMessage(),getLocation()
            );
        }
    }


    @Override
    protected void codeGenDeclVar(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenDeclVar'");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Unimplemented method 'decompile'");
        type.decompile(s);
        s.print(" ");
        name.decompile(s); 
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, true);
        name.prettyPrint(s, prefix, false); 
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
    }

}
