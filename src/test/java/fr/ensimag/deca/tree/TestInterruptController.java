//package fr.ensimag.deca.tree;
//
//import fr.ensimag.deca.CompilerOptions;
//import fr.ensimag.deca.DecacCompiler;
//import fr.ensimag.deca.codegen.InterruptController;
//import fr.ensimag.deca.codegen.InterruptVector;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.EnumSource;
//
/// **
// * Vérifie que chaque vecteur d'interruption génère le bon code assembleur.
// */
//public class TestInterruptController {
//
//    /**
//     * Ce test sera exécuté une fois pour CHAQUE valeur définie dans InterruptVector.
//     * @param vector Le vecteur d'erreur injecté par JUnit
//     */
//    @ParameterizedTest
//    @EnumSource(InterruptVector.class)
//    public void testGenerationErreur(InterruptVector vector) {
//        // 1. Initialisation d'un compilateur vide (juste pour récupérer le buffer assembleur)
//        // On passe null car on n'a pas besoin de fichier source pour ce test unitaire
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // On récupère le contrôleur (ou on en instancie un si tu n'as pas de getter)
//        // Supposons que tu utilises celui du compilateur :
//        InterruptController controller = compiler.getIrqController();
//
//        // 2. Action : On déclenche l'interruption et on génère le code
//        controller.triggerInterrupt(compiler, vector);
//        controller.flashServiceRoutines(compiler);
//
//        // 3. Récupération du code généré
//        String asmCode = compiler.displayIMAProgram();
//        System.out.println("Test de " + vector + " :\n" + asmCode); // Debug visuel optionnel
//
//        // 4. Vérifications (Assertions)
//
//        // A. Vérifie qu'on a bien l'instruction de saut (BOV label)
//        // Note: triggerInterrupt génère le BOV immédiatement
//        Assertions.assertTrue(asmCode.contains("BOV " + vector.getLabelName()),
//                "Le code doit contenir le saut (BOV) vers le label " + vector.getLabelName());
//
//        // B. Vérifie que le label est bien défini (Label:)
//        Assertions.assertTrue(asmCode.contains(vector.getLabelName() + ":"),
//                "Le label de destination " + vector.getLabelName() + " doit être généré");
//
//        // C. Vérifie que le message d'erreur est correct
//        // On cherche WSTR "Le message"
//        Assertions.assertTrue(asmCode.contains("WSTR \"" + vector.getMessage() + "\""),
//                "Le message d'erreur affiché doit être : " + vector.getMessage());
//
//        // D. Vérifie que le programme s'arrête en erreur
//        // On cherche l'instruction ERROR juste après ou à la fin
//        Assertions.assertTrue(asmCode.contains("ERROR"),
//                "La routine doit se terminer par l'instruction ERROR");
//    }
//}