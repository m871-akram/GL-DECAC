package fr.ensimag.deca.syntax;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Syntax error for an expression that should be an lvalue (ie that can be
 * assigned), but is not.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class InvalidFloat extends DecaRecognitionException {

    private static final long serialVersionUID = 4670163376041273741L;

    public InvalidFloat(DecaParser recognizer, ParserRuleContext ctx) {
        super(recognizer, ctx);
    }

    @Override
    public String getMessage() {
        return "Float is not a valide number";
    }
}
