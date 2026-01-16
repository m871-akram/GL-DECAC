package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

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
    private final ListDeclField fields;
    private final ListDeclMethod methodes;

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass, ListDeclField fields, ListDeclMethod methodes) {
        Validate.notNull(name);
        Validate.notNull(superClass);
        Validate.notNull(fields);
        Validate.notNull(methodes);
        this.name = name;
        this.superClass = superClass;
        this.fields = fields;
        this.methodes = methodes;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        s.print(" extends ");
        superClass.decompile(s);
        s.println(" {");
        
        s.indent();
        
        fields.decompile(s);
        methodes.decompile(s);
        
        s.unindent();
        s.print("}");

    }



    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        
        Symbol name = this.name.getName();
        Symbol superName = this.superClass.getName();
        
        TypeDefinition superTypeDef = compiler.environmentType.defOfType(superName);
     
        //on verifie si la super classe existe ou non
        if (superTypeDef == null) {
            // La super classe n'existe pas
            throw new ContextualError(
                "La super-classe :" + superName.getName() + "n'existe pas",
                this.getLocation()
            );
        }
        
        //on verifie  que c'est une classe
        if (!superTypeDef.isClass()) {
            throw new ContextualError(
                superName.getName() + "n'est pas une classe", this.getLocation()
            );
        }
        
        //caster en class definition
        ClassDefinition superClassDef = (ClassDefinition) superTypeDef;

        ClassType classType = new ClassType(
            name, this.getLocation(),
            superClassDef
        );
            
        
        ClassDefinition classDef = classType.getDefinition();
    
        compiler.environmentType.declareClass(name, classDef);
    
        Logger.getLogger(DeclClass.class).debug("Classe :" + name.getName() + "ajoutée, super = " + superName.getName() + "'");
    }



    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        Symbol className = this.name.getName();
        ClassDefinition currentClassDef = (ClassDefinition) compiler.environmentType.defOfType(className);
        
        // environnement de la super classe
        Symbol superName = this.superClass.getName();
        ClassDefinition superClassDef = (ClassDefinition)
            compiler.environmentType.defOfType(superName);
        
        EnvironmentExp superClassEnv = null;
        if (superClassDef != null) {
            superClassEnv = superClassDef.getMembers();
        }
        
        // on vérifier les champs
        fields.verifyDeclFieldPrototype(compiler, superClassEnv, currentClassDef,currentClassDef.getMembers());
        // on Verifier les méthodes
        methodes.verifyDeclMethodPrototype(compiler, superClassEnv, currentClassDef,currentClassDef.getMembers());
        name.setDefinition(currentClassDef);
        name.setType(currentClassDef.getType());

        superClass.setDefinition(superClassDef);
        superClass.setType(superClassDef.getType());
    }
    
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        ClassDefinition currentClassDef = this.name.getClassDefinition();
        EnvironmentExp classEnv = currentClassDef.getMembers();
        fields.verifyDeclFieldPrototype2(compiler, classEnv, currentClassDef);
        methodes.verifyDeclMethodPrototype2(compiler, classEnv, currentClassDef);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, true);
        superClass.prettyPrint(s, prefix, true);
        fields.prettyPrint(s, prefix, true);
        methodes.prettyPrint(s, prefix, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        superClass.iter(f);
        fields.iterChildren(f);
        methodes.iterChildren(f);
    }

    @Override
    protected void codeGenDeclClass(DecacCompiler compiler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenDeclClass'");
    }

}
