package fr.ensimag.deca.context;

import java.io.IOException;


public class TestOpInvalidType extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/op_invalid_type.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestOpInvalidType().runTest();
        System.out.println("Test OpInvalidType OK");
    }
}
