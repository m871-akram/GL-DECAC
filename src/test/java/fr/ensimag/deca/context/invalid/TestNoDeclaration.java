package fr.ensimag.deca.context.invalid;

import java.io.IOException;


public class TestNoDeclaration extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/noDeclaration.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestNoDeclaration().runTest();
        System.out.println("Test NoDeclaration OK");
    }
}
