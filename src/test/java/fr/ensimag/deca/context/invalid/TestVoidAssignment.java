package fr.ensimag.deca.context;

import java.io.IOException;


public class TestVoidAssignment extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/void_assignment.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestVoidAssignment().runTest();
        System.out.println("Test VoidAssignment OK");
    }
}
