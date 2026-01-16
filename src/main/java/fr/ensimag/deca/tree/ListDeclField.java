package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;

import fr.ensimag.deca.tools.IndentPrintStream;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.context.*;


public class ListDeclField extends TreeList<AbstractDeclField> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField f : getList()) {
            f.decompile(s);
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

}

