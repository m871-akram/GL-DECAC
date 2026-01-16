# Deca Compiler - AI Agent Guide

## Project Overview
This is a **multi-pass compiler** for Deca (Java-like language) → IMA assembly, developed at Ensimag (gl51 team). The compiler implements a classic 4-phase pipeline: lexing → parsing → contextual analysis → code generation.

## Critical Architecture

### Compilation Pipeline
- **Entry Point**: [DecacMain.java](src/main/java/fr/ensimag/deca/DecacMain.java) → `DecacCompiler.compile()`
- **Flow**: `doLexingAndParsing()` → `verifyProgram()` → `codeGenProgram()`
- **Orchestrator**: `DecacCompiler` manages symbol tables, type environments, IMA program, and register allocation

### Core Package Structure
1. **`fr.ensimag.deca.syntax`**: ANTLR4 grammars ([DecaLexer.g4](src/main/antlr4/fr/ensimag/deca/syntax/DecaLexer.g4), [DecaParser.g4](src/main/antlr4/fr/ensimag/deca/syntax/DecaParser.g4))
2. **`fr.ensimag.deca.tree`**: AST nodes (inherit from `Tree`). Each implements:
   - `verify*()` methods for contextual analysis (type-checking, scoping)
   - `codeGen*()` methods for IMA instruction emission
   - `decompile()` for source reconstruction
3. **`fr.ensimag.deca.context`**: Type system (`Type` hierarchy), environment management (`EnvironmentExp` = chained scopes), definitions (`VariableDefinition`, `MethodDefinition`)
4. **`fr.ensimag.deca.codegen`**: `RegisterManager` (naive stack-based allocation, R2-R15)
5. **`fr.ensimag.ima.pseudocode`**: IMA instruction representations (`LOAD`, `ADD`, `BEQ`, etc.)

## Key Design Patterns

### Visitor Pattern (Partial)
- **NOT** pure visitor: AST nodes embed behavior (verify/codegen methods)
- Each node type implements phase-specific logic directly (e.g., `Plus.codeGenExpr()`)

### Two-Pass Decoration
1. **Pass 1 (verify)**: Attach `Type` to each expression via `setType()`
2. **Pass 2 (codegen)**: Read types with `getType()` to emit correct IMA instructions

### Register Allocation Strategy
- **Naive stack-based**: Allocate R2-R15 sequentially, spill to stack when exhausted
- **Spill mechanism** (see [AbstractOpArith](src/main/java/fr/ensimag/deca/tree/AbstractOpArith.java)):
  ```java
  // Evaluate left → PUSH → Evaluate right → POP → Operate
  compiler.addInstruction(new PUSH(leftReg));
  rightExpr.codeGenExpr(compiler, leftReg);
  compiler.addInstruction(new POP(R0));
  compiler.addInstruction(new ADD(R0, leftReg));
  ```

### Label Generation for Control Flow
- **Unique labels**: Static counters in `IfThenElse`, `While` classes
- **While loop** structure: Jump to condition first, loop body precedes condition check (avoids code duplication)

## Build & Test Workflows

### Maven Commands
```bash
mvn clean compile              # Build compiler (generates ANTLR parsers)
mvn test                       # Run JUnit tests + shell script tests
mvn test -Djacoco.skip=false   # Generate code coverage (target/site/jacoco/)
```

### Using the Compiler
```bash
./src/main/bin/decac file.deca       # Compile to file.ass
./src/main/bin/decac -p file.deca    # Parse-only mode
./src/main/bin/decac -v file.deca    # Stop after contextual analysis
./src/main/bin/decac -r N file.deca  # Limit to N registers (default 16)
ima file.ass                         # Run generated IMA assembly
```

### Test Organization
- **Unit tests** (JUnit 5): [src/test/java/fr/ensimag/deca/](src/test/java/fr/ensimag/deca/)
  - Use **Mockito** for AST node testing (see [TestPlusPlain.java](src/test/java/fr/ensimag/deca/context/TestPlusPlain.java))
  - Compare with manual approach in [TestPlusWithoutMock.java](src/test/java/fr/ensimag/deca/context/TestPlusWithoutMock.java)
- **Shell integration tests**: [src/test/script/](src/test/script/)
  - [basic-gencode.sh](src/test/script/basic-gencode.sh): Compile & run with ima
  - [common-tests.sh](src/test/script/common-tests.sh): Smoke tests (hello-world, syntax errors)
- **Test inputs**: [src/test/deca/](src/test/deca/) (codegen/valid, codegen/invalid, context/valid, etc.)

### Docker Environment
- Use `docker/` for Ensimag-like environment (includes ima assembler)
- Mount project directory: `docker create -v $(pwd):/home/gl/projet_gl --name projetgl ...`

## Project-Specific Conventions

### Code Style
- **Package structure mirrors compilation phases**: syntax → tree → context → codegen
- **Javadoc tags**: `@author gl51`, `@date 01/01/2026`
- **Error handling**: Throw `ContextualError` during verify, `DecacInternalError` for compiler bugs

### AST Node Implementation Pattern
When adding new AST nodes:
1. Extend appropriate base class (`AbstractExpr`, `AbstractInst`, etc.)
2. Implement `verify*()` to type-check and set type
3. Implement `codeGen*()` to emit IMA instructions via `compiler.addInstruction()`
4. Implement `decompile()` for pretty-printing

### Register Management
- **Always allocate via RegisterManager**: `registerManager.prendreRegistre()` / `libererRegistre()`
- **Stack tracking**: Call `empiler()` before PUSH, `depiler()` after POP
- **Stack overflow protection**: Generated code includes `TSTO #size` + `BOV stack_overflow_error`

## Common Pitfalls

1. **ANTLR grammar changes require rebuild**: Run `mvn clean compile` after editing `.g4` files
2. **Register leaks**: Every `prendreRegistre()` needs matching `libererRegistre()` or explicit spill
3. **Label collisions**: Always use static counters for unique labels in control flow nodes
4. **Type decoration missing**: Verify phases must call `setType()` before codegen reads it
5. **Test file naming**: `.deca` test files must have alphanumeric-only paths (see [common-tests.sh](src/test/script/common-tests.sh) validation)

## Integration Points

- **ANTLR4 Runtime 4.13.2**: Parser generation dependency
- **IMA Assembler**: External tool (ima) for executing .ass files
- **Log4j 1.2.17**: Logging via `Logger.getLogger(ClassName.class)`
- **JaCoCo**: Code coverage agent (instrumented tests when `jacoco.skip=false`)
- **JUnit 5 + Mockito**: Testing framework (see [pom.xml](pom.xml) dependencies)

## Documentation References
- Full architecture doc: [docs/suivi3/architecture.md](docs/suivi3/architecture.md)
- Example usage: [examples/calc/](examples/calc/) (simple calculator compiler)
- Test coverage reports: `target/site/jacoco/index.html` (after running with coverage)

## Quick Start for New Contributors
1. Build: `mvn clean compile`
2. Run tests: `mvn test`
3. Try compiler: `./src/main/bin/decac examples/calc/src/main/deca/hello.deca`
4. Study example: Start with [TestPlusPlain.java](src/test/java/fr/ensimag/deca/context/TestPlusPlain.java) for testing patterns
5. Read: [architecture.md](docs/suivi3/architecture.md) sections 2-4 for pipeline details
