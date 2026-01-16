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
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField field : getList()) {
                field.decompile(s);
                s.println();
        }
    }

//    /**
//     * Pass 2: Verify field signatures and add them to the class environment [8, 9].
//     */
//    void verifyListDeclField(DecacCompiler compiler, Symbol superClass, Symbol nameClass)
//            throws ContextualError {
//        for (AbstractDeclField f : getList()) {
//            f.verifyFieldMembers(compiler, superClass, nameClass);
//        }
//    }
//
//    /**
//     * Pass 3: Verify field initializations within the class body [10, 11].
//     */
//    void verifyListDeclFieldBody(DecacCompiler compiler, EnvironmentType envTypes, ClassDefinition nameClass)
//            throws ContextualError {
//        for (AbstractDeclField f : getList()) {
//            f.verifyFieldBody(compiler, envTypes, nameClass);
//        }
//    }

    public void codeGenListDeclField(DecacCompiler compiler) {
        for (AbstractDeclField field : getList()) {
            field.codeGenInitField(compiler);
        }
    }

}

