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
     * PASSE 1 : Génération de la VTable
     */
    @Override
    protected void codeGenDeclClass(DecacCompiler compiler) {
        ClassDefinition classDef = (ClassDefinition) this.name.getDefinition();
        ClassDefinition superClassDef = (ClassDefinition) this.superClass.getDefinition();
        String className = this.name.getName().getName();

        compiler.addComment("--------------------------------------------------");
        compiler.addComment("Classe " + className);
        compiler.addComment("--------------------------------------------------");

        // 1. Allouer l'espace pour la VTable dans la zone Statique via MMU
        // Taille = 1 (ptr super) + Nombre total de méthodes
        int vTableSize = 1 + classDef.getNumberOfMethods();
        RegisterOffset vTableAddr = compiler.getMMU().allocGlobal(vTableSize);

        // Stocker l'adresse de la VTable dans la définition (champ 'operand' hérité de Definition)
        classDef.setOperand(vTableAddr);

        compiler.addComment("Table des méthodes de " + className + " à l'adresse " + vTableAddr);

        // 2. Gestion du pointeur Super-Classe (Index 0)
        RegisterOffset superVTableAddr = null; // Adresse VTable du père

        if (superClassDef == null || "Object".equals(superClassDef.getType().getName().getName())) {
            // Si c'est Object (ou racine), pas de super VTable - stocker 0
            compiler.addInstruction(new LOAD(0, Register.R0));
        } else {
            // Récupérer l'adresse VTable du père (stockée dans son operand)
            superVTableAddr = (RegisterOffset) superClassDef.getOperand();
            // Charger l'adresse effective (LEA pour obtenir l'adresse GB, pas la valeur pointée)
            compiler.addInstruction(new LEA(superVTableAddr, Register.R0));
        }
        // Écrire le pointeur super à l'index 0
        compiler.addInstruction(new STORE(Register.R0, vTableAddr));


        // 3. Copier les méthodes héritées (Si classe fille)
        // On copie les entrées de la VTable du père vers la VTable du fils
        if (superVTableAddr != null) {
            int nbSuperMethods = superClassDef.getNumberOfMethods();
            for (int i = 1; i <= nbSuperMethods; i++) {
                // Lire adresse méthode du père
                RegisterOffset slotPere = new RegisterOffset(superVTableAddr.getOffset() + i, Register.GB);
                compiler.addInstruction(new LOAD(slotPere, Register.R0));

                // Ecrire dans slot fils
                RegisterOffset slotFils = new RegisterOffset(vTableAddr.getOffset() + i, Register.GB);
                compiler.addInstruction(new STORE(Register.R0, slotFils));
            }
        }

        // 4. Installer les méthodes de la classe courante (Nouveautés ou Overrides)
        // On parcourt la liste AST des méthodes déclarées ici
        for (AbstractDeclMethod absMethod : methodes.getList()) {
            DeclMethod method = (DeclMethod) absMethod;
            // Récupérer l'index calculé lors de la passe 2
            int methodIndex = method.getMethodName().getMethodDefinition().getIndex();
            Label methodLabel = method.getMethodName().getMethodDefinition().getLabel();

            // Charger l'adresse du code (LOAD #Label, R0)
            compiler.addInstruction(new LOAD(new LabelOperand(methodLabel), Register.R0));

            // Stocker dans la VTable à la bonne position
            RegisterOffset slot = new RegisterOffset(vTableAddr.getOffset() + methodIndex, Register.GB);
            compiler.addInstruction(new STORE(Register.R0, slot));
        }
    }

    /**
     * PASSE 2 : Initialisation des champs
     * Appelée par Program.codeGenProgram -> ListDeclClass.codeGenListInit
     */
    public void codeGenInit(DecacCompiler compiler) {
        String className = this.name.getName().getName();
        ClassDefinition classDef = (ClassDefinition) this.name.getDefinition();
        ClassDefinition superClassDef = (ClassDefinition) this.superClass.getDefinition();

        compiler.addLabel(new Label("init." + className));
        compiler.addComment("Initialisation des champs de " + className);

        // --- Préambule ---
        // TSTO : On a besoin de empiler R1 et de faire un appel (BSR)
        // Pas de calcul complexe ici, on peut mettre une petite valeur ou utiliser MMU
        // compiler.addInstruction(new TSTO(3));
        // compiler.addInstruction(new BOV(new Label("stack_overflow")));

        compiler.addInstruction(new PUSH(Register.R1)); // Sauvegarde
        compiler.getMMU().notifyPush(1);

        // --- 1. Initialisation Héritée ---
        // L'objet courant est passé dans -2(LB) (convention implicite d'appel méthode/init)
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1)); // this -> R1

        // Si on n'est pas Object, on appelle init.Super
        if (superClassDef != null && !"Object".equals(superClassDef.getType().getName().getName())) {
            // Empiler 'this' pour l'appel au parent
            compiler.addInstruction(new PUSH(Register.R1));
            compiler.addInstruction(new BSR(new Label("init." + superClassDef.getType().getName().getName())));
            compiler.addInstruction(new SUBSP(1)); // Nettoyage param
        }

        // --- 2. Initialisation Champs Propres ---
        // Délégation à la liste des champs
        fields.codeGenListDeclField(compiler);

        // --- Fin ---
        compiler.addInstruction(new POP(Register.R1));
        compiler.getMMU().notifyPop(1);
        compiler.addInstruction(new RTS());
    }

    /**
     * PASSE 3 : Génération du code des méthodes
     */
    public void codeGenMethods(DecacCompiler compiler) {
        String className = this.name.getName().getName();
        compiler.addComment("--------------------------------------------------");
        compiler.addComment("Méthodes de la classe " + className);
        compiler.addComment("--------------------------------------------------");
        
        // Déléguer à la liste des méthodes
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
