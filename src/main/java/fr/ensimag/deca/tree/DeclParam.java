package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

public class DeclParam extends AbstractDeclParam {

    private final AbstractIdentifier type;
    private final AbstractIdentifier name;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        Validate.notNull(type);
        Validate.notNull(name);
        this.type = type;
        this.name = name;
    }

    public AbstractIdentifier getName() { return name; }

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


//    @Override
//    protected Type verifyDeclParam(DecacCompiler compiler) throws ContextualError {
//        // Vérification du type (Passe 2)
//        Type paramType = this.type.verifyType(compiler);
//
//        if (paramType.isVoid()) {
//            throw new ContextualError("Un paramètre ne peut pas être void", getLocation());
//        }
//        return paramType;
//    }

    @Override
    protected void verifyDeclParamBody(DecacCompiler compiler, EnvironmentExp localEnv) throws ContextualError {
        // Déclaration dans l'environnement local (Passe 3)
        ParamDefinition paramDef = new ParamDefinition(this.type.getType(), getLocation());

        try {
            localEnv.declare(name.getName(), paramDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("Paramètre " + name.getName() + " déjà déclaré", getLocation());
        }

        this.name.setDefinition(paramDef);
    }

    @Override
    protected void codeGenDeclParam(DecacCompiler compiler, int index) {
        // Calcul de l'adresse relative à LB
        // index = 0 -> -3(LB)
        // index = 1 -> -4(LB)
        int offset = -3 - index;

        // On lie le symbole à cette adresse physique
        // ATTENTION: getParamDefinition() doit être défini dans Identifier ou on cast
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
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
    }
}