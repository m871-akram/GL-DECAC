package fr.ensimag.deca.syntax;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestIncludeErrors {

    private DecaLexer emptyLexer() {
        return new DecaLexer(CharStreams.fromString(""));
    }

    @Test
    public void testIncludeFileNotFoundMessage() {
        DecaLexer lexer = emptyLexer();
        IncludeFileNotFound ex = new IncludeFileNotFound(
                "missing.deca", lexer, CharStreams.fromString(""));
        assertEquals("missing.deca: include file not found", ex.getMessage());
    }

    @Test
    public void testIncludeFileNotFoundGetName() {
        DecaLexer lexer = emptyLexer();
        IncludeFileNotFound ex = new IncludeFileNotFound(
                "foo/bar.deca", lexer, CharStreams.fromString(""));
        assertEquals("foo/bar.deca", ex.getName());
    }

    @Test
    public void testIncludeFileNotFoundIsRecognitionException() {
        DecaLexer lexer = emptyLexer();
        IncludeFileNotFound ex = new IncludeFileNotFound(
                "x.deca", lexer, CharStreams.fromString(""));
        assertInstanceOf(DecaRecognitionException.class, ex);
    }

    @Test
    public void testCircularIncludeMessage() {
        DecaLexer lexer = emptyLexer();
        CircularInclude ex = new CircularInclude(
                "self.deca", lexer, CharStreams.fromString(""));
        assertTrue(ex.getMessage().contains("self.deca"));
        assertTrue(ex.getMessage().toLowerCase().contains("circular"));
    }

    @Test
    public void testCircularIncludeIsRecognitionException() {
        DecaLexer lexer = emptyLexer();
        CircularInclude ex = new CircularInclude(
                "loop.deca", lexer, CharStreams.fromString(""));
        assertInstanceOf(DecaRecognitionException.class, ex);
    }

    @Test
    public void testLexingMissingIncludeThrows(@TempDir Path tmpDir) throws Exception {
        File src = tmpDir.resolve("test.deca").toFile();
        try (FileWriter fw = new FileWriter(src)) {
            fw.write("#include \"nonexistent_xyz_abc_999.deca\"\n{ }");
        }
        DecaLexer lexer = AbstractDecaLexer.createLexerFromArgs(
                new String[]{src.getAbsolutePath()});
        boolean errorSeen = false;
        try {
            while (lexer.nextToken().getType() != org.antlr.v4.runtime.Token.EOF) { /* drain */ }
        } catch (IncludeFileNotFound e) {
            assertTrue(e.getMessage().contains("nonexistent_xyz_abc_999.deca"));
            errorSeen = true;
        } catch (Exception e) {
            errorSeen = true;
        }
        assertTrue(errorSeen, "A bad #include must produce an error");
    }
}
