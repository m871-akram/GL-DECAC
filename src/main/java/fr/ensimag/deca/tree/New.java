package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


public class New extends AbstractExpr {

    private final AbstractIdentifier className;

    public New(AbstractIdentifier className) {
        this.className = className;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        className.decompile(s);
        s.print("()");
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler,
        EnvironmentExp localEnv, ClassDefinition currentClass)
        throws ContextualError {
        // on vérifier que le nom est une classe qui exist
        TypeDefinition typeDef = compiler.environmentType.defOfType(className.getName());

        // on verifie si ce objet 'classs' existe
        if (typeDef == null) {
            throw new ContextualError(
                "la Classe :" + className.getName().getName() + ",est inconnue", getLocation()
            );
        }

        //et s'il existe , est ce une classe ,ou un autre type
        if (!typeDef.isClass()) {
            throw new ContextualError(
                className.getName().getName() + " : n'est pas une classe",
                getLocation()
            );
        }

        // si tout est bon , on retourn son type
        ClassDefinition classDef = (ClassDefinition) typeDef;
        Type classType = classDef.getType();

        setType(classType);
        return classType;
    }



    @Override
    protected void codeGenExpr(DecacCompiler compiler, fr.ensimag.ima.pseudocode.GPRegister dest) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
    }

//    @Override
//    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
//            throws ContextualError {
//        // Rule (3.42): Verify that the identifier represents a valid class type [2].
//        Type type = className.verifyType(compiler);
//
//        if (!type.isClass()) {
//            throw new ContextualError("The 'new' operator can only be applied to class types.",
//                    className.getLocation());
//        }
//
//        this.setType(type);
//        return type;
//    }
}

