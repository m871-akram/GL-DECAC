# Deca Compiler Project - AI Agent Instructions

## Project Overview
This is a **Deca compiler** (object-oriented language) built with Java 21, ANTLR4, and targeting the IMA assembly architecture. Developed as an educational project (Ensimag GL51), it follows a classic 3-phase compiler architecture: lexical analysis → contextual verification → code generation.

## Architecture & Key Components

### Compilation Pipeline
1. **Lexer/Parser** (`src/main/antlr4/fr/ensimag/deca/syntax/`): ANTLR4 grammars (`DecaLexer.g4`, `DecaParser.g4`) generate lexer/parser that extend `AbstractDecaLexer` and `AbstractDecaParser`
2. **AST Tree** (`src/main/java/fr/ensimag/deca/tree/`): All nodes extend `Abstract*` base classes (e.g., `AbstractExpr`, `AbstractInst`, `AbstractDeclClass`)
3. **Context/Type System** (`src/main/java/fr/ensimag/deca/context/`): Type checking with `EnvironmentType`, `EnvironmentExp`, and type definitions (`ClassType`, `IntType`, etc.)
4. **Code Generation** (`src/main/java/fr/ensimag/deca/codegen/`): Emits IMA assembly via `RegisterManager`, `MemoryManagementUnit`, `InterruptController`, `SignalSequencer`
5. **IMA Target** (`src/main/java/fr/ensimag/ima/pseudocode/`): IMA instruction set representation

### Core Compilation Flow
```java
DecacCompiler → parse() → verifyProgram() → codeGenProgram()
```
- Entry point: `DecacMain.java` calls `DecacCompiler.compile()`
- Each AST node implements: `verifyXXX(DecacCompiler)` for type checking, `codeGenXXX(DecacCompiler)` for assembly generation
- Program structure: `Program` = `ListDeclClass` + `AbstractMain`

## Critical Developer Workflows

### Building & Running
```bash
# Full build with compilation
mvn clean compile

# Run tests (JUnit + custom shell scripts)
mvn test

# Execute compiler (wrapper script)
./src/main/bin/decac [options] <source.deca>

# Common compiler options:
# -p : Parse and pretty-print only (no verification/codegen)
# -v : Parse + verify only (no code generation)
# -r N : Limit register usage to N registers (default: 16)
# -b : Print team banner
```

### Testing Infrastructure
Tests are categorized by compiler phase:
- **Lexical**: `src/test/deca/lexical/{valid,invalid}/`
- **Syntax**: `src/test/deca/syntax/{valid,invalid}/`
- **Context**: `src/test/deca/context/{valid,invalid}/`
- **Codegen**: `src/test/deca/codegen/{valid,invalid,perf}/`

Shell test launchers:
- `test_lex`, `test_synt`, `test_context` in `src/test/script/launchers/`
- Run via `basic-lex.sh`, `basic-synt.sh`, `basic-context.sh`, `basic-gencode.sh`
- `common-tests.sh`: Minimal smoke tests for `-p`, `-v`, full compilation

### Code Coverage
- JaCoCo configured with 20% minimum instruction coverage
- Run report: `./src/test/script/jacoco-report.sh`
- Generated files: `target/site/jacoco/`, `jacoco.exec`

## Project-Specific Conventions

### AST Node Pattern
Every tree node follows this hierarchy:
1. **Abstract base** (e.g., `AbstractExpr`): Defines contract with abstract methods `verifyExpr()`, `codeGenExpr()`
2. **Concrete implementations** (e.g., `Plus`, `Minus`, `Identifier`): Override verification and codegen
3. **TreeList** wrappers (e.g., `ListDeclClass`, `ListInst`): Homogeneous collections with batch operations

Example from [src/main/java/fr/ensimag/deca/tree/AbstractUnaryExpr.java](src/main/java/fr/ensimag/deca/tree/AbstractUnaryExpr.java):
```java
protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
    getOperand().codeGenExpr(compiler, register);  // Evaluate operand
    codeGenUnary(compiler, register);              // Apply operation
}
protected abstract void codeGenUnary(DecacCompiler compiler, GPRegister register);
```

### Register Management
- `RegisterManager` tracks available registers (R0-R15, configurable via `-r`)
- Always use `compiler.getRegisterManager()` for allocation
- `MemoryManagementUnit` handles stack frames and local variables
- `InterruptController` manages error handlers for division-by-zero, overflow, etc.

### Error Handling
- **Compile-time**: Throw `ContextualError` with `Location` during `verifyXXX()`
- **Runtime errors**: Use `InterruptController` labels (e.g., `io_error`, `overflow_error`)
- **Fatal errors**: `DecacFatalError` for unrecoverable compilation failures

### Testing Conventions
- Test files must have alphanumeric names only (validated by `common-tests.sh`)
- Valid tests: Expected to compile/run without errors
- Invalid tests: Must produce error messages matching `filename:line:` pattern
- Assembly output generated alongside source: `file.deca` → `file.ass`

## Integration Points

### ANTLR4 Integration
- Grammar files in `src/main/antlr4/` generate code to `target/generated-sources/antlr4/`
- Custom superclasses: `AbstractDecaLexer`, `AbstractDecaParser` provide utilities
- Maven antlr4-maven-plugin handles generation during compile phase

### IMA Simulator
- Target architecture: IMA (Imaginary Machine Architecture)
- Execute via `ima` command: `ima file.ass`
- Math library: `src/main/resources/include/Math.decah` provides builtin functions

### Docker Development Environment
- Image available: `gitlab.ensimag.fr:5050/reigniep/dockergl`
- Container maps local workspace to `/home/gl/projet_gl`
- Pre-installed: Java, Maven, IMA simulator
- See [docker/README.md](docker/README.md) for setup

## Key Files to Reference

- **Compiler entry**: [src/main/java/fr/ensimag/deca/DecacMain.java](src/main/java/fr/ensimag/deca/DecacMain.java), [src/main/java/fr/ensimag/deca/DecacCompiler.java](src/main/java/fr/ensimag/deca/DecacCompiler.java)
- **AST root**: [src/main/java/fr/ensimag/deca/tree/Program.java](src/main/java/fr/ensimag/deca/tree/Program.java), [src/main/java/fr/ensimag/deca/tree/AbstractProgram.java](src/main/java/fr/ensimag/deca/tree/AbstractProgram.java)
- **Type system**: [src/main/java/fr/ensimag/deca/context/EnvironmentType.java](src/main/java/fr/ensimag/deca/context/EnvironmentType.java), [src/main/java/fr/ensimag/deca/context/Type.java](src/main/java/fr/ensimag/deca/context/Type.java)
- **Grammars**: [src/main/antlr4/fr/ensimag/deca/syntax/DecaLexer.g4](src/main/antlr4/fr/ensimag/deca/syntax/DecaLexer.g4), [src/main/antlr4/fr/ensimag/deca/syntax/DecaParser.g4](src/main/antlr4/fr/ensimag/deca/syntax/DecaParser.g4)
- **Build config**: [pom.xml](pom.xml) (Maven 3.6.3+, Java 21, ANTLR 4.13.2)

## Common Pitfalls

1. **ANTLR generation**: Always run `mvn compile` after grammar changes - hand-editing generated files will be overwritten
2. **Register spilling**: When `RegisterManager` exhausted, implement stack spilling in `MemoryManagementUnit`
3. **Type conversion**: Use `ConvFloat` node explicitly when context requires float coercion
4. **Error locations**: Always construct `Location` from ANTLR tokens for accurate error reporting
5. **Test placement**: Invalid tests go in `invalid/` directories and MUST trigger compilation errors
