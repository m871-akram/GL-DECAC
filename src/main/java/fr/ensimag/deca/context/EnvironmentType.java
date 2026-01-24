package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;

import java.util.HashMap;
import java.util.Map;
// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca

/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl51
 * @date 01/01/2026
 */
public class EnvironmentType {
    public final VoidType VOID;
    public final IntType INT;
    public final FloatType FLOAT;
    public final StringType STRING;
    public final BooleanType BOOLEAN;
    public final NullType NULL;
    private final Map<Symbol, TypeDefinition> envTypes;

    public EnvironmentType(DecacCompiler compiler) {

        envTypes = new HashMap<Symbol, TypeDefinition>();

        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.
        Symbol nullSymb = compiler.createSymbol("null");
        this.NULL = new NullType(nullSymb);

        // Classe  Objet necessaire en Partie Objet
        Symbol objectSymb = compiler.createSymbol("Object");

        ClassType objectType = new ClassType(objectSymb, Location.BUILTIN, null);
        ClassDefinition objectDef = objectType.getDefinition();

        EnvironmentExp objectMembers = objectDef.getMembers();
        // Signature equalsSign = new Signature();
        // MethodDefinition equalsMethodDef = new MethodDefinition(
        //     BOOLEAN,
        //     Location.BUILTIN,
        //     equalsSign,
        //     0
        // );

        Symbol equalsSymbol = compiler.createSymbol("equals");
        Signature equalsSignature = new Signature();
        equalsSignature.add(objectType);
        MethodDefinition equalsExpDef = new MethodDefinition(
                BOOLEAN,
                Location.BUILTIN,
                equalsSignature,
                1  // Index 1 car index 0 est réservé pour le pointeur vers le parent
        );
        equalsExpDef.setLabel(new Label("equals"));
        try {
            objectMembers.declare(equalsSymbol, equalsExpDef);
        } catch (DoubleDefException e) {
            //ne dois jammais arriver
        }
        envTypes.put(objectSymb, objectDef);
        objectDef.incNumberOfMethods();
    }

    public void declareClass(Symbol symb, TypeDefinition classDef) {
        envTypes.put(symb, classDef);
    }

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    public boolean subType(Type T1, Type T2) {
        if (T1.sameType(T2)) {
            return true;
        }
        if (T1.isClass() && T2.isNull()) {
            return true;
        }
        if (T1.isClass() && T2.isClass()) {
            ClassDefinition defT2 = ((ClassType) T2).getDefinition();
            ClassDefinition defT1 = ((ClassType) T1).getDefinition();
            while (defT2 != null) {
                if (defT2 == defT1) {
                    return true;
                }
                defT2 = defT2.getSuperClass();
            }
        }
        return false;
    }

    public boolean assignCompatible(Type T1, Type T2) {

        if (T1.isFloat() && T2.isInt()) {
            return true;
        }
        if (subType(T1, T2)) {
            return true;
        }


        return false;
    }

    public boolean aritCompatible(Type T1, Type T2) {
        if (T1.isFloat() && T2.isInt()) {
            return true;
        }
        if (T2.isFloat() && T1.isInt()) {
            return true;
        }
        if (T2.isInt() && T1.isInt()) {
            return true;
        }
        if (T1.isFloat() && T1.isFloat()) {
            return true;
        }
        return false;
    }

    public boolean castCompatible(Type T1, Type T2) {
        if (T1.isVoid()) {
            return false;
        }
        if (assignCompatible(T1, T2) || assignCompatible(T2, T1)) {
            return true;
        }

        return false;
    }
}
