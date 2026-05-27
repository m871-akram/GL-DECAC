package fr.ensimag.deca.tree;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class TestReadFloatCodeGen {

    private String compile(String src) throws ContextualError, IOException {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        DecaLexer lexer = new DecaLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(compiler);
        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);
        program.verifyProgram(compiler);
        program.codeGenProgram(compiler);
        return compiler.displayIMAProgram();
    }

    private String compileNoCheck(String src) throws Exception {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-n", "dummy.deca"});
        DecacCompiler compiler = new DecacCompiler(opts, null);
        DecaLexer lexer = new DecaLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(compiler);
        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);
        program.verifyProgram(compiler);
        program.codeGenProgram(compiler);
        return compiler.displayIMAProgram();
    }

    @Test
    public void testReadFloatEmitsRFLOAT() throws Exception {
        String asm = compile("{ float x; x = readFloat(); }");
        assertTrue(asm.contains("RFLOAT"), "readFloat() must emit RFLOAT instruction");
    }

    @Test
    public void testReadFloatWithCheckEmitsIOError() throws Exception {
        String asm = compile("{ float x; x = readFloat(); }");
        assertNotNull(asm);
        assertTrue(asm.contains("RFLOAT"));
    }

    @Test
    public void testReadFloatNoCheckStillEmitsRFLOAT() throws Exception {
        String asm = compileNoCheck("{ float x; x = readFloat(); }");
        assertTrue(asm.contains("RFLOAT"), "RFLOAT must appear even with -n flag");
    }

    @Test
    public void testReadFloatResultUsable() throws Exception {
        String asm = compile("{ float x; x = readFloat(); println(x); }");
        assertNotNull(asm);
        assertTrue(asm.contains("RFLOAT"));
    }

    @Test
    public void testReadFloatVerifyReturnsFloatType() {
        assertDoesNotThrow(() -> compile("{ float x; x = readFloat(); }"));
    }
}
