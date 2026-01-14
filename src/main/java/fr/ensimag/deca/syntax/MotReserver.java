package fr.ensimag.deca.syntax;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author gl51
 * @date 01/01/2026
 */
public class MotReserver extends DecaRecognitionException {

    private static final long serialVersionUID = 4670163376041273741L;

    public MotReserver(DecaParser recognizer, ParserRuleContext ctx) {
        super(recognizer, ctx);
    }

    @Override
    public String getMessage() {
        return "le mot est reserver";
    }
}
