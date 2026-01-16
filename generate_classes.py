import os

# Configuration du chemin vers le package tree
# Adaptez ce chemin si votre structure est différente
BASE_DIR = "src/main/java/fr/ensimag/deca/tree"

# Contenu de base pour les fichiers Java
PACKAGE_DECL = "package fr.ensimag.deca.tree;\n\n"

IMPORTS = """import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
"""

# Définition des classes à créer
# Format: (NomClasse, ClasseMère, TypeDeSquelette)
CLASSES_TO_CREATE = [
    # 1. Membres des classes
    ("AbstractDeclField", "Tree", "abstract"),
    ("DeclField", "AbstractDeclField", "concrete"),
    ("ListDeclField", "TreeList<AbstractDeclField>", "list"),

    ("AbstractDeclMethod", "Tree", "abstract"),
    ("DeclMethod", "AbstractDeclMethod", "concrete"),
    ("ListDeclMethod", "TreeList<AbstractDeclMethod>", "list"),

    ("AbstractDeclParam", "Tree", "abstract"),
    ("DeclParam", "AbstractDeclParam", "concrete"),
    ("ListDeclParam", "TreeList<AbstractDeclParam>", "list"),

    # 2. Corps des méthodes
    ("AbstractMethodBody", "Tree", "abstract"),
    ("MethodBody", "AbstractMethodBody", "concrete"),
    ("MethodAsmBody", "AbstractMethodBody", "concrete"),

    # 3. Expressions Objet
    ("Cast", "AbstractExpr", "expr"),
    ("InstanceOf", "AbstractExpr", "expr"),
    ("MethodCall", "AbstractExpr", "expr"),
    ("New", "AbstractExpr", "expr"),
    ("This", "AbstractExpr", "expr"),
    ("Null", "AbstractExpr", "expr"),
    ("Selection", "AbstractLValue", "lvalue"), # Attention: hérite de LValue

    # 4. Instructions Objet
    ("Return", "AbstractInst", "inst"),
]

def get_class_content(name, parent, skeleton_type):
    content = PACKAGE_DECL + IMPORTS

    # Gestion des imports spécifiques selon le parent
    if "Expr" in parent or "LValue" in parent:
        content += "import fr.ensimag.deca.context.Type;\n"

    content += "\n"

    # Déclaration de la classe
    modifiers = "public abstract" if skeleton_type == "abstract" else "public"
    content += f"{modifiers} class {name} extends {parent} {{\n\n"

    # Méthodes selon le type
    if skeleton_type == "list":
        content += f"""    @Override
    public void decompile(IndentPrintStream s) {{
        for ({name.replace('List', '')} i : getList()) {{
            i.decompile(s);
        }}
    }}
"""
    elif skeleton_type == "abstract":
        content += "    // Classe abstraite pour factoriser le code si besoin\n"

    elif skeleton_type in ["concrete", "expr", "inst", "lvalue"]:
        # Decompile
        content += """    @Override
    public void decompile(IndentPrintStream s) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
"""
        # Méthodes de vérification contextuelle spécifiques
        if "DeclField" in name:
            content += """
    protected void verifyDeclField(DecacCompiler compiler, Symbol currentClass, Symbol superClass)
            throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }
"""
        elif "DeclMethod" in name:
            content += """
    protected void verifyDeclMethod(DecacCompiler compiler, Symbol currentClass, Symbol superClass)
            throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }
"""
        elif skeleton_type == "expr" or skeleton_type == "lvalue":
            content += """
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }
"""
        elif skeleton_type == "inst":
            content += """
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        throw new UnsupportedOperationException("Not yet implemented");
    }
"""

    content += "}\n"
    return content

def create_files():
    if not os.path.exists(BASE_DIR):
        print(f"Erreur: Le répertoire {BASE_DIR} n'existe pas.")
        print("Veuillez vérifier que vous lancez le script depuis la racine du projet.")
        return

    print(f"Génération des classes dans {BASE_DIR}...\n")

    for name, parent, skel_type in CLASSES_TO_CREATE:
        file_path = os.path.join(BASE_DIR, f"{name}.java")

        if os.path.exists(file_path):
            print(f"[SKIP] {name}.java existe déjà.")
            continue

        with open(file_path, "w") as f:
            f.write(get_class_content(name, parent, skel_type))
        print(f"[OK] {name}.java créé.")

if __name__ == "__main__":
    create_files()