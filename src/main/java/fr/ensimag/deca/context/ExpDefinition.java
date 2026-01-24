package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;

/**
 * Definition associated to identifier in expressions.
 *
 * @author gl51
 * @date 01/01/2026
 */
public abstract class ExpDefinition extends Definition {

    private DAddr operand;

    public ExpDefinition(Type type, Location location) {
        super(type, location);
    }

    public DAddr getOperand() {
        return operand;
    }

    public void setOperand(DAddr operand) {
        this.operand = operand;
    }

}
