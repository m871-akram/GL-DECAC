package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;


import fr.ensimag.deca.context.*;


public class MethodBody extends AbstractMethodBody {

    private final ListDeclVar locals;
    private final ListInst insts;

    public MethodBody(ListDeclVar locals, ListInst insts) {
        this.locals = locals;
        this.insts = insts;
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
                                    ClassDefinition currentClass, Type returnType) throws ContextualError {
        // Vérification des variables locales
        locals.verifyListDeclVariable(compiler, localEnv, currentClass);

        // Vérification des instructions
        insts.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        // Génération des variables locales (allocation pile)
        locals.codeGenListDeclVar(compiler);

        // On notifie la MMU de la taille des locales pour le TSTO
        compiler.getMMU().notifyLocalBlockAllocation(locals.size());

        // Génération des instructions
        insts.codeGenListInst(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        locals.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        locals.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        locals.iter(f);
        insts.iter(f);
    }
}