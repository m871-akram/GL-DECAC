package fr.ensimag.deca.context.invalid;

import java.io.IOException;


public class TestIntToBool extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/int_to_bool.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestIntToBool().runTest();
        System.out.println("Test IntToBool OK");
    }
}
