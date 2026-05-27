# Deca Compiler

A compiler for the **Deca** language targeting the IMA (Interactive Machine Abstraite) virtual machine, developed as part of the *Projet Génie Logiciel* at Ensimag (gl51, 2026).

Deca is a statically-typed, object-oriented language with Java-like syntax. This compiler translates `.deca` source files into IMA assembly (`.ass`), which can be executed by the `ima` virtual machine.

---

## Table of Contents

- [Quick Start](#quick-start)
- [Requirements](#requirements)
- [IMA Setup](#ima-setup)
- [Build](#build)
- [Usage](#usage)
- [Deca Language](#deca-language)
- [Compiler Architecture](#compiler-architecture)
- [Testing](#testing)
- [Coverage](#coverage)
- [Performance](#performance)
- [Docker](#docker)
- [License](#license)

---

## Quick Start

```bash
# 1. Set up IMA (Ensimag machines only)
ln -s /matieres/3MM1PGL/global global
export PATH="$PWD/global/bin:$PATH"

# 2. Build
mvn package

# 3. Compile and run a Deca program
./src/main/bin/decac hello.deca
ima hello.ass
```

---

## Requirements

| Tool | Version |
|------|---------|
| Java JDK | 21+ |
| Maven | 3.6.3+ |
| IMA virtual machine | provided by Ensimag (see below) |

---

## IMA Setup

The `ima` binary is not bundled in this repository. On **Ensimag machines**, it is available on the network filesystem.

**Option 1 — symlink (recommended):** makes `decac` find `ima` automatically via `global/bin/`

```bash
ln -s /matieres/3MM1PGL/global global
```

**Option 2 — add to PATH only:**

```bash
export PATH="/matieres/3MM1PGL/global/bin:$PATH"
```

To make the PATH change permanent, add the export line to your `~/.bashrc`.

**On a personal machine:** use the [Docker image](#docker), which bundles `ima` from the Ensimag sources.

---

## Build

```bash
# Compile source only
mvn compile

# Full build: compile + run tests + produce standalone package
mvn package

# Skip tests for a faster build
mvn package -DskipTests
```

`mvn package` produces:
- `target/Deca-0.0.1-jar-with-dependencies.jar` — standalone executable jar
- `target/package/` — directory with the `decac` wrapper script and jar

The classpath used by the `decac` script is written to `target/generated-sources/classpath.txt` automatically during the build.

---

## Usage

### Compile a Deca file

```bash
./src/main/bin/decac <file.deca>
```

Produces `<file.ass>` in the same directory as the source file.

### Run the generated assembly

```bash
ima <file.ass>
```

### Compile multiple files in parallel

```bash
./src/main/bin/decac -P file1.deca file2.deca file3.deca
```

### Compiler flags

| Flag | Description |
|------|-------------|
| `-b` | Print team banner (must be the only argument) |
| `-p` | Stop after parsing and decompile the AST to stdout |
| `-v` | Stop after contextual verification (no code generated) |
| `-n` | Disable runtime error checks (divide-by-zero, null deref, etc.) |
| `-r X` | Limit general-purpose registers to X (4 ≤ X ≤ 16, default 16) |
| `-d` | Enable debug logging (repeat up to 4× for more verbosity: `-d`, `-dd`, `-ddd`, `-dddd`) |
| `-P` | Compile multiple source files in parallel |

### Examples

```bash
# Inspect the AST of a program
./src/main/bin/decac -p src/test/deca/codegen/valid/method.deca

# Compile with minimal registers (stress-tests register allocation)
./src/main/bin/decac -r 4 src/test/deca/codegen/valid/method.deca

# Compile without runtime checks (faster generated code)
./src/main/bin/decac -n src/test/deca/codegen/valid/method.deca
```

---

## Deca Language

Deca is a subset of Java with:
- Primitive types: `int`, `float`, `boolean`
- Classes with single inheritance, fields, and methods
- `this`, `new`, `instanceof`, casts
- Control flow: `if/else`, `while`
- I/O: `print`, `println`, `readInt`, `readFloat`
- `#include` for splitting programs across files
- Inline assembly via `asm("...")`

### Example

```deca
class Total {
    int somme(int a, int b, int c) {
        return a + b + c;
    }
}

{
    Total t = new Total();
    println(t.somme(1, 2, 3));   // prints: 6
}
```

```deca
class Shape {
    protected float area() { return 0.0; }
}

class Circle extends Shape {
    protected float radius;

    float area() {
        return 3.14159 * this.radius * this.radius;
    }
}

{
    Shape s = new Circle();
    println(s.area());
}
```

---

## Compiler Architecture

The compiler runs in three sequential passes:

```
Source (.deca)
    │
    ▼
┌─────────────────────────────┐
│  Pass 1 – Lexing & Parsing  │  ANTLR4 grammar → AST
│  fr.ensimag.deca.syntax     │  DecaLexer, DecaParser
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│  Pass 2 – Verification      │  Type checking, scopes,
│  fr.ensimag.deca.context    │  method resolution
│  fr.ensimag.deca.tree       │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│  Pass 3 – Code Generation   │  IMA instruction emission,
│  fr.ensimag.deca.tree       │  register allocation,
│  fr.ensimag.deca.codegen    │  VTable construction
│  fr.ensimag.ima.pseudocode  │
└─────────────┬───────────────┘
              │
              ▼
        Assembly (.ass)
```

### Key packages

| Package | Role |
|---------|------|
| `fr.ensimag.deca` | Entry point (`DecacMain`), CLI options, compiler orchestration |
| `fr.ensimag.deca.syntax` | ANTLR4 lexer/parser, `#include` handling |
| `fr.ensimag.deca.tree` | AST node classes, verify and codegen visitors |
| `fr.ensimag.deca.context` | Type system, environment, symbol definitions |
| `fr.ensimag.deca.codegen` | Register manager, memory unit, interrupt controller |
| `fr.ensimag.ima.pseudocode` | IMA instruction model and program builder |
| `fr.ensimag.ima.pseudocode.instructions` | Concrete IMA instructions (LOAD, STORE, ADD, …) |

---

## Testing

### Run all tests

```bash
mvn test
```

Runs JUnit tests and all integration shell scripts. Requires `ima` on PATH for `common-tests.sh`.

### Individual stage scripts

Make the scripts executable first if needed:

```bash
chmod +x src/test/script/*.sh src/test/deca/Trigo/*.sh src/test/score_perf.sh
```

| Script | What it tests |
|--------|--------------|
| `src/test/script/basic-lex.sh` | Lexer only |
| `src/test/script/basic-synt.sh` | Parser only |
| `src/test/script/basic-context.sh` | Contextual analysis |
| `src/test/script/basic-gencode.sh` | Code generation |
| `src/test/script/basic-decac.sh` | Full compiler (`-b` banner check) |
| `src/test/script/common-tests.sh` | End-to-end: compile + run with `ima` |

### Test layout

```
src/test/
├── java/               # JUnit 5 unit and integration tests
├── deca/
│   ├── codegen/        # Code generation tests (valid/ and invalid/)
│   ├── context/        # Contextual analysis tests
│   ├── syntax/         # Syntax tests
│   ├── lexical/        # Lexer tests
│   ├── Trigo/          # Trigonometric extension tests
│   └── demo/           # End-to-end demo programs
└── script/             # Shell-based integration test runners
```

---

## Coverage

JaCoCo is integrated but disabled by default. To generate a coverage report:

```bash
# Run tests with instrumentation
mvn test -Djacoco.skip=false

# Generate the HTML report
src/test/script/jacoco-report.sh

# Open in browser
xdg-open target/site/jacoco/index.html
```

> **Note:** Only tests running in-process (JUnit via Surefire) contribute to coverage. Shell-based integration tests launch a separate JVM and are not measured.

Current coverage: **~81% instructions / ~70% branches** across the compiler.

---

## Performance

The `score_perf.sh` script benchmarks the compiler on dedicated performance tests and measures IMA cycle counts:

```bash
src/test/score_perf.sh
```

Requires `ima` with the `-s` flag (cycle counting) to be on PATH.

---

## Docker

For development on a personal machine without a native Ensimag environment:

### Using the pre-built Ensimag image

```bash
docker login gitlab.ensimag.fr:5050

docker create --interactive --tty \
  -v <absolute-path-to-GL-DECAC>:/home/gl/projet_gl \
  --name projetgl \
  gitlab.ensimag.fr:5050/reigniep/dockergl

docker start -a -i projetgl
```

### Building the image locally

First copy `ima_sources.tgz` from an Ensimag machine:

```bash
scp <login>@ensimag-machine:/matieres/4MMPGL/GL/global/ima_sources.tgz docker/
```

Then build and run:

```bash
docker build -t projetgl docker/
docker create --interactive --tty \
  -v <absolute-path-to-GL-DECAC>:/home/gl/projet_gl \
  --name projetgl projetgl
docker start -a -i projetgl
```

The container uses user `gl` (password: `gl`) with sudo access. Your project directory is mounted, so changes are visible both inside and outside the container.

VS Code users: see the [Dev Containers documentation](https://code.visualstudio.com/docs/devcontainers/containers) to develop directly inside the container.

---

## License

Copyright © 2026 Ensimag. Private project — distribution not permitted.
