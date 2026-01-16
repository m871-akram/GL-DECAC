package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.GPRegister;


/**
 * Integer literal
 *
 * @author G51
 * @date 16/01/2026
 */
public class Return extends AbstractInst {
    private final AbstractExpr value;
    
    public Return(AbstractExpr value) {
        this.value = value;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return");
        s.print(" ");
        value.decompile(s);
        s.print(";");
    }


    @Override
    protected void verifyInst(DecacCompiler compiler,  EnvironmentExp localEnv,
                            ClassDefinition currentClass, Type returnType) throws ContextualError {
        
        if (returnType.isVoid()) {
            throw new ContextualError(
                "return avec un void interdit", getLocation()
            );
        } 
        
        // on vérifier que la valeur est compatible
        Type valueType = value.verifyExpr(compiler, localEnv, currentClass);
        
        if (!compiler.environmentType.assignCompatible(returnType, valueType)) {
            throw new ContextualError(
                "erreur dans le type de return : expected " + returnType + ", y on a: " + valueType, getLocation()
            );
        }
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenInst'");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        value.prettyPrintChildren(s, prefix);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        value.iterChildren(f);
    }
}


