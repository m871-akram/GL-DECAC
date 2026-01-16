package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;

public class Selection extends AbstractSelectExpr {
    public Selection(AbstractExpr object, AbstractIdentifier methode){
        super(object, methode);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type objectType = getObject().verifyExpr(compiler, localEnv, currentClass);
        ClassDefinition classDef = objectType.asClassType("Sélection impossible sur un type non-classe", getLocation()).getDefinition();
        var def = classDef.getMembers().get(getItem().getName());
    
        if (def == null) {
            throw new ContextualError(
                "Champ " + getItem().getName().getName() + " inexistant",
                getLocation()
            );
        }
    
        if (!def.isField()) {
            throw new ContextualError(
                getItem().getName().getName() + " n'est pas un champ",
                getLocation()
            );
        }
    
        FieldDefinition fieldDef = def.asFieldDefinition("Ce n'est pas un champ", getLocation());
    
        if (fieldDef.getVisibility()== Visibility.PROTECTED && !currentClass.isSubClassOf(fieldDef.getContainingClass())) {
            throw new ContextualError(
                "Champ PROTECTED '" + getItem().getName().getName() + "' non accessible dans cette classe",
                getLocation()
            );
        }
    
        getItem().setDefinition(fieldDef);
        Type fieldType = fieldDef.getType();
        setType(fieldType);
        return fieldType;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenExpr'");
    }

    @Override
    protected void codeGenStore(DecacCompiler compiler, GPRegister register) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'codeGenStore'");
    }
}