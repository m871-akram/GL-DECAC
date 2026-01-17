package fr.ensimag.deca.syntax;

import org.antlr.v4.runtime.IntStream;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class UnclosedComment extends DecaRecognitionException {

    private static final long serialVersionUID = 4670163376041273741L;


    public UnclosedComment(AbstractDecaLexer recognizer, IntStream input) {
        super(recognizer, input);
    }

    @Override
    public String getMessage() {
        return "le commentaire n'est pas fermer";
    }
}
