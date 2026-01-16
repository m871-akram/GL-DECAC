package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.tools.IndentPrintStream;

public abstract class AbstractSelectExpr extends AbstractLValue {
    private final AbstractExpr object;
    private final AbstractIdentifier item;
    public AbstractSelectExpr(AbstractExpr object, AbstractIdentifier item){
        Validate.notNull(object);
        Validate.notNull(item);
        this.object=object;
        this.item=item;
    }
    @Override
    public void decompile(IndentPrintStream s) {
        if(!getObject().isImplicit()){
            getObject().decompile(s);
            s.print(".");
        }
        getItem().decompile(s);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        getObject().prettyPrintChildren(s, prefix);
        getItem().prettyPrintChildren(s, prefix);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        getObject().iter(f);
        getItem().iter(f);
    }
    public AbstractIdentifier getItem() {
        return item;
    }
    public AbstractExpr getObject() {
        return object;
    }
}
