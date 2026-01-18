# Instructions IMA Inutilisées et Optimisations

Ce document répertorie les instructions IMA définies dans `fr.ensimag.ima.pseudocode.instructions` qui ne sont pas actuellement utilisées dans le répertoire `src/main/java/fr/ensimag/deca`. Il propose également des manières d'employer ces instructions pour optimiser ou simplifier le code généré.

## Liste des Instructions Inutilisées

Les instructions suivantes ont été identifiées comme n'ayant aucun usage dans le code source de la partie `deca` :

- **Branchements conditionnels :** `BGE`, `BGT`, `BLE`, `BLT`
- **Arithmétique et calcul :** `FMA`, `SHL`, `SHR`
- **Gestion de la pile et adresses :** `PEA`
- **Contrôle et arrondis :** `SETROUND_DOWNWARD`, `SETROUND_TONEAREST`, `SETROUND_TOWARDZERO`, `SETROUND_UPWARD`, `SOV`
- **Entrées/Sorties :** `WFLOATX`
- **Gestion mémoire (non implémentée) :** `DEL`

---

## Propositions d'Optimisation

### 1. Branchements Conditionnels Directs
**Instructions :** `BGE`, `BGT`, `BLE`, `BLT`

Actuellement, pour les structures de contrôle (`if`, `while`), le compilateur utilise une approche en deux étapes : transformer la comparaison en un booléen (0 ou 1) avec `Sxx`, puis comparer ce booléen à 0.

**Code actuel (approximatif) pour `if (a < b)` :**
```ima
    LOAD a, R2
    CMP b, R2      ; Compare R2 - b
    SLT R2         ; R2 = 1 si R2 < b, sinon 0
    CMP #0, R2     ; Comparaison avec faux
    BNE label_vrai ; Saute si vrai
```

**Optimisation avec `BLT` :**
On peut sauter directement après la comparaison initiale.
```ima
    LOAD a, R2
    CMP b, R2
    BLT label_vrai ; Plus court et plus rapide
```

### 2. Décalages Binaires pour les Puissances de 2
**Instructions :** `SHL`, `SHR`

Pour multiplier ou diviser un entier par une puissance de 2, il est plus efficace d'utiliser des décalages binaires plutôt que l'instruction `MUL` ou `DIV` matérielle.

**Code actuel pour `x * 2` :**
```ima
    LOAD x, R2
    MUL #2, R2
```

**Optimisation avec `SHL` :**
```ima
    LOAD x, R2
    SHL R2
```

### 3. Empilement d'Adresses Efficace
**Instruction :** `PEA` (Push Effective Address)

Lors du passage de paramètres ou de la manipulation d'adresses (par exemple pour les tables des méthodes ou les objets), on peut éviter de passer par un registre intermédiaire.

**Code actuel :**
```ima
    LEA 1(GB), R1
    PUSH R1
```

**Optimisation avec `PEA` :**
```ima
    PEA 1(GB)
```

### 4. Calculs Arithmétiques Combinés
**Instruction :** `FMA` (Fused Multiply-Add)

Si l'architecture IMA supporte l'opération `a * b + c` via `FMA`, cela peut être utilisé pour optimiser les expressions complexes.

**Optimisation :**
Remplacer une séquence `MUL` suivie d'un `ADD` par un unique `FMA` lorsque c'est possible.

### 5. Affichage pour le Débogage
**Instruction :** `WFLOATX`

Cette instruction permet d'afficher des flottants en format hexadécimal, ce qui est crucial pour déboguer des problèmes de précision ou de représentation binaire sans les arrondis de l'affichage décimal standard.

**Usage :**
Ajouter une option au compilateur (ex: `-v` ou `-debug`) pour utiliser `WFLOATX` au lieu de `WFLOAT`.

### 6. Gestion des Arrondis
**Instructions :** `SETROUND_*`

Pour les calculs scientifiques nécessitant un contrôle strict de l'erreur numérique, on pourrait implémenter en Deca des fonctions prédéfinies permettant de changer le mode d'arrondi du processeur IMA.

---

## Conclusion

L'intégration de ces instructions permettrait de réduire la taille du code généré (moins d'instructions `PUSH/POP` et de sauts intermédiaires) et d'améliorer les performances d'exécution des programmes Deca compilés.
