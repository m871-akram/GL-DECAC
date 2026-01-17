# Deca Compiler AI Assistant Guide

## Project Overview
This is a Java-based compiler for Deca (Java-like language) that translates to IMA assembly. Built at Ensimag (GL51) using ANTLR4 for lexing/parsing and following the Interpreter pattern for code generation.

**Build**: Maven (Java 21, requires Maven 3.6.3+)  
**Entry point**: `src/main/bin/decac` (wrapper script) → `DecacMain.java`  
**Pipeline**: Lexical → Syntactic → Contextual → Code Generation

## Key Architecture Patterns

### 1. Four-Phase Compilation Pipeline
Each phase is orchestrated by `DecacCompiler`:
```
DecaLexer.g4 → DecaParser.g4 → verify*() methods → codeGen*() methods
```

**Phase methods pattern**:
- `verifyExpr()/verifyInst()` for contextual analysis (type checking)
- `codeGenExpr()/codeGenInst()` for IMA assembly generation
- All AST nodes in `fr.ensimag.deca.tree` follow this pattern

### 2. Register Management (Naïve Allocation)
`RegisterManager` uses R2-R15 with stack spilling:
- R0-R1 are **scratch registers** (caller-saved)
- R2-R15 for computation (must be saved/restored in methods)
- Track `registreCourant` (next free) and `taillePileMax` (for TSTO)

**Spill strategy in binary operations**:
1. Evaluate left operand in `register`
2. If free register exists: evaluate right in new register, operate
3. Else: PUSH left → evaluate right in same register → POP into R0 → operate with R0

### 3. Context and Type Decoration
- `EnvironmentExp`: Linked list of scopes (parent chain for variable lookup)
- `EnvironmentType`: Global type environment (int, float, boolean, Object, classes)
- Each AST node gets decorated with `Type` during `verify*()` phase via `setType()`
- `Definition` subclasses: `VariableDefinition`, `MethodDefinition`, `FieldDefinition`, etc.

### 4. Control Flow Code Generation
**Labels use static counters for uniqueness**: `else.1`, `end_if.1`, `while_start.2`

**IfThenElse pattern**:
```
<Code(Condition, false, E_Sinon)>  // Jump if false
<Code(Then)>
BRA E_Fin
E_Sinon:
<Code(Else)>
E_Fin:
```

**While pattern** (condition evaluated at end):
```
BRA E_Cond.n
E_Debut.n:
<Code(Body)>
E_Cond.n:
<Code(Condition, true, E_Debut.n)>  // Loop if true
```

`codeGenBool(compiler, branchOn, targetLabel)`: Evaluates boolean to register, then CMP/BEQ or BNE

### 5. OOP Support (Object-Oriented Programming)
**VTable structure** (generated in first pass before code):
```
Offset 0: Pointer to superclass vTable (LEA instruction)
Offset 1+: Method addresses (code.ClassName.methodName labels)
```

**Object structure in heap**:
```
Offset 0: vTable address
Offset 1+: Fields (inherited first, then class fields)
```

Classes use `ClassDefinition` with vTable address stored in `addrVTable` field.

## Build and Test Commands

### Build
```bash
mvn clean package              # Full build with tests
mvn compile                    # Compile only (no tests)
mvn test                       # Run unit tests
```

### Compiler Usage
```bash
src/main/bin/decac file.deca              # Compile to file.ass
src/main/bin/decac -p file.deca           # Parse only (stop after syntax)
src/main/bin/decac -v file.deca           # Verify only (stop after context)
src/main/bin/decac -r X file.deca         # Use X registers (4-16, default 16)
src/main/bin/decac -n file.deca           # No runtime checks (no overflow/null)
src/main/bin/decac -b                     # Print banner
```

### Test Scripts (in `src/test/script/`)
```bash
./src/test/script/basic-lex.sh            # Lexer tests
./src/test/script/basic-synt.sh           # Parser tests  
./src/test/script/basic-context.sh        # Contextual tests
./src/test/script/basic-gencode.sh        # Code generation tests
./src/test/script/jacoco-report.sh        # Generate coverage report
```

**JaCoCo coverage**: Set `-Djacoco.skip=false` to enable, report in `target/site/jacoco/`

## Critical File Locations

### Core Compiler Components
- `src/main/java/fr/ensimag/deca/DecacCompiler.java` - Orchestrates all phases
- `src/main/java/fr/ensimag/deca/codegen/RegisterManager.java` - Register/stack tracking
- `src/main/antlr4/fr/ensimag/deca/syntax/Deca{Lexer,Parser}.g4` - ANTLR grammars

### AST Hierarchy (all in `tree/`)
- `AbstractProgram` → `Program` (entry point: `verifyProgram()`, `codeGenProgram()`)
- `AbstractExpr` hierarchy: `AbstractOpArith`, `AbstractOpCmp`, `AbstractOpBool`, literals
- `AbstractInst` hierarchy: `IfThenElse`, `While`, `Assign`, `Print`, `Return`
- `AbstractDeclClass` → `DeclClass` (OOP declarations)

### Context/Type System (all in `context/`)
- `Type` subclasses: `IntType`, `FloatType`, `BooleanType`, `ClassType`
- `EnvironmentExp` - Variable scope management
- `Definition` subclasses - Symbol definitions

### IMA Pseudocode (all in `ima/pseudocode/`)
- `IMAProgram` - Accumulates instructions/labels/comments
- `Instruction` subclasses - IMA operations (LOAD, STORE, ADD, BEQ, etc.)
- `Register`, `RegisterOffset`, `DAddr` - Operand representations

## Conventions and Patterns

### Code Generation Delegation
Use `DecacCompiler` methods instead of accessing `program` directly:
```java
compiler.addInstruction(new LOAD(new ImmediateInteger(42), Register.R2));
compiler.addComment("Initialize counter");
compiler.addLabel(new Label("loop_start"));
```

### Stack Management
**Always** update `RegisterManager` when manipulating stack:
```java
compiler.getRegisterManager().empiler();    // Before PUSH
compiler.getRegisterManager().depiler();    // After POP
compiler.getRegisterManager().ajouterVariablesLocales(n);  // For local vars
```

### Error Handling
- `ContextualError` - Type/scope errors during verification
- `DecacFatalError` - Compilation failures
- `DecacInternalError` - Invariant violations (asserts)

Runtime errors use predefined labels: `stack_overflow_error`, `io_error`, etc.

### Testing Patterns
See `src/test/java/fr/ensimag/deca/codegen/TestBooleanOpsCodeGen.java`:
1. Create `DecacCompiler` with `CompilerOptions`
2. Build AST manually (e.g., `new BooleanLiteral(true)`)
3. Call `verifyProgram()` then `codeGenProgram()`
4. Assert on `displayIMAProgram()` output

Mock context using Mockito for unit tests.

## Project-Specific Quirks

1. **TSTO/ADDSP inserted in reverse order**: Use `addFirstInstruction()` to prepend header instructions
2. **Short-circuit evaluation NOT implemented**: `&&` and `||` always evaluate both operands
3. **No register graph coloring**: Simple stack-based allocation with explicit spill logic
4. **Label counters are static**: Thread-safe by design (single-threaded compilation)
5. **R0 is special**: Used as scratch for spills and method return values
6. **Global variables use GB register**: Indexed from 1, tracked by `nbGlobales` in `RegisterManager`

## Documentation
- Full architecture: [docs/suivi3/architecture.md](docs/suivi3/architecture.md)
- OOP implementation plan: [etape_c_objet.md](etape_c_objet.md)
- Docker environment: [docker/README.md](docker/README.md)
