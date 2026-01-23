package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.log4j.Logger;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

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

//        // On initialise le nombre de champs et méthodes avec ceux de la super-classe
        currentClassDef.setNumberOfFields(superClassDef.getNumberOfFields());
        currentClassDef.setNumberOfMethods(superClassDef.getNumberOfMethods());

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
        fields.verifyListDeclFieldInit(compiler, classEnv, currentClassDef);
        methodes.verifyDeclMethodContent(compiler, classEnv, currentClassDef);
    }

    /**
     * gen vtable
     */
    @Override
    protected void codeGenDeclClass(DecacCompiler compiler) {
        ClassDefinition classDef = (ClassDefinition) this.name.getDefinition();
        ClassDefinition superClassDef = (ClassDefinition) this.superClass.getDefinition();
        String className = this.name.getName().getName();

        compiler.addComment("Classe " + className);

        // alloue espace vtable
        int vTableSize = 1 + classDef.getNumberOfMethods();
        RegisterOffset vTableAddr = compiler.getMMU().allocGlobal(vTableSize);

        // stocke adresse vtable
        classDef.setOperand(vTableAddr);

        compiler.addComment("Table des méthodes de " + className + " à l'adresse " + vTableAddr);

        // gestion pointeur super classe
        RegisterOffset superVTableAddr = null;

        if (superClassDef == null || "Object".equals(superClassDef.getType().getName().getName())) {
            // pas de super vtable
            compiler.addInstruction(new LOAD(0, Register.R0));
        } else {
            // recup adresse vtable du pere
            superVTableAddr = (RegisterOffset) superClassDef.getOperand();
            compiler.addInstruction(new LEA(superVTableAddr, Register.R0));
        }
        // ecrit pointeur super
        compiler.addInstruction(new STORE(Register.R0, vTableAddr));


        // copie methodes heritees
        if (superVTableAddr != null) {
            int nbSuperMethods = superClassDef.getNumberOfMethods();
            for (int i = 1; i <= nbSuperMethods; i++) {
                // lit methode du pere
                RegisterOffset slotPere = new RegisterOffset(superVTableAddr.getOffset() + i, Register.GB);
                compiler.addInstruction(new LOAD(slotPere, Register.R0));

                // ecrit dans fils
                RegisterOffset slotFils = new RegisterOffset(vTableAddr.getOffset() + i, Register.GB);
                compiler.addInstruction(new STORE(Register.R0, slotFils));
            }
        }

        // installe methodes courantes
        for (AbstractDeclMethod absMethod : methodes.getList()) {
            DeclMethod method = (DeclMethod) absMethod;
            // recup index
            int methodIndex = method.getName().getMethodDefinition().getIndex();
            Label methodLabel = method.getName().getMethodDefinition().getLabel();

            // charge adresse code
            compiler.addInstruction(new LOAD(new LabelOperand(methodLabel), Register.R0));

            // stocke dans vtable
            RegisterOffset slot = new RegisterOffset(vTableAddr.getOffset() + methodIndex, Register.GB);
            compiler.addInstruction(new STORE(Register.R0, slot));
        }
    }

    /**
     * init champs
     */
    public void codeGenInit(DecacCompiler compiler) {
        String className = this.name.getName().getName();
        ClassDefinition classDef = (ClassDefinition) this.name.getDefinition();
        ClassDefinition superClassDef = (ClassDefinition) this.superClass.getDefinition();

        compiler.addLabel(new Label("init." + className));
        compiler.addComment("Initialisation des champs de " + className);

        // sauvegarde r1
        compiler.addInstruction(new PUSH(Register.R1));
        compiler.getMMU().notifyPush(1);

        // charge this
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));

        // init heritee
        if (superClassDef != null && !"Object".equals(superClassDef.getType().getName().getName())) {
            // empile this pour appel parent
            compiler.addInstruction(new PUSH(Register.R1));
            compiler.addInstruction(new BSR(new Label("init." + superClassDef.getType().getName().getName())));
            compiler.addInstruction(new SUBSP(1));
        }

        // init champs propres
        fields.codeGenListDeclField(compiler);

        // restauration
        compiler.addInstruction(new POP(Register.R1));
        compiler.getMMU().notifyPop(1);
        compiler.addInstruction(new RTS());
    }

    /**
     * gen code methodes
     */
    public void codeGenMethods(DecacCompiler compiler) {
        String className = this.name.getName().getName();
        compiler.addComment("Méthodes de la classe " + className);
        // delegation
        for (AbstractDeclMethod method : methodes.getList()) {
            method.codeGenDeclMethod(compiler);
        }
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        superClass.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methodes.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        superClass.iter(f);
        fields.iterChildren(f);
        methodes.iterChildren(f);
    }

}
