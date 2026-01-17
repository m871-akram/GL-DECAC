package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

public class DeclMethod extends AbstractDeclMethod {
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;
    private final ListDeclParam params;
    private final AbstractMethodBody body;
    
    
    
    public DeclMethod(AbstractIdentifier type, AbstractIdentifier name,
                    ListDeclParam params, AbstractMethodBody body) {
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(params);
        Validate.notNull(body);
        this.type = type;
        this.name = name;
        this.params = params;
        this.body=body;
    }
    
    public void verifyDeclMethodPrototype(DecacCompiler compiler,
                                         EnvironmentExp superClassEnv, ClassDefinition currentClassDef, EnvironmentExp localEnv) 
        throws ContextualError {
        ExpDefinition superDef = superClassEnv.get(name.getName());
        Signature signature = params.verifyListDeclParam(compiler);
        Type returnType = type.verifyType(compiler);
        if (superDef != null) {

            MethodDefinition superMethod = superDef.asMethodDefinition("Le nom existe dans la super-classe mais ce n'est pas une methode", getLocation());

            Signature sigSuper = superMethod.getSignature();

            if (signature.size() != sigSuper.size()) {
                throw new ContextualError(
                    "La signature de la methode redefinie doit avoir le meme nombre de parametres",
                    getLocation()
                );
            }

            for (int i = 0; i < signature.size(); i++) {
                if (!signature.paramNumber(i).sameType(sigSuper.paramNumber(i))) {
                    throw new ContextualError(
                        "Les types des parametres doivent correspondre à ceux de la methode heritee",
                        getLocation()
                    );
                }
            }
            
            Type typeSuper = superMethod.getType();

            if (!compiler.environmentType.subType(returnType,typeSuper)) {
                throw new ContextualError(
                    "Le type de retour de la methode redefinie doit etre un sous-type du type de la super-classe",
                    getLocation()
                );
            }
        }
        MethodDefinition methodDef = new MethodDefinition(returnType, getLocation(), signature, currentClassDef.incNumberOfMethods());
        try {
            localEnv.declare(name.getName(), methodDef);
        } catch (DoubleDefException e) {
            throw new ContextualError(e.getMessage(), getLocation());
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        
        name.decompile(s);
        
        s.print("(");
        params.decompile(s);
        s.print(")");
        

        body.decompile(s);
        
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, true);
        name.prettyPrint(s, prefix, true);
        params.prettyPrint(s, prefix, true);
        body.prettyPrint(s, prefix, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        params.iterChildren(f);
        body.iter(f);
    }

    @Override
    protected void verifyDeclMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp methodEnv = new EnvironmentExp(localEnv);
        params.verifyListDeclParamEnv(compiler,methodEnv);
        body.verifyMethodBody(compiler, methodEnv, currentClass, type.getType());
    }

    @Override
    protected void verifyDeclMethodContent(DecacCompiler compiler, ClassDefinition currentClassDef,
            EnvironmentExp localEnv) throws ContextualError {
        EnvironmentExp methodEnv = new EnvironmentExp(localEnv);

        this.params.verifyListDeclParamEnv(compiler, methodEnv);
        this.body.verifyMethodBody(compiler, methodEnv, currentClassDef, type.getType());
        MethodDefinition methodDef = (MethodDefinition) localEnv.get(name.getName()); // récupérée en passe 2
        this.name.setDefinition(methodDef);
        this.name.setType(type.getType());
    }

}