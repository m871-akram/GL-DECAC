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

    private ListDeclVar locals;
    private ListInst insts;
    public MethodBody(ListDeclVar locals, ListInst insts) {
        this.locals = locals;
        this.insts = insts;
    }



    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        // 1. Notifier la MMU du nombre de variables locales
        int nbLocales = locals.size();
        compiler.getMMU().notifyLocalBlockAllocation(nbLocales);
        
        // 2. Génération des variables locales (allocation pile) - sans générer d'instruction
        locals.codeGenListDeclVar(compiler);

        // 3. Génération des instructions du corps (ceci va appeler notifyPush/notifyPop)
        insts.codeGenListInst(compiler);
    }
    
    /**
     * Retourne le nombre de variables locales (pour le calcul de ADDSP)
     */
    public int getLocalVarsCount() {
        return locals.size();
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

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
                                    ClassDefinition currentClass, Type returnType) throws ContextualError{
        locals.verifyListDeclVariable(compiler, localEnv, currentClass);
        insts.verifyListInst(compiler, localEnv, currentClass, returnType);
    };



}