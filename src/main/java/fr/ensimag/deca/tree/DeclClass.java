package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.ima.pseudocode.Label;


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
    private final ListDeclMethod methods;
    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass) {
        this.name = name;
        this.superClass = superClass;
        this.fields = fields;
        this.methods = methods;
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
        methods.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {

        Symbol name = this.name.getName();
        Symbol superName = this.superClass.getName();

        // on verifie la condition : est ce la super class existe ?
        ClassDefinition superDef = (ClassDefinition) compiler.environmentType.defOfType(superName);
        if(superDef == null || !superDef.isClass()){  // on verifie i la cuper class existe deja ou non
        // et aussi , dans le cas ou elle existe mais pas une classe
        throw new  RuntimeException("Super-class n'existe pas");
        }

        ClassType classType=new ClassType( name,  this.getLocation(),  superDef);
        ClassDefinition classDef =new ClassDefinition( classType, this.getLocation(),  superDef);
        compiler.environmentType.declareClass(name, classDef);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler) throws ContextualError {
        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(name.getName());

        // Vérification des champs (Règle 2.5) et méthodes (Règle 2.7)
        fields.verifyListDeclField(compiler, superClass.getName(), name.getName());
        methods.verifyListDeclMethod(compiler, superClass.getName());

        // Mise à jour des compteurs (numberOfFields, numberOfMethods) via les définitions décorées [5]
    }

//    @Override
//    protected void verifyClassMembers(DecacCompiler compiler)
//            throws ContextualError {
//        throw new UnsupportedOperationException("not yet implemented");
//    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        ClassDefinition classDef = (ClassDefinition) compiler.environmentType.defOfType(name.getName());

        // Vérification des corps des champs (initialisations) et des méthodes
        fields.verifyListDeclFieldBody(compiler, compiler.environmentType, classDef);
        methods.verifyListDeclMethodBody(compiler, compiler.environmentType, classDef);
    }

//    @Override
//    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
//        throw new UnsupportedOperationException("not yet implemented");
//    }



    @Override
    protected void codeGenDeclClass(DecacCompiler compiler) {
        // 1. Génération de la vTable (Passe 1 de l'étape C)
        // LOAD #null / LEA super_vtable, R0 ... STORE ... [9]

        // 2. Génération du sous-programme d'initialisation init.NomClasse [11]
        compiler.addLabel(new Label("init." + name.getName()));
        // Code pour init (Test TSTO, initialisation des champs hérités puis propres) [12]

        // 3. Génération du code des méthodes [13]
        methods.codeGenListDeclMethod(compiler);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        superClass.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        superClass.iter(f);
        fields.iter(f);
        methods.iter(f);
    }

}
