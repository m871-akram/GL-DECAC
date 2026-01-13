package fr.ensimag.deca.context;

import java.io.IOException;

public class TestIfConditionInt extends AbstractContextInvalidTest {

    @Override
    protected String getDecaFilePath() {
        return "src/test/deca/context/invalid/if_condition_int.deca";
    }

    public static void main(String[] args) throws IOException {
        new TestIfConditionInt().runTest();
        System.out.println("Test IfConditionInt OK");
    }
}
