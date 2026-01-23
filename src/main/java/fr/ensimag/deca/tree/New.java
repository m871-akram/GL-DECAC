package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.InterruptVector;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;



/**
 * Integer literal
 * new classname()
 * @author G51
 * @date 16/01/2026
 */
public class New extends AbstractExpr {
    private final AbstractIdentifier className;

    public New(AbstractIdentifier className) {
        Validate.notNull(className);
        this.className = className;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        className.decompile(s);
        s.print("()");
    }


    @Override
    public Type verifyExpr(DecacCompiler compiler,
        EnvironmentExp localEnv, ClassDefinition currentClass)
        throws ContextualError {
        // on vérifier que le nom est une classe qui exist
        TypeDefinition typeDef = compiler.environmentType.defOfType(className.getName());

        // on verifie si ce objet 'classs' existe
        if (typeDef == null) {
            throw new ContextualError(
                "la Classe :" + className.getName().getName() + ",est inconnue", getLocation()
            );
        }

        //et s'il existe , est ce une classe ,ou un autre type
        if (!typeDef.isClass()) {
            throw new ContextualError(
                className.getName().getName() + " : n'est pas une classe",
                getLocation()
            );
        }

        // si tout est bon , on retourn son type
        ClassDefinition classDef = (ClassDefinition) typeDef;
        Type classType = classDef.getType();
        className.setType(classType);
        className.setDefinition(classDef);
        setType(classType);
        return classType;
    }



    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        ClassDefinition classDef = (ClassDefinition) className.getDefinition();

        // alloc dans le tas
        int objectSize = 1 + classDef.getNumberOfFields();
        compiler.addInstruction(new NEW(objectSize, register));

        // verif debordement tas
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.getIrqController().triggerInterrupt(compiler,
                    InterruptVector.IRQ_HEAP_FULL);
        }

        // init pointeur vtable
        RegisterOffset vTableAddr = (RegisterOffset) classDef.getOperand();

        // charge adresse vtable
        compiler.addInstruction(new LEA(vTableAddr, Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, register)));

        // appel constructeur : push this
        compiler.addInstruction(new PUSH(register));
        compiler.getMMU().notifyPush(1);

        // appel init
        String initLabel = "init." + classDef.getType().getName().getName();
        compiler.addInstruction(new BSR(new Label(initLabel)));

        // restaure adresse objet (pop this)
        compiler.addInstruction(new POP(register));
        compiler.getMMU().notifyPop(1);
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
    }
}


