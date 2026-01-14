package fr.ensimag.deca.context;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.CommonTokenStream;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.syntax.AbstractDecaLexer;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tree.AbstractProgram;


public abstract class AbstractContextInvalidTest {


    protected abstract String getDecaFilePath();

    /**
     * Test principal
     */
    public void runTest() throws IOException {
        AbstractProgram program = parse(getDecaFilePath());
        DecacCompiler compiler =
                new DecacCompiler(new CompilerOptions(), null);

        boolean errorThrown = false;

        try {
            program.verifyProgram(compiler);
        } catch (ContextualError e) {
            errorThrown = true;
            System.out.println("Erreur contextuelle détectée (attendue)");
            System.out.println(e.getMessage());
        }

        assert errorThrown : "Erreur contextuelle attendue mais non levée";
    }

    /**
     * Méthode utilitaire de parsing
     */
    private AbstractProgram parse(String filePath) throws IOException {
        DecaLexer lexer =
                AbstractDecaLexer.createLexerFromArgs(
                        new String[] { filePath });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DecaParser parser = new DecaParser(tokens);

        File sourceFile = new File(filePath);
        DecacCompiler compiler =
                new DecacCompiler(new CompilerOptions(), sourceFile);

        parser.setDecacCompiler(compiler);

        return parser.parseProgramAndManageErrors(System.err);
    }
}
