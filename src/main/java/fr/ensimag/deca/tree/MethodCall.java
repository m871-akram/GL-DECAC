package fr.ensimag.deca.tree;
import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;

public class MethodCall extends AbstractExpr {
    private final ListExpr args;
    private final AbstractExpr object;
    private final AbstractIdentifier methode;
    public MethodCall(AbstractExpr object, AbstractIdentifier methode, ListExpr args){
        Validate.notNull(object);
        Validate.notNull(methode);
        Validate.notNull(args);
        this.object=object;
        this.methode=methode;
        this.args=args;
    }
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        Type callType = verifyExpr(compiler, localEnv, currentClass);
        if (returnType != null && !callType.sameType(returnType)) {
            throw new ContextualError(
                "Type de retour de la methode incorrect : attendu " 
                + returnType + ", trouve " + callType,
                getLocation()
            );
        }
    }
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type objectType = object.verifyExpr(compiler, localEnv, currentClass);

        if (!objectType.isClass()) {
            throw new ContextualError(
                "Appel de methode sur un type non-classe",
                getLocation()
            );
        }
        ClassDefinition classDef = objectType.asClassType(
            "Type non-classe dans un appel de methode",
            getLocation()
        ).getDefinition();

        var def = classDef.getMembers().get(methode.getName());
        if (def == null) {
            throw new ContextualError(
                "Méthode '" + methode.getName().getName() + "' inexistante",
                getLocation()
            );
        }
    
        if (!def.isMethod()) {
            throw new ContextualError(
                "'" + methode.getName().getName() + "' n'est pas une méthode",
                getLocation()
            );
        }
    
        var methodDef = def.asMethodDefinition(
            "Ce n'est pas une méthode",
            getLocation()
        );
        args.verifyRValue(compiler, localEnv, currentClass, methodDef.getSignature(), getLocation());
    
        methode.setDefinition(methodDef);
    
        Type returnType = methodDef.getType();
        setType(returnType);
        return returnType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenExpr'");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if(!object.isImplicit()){
            object.decompile(s);
            s.print(".");
        }
        methode.decompile(s);
        s.print("(");
        args.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix,true);
        methode.prettyPrint(s, prefix,true);
        args.prettyPrint(s, prefix,false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iterChildren(f);
        methode.iterChildren(f);
        args.iterChildren(f);
    }
    
}