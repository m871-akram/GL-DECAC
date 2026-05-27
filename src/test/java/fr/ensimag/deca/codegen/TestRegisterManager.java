package fr.ensimag.deca.codegen;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestRegisterManager {

    @Test
    public void testDefaultHasFreeRegisters() {
        RegisterManager rm = new RegisterManager(16);
        assertTrue(rm.registreLibre());
    }

    @Test
    public void testTakeRegisterReturnsR2First() {
        RegisterManager rm = new RegisterManager(16);
        GPRegister r = rm.prendreRegistre();
        assertEquals(Register.getR(2), r);
    }

    @Test
    public void testTakeAndRelease() {
        RegisterManager rm = new RegisterManager(16);
        rm.prendreRegistre();
        rm.libererRegistre();
        assertTrue(rm.registreLibre());
    }

    @Test
    public void testMinRegistersExhaustion() {
        RegisterManager rm = new RegisterManager(4);
        rm.prendreRegistre(); // R2
        rm.prendreRegistre(); // R3
        assertFalse(rm.registreLibre());
        assertThrows(DecacInternalError.class, rm::prendreRegistre);
    }

    @Test
    public void testInvalidRegistersLow() {
        assertThrows(DecacInternalError.class, () -> new RegisterManager(3));
    }

    @Test
    public void testInvalidRegistersHigh() {
        assertThrows(DecacInternalError.class, () -> new RegisterManager(17));
    }

    @Test
    public void testReset() {
        RegisterManager rm = new RegisterManager(4);
        rm.prendreRegistre();
        rm.prendreRegistre();
        assertFalse(rm.registreLibre());
        rm.reset();
        assertTrue(rm.registreLibre());
        assertEquals(Register.getR(2), rm.prendreRegistre());
    }

    @Test
    public void testLiberFromBaseNoOp() {
        RegisterManager rm = new RegisterManager(16);
        assertDoesNotThrow(rm::libererRegistre);
    }

    @Test
    public void testAllocateAllThenFreeAll() {
        RegisterManager rm = new RegisterManager(16);
        for (int i = 2; i <= 15; i++) {
            assertTrue(rm.registreLibre());
            rm.prendreRegistre();
        }
        assertFalse(rm.registreLibre());
        for (int i = 2; i <= 15; i++) {
            rm.libererRegistre();
        }
        assertTrue(rm.registreLibre());
    }
}
