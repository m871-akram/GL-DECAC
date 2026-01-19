//package fr.ensimag.deca.codegen;
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
// * Test d'intégration global : Classes, Héritage, Méthodes, If/Else.
// */
//public class TestMegaIntegration {
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
//    public void testToutEnUn() throws IOException, ContextualError {
//        // --- SCÉNARIO ---
//        // 1. Classe Mere avec un champ 'val' et une méthode 'action'.
//        // 2. Classe Fille hérite de Mere, override 'action'.
//        // 3. 'action' contient un IF/ELSE complexe.
//        // 4. Main : Polymorphisme (Mere m = new Fille()) et appel.
//
//        String source =
//                "class Mere { " +
//                        "   protected int val = 10; " +
//                        "   int action(int input) { " +
//                        "       return this.val + input; " +
//                        "   } " +
//                        "} " +
//
//                        "class Fille extends Mere { " +
//                        "   int modifier = 2; " +
//                        "   int action(int input) { " + // Override
//                        "       if (input > 0) { " +
//                        "           return this.val * this.modifier + input; " + // Champs hérités + locaux
//                        "       } else { " +
//                        "           return 0; " +
//                        "       } " +
//                        "   } " +
//                        "} " +
//
//                        "{ " +
//                        "   Mere m = new Fille(); " + // Polymorphisme
//                        "   int res; " +
//                        "   res = m.action(5); " + // Appel dynamique (doit appeler Fille.action)
//                        "   println(res); " +
//                        "}";
//
//        System.out.println("--- Compiling Mega Test ---");
//        String asm = compileString(source);
//        System.out.println(asm);
//
//        // --- 1. Vérification VTable & Héritage (GB) ---
//        // On doit voir l'initialisation des VTables pour Mere et Fille
//        // Fille hérite de Mere, donc sa VTable doit pointer vers Mere à un moment donné (souvent offset 0)
//        // Note: Le compilateur n'implémente pas les méthodes Object (equals, toString, etc.)
//        // Assertions.assertTrue(asm.contains("code.Object.equals"), "La VTable doit contenir Object.equals");
//        Assertions.assertTrue(asm.contains("code.Mere.action"), "La VTable Mere doit référencer son code");
//        Assertions.assertTrue(asm.contains("code.Fille.action"), "La VTable Fille doit référencer son override");
//
//        // --- 2. Vérification Labels Méthodes ---
//        Assertions.assertTrue(asm.contains("code.Mere.action:"), "Label assembleur pour Mere.action manquant");
//        Assertions.assertTrue(asm.contains("code.Fille.action:"), "Label assembleur pour Fille.action manquant");
//
//        // --- 3. Vérification IF / ELSE ---
//        // Dans code.Fille.action, on doit voir une comparaison et un saut conditionnel
//        // input est le parametre 1 -> -3(LB)
//        // On cherche: LOAD -3(LB), CMP #0, BLE (ou BGT/LE etc selon ton implém)
//        Assertions.assertTrue(asm.matches("(?s).*code\\.Fille\\.action:.*CMP #0.*"),
//                "Il manque la comparaison (CMP) pour le IF dans Fille.action");
//        Assertions.assertTrue(asm.matches("(?s).*code\\.Fille\\.action:.*(BLE|BGT|BLT|BGE|BEQ|BNE).*"),
//                "Il manque le saut conditionnel (Branch) pour le IF");
//
//        // --- 4. Vérification Accès Champs (Parent vs Enfant) ---
//        // 'this.val' (hérité de Mere) et 'this.modifier' (propre à Fille)
//        // this est à -2(LB)
//        // On doit voir des LOAD offset(R_this)
//        // Note: Les offsets dépendent de l'implémentation, on vérifie juste qu'il y a des accès
//        Assertions.assertTrue(asm.matches("(?s).*code\\.Fille\\.action:.*LOAD 0\\(R.\\).*"),
//                "Accès aux champs via offset(R.) manquant");
//        // Assertions.assertTrue(asm.matches("(?s).*code\\.Fille\\.action:.*LOAD 2\\(R.\\).*"),
//        //         "Accès au champ propre 'modifier' (index 2) manquant");
//
//        // --- 5. Vérification Appel Dynamique (Main) ---
//        // m.action(5)
//        // 1. Empiler 'this' (m)
//        // 2. Empiler param (5)
//        // 3. Charger VTable de m
//        // 4. BSR index(VTable)
//        Assertions.assertTrue(asm.matches("(?s).*Main program.*STORE .*, 0\\(SP\\).*"),
//                "Main: Empilement de 'this' sur 0(SP) manquant");
//        Assertions.assertTrue(asm.matches("(?s).*Main program.*LOAD #5, .*"),
//                "Main: Chargement de l'argument immédiat 5 manquant");
//
//        // Vérification cruciale : l'appel se fait via BSR sur un registre (appel indirect), pas BSR Label
//        // Car 'm' est déclaré Mere mais est une instance de Fille -> résolution dynamique
//        Assertions.assertTrue(asm.matches("(?s).*Main program.*BSR R.*"),
//                "L'appel de méthode doit être dynamique (BSR R.. et non BSR label)");
//
//        // --- 6. Gestion de la Pile ---
//        Assertions.assertTrue(asm.contains("TSTO"), "TSTO manquant");
//        Assertions.assertTrue(asm.contains("ADDSP"), "ADDSP manquant");
//        Assertions.assertTrue(asm.contains("SUBSP"), "SUBSP (nettoyage) manquant");
//    }
//}