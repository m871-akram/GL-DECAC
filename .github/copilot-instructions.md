# Deca Compiler Development Guide

## Project Overview
This is a compiler for the Deca language (an object-oriented educational language), targeting IMA assembly. The project follows a three-phase compilation pipeline: lexing/parsing (ANTLR4), contextual verification, and code generation to IMA pseudocode.

## Architecture

### Core Components
- **DecacMain**: CLI entry point (`src/main/bin/decac` wrapper)
- **DecacCompiler**: Per-file compiler instance managing symbol tables, IMA program generation, and specialized subsystems (RegisterManager, MemoryManagementUnit, InterruptController, SignalSequencer)
- **Tree package** (`fr.ensimag.deca.tree`): AST nodes extending AbstractExpr/AbstractInst with visitor methods for verification (`verifyExpr`, `verifyInst`) and code generation (`codeGenExpr`, `codeGenInst`)
- **IMAProgram** (`fr.ensimag.ima.pseudocode`): Target assembly representation with instruction builders

### Compilation Pipeline
1. **Lexing/Parsing**: ANTLR4 grammars in `src/main/antlr4/fr/ensimag/deca/syntax/` (DecaLexer.g4, DecaParser.g4)
2. **Contextual Verification**: Three passes via AST visitor methods (`verifyExpr`, `verifyRValue` with implicit ConvFloat insertion)
3. **Code Generation**: AST nodes emit IMA instructions via `codeGenXxx` methods

### Key Subsystems
- **RegisterManager**: Manages GPRegisters (default 16, configurable via `-r` flag)
- **MemoryManagementUnit**: Stack/heap management for variables and objects
- **EnvironmentType**: Predefined types (int, float, boolean, void, string, Object) with assignment compatibility rules
- **SymbolTable**: String interning for identifiers

## Build and Test

### Essential Commands
```bash
mvn clean compile             # Build (auto-generates ANTLR parsers)
mvn test                      # Run JUnit tests
./src/main/bin/decac file.deca  # Compile Deca → .ass assembly
./src/test/script/basic-decac.sh # Integration test CLI flags
```

### JaCoCo Coverage
- Enable via `mvn -Djacoco.skip=false clean test`
- View report: `target/site/jacoco/index.html`
- Min coverage: 20% instruction ratio (configured in pom.xml)

## Testing Strategy

### Test Organization
- **Unit tests** (`src/test/java/fr/ensimag/deca/`): Direct AST/context/codegen testing with JUnit 5 + Mockito
  - Example pattern: Create AST manually, call `verifyProgram()` + `codeGenProgram()`, assert IMA output
- **Integration tests** (`src/test/script/*.sh`): Shell scripts testing CLI behavior
- **Deca fixtures** (`src/test/deca/{syntax,context,codegen}/`): Valid/invalid .deca files for parser/semantic testing

### Test File Patterns
- Tests must match `Test*.java` or `*Test.java`
- Use `@Test` annotation from `org.junit.jupiter.api`
- Context tests often use `DecacCompiler(new CompilerOptions(), null)` pattern

## Code Conventions

### AST Node Implementation
1. Extend AbstractExpr/AbstractInst/AbstractDeclXxx
2. Implement `verifyExpr(DecacCompiler, EnvironmentExp, ClassDefinition)` → sets type decoration
3. Implement `codeGenExpr(DecacCompiler)` → returns GPRegister with result
4. Use `verifyRValue()` for assignment contexts (handles implicit float conversion)

### Error Handling
- Throw `ContextualError` with location during verification phase
- `DecacFatalError` for unrecoverable compiler errors
- `LocationException` for user-facing syntax/semantic errors
- `DecacInternalError` for impossible states (assertions enabled by default)

### IMA Code Generation
```java
// Standard pattern in codeGenExpr methods:
GPRegister reg = compiler.getRegisterManager().allocate();
compiler.addInstruction(new LOAD(new ImmediateInteger(42), reg));
return reg;
```

## Critical Patterns

### Type Checking
- All expressions must have type decoration after `verifyExpr()`
- Use `compiler.environmentType.assignCompatible(expected, actual)` for assignment checks
- Implicit float conversions wrapped with `ConvFloat` node during `verifyRValue()`

### Compiler Options Flags
- `-p`: Print AST (stops after parsing)
- `-v`: Print AST after verification
- `-n`: No contextual checking
- `-r N`: Use N registers (default 16)
- `-d`: Debug output
- `-b`: Print team banner

### Known Incomplete Features
- Parallel compilation (`-P` flag throws UnsupportedOperationException in DecacMain)
- Some TODO comments reference "partie objet" (object-oriented features)

## File Locations
- Grammar: `src/main/antlr4/fr/ensimag/deca/syntax/`
- AST: `src/main/java/fr/ensimag/deca/tree/`
- IMA instructions: `src/main/java/fr/ensimag/ima/pseudocode/instructions/`
- Test fixtures: `src/test/deca/` (organized by compilation phase)

## Development Tips
- Maven generates ANTLR sources to `target/generated-sources/antlr4/`
- Classpath saved to `target/generated-sources/classpath.txt` (used by decac wrapper)
- Log4j configured in `src/main/resources/log4j.properties`
- Use `compiler.addComment("debug info")` to annotate generated assembly
