package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

public class DeclParam extends AbstractDeclParam {

    private final AbstractIdentifier type;
    private final AbstractIdentifier name;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        this.type = type;
        this.name = name;
    }


    @Override
    protected void verifyDeclParam(DecacCompiler compiler, EnvironmentExp localEnv,
                                ClassDefinition currentClass) throws ContextualError {

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

        Symbol paramName = name.getName();

        if (localEnv != null && localEnv.get(paramName) != null) {
            throw new ContextualError(
                "Paramètre:" + paramName.getName() + "est déjà déclaré",getLocation()
            );
        }

        ExpDefinition paramDef = new ExpDefinition(
            paramType,
            getLocation()
        );

        if (localEnv != null) {
            try {
                localEnv.declare(paramName, paramDef);
            } catch (DoubleDefException e) {
                throw new ContextualError(e.getMessage(), getLocation());
            }
        }
    }




    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("Unimplemented method 'decompile'");
        type.decompile(s);
        s.print(" ");
        paramName.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        paramName.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        paramName.iter(f);
    }

//    @Override
//    protected Type verifyParamMembers(DecacCompiler compiler) throws ContextualError {
//        // Règle (2.9) : Le type d'un paramètre ne peut pas être void [10]
//        Type t = type.verifyType(compiler);
//        if (t.isVoid()) {
//            throw new ContextualError("Un paramètre ne peut pas être de type void", type.getLocation());
//        }
//        paramName.setType(t);
//        return t;
//    }
//
//    @Override
//    protected void verifyParamBody(DecacCompiler compiler, EnvironmentExp localEnv)
//            throws ContextualError {
//        // Passe 3 : Déclarer le paramètre dans l'environnement local de la méthode [6]
//        ParamDefinition paramDef = new ParamDefinition(type.getType(), getLocation());
//        try {
//            localEnv.declare(paramName.getName(), paramDef);
//        } catch (EnvironmentExp.DoubleDefException e) {
//            throw new ContextualError("Paramètre " + paramName.getName() + " déjà défini", getLocation());
//        }
//        paramName.setDefinition(paramDef);
//
//        // Convention de liaison : Adressage à partir de -3(LB) [8, 9]
//        // (this est à -2(LB), les paramètres commencent à -3(LB))
//        int index = - (compiler.getParamCounter() + 3);
//        paramDef.setOperand(new RegisterOffset(index, Register.LB));
//        compiler.incrementParamCounter();
//    }
}



