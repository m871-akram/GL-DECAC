package fr.ensimag.deca.context;

import java.io.IOException;


public class TestFloatToIntAssign extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/float_to_int_assign.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestFloatToIntAssign().runTest();
        System.out.println("Test float_to_int_assign OK");
    }
}
