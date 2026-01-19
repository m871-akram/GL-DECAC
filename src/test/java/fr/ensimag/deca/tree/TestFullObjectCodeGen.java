//package fr.ensimag.deca.tree;
//
//import fr.ensimag.deca.CompilerOptions;
//import fr.ensimag.deca.DecacCompiler;
//import fr.ensimag.deca.context.ContextualError;
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
///**
// * Test d'intégration complet pour la génération de code Objet.
// * Vérifie l'allocation mémoire (GB, LB, SP, TSTO).
// */
//public class TestFullObjectCodeGen {
//
//    private String compileString(String sourceCode) throws ContextualError, IOException {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//        DecaLexer lexer = new DecaLexer(CharStreams.fromString(sourceCode));
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        DecaParser parser = new DecaParser(tokens);
//        parser.setDecacCompiler(compiler);
//        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//        return compiler.displayIMAProgram();
//    }
//
//    @Test
//    public void testArchitectureMemoire() throws IOException, ContextualError {
//        // --- Scénario Complexe ---
//        // 1. Classe A avec un champ (offset 1)
//        // 2. Classe B hérite de A, ajoute un champ (offset 2) et une méthode
//        // 3. Méthode calcul: prend 2 params, a 2 locales, fait un calcul
//        // 4. Main: Instancie B, déclare une globale, appelle la méthode
//        String source =
//                "class A { protected int x; } " +
//                        "class B extends A { " +
//                        "   int y; " +
//                        "   int calcul(int p1, int p2) { " +
//                        "       int loc1 = 10; " +
//                        "       int loc2 = 20; " +
//                        "       return this.x + this.y + p1 + p2 + loc1 + loc2; " +
//                        "   } " +
//                        "} " +
//                        "{ " +
//                        "   B b = new B(); " +
//                        "   int global = 50; " +
//                        "   int result = b.calcul(global, 100); " +
//                        "}";
//
//        String asm = compileString(source);
//        System.out.println(asm); // Pour debug visuel
//
//        // --- 1. Vérification de la VTable (GB) ---
//        // La VTable doit être stockée dans la zone statique (GB).
//        // On s'attend à voir des STORE R0, X(GB) au début.
//        Assertions.assertTrue(asm.matches("(?s).*STORE R., 1\\(GB\\).*"),
//                "La VTable doit être initialisée en base de GB (ex: 1(GB))");
//
//        // --- 2. Vérification des Variables Globales (Main) ---
//        // 'b' et 'global' sont des variables du main -> stockées en GB après la VTable.
//        // Comme on a des VTables, l'offset doit être assez haut (ex: > 3(GB)).
//        // On cherche un ADDSP dans le main.
//        Assertions.assertTrue(asm.matches("(?s).*Main program.*ADDSP #[2-9].*"),
//                "Le Main doit réserver de la place pour 'b' et 'global' (ADDSP)");
//
//        // --- 3. Vérification Méthode 'calcul' (TSTO/ADDSP) ---
//        // code.B.calcul doit avoir ADDSP #2 (pour loc1 et loc2)
//        Assertions.assertTrue(asm.matches("(?s).*code\\.B\\.calcul:.*ADDSP #2.*"),
//                "Méthode calcul: ADDSP #2 attendu pour loc1, loc2");
//
//        // TSTO doit être suffisant :
//        // 2 locales minimum (le compilateur peut optimiser et ne pas compter les sauvegardes ici)
//        Assertions.assertTrue(asm.matches("(?s).*code\\.B\\.calcul:.*TSTO #[2-9].*"),
//                "Méthode calcul: TSTO doit couvrir au moins les locales");
//
//        // --- 4. Vérification Accès Paramètres (LB négatif) ---
//        // p1 est le 1er paramètre -> -3(LB)
//        // p2 est le 2eme paramètre -> -4(LB)
//        // On cherche l'instruction LOAD -3(LB), ... ou similaire
//        Assertions.assertTrue(asm.contains("-3(LB)"),
//                "Accès paramètre p1 : Doit utiliser -3(LB)");
//        Assertions.assertTrue(asm.contains("-4(LB)"),
//                "Accès paramètre p2 : Doit utiliser -4(LB)");
//
//        // --- 5. Vérification Accès 'this' ---
//        // 'this' est implicitement à -2(LB)
//        Assertions.assertTrue(asm.contains("-2(LB)"),
//                "Accès à 'this' : Doit utiliser -2(LB)");
//
//        // --- 6. Vérification Accès Locales (LB positif) ---
//        // loc1 -> 1(LB), loc2 -> 2(LB)
//        Assertions.assertTrue(asm.contains("1(LB)"),
//                "Accès locale loc1 : Doit utiliser 1(LB)");
//        Assertions.assertTrue(asm.contains("2(LB)"),
//                "Accès locale loc2 : Doit utiliser 2(LB)");
//
//        // --- 7. Vérification Appel de Méthode (Pile SP) ---
//        // Dans le Main, lors de l'appel b.calcul(global, 100) :
//        // On doit empiler les paramètres sur la pile (SP)
//        // On cherche des STORE ..., 0(SP) ou -1(SP)
//        Assertions.assertTrue(asm.matches("(?s).*STORE .*, 0\\(SP\\).*"),
//                "Appel méthode : Doit empiler sur 0(SP) (souvent 'this')");
//
//        // On doit voir le BSR (Branch to SubRoutine)
//        Assertions.assertTrue(asm.contains("BSR"),
//                "Appel méthode : Doit contenir une instruction BSR");
//
//        // On doit voir le nettoyage de pile après appel (SUBSP)
//        // 2 params + this = 3 mots
//        Assertions.assertTrue(asm.contains("SUBSP #3"),
//                "Après appel : Doit nettoyer 3 mots (this + 2 params)");
//    }
//}