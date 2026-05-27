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

public class TestNoOperationAndListDeclMethod {

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

    private AbstractProgram parse(String src) throws IOException {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        DecaLexer lexer = new DecaLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(compiler);
        return parser.parseProgramAndManageErrors(System.err);
    }

    @Test
    public void testNoOperationCompiles() throws Exception {
        assertNotNull(compile("{ ; }"));
    }

    @Test
    public void testNoOperationDecompile() throws Exception {
        AbstractProgram prog = parse("{ ; }");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        prog.decompile(new IndentPrintStream(new PrintStream(buf)));
        assertTrue(buf.toString().contains(";"));
    }

    @Test
    public void testMultipleNoOperations() throws Exception {
        assertNotNull(compile("{ ; ; ; }"));
    }

    @Test
    public void testNoOperationVerify() {
        assertDoesNotThrow(() -> compile("{ ; }"));
    }

    @Test
    public void testClassWithMultipleMethods() throws Exception {
        String src =
                "class Foo { " +
                "   int add(int a, int b) { return a; } " +
                "   boolean check(int x) { return true; } " +
                "   void reset() { } " +
                "} { }";
        String asm = compile(src);
        assertTrue(asm.contains("code.Foo.add"));
        assertTrue(asm.contains("code.Foo.check"));
        assertTrue(asm.contains("code.Foo.reset"));
    }

    @Test
    public void testEmptyMethodList() {
        assertDoesNotThrow(() -> compile("class Empty { } { }"));
    }

    @Test
    public void testInheritedMethodList() throws Exception {
        String src =
                "class Base { int val() { return 1; } } " +
                "class Child extends Base { int extra() { return 2; } } " +
                "{ }";
        String asm = compile(src);
        assertTrue(asm.contains("code.Base.val"));
        assertTrue(asm.contains("code.Child.extra"));
    }
}
