package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import java.io.PrintStream;

public class DeclParam extends AbstractDeclParam {
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;


    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        this.type = type;
        this.name = name;
    }

    public AbstractIdentifier getName() {
        return name;
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
    protected void verifyDeclParamEnv(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        ParamDefinition paramDef = new ParamDefinition(this.type.getType(), getLocation());
        this.name.setDefinition(paramDef);
        this.name.setType(this.type.getType());
        try {
            localEnv.declare(name.getName(), paramDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(
                    e.getMessage(), getLocation()
            );
        }
    }


    @Override
    protected void codeGenDeclParam(DecacCompiler compiler, int index) {
        // calc adresse relative lb
        int offset = -3 - index;

        // lie symbole a adresse physique
        ParamDefinition paramDef = (ParamDefinition) this.name.getDefinition();
        paramDef.setOperand(new RegisterOffset(offset, Register.LB));
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
