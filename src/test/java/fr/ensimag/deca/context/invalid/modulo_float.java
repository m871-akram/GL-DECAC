package fr.ensimag.deca.context;

import java.io.IOException;


public class TestModuloFloat extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/modulo_float.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestModuloFloat().runTest();
        System.out.println("Test ModuloFloat OK");
    }
}
