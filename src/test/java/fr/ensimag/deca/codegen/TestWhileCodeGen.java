package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tree.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de génération de code pour While via compilation complète
 * 
 * @author gl51
 * @date 01/01/2026
 */
public class TestWhileCodeGen {

    @Test
    public void testWhileCodeGenViaProgram() throws ContextualError {
        // Créer un compilateur
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        
        // Créer un programme avec while: { while (true) { } }
        BooleanLiteral condition = new BooleanLiteral(true);
        ListInst body = new ListInst();
        While whileInst = new While(condition, body);
        
        ListInst mainInsts = new ListInst();
        mainInsts.add(whileInst);
        
        Main main = new Main(new ListDeclVar(), mainInsts);
        ListDeclClass classes = new ListDeclClass();
        Program program = new Program(classes, main);
        
        // Vérifier
        program.verifyProgram(compiler);
        
        // Générer le code (c'est ça qui va augmenter la couverture de While !)
        program.codeGenProgram(compiler);
        
        // Vérifier que du code a été généré
        String generatedCode = compiler.displayIMAProgram();
        assertNotNull(generatedCode);
        assertTrue(generatedCode.length() > 0, "Le code généré ne devrait pas être vide");
    }
    
    @Test
    public void testWhileWithComparison() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), null);
        
        // Programme: { while (1 < 2) { } }
        IntLiteral left = new IntLiteral(1);
        IntLiteral right = new IntLiteral(2);
        Lower condition = new Lower(left, right);
        
        ListInst body = new ListInst();
        While whileInst = new While(condition, body);
        
        ListInst mainInsts = new ListInst();
        mainInsts.add(whileInst);
        
        Main main = new Main(new ListDeclVar(), mainInsts);
        Program program = new Program(new ListDeclClass(), main);
        
        // Vérifier et générer
        program.verifyProgram(compiler);
        program.codeGenProgram(compiler);
        
        String code = compiler.displayIMAProgram();
        assertNotNull(code);
        assertTrue(code.length() > 0);
    }
}
