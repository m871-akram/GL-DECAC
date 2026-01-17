package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclField extends TreeList<AbstractDeclField>{
    public void verifyDeclFieldPrototype(DecacCompiler compiler, EnvironmentExp superClassEnv, ClassDefinition currentClassDef, EnvironmentExp localEnv) throws ContextualError{
        for (AbstractDeclField method : getList()) {
            method.verifyDeclField(compiler, superClassEnv,localEnv,currentClassDef);
        }
    }
    public void verifyListDeclFieldInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClassDef) throws ContextualError{
        for (AbstractDeclField method : getList()) {
            method.verifyDeclFieldInit(compiler, currentClassDef, localEnv);
        }
    }
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField field : getList()) {
                field.decompile(s);
                s.println();
        }    
    }
}