package fr.ensimag.deca.context;

import java.io.IOException;

public class TestDoubleDeclaration extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/doubleDeclaration.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestDoubleDeclaration().runTest();
        System.out.println("Test doubleDeclaration OK");
    }
}
