package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import fr.ensimag.deca.context.ClassDefinition;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl51
 * @date 01/01/2026
 */
public class DeclClass extends AbstractDeclClass {
    private final AbstractIdentifier name;
    private final AbstractIdentifier superClass;
    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass) {
        this.name = name;
        this.superClass = superClass;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class { ... A FAIRE ... }");
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        
        Symbol name = this.name.getName();
        Symbol superName = this.superClass.getName();

        // on verifie la condition : est ce la super class existe ?
        ClassDefinition superDef = (ClassDefinition) compiler.environmentType.defOfType(superName);
        if(superDef == null || !superDef.isClass()){  // on verifie i la cuper class existe deja ou non
        // et aussi , dans le cas ou elle existe mais pas une classe 
        throw new  RuntimeException("Super-class n'existe pas");
        }
        
        ClassType classType=new ClassType( name,  this.getLocation(),  superDef);
        ClassDefinition classDef =new ClassDefinition( classType, this.getLocation(),  superDef);
        compiler.environmentType.declareClass(name, classDef);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        throw new UnsupportedOperationException("Not yet supported");
    }

}
