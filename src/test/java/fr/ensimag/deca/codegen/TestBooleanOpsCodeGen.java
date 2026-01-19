package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tree.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de génération de code pour les opérateurs booléens via programmes complets
 * 
 * @author gl51
 * @date 01/01/2026
 */
public class TestBooleanOpsCodeGen {

    @Test
    public void testOrCodeGenViaIfCondition() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        
        // Programme: { if (true || false) { } }
        BooleanLiteral left = new BooleanLiteral(true);
        BooleanLiteral right = new BooleanLiteral(false);
        Or orExpr = new Or(left, right);
        
        ListInst thenBody = new ListInst();
        IfThenElse ifStmt = new IfThenElse(orExpr, thenBody, new ListInst());
        
        ListInst mainInsts = new ListInst();
        mainInsts.add(ifStmt);
        
        Main main = new Main(new ListDeclVar(), mainInsts);
        Program program = new Program(new ListDeclClass(), main);
        
        program.verifyProgram(compiler);
        program.codeGenProgram(compiler);
        
        String code = compiler.displayIMAProgram();
        assertNotNull(code);
        assertTrue(code.length() > 0);
    }

    @Test
    public void testNotCodeGenViaWhile() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        
        // Programme: { while (!false) { } }
        BooleanLiteral operand = new BooleanLiteral(false);
        Not notExpr = new Not(operand);
        
        ListInst body = new ListInst();
        While whileStmt = new While(notExpr, body);
        
        ListInst mainInsts = new ListInst();
        mainInsts.add(whileStmt);
        
        Main main = new Main(new ListDeclVar(), mainInsts);
        Program program = new Program(new ListDeclClass(), main);
        
        program.verifyProgram(compiler);
        program.codeGenProgram(compiler);
        
        String code = compiler.displayIMAProgram();
        assertNotNull(code);
    }
    
    @Test
    public void testAndCodeGenViaIf() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        
        // Programme: { if (true && false) { } }
        BooleanLiteral left = new BooleanLiteral(true);
        BooleanLiteral right = new BooleanLiteral(false);
        And andExpr = new And(left, right);
        
        ListInst thenBody = new ListInst();
        IfThenElse ifStmt = new IfThenElse(andExpr, thenBody, new ListInst());
        
        ListInst mainInsts = new ListInst();
        mainInsts.add(ifStmt);
        
        Main main = new Main(new ListDeclVar(), mainInsts);
        Program program = new Program(new ListDeclClass(), main);
        
        program.verifyProgram(compiler);
        program.codeGenProgram(compiler);
        
        String code = compiler.displayIMAProgram();
        assertNotNull(code);
    }
}
