# Deca Compiler

![CI](https://github.com/m871-akram/GL-DECAC/actions/workflows/ci.yml/badge.svg)

Compiler for **Deca** — a statically-typed, object-oriented subset of Java — targeting the
IMA abstract machine. Built by team gl51 for the *Projet Génie Logiciel* at Ensimag (2026)
on the official course skeleton; the three compiler passes, the test suite and the math
extension are ours.

## Quick start

Needs JDK 21+, Maven 3.6+, and Ensimag's `ima` VM (not redistributable — on a personal
machine, use the [Docker image](#docker)). On Ensimag machines:

```bash
ln -s /matieres/3MM1PGL/global global && export PATH="$PWD/global/bin:$PATH"
mvn package
./src/main/bin/decac hello.deca
ima hello.ass
```

### decac flags

| Flag | Effect |
|------|--------|
| `-b` | Print the team banner |
| `-p` | Stop after parsing, decompile the AST |
| `-v` | Stop after contextual verification |
| `-n` | Disable runtime checks (divide-by-zero, null deref, …) |
| `-r X` | Limit general-purpose registers to X (4–16) |
| `-d` … `-dddd` | Debug logging, increasing verbosity |
| `-P` | Compile several files in parallel |

`decac -r 4 file.deca` is the register-allocation stress test.

## The language

Primitive types (`int`, `float`, `boolean`), classes with single inheritance, `this`,
`new`, `instanceof`, casts, `if/else`, `while`, console I/O, `#include`, and inline
`asm("...")`.

```deca
class Circle {
    protected float radius;
    float area() { return 3.14159 * radius * radius; }
}

{
    Circle c = new Circle();
    println(c.area());
}
```

## Architecture

```
Source (.deca)
    │
    ▼  Pass 1 — lexing & parsing      ANTLR4 grammar → AST          fr.ensimag.deca.syntax
    ▼  Pass 2 — verification          types, scopes, methods        fr.ensimag.deca.context / .tree
    ▼  Pass 3 — code generation       IMA emission, registers,      fr.ensimag.deca.codegen /
    │                                 VTables                        fr.ensimag.ima.pseudocode
    ▼
Assembly (.ass)
```

| Package | Role |
|---------|------|
| `fr.ensimag.deca` | Entry point, CLI options, orchestration |
| `fr.ensimag.deca.syntax` | ANTLR4 lexer/parser, `#include` |
| `fr.ensimag.deca.tree` | AST nodes with verify & codegen visitors |
| `fr.ensimag.deca.context` | Type system, environments, definitions |
| `fr.ensimag.deca.codegen` | Register manager, memory unit, runtime errors |
| `fr.ensimag.ima.pseudocode` | IMA instruction model |

## Tests

916 Deca integration tests plus JUnit suites. `mvn test` runs everything (the shell
suites need `ima`); CI runs the JUnit layer alone (`mvn test -Dexec.skip=true`).
Stage-by-stage runners live in `src/test/script/` (`basic-lex.sh` → `common-tests.sh`).

Coverage (JaCoCo, JUnit layer): **~81% instructions / ~70% branches** —
`mvn test -Djacoco.skip=false && src/test/script/jacoco-report.sh`.

## Extension: a math library in pure Deca

`src/main/resources/include/Math.decah` implements `sin`, `cos`, `asin`, `acos`, `atan`
and `ulp` **entirely in Deca** — no standard library, no bit operations, float32 only:

- **CORDIC** — rotation mode (24 iterations) for `sin`/`cos`, vectoring mode (30) for
  `atan`, driven by a pre-generated `atan(2^-i)` table;
- **128-node table + Taylor-corrected interpolation** (step π/256) for small angles;
- identity-based range reduction, `asin`/`acos` via `atan(x/√(1−x²))` with a
  7-iteration Newton square root, and `ulp(f)` reconstructed by exponent search.

Measured accuracy (float32-faithful simulation of the algorithm and its tables,
20 001-point sweeps; reproduce on ima with `src/test/deca/Trigo/verify_valid.sh`):

| Function | Domain | Max abs. error | ≈ ULP (float32, at \|f(x)\| ≈ 1) |
|---|---|---|---|
| `sin` | [−2π, 2π] | 6.3 × 10⁻⁷ | ~5 |
| `cos` | [−2π, 2π] | 7.8 × 10⁻⁷ | ~7 |
| `atan` | [−10, 10] | 2.0 × 10⁻⁷ | ~2 |
| `sin` | [−100, 100] | 6.4 × 10⁻⁶ | ~53 (range-reduction limit) |

## Performance

`src/test/score_perf.sh` compiles the three provided benchmark programs with `-n` and
sums their IMA cycle counts (`ima -s`).

<!-- Fill after running score_perf.sh in an ima environment:
| Program | IMA cycles (decac -n) |
|---|---|
| `ln2.deca` | … |
| `ln2_fct.deca` | … |
| `syracuse42.deca` | … |
| **Total (wiki score)** | **…** |
-->

## Docker

For a personal machine (bundles `ima`):

```bash
docker login gitlab.ensimag.fr:5050
docker create -it -v "$PWD":/home/gl/projet_gl --name projetgl \
  gitlab.ensimag.fr:5050/reigniep/dockergl
docker start -a -i projetgl
```

---

Course skeleton © Ensimag; compiler passes, tests and extensions by team gl51.
