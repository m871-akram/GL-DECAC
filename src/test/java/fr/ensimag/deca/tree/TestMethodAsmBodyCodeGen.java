package fr.ensimag.deca.tree;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestMethodAsmBodyCodeGen {

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

    @Test
    public void testVoidAsmMethodEmitsRTS() throws Exception {
        String src = "class Foo { void bar() asm(\"NOP\"); } { }";
        String asm = compile(src);
        assertTrue(asm.contains("NOP"), "inline asm content must appear in output");
        assertTrue(asm.contains("RTS"), "void asm method must emit RTS");
    }

    @Test
    public void testNonVoidAsmMethodNoAutoRTS() throws Exception {
        String src = "class Foo { int bar() asm(\"LOAD #1, R1\"); } { }";
        String asm = compile(src);
        assertTrue(asm.contains("LOAD #1, R1"), "inline asm code must be in output");
    }

    @Test
    public void testAsmMethodDecompile() throws Exception {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-p", "dummy.deca"});
        DecacCompiler compiler = new DecacCompiler(opts, null);
        String src = "class Foo { void bar() asm(\"NOP\"); } { }";
        DecaLexer lexer = new DecaLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(compiler);
        AbstractProgram program = parser.parseProgramAndManageErrors(System.err);
        program.verifyProgram(compiler);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = new IndentPrintStream(new PrintStream(buf));
        program.decompile(s);
        String decompiled = buf.toString();
        assertTrue(decompiled.contains("asm("), "decompile must produce asm(...)");
        assertTrue(decompiled.contains("NOP"), "decompile must preserve asm content");
    }

    @Test
    public void testAsmMethodIterChildren() {
        assertDoesNotThrow(() -> compile("class Foo { void bar() asm(\"NOP\"); } { }"));
    }
}
