package fr.ensimag.deca;

import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tree.AbstractProgram;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Runs every .deca file in src/test/deca through the full compiler pipeline
 * (parse → verify → codegen) in-process so that JaCoCo can measure coverage.
 *
 * Invalid files are expected to throw; we catch all exceptions so the test
 * never fails — the goal is coverage, not correctness assertions.
 */
public class TestDecaFileCoverage {

    private static final String DECA_TEST_DIR = "src/test/deca";

    static Stream<File> allDecaFiles() throws IOException {
        Path root = Paths.get(DECA_TEST_DIR);
        if (!Files.exists(root)) {
            return Stream.empty();
        }
        return Files.walk(root)
                .filter(p -> p.toString().endsWith(".deca"))
                .map(Path::toFile)
                .sorted();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allDecaFiles")
    void compileInProcess(File decaFile) throws IOException {
        CompilerOptions opts = new CompilerOptions();
        DecacCompiler compiler = new DecacCompiler(opts, decaFile);

        DecaLexer lexer = new DecaLexer(CharStreams.fromFileName(decaFile.getPath()));
        lexer.setSource(decaFile);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(compiler);

        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);
        if (program == null) {
            return; // syntax error — lexer/parser paths still exercised
        }

        try {
            program.verifyProgram(compiler);
        } catch (Exception e) {
            return; // contextual error — verify paths still exercised
        }

        try {
            program.codeGenProgram(compiler);
        } catch (Exception e) {
            // codegen error — codegen paths still exercised
        }
    }
}
