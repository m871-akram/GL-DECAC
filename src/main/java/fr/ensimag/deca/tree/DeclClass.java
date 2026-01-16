package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.log4j.Logger;

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
    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass) {
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
        s.println("}");
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

    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
         ClassDefinition classDef = (ClassDefinition) this.name.getDefinition();
         methodes.verifyListMethodBody(compiler, classDef);
    }

//    @Override
//    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
//        throw new UnsupportedOperationException("not yet implemented");
//    }



//    @Override
//    protected void codeGenDeclClass(DecacCompiler compiler) {
//        // 1. Génération de la vTable (Passe 1 de l'étape C)
//        // LOAD #null / LEA super_vtable, R0 ... STORE ... [9]
//
//        // 2. Génération du sous-programme d'initialisation init.NomClasse [11]
//        compiler.addLabel(new Label("init." + name.getName()));
//        // Code pour init (Test TSTO, initialisation des champs hérités puis propres) [12]
//
//        // 3. Génération du code des méthodes [13]
//        methods.codeGenListDeclMethod(compiler);
//    }

    /**
     * Génère la vTable (Passe 1)
     */
    @Override
    protected void codeGenDeclClass(DecacCompiler compiler) {
        // Récupération de la définition via le nom
        ClassDefinition classDef = (ClassDefinition) this.name.getDefinition();
        String className = this.name.getName().getName();

        compiler.addComment("===== Table des méthodes de " + className + " =====");

        // 1. Calcul Adresse vTable (dans GB)
        int addrVTable = compiler.getRegisterManager().getNbGlobales();

        // IMPORTANT: Stocker l'adresse pour les NEW plus tard
        // classDef.setVTableAddr(addrVTable); // Décommenter quand ClassDefinition aura setVTableAddr

        compiler.getRegisterManager().incrNbGlobales(); // +1 pour le Pointeur Super

        // 2. Pointeur Super-Classe
        ClassDefinition superClassDef = classDef.getSuperClass();

        if (superClassDef == null || "Object".equals(superClassDef.getType().getName().getName())) {
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        } else {
            // LEA addrSuper, R0
            // compiler.addInstruction(new LEA(new RegisterOffset(superClassDef.getVTableAddr(), Register.GB), Register.R0));
            // Placeholder temporaire :
            compiler.addInstruction(new LEA(new RegisterOffset(1, Register.GB), Register.R0));
        }

        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(addrVTable, Register.GB)));

        // 3. Méthodes (Construction vTable)
        int nbMethods = classDef.getNumberOfMethods();
        for (int i = 1; i <= nbMethods; i++) {
            // MethodDefinition methodDef = classDef.getMethodByIndex(i);
            MethodDefinition methodDef = null; // Placeholder

            if (methodDef == null) continue;

            String methodLabel = "code." + methodDef.getLabel().getName();

            compiler.getRegisterManager().incrNbGlobales();
            int offset = addrVTable + i;

            compiler.addInstruction(new LOAD(new LabelOperand(new Label(methodLabel)), Register.R0));
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(offset, Register.GB)));
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
        fields.iter(f);
        methodes.iter(f);
    }

}
