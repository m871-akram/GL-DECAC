//package fr.ensimag.deca.codegen;
//
//import fr.ensimag.deca.DecacCompiler;
//import fr.ensimag.deca.CompilerOptions;
//import fr.ensimag.deca.context.*;
//import fr.ensimag.deca.tree.*;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
/// **
// * Tests de génération de code pour les opérateurs de comparaison via programmes complets
// *
// * @author gl51
// * @date 01/01/2026
// */
//public class TestComparisonOpsCodeGen {
//
//    @Test
//    public void testLowerCodeGenViaIf() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { if (1 < 2) { } }
//        IntLiteral left = new IntLiteral(1);
//        IntLiteral right = new IntLiteral(2);
//        Lower lowerExpr = new Lower(left, right);
//
//        ListInst thenBody = new ListInst();
//        IfThenElse ifStmt = new IfThenElse(lowerExpr, thenBody, new ListInst());
//
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(ifStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//        assertTrue(code.length() > 0);
//    }
//
//    @Test
//    public void testGreaterCodeGenViaWhile() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { while (5 > 3) { } }
//        IntLiteral left = new IntLiteral(5);
//        IntLiteral right = new IntLiteral(3);
//        Greater greaterExpr = new Greater(left, right);
//
//        ListInst body = new ListInst();
//        While whileStmt = new While(greaterExpr, body);
//
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(whileStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//    }
//
//    @Test
//    public void testLowerOrEqualCodeGen() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { if (3 <= 3) { } }
//        IntLiteral left = new IntLiteral(3);
//        IntLiteral right = new IntLiteral(3);
//        LowerOrEqual leExpr = new LowerOrEqual(left, right);
//
//        IfThenElse ifStmt = new IfThenElse(leExpr, new ListInst(), new ListInst());
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(ifStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//    }
//
//    @Test
//    public void testGreaterOrEqualCodeGen() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { if (10 >= 5) { } }
//        IntLiteral left = new IntLiteral(10);
//        IntLiteral right = new IntLiteral(5);
//        GreaterOrEqual geExpr = new GreaterOrEqual(left, right);
//
//        IfThenElse ifStmt = new IfThenElse(geExpr, new ListInst(), new ListInst());
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(ifStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//    }
//
//    @Test
//    public void testEqualsCodeGen() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { if (7 == 7) { } }
//        IntLiteral left = new IntLiteral(7);
//        IntLiteral right = new IntLiteral(7);
//        Equals eqExpr = new Equals(left, right);
//
//        IfThenElse ifStmt = new IfThenElse(eqExpr, new ListInst(), new ListInst());
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(ifStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//    }
//
//    @Test
//    public void testNotEqualsCodeGen() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { if (5 != 3) { } }
//        IntLiteral left = new IntLiteral(5);
//        IntLiteral right = new IntLiteral(3);
//        NotEquals neExpr = new NotEquals(left, right);
//
//        IfThenElse ifStmt = new IfThenElse(neExpr, new ListInst(), new ListInst());
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(ifStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//    }
//
//    @Test
//    public void testComparisonWithFloats() throws ContextualError {
//        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
//
//        // Programme: { if (1.5 < 2.5) { } }
//        FloatLiteral left = new FloatLiteral(1.5f);
//        FloatLiteral right = new FloatLiteral(2.5f);
//        Lower lowerExpr = new Lower(left, right);
//
//        IfThenElse ifStmt = new IfThenElse(lowerExpr, new ListInst(), new ListInst());
//        ListInst mainInsts = new ListInst();
//        mainInsts.add(ifStmt);
//
//        Main main = new Main(new ListDeclVar(), mainInsts);
//        Program program = new Program(new ListDeclClass(), main);
//
//        program.verifyProgram(compiler);
//        program.codeGenProgram(compiler);
//
//        String code = compiler.displayIMAProgram();
//        assertNotNull(code);
//    }
//}
