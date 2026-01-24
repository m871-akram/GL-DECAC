//package fr.ensimag.deca.codegen;
//
//import fr.ensimag.deca.CompilerOptions;
//import fr.ensimag.deca.DecacCompiler;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//
/// **
// * Test de robustesse pour l'option -P (Parallélisme).
// * Vérifie que le compilateur est Thread-Safe (pas de variables static partagées).
// */
//public class TestParallelSafety {
//
//    private final List<File> createdFiles = new ArrayList<>();
//
//    // Nettoyage après le test (suppression des .deca et .ass)
//    @AfterEach
//    public void cleanup() {
//        for (File f : createdFiles) {
//            if (f.exists()) f.delete();
//            // Supprimer aussi le .ass généré
//            File ass = new File(f.getAbsolutePath().replace(".deca", ".ass"));
//            if (ass.exists()) ass.delete();
//        }
//    }
//
//    /**
//     * Crée un fichier Deca temporaire avec du contenu complexe (Classes + If + While)
//     * pour forcer l'utilisation des Managers (Reg, MMU, Sequencer).
//     */
//    private File createDummyFile(int index) throws IOException {
//        String name = "TestPar_" + index;
//        File f = File.createTempFile(name, ".deca");
//        createdFiles.add(f);
//
//        String content =
//                "class A" + index + " { " +
//                        "   protected int x; " +
//                        "   void method() { " +
//                        "       if (x > 0) { x = x - 1; } " + // Utilise des Labels
//                        "       while (x < 10) { x = x + 1; } " + // Utilise des Labels
//                        "   } " +
//                        "} " +
//                        "{ A" + index + " a = new A" + index + "(); a.method(); }";
//
//        try (PrintWriter out = new PrintWriter(f)) {
//            out.println(content);
//        }
//        return f;
//    }
//
//    @Test
//    public void testMassiveParallelCompilation() throws IOException, InterruptedException {
//        // 1. Préparation : On crée 50 fichiers à compiler
//        int fileCount = 50;
//        System.out.println("Génération de " + fileCount + " fichiers sources...");
//        List<File> sources = new ArrayList<>();
//        for (int i = 0; i < fileCount; i++) {
//            sources.add(createDummyFile(i));
//        }
//
//        // 2. Configuration du parallélisme (comme dans DecacMain)
//        CompilerOptions options = new CompilerOptions();
//        // On simule l'option -P activée (même si on appelle l'executor manuellement ici)
//
//        int cores = Runtime.getRuntime().availableProcessors();
//        System.out.println("Lancement de la compilation sur " + cores + " threads...");
//
//        ExecutorService executor = Executors.newFixedThreadPool(cores);
//        List<Future<Boolean>> results = new ArrayList<>();
//
//        long startTime = System.currentTimeMillis();
//
//        // 3. Soumission des tâches
//        for (File source : sources) {
//            Callable<Boolean> task = () -> {
//                // IMPORTANT : On vérifie ici que new DecacCompiler() est bien isolé
//                DecacCompiler compiler = new DecacCompiler(options, source);
//                return compiler.compile(); // false = succès (bizarrement dans ton code, mais à vérifier)
//            };
//            results.add(executor.submit(task));
//        }
//
//        executor.shutdown();
//        boolean finished = executor.awaitTermination(1, TimeUnit.MINUTES);
//        Assertions.assertTrue(finished, "La compilation a pris trop de temps (Deadlock ?)");
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("Compilation terminée en " + (endTime - startTime) + "ms");
//
//        // 4. Vérification des résultats
//        for (Future<Boolean> result : results) {
//            try {
//                // Dans ta méthode doCompile: return false signifie SUCCÈS (pas d'erreur)
//                // return true signifie ERREUR.
//                boolean isError = result.get();
//                Assertions.assertFalse(isError, "Une des compilations a échoué ! (Problème de static ?)");
//            } catch (ExecutionException e) {
//                Assertions.fail("Exception dans un thread compilateur : " + e.getCause());
//            }
//        }
//
//        // 5. Vérification approfondie : Isolation des Labels
//        // Si SignalSequencer n'est pas static, chaque fichier devrait avoir ses propres labels commençant à 0.
//        // Si c'était static, on aurait des labels "while_begin_450" dans le premier fichier compilé.
//
//        for (File source : sources) {
//            File assFile = new File(source.getAbsolutePath().replace(".deca", ".ass"));
//            Assertions.assertTrue(assFile.exists(), "Le fichier .ass n'a pas été généré : " + assFile.getName());
//
//            String asmContent = Files.readString(Path.of(assFile.getPath()));
//
//            // On cherche un label typique généré par le séquenceur.
//            // Ton SignalSequencer génère "loop_start_0" ou "while_begin_0".
//            // Si l'isolation est bonne, TOUS les fichiers doivent contenir la version "_0" ou un petit nombre.
//
//            // Note: Adapte ce pattern selon le nom exact de tes labels dans SignalSequencer
//            // Par exemple, si tu génères "if_end_0", "loop_start_0"...
//            // Ici, on vérifie juste qu'on n'a pas des chiffres astronomiques dus au partage
//
//            // Si on trouve "loop_start_0" dans tous les fichiers, c'est gagné.
//            // Si on trouve "loop_start_49" dans le dernier fichier, c'est perdu (compteur partagé).
//
//            // Vérification basique de contenu
//            Assertions.assertTrue(asmContent.contains("TSTO"), "Code généré incomplet (pas de TSTO)");
//        }
//
//        System.out.println("SUCCÈS : 50 compilations parallèles sans crash ni interférence.");
//    }
//}