# Projet Génie Logiciel, Ensimag, Deca Compiler

gl51, 01/01/2026.


Decac is a compiler for the Deca language, developed as part of the "Projet Génie Logiciel" at Ensimag. It targets the IMA (Interactive Machine Abstraite) assembly language.


## Requirements

- **Java JDK 21** (or higher)
- **Maven 3.6.3** (or higher)
- **IMA (Interactive Machine Abstraite)**: Required to run the generated assembly code. Binaries for different architectures are located in `global/bin`.

## Setup & Build

To compile the project and generate the compiler:

```bash
mvn compile
```

To package the project (creates the `target/package` directory with a standalone version):

```bash
mvn package
```

This will also generate the necessary classpath information in `target/generated-sources/classpath.txt`, which is used by the `decac` wrapper script.

## Usage

The main entry point is the `src/main/bin/decac` script.

### Basic Compilation

```bash
./src/main/bin/decac <file.deca>
```
This produces a `<file.ass>` assembly file.

### Running the Generated Code

Use the `ima` tool to execute the assembly:

```bash
ima <file.ass>
```

### Compiler Options

```text
Usage: decac [[-p | -v] [-n] [-r X] [-d]* [-P] <fichier.deca>...] | [-b]

Options:
  -b        Display team banner (must be the only option)
  -p        Stop after parsing (decompiles the AST)
  -v        Stop after verification (contextual analysis)
  -n        Disable runtime error checks (optimization)
  -r X      Limit number of available registers (4 <= X <= 16)
  -d        Enable debug mode (repeatable for more verbosity)
  -P        Enable parallel compilation of multiple source files
```

## Project Structure

- `src/main/java`: Java source code for the compiler.
- `src/main/antlr4`: ANTLR4 grammar files for the Deca language.
- `src/main/bin`: Wrapper scripts (e.g., `decac`).
- `src/main/resources`: Configuration files (log4j, jacoco) and standard libraries/includes.
- `src/test/java`: JUnit 5 tests.
- `src/test/deca`: Deca source files for integration tests.
- `src/test/script`: Shell scripts for automated testing of different compiler stages.
- `global/bin`: Binaries for the IMA virtual machine.
- `docs/`: Project documentation and progress reports.
- `examples/`: Example projects using similar technologies.

## Testing

Run all tests (JUnit and integration scripts) using Maven:

```bash
mvn test
```

### Integration Test Scripts
Individual stages can be tested using scripts in `src/test/script/`:
- `basic-lex.sh`: Lexical analysis testing.
- `basic-synt.sh`: Syntactic analysis testing.
- `basic-context.sh`: Contextual analysis testing.
- `basic-gencode.sh`: Code generation testing.
- `basic-decac.sh`: Full compiler integration tests.
- `common-tests.sh`: Set of common regression tests.

## Scripts

### Performance Scoring
The `score_perf.sh` script calculates a performance score by running the compiler on provided performance tests and measuring cycle counts using `ima -s`.

```bash
./score_perf.sh
```

## License

Copyright © 2026 Ensimag.
