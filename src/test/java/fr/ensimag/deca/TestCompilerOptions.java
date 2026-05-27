package fr.ensimag.deca;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCompilerOptions {

    @Test
    public void testBannerFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-b"});
        assertTrue(opts.getPrintBanner());
    }

    @Test
    public void testBannerWithOtherFlagThrows() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"-b", "foo.deca"}));
    }

    @Test
    public void testParseFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-p", "foo.deca"});
        assertEquals(0, opts.getActionSpecial());
    }

    @Test
    public void testVerifyFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-v", "foo.deca"});
        assertEquals(1, opts.getActionSpecial());
    }

    @Test
    public void testPandVConflict() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"-p", "-v", "foo.deca"}));
    }

    @Test
    public void testVandPConflict() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"-v", "-p", "foo.deca"}));
    }

    @Test
    public void testNoCheckFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-n", "foo.deca"});
        assertTrue(opts.getNoCheck());
    }

    @Test
    public void testParallelFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-P", "foo.deca"});
        assertTrue(opts.getParallel());
    }

    @Test
    public void testDebugFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-d", "foo.deca"});
        assertEquals(1, opts.getDebug());
    }

    @Test
    public void testDoubleDDebugFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-dd", "foo.deca"});
        assertEquals(2, opts.getDebug());
    }

    @Test
    public void testTripleDDebugFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-ddd", "foo.deca"});
        assertEquals(3, opts.getDebug());
    }

    @Test
    public void testQuadrupleDDebugFlag() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-dddd", "foo.deca"});
        assertEquals(4, opts.getDebug());
    }

    @Test
    public void testRegisterFlagMin() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-r", "4", "foo.deca"});
        assertEquals(4, opts.getMaxRegisters());
        assertEquals(4, opts.getRegisters());
    }

    @Test
    public void testRegisterFlagMid() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-r", "8", "foo.deca"});
        assertEquals(8, opts.getMaxRegisters());
    }

    @Test
    public void testRegisterFlagMax() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-r", "16", "foo.deca"});
        assertEquals(16, opts.getMaxRegisters());
    }

    @Test
    public void testRegisterFlagTooSmall() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"-r", "3", "foo.deca"}));
    }

    @Test
    public void testRegisterFlagTooBig() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"-r", "17", "foo.deca"}));
    }

    @Test
    public void testRegisterFlagMissingArg() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"-r"}));
    }

    @Test
    public void testNoArgs() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{}));
    }

    @Test
    public void testInvalidFileExtension() {
        CompilerOptions opts = new CompilerOptions();
        assertThrows(CLIException.class, () -> opts.parseArgs(new String[]{"foo.txt"}));
    }

    @Test
    public void testMultipleSourceFiles() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"a.deca", "b.deca"});
        assertEquals(2, opts.getSourceFiles().size());
    }

    @Test
    public void testDuplicateSourceFileIgnored() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"a.deca", "a.deca"});
        assertEquals(1, opts.getSourceFiles().size());
    }

    @Test
    public void testGetRegistersDefault() {
        CompilerOptions opts = new CompilerOptions();
        assertEquals(16, opts.getRegisters());
    }

    @Test
    public void testDisplayUsageDoesNotThrow() {
        new CompilerOptions().displayUsage();
    }

    @Test
    public void testCombinedFlags() throws CLIException {
        CompilerOptions opts = new CompilerOptions();
        opts.parseArgs(new String[]{"-n", "-P", "-d", "-r", "8", "foo.deca"});
        assertTrue(opts.getNoCheck());
        assertTrue(opts.getParallel());
        assertEquals(1, opts.getDebug());
        assertEquals(8, opts.getMaxRegisters());
    }
}
