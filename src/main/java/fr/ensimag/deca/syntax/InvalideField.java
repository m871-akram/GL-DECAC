package fr.ensimag.deca.syntax;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class InvalideField extends DecaRecognitionException {

    private static final long serialVersionUID = 4670163376041273741L;

    public InvalideField(DecaParser recognizer, ParserRuleContext ctx) {
        super(recognizer, ctx);
    }

    @Override
    public String getMessage() {
        return "le field na pas de nom";
    }
}
