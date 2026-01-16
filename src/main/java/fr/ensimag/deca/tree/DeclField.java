package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;


/**
 * Declaration of a Field 
 * @author G51
 * @date 15/01/2026
 */

public class DeclField extends AbstractDeclField{
    private final Visibility visibility;
    private final AbstractIdentifier type;
    private final AbstractIdentifier name;
    private final AbstractInitialization initialization;

    public DeclField(Visibility visibility, AbstractIdentifier type, 
                    AbstractIdentifier name, AbstractInitialization initialization) {
        this.visibility = visibility;
        this.type = type;
        this.name = name;
        this.initialization = initialization;
    }
    



    @Override
    public void verifyDeclField(DecacCompiler compiler, 
                               EnvironmentExp superClassEnv,
                               EnvironmentExp localEnv, 
                               ClassDefinition currentClassDef) throws ContextualError {
   
        // on vérifier le type du champ
        Type fieldType = this.type.verifyType(compiler);
        if (fieldType.isVoid()) {
            throw new ContextualError("Un champ ne peut pas être de type void", getLocation());
        }
        
        // on vérifier si le champ existe déjà dans la super classe
        Symbol fieldName = this.name.getName();
        if (superClassEnv != null && superClassEnv.get(fieldName) != null) {
            // on vérifier que c'est bien un champ (et pas une méthode par exemple)
            ExpDefinition def = superClassEnv.get(fieldName);
            if (!def.isField()) {
                throw new ContextualError(
                    fieldName.getName() + ":existe déjà dans la super classe mais n'est pas un champ",
                    getLocation()
                );
            }
        }
        
        
        
        FieldDefinition fieldDef = new FieldDefinition(
            fieldType,
            getLocation(),
            this.visibility, currentClassDef, currentClassDef.getNumberOfFields()
        );
    
        try {
            localEnv.declare(fieldName, fieldDef);
        } catch (DoubleDefException e) {
            throw new ContextualError(e.getMessage(), getLocation());
        }
        name.setDefinition(fieldDef);
        name.setType(fieldType);
    }


        

    @Override
    public void decompile(IndentPrintStream s) {
        if (visibility == Visibility.PROTECTED) {//pas besoin si ce n'est pas protected 
            s.print("protected ");
        }
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }
    

    
    public Visibility getVisibility() { 
        return visibility; 
    }
    
    public AbstractIdentifier getType() { 
        return type; 
    }
    
    public AbstractIdentifier getName() { 
        return name; 
    }
    
    public AbstractInitialization getInitialization() { 
        return initialization; 
    }

    @Override
    protected void codeGenDeclVar(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenDeclVar'");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        initialization.iter(f);
    }




    @Override
    protected void verifyDeclField2(DecacCompiler compiler, ClassDefinition currentClassDef, EnvironmentExp localEnv)
            throws ContextualError {
        Type fieldType = type.getType(); 
        initialization.verifyInitialization(compiler, fieldType, localEnv, currentClassDef);
        
    }
}