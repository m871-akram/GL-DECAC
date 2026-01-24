//package fr.ensimag.deca.tree;
//
//import fr.ensimag.deca.CompilerOptions;
//import fr.ensimag.deca.DecacCompiler;
//import fr.ensimag.deca.context.ContextualError;
//import fr.ensimag.deca.syntax.AbstractDecaLexer;
//import fr.ensimag.deca.syntax.DecaLexer;
//import fr.ensimag.deca.syntax.DecaParser;
//import fr.ensimag.deca.tree.AbstractProgram;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//
/// **
// * Test JUnit pour vérifier le calcul de TSTO et ADDSP.
// */
//public class TestStackManagement {
//
//    /**
//     * Méthode utilitaire pour compiler un bout de code Deca en mémoire
//     * et récupérer le code assembleur généré sous forme de String.
//     */
//    private String compileString(String sourceCode) throws ContextualError, IOException {
//        // 1. Création du Compilateur
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // 2. Parsing (Lexer + Parser)
//        DecaLexer lexer = new DecaLexer(CharStreams.fromString(sourceCode));
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        DecaParser parser = new DecaParser(tokens);
//        parser.setDecacCompiler(compiler); // Important pour la gestion d'erreurs
//
//        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);
//
//        // 3. Vérification Contextuelle (Essentielle pour calculer le nombre de variables locales !)
//        // On suppose que verifyProgram renvoie void ou qu'on ignore le retour
//        program.verifyProgram(compiler);
//
//        // 4. Génération de Code
//        program.codeGenProgram(compiler);
//
//        // 5. Récupération du résultat
//        return compiler.displayIMAProgram();
//    }
//
//    @Test
//    public void testAddspMain() throws IOException, ContextualError {
//        // Scénario : 3 variables locales dans le main
//        String source = "{ int x; int y; int z; }";
//
//        String asm = compileString(source);
//        System.out.println("--- ASM Main ---");
//        System.out.println(asm);
//
//        // Vérification : On doit trouver ADDSP #3
//        // Le (?s) permet au point . de matcher les retours à la ligne
//        Assertions.assertTrue(asm.contains("ADDSP #3"),
//                "Le code doit contenir ADDSP #3 pour 3 variables locales");
//    }
//
//    @Test
//    public void testAddspMethod() throws IOException, ContextualError {
//        // Scénario : Une méthode avec 2 variables locales
//        String source =
//                "class A { " +
//                        "   void m() { int a; int b; } " +
//                        "} " +
//                        "{ A a = new A(); a.m(); }";
//
//        String asm = compileString(source);
//        System.out.println("--- ASM Method ---");
//        System.out.println(asm);
//
//        // Vérification : On doit trouver ADDSP #2 dans le code de la méthode
//        // On cherche le label de la méthode suivi plus loin de ADDSP #2
//        Assertions.assertTrue(asm.matches("(?s).*code\\.A\\.m:.*ADDSP #2.*"),
//                "La méthode m doit contenir ADDSP #2");
//    }
//
//    @Test
//    public void testTstoCalculation() throws IOException, ContextualError {
//        // Scénario :
//        // - 2 variables locales (ADDSP 2)
//        // - Appel de méthode avec 3 paramètres (PUSH * 3) + this (PUSH * 1) = 4 Pushes
//        // - Sauvegarde registres (Supposons que ton compilateur sauve au moins R2)
//        // TSTO devrait être au moins 2 + 4 = 6.
//        String source =
//                "class A { " +
//                        "   void callee(int x, int y, int z) {} " +
//                        "   void caller() { " +
//                        "       int loc1; int loc2; " +
//                        "       callee(1, 2, 3); " +
//                        "   } " +
//                        "} " +
//                        "{ A a = new A(); a.caller(); }";
//
//        String asm = compileString(source);
//        System.out.println("--- ASM TSTO ---");
//        System.out.println(asm);
//
//        // Vérification pour la méthode 'caller'
//        // On vérifie qu'il y a un TSTO avec une valeur raisonnable (par ex >= 6)
//        // Regex : code.A.caller ... TSTO #(\d+) ...
//        // Note : C'est dur de vérifier la valeur exacte sans connaître ta gestion précise des registres,
//        // mais on peut vérifier que TSTO existe.
//        Assertions.assertTrue(asm.matches("(?s).*code\\.A\\.caller:.*TSTO #([6-9]|[1-9][0-9]).*"),
//                "TSTO doit être présent dans 'caller' avec une valeur suffisante (>= 6)");
//
//        Assertions.assertTrue(asm.matches("(?s).*code\\.A\\.caller:.*ADDSP #2.*"),
//                "ADDSP #2 doit être présent dans 'caller'");
//    }
//}