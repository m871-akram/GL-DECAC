package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import org.apache.commons.lang.Validate;

/**
 * Definition of a class.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class ClassDefinition extends TypeDefinition {


    private final EnvironmentExp members;
    private final ClassDefinition superClass;
    private int numberOfFields = 0;
    private int numberOfMethods = 0;
    private fr.ensimag.ima.pseudocode.DAddr operand;  // Address of the vTable

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public void incNumberOfFields() {
        this.numberOfFields++;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }

    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    public fr.ensimag.ima.pseudocode.DAddr getOperand() {
        return operand;
    }

    ;

    public void setOperand(fr.ensimag.ima.pseudocode.DAddr operand) {
        this.operand = operand;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    }

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    public EnvironmentExp getMembers() {
        return members;
    }

}
