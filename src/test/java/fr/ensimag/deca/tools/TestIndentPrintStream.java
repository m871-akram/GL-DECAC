package fr.ensimag.deca.tools;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestIndentPrintStream {

    private static final String NL = System.lineSeparator();

    private IndentPrintStream make(ByteArrayOutputStream buf) {
        return new IndentPrintStream(new PrintStream(buf));
    }

    @Test
    public void testPrintNoIndent() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        make(buf).print("hello");
        assertEquals("hello", buf.toString());
    }

    @Test
    public void testPrintlnString() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        make(buf).println("hello");
        assertEquals("hello" + NL, buf.toString());
    }

    @Test
    public void testPrintlnEmpty() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        make(buf).println();
        assertEquals(NL, buf.toString());
    }

    @Test
    public void testIndentAddsTab() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.print("x");
        assertEquals("\tx", buf.toString());
    }

    @Test
    public void testDoubleIndent() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.indent();
        s.print("x");
        assertEquals("\t\tx", buf.toString());
    }

    @Test
    public void testUnindentCancelsIndent() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.unindent();
        s.print("x");
        assertEquals("x", buf.toString());
    }

    @Test
    public void testIndentOnlyPrintedOncePerLine() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.print("a");
        s.print("b");
        assertEquals("\tab", buf.toString());
    }

    @Test
    public void testIndentResetsAfterNewline() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.println("a");
        s.print("b");
        assertEquals("\ta" + NL + "\tb", buf.toString());
    }

    @Test
    public void testPrintChar() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        make(buf).print('Z');
        assertEquals("Z", buf.toString());
    }

    @Test
    public void testPrintCharWithIndent() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.print('Z');
        assertEquals("\tZ", buf.toString());
    }

    @Test
    public void testPrintCharAlreadyIndented() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.print("a");
        s.print('b');
        assertEquals("\tab", buf.toString());
    }

    @Test
    public void testMultiLineOutput() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        IndentPrintStream s = make(buf);
        s.indent();
        s.println("line1");
        s.println("line2");
        assertEquals("\tline1" + NL + "\tline2" + NL, buf.toString());
    }
}
