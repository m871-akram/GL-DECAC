package fr.ensimag.deca.syntax;

import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class CarractereNonReconue extends DecaRecognitionException {

    private static final long serialVersionUID = 4670163376041273741L;


    public CarractereNonReconue(AbstractDecaLexer recognizer, IntStream input) {
        super(recognizer, input);
    }

    @Override
    public String getMessage() {
        return "le carratere n'est pas reconue";
    }
}
