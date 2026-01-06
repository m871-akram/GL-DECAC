/* la classe Passe 1 necssaire pour la partie B , 
dans cette classe , on verifie la structure des classes , (super classe , sous classes)
*/
package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractDeclClass;
import fr.ensimag.deca.tree.DeclClass;
import fr.ensimag.deca.tree.ListDeclClass;
import fr.ensimag.deca.tree.Program;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import net.bytebuddy.dynamic.scaffold.MethodGraph.Compiler;



public  class Passe1{
    // on initialise les environments
    private EnvironmentType envTypesPredef;
    private EnvironmentType envTypes;
    

    public EnvironmentType verifie(Program programe, DecacCompiler compiler){
        // on initialise le envTypePredef en Null 
        envTypesPredef = new EnvironmentType(compiler);
        initPredefnedTypes();

        envTypes = new EnvironmentType(compiler);

        //regle1.2: traitement des classes
        processClasses(programe.getClasses());

        return envTypes;
    }


    public void processClasses(ListDeclClass classes){
        //on initialise le currentenv
        EnvironmentType currentEnv = envTypes;

        // parcours sur tous les classes , 
        for(DeclClass declClass : classes.getList()){
            processUneClass(declClass , currentEnv);
        }
    }


    private void processUneClass(DeclClass declClass , EnvironmentType env){
        Symbol name = declClass.getName().getName();
        Symbol superName = declClass.getSuperClass().getName();
        
        // on verifie la condition : est ce la super class existe ?
        TypeDefinition superDef = env.get(superName);
        if(superDef == null || !superDef.isClass()){  // on verifie i la cuper class existe deja ou non
        // et aussi , dans le cas ou elle existe mais pas une classe 
        throw new  RuntimeException("Super-class n'existe pas");
        }

        ClassDefinition classDef = new ClassDefinition(
            name,
            (ClassDefinition) superDef,
            new EnvironmentExp(null)        // vide pour le moment
        );

        env.declare(name,classeDef);
    }
}