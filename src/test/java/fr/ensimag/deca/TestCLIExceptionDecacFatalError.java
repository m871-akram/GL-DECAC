package fr.ensimag.deca;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCLIExceptionDecacFatalError {

    @Test
    public void testCLIExceptionMessage() {
        CLIException ex = new CLIException("bad option");
        assertEquals("bad option", ex.getMessage());
    }

    @Test
    public void testCLIExceptionIsException() {
        CLIException ex = new CLIException("oops");
        assertInstanceOf(Exception.class, ex);
    }

    @Test
    public void testDecacFatalErrorMessage() {
        DecacFatalError err = new DecacFatalError("file not readable");
        assertEquals("file not readable", err.getMessage());
    }

    @Test
    public void testDecacFatalErrorIsException() {
        DecacFatalError err = new DecacFatalError("fatal");
        assertInstanceOf(Exception.class, err);
    }

    @Test
    public void testCLIExceptionThrownAndCaught() {
        assertThrows(CLIException.class, () -> { throw new CLIException("thrown"); });
    }

    @Test
    public void testDecacFatalErrorThrownAndCaught() {
        assertThrows(DecacFatalError.class, () -> { throw new DecacFatalError("thrown"); });
    }
}
