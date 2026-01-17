#! /bin/sh

# Auteur : gl51
# Version initiale : 01/01/2026

# Encore un test simpliste. On compile un fichier (cond0.deca), on
# lance ima dessus, et on compare le résultat avec la valeur attendue.

# Ce genre d'approche est bien sûr généralisable, en conservant le
# résultat attendu dans un fichier pour chaque fichier source.


# On ne teste qu'un fichier. Avec une boucle for appropriée, on
# pourrait faire bien mieux ...

# Tests de génération de code : valid / invalid / perf

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:./src/main/bin:"$PATH"

BASE=src/test/deca/codegen

#######################################
# VALID
#######################################
echo "=== Tests gencode VALID ==="

find src/test/deca/codegen/valid -name "*.deca" | while read f
do
    dir=$(dirname "$f")
    base=$(basename "$f" .deca)
    ass="$dir/$base.ass"
    out="$dir/$base.out"

    echo "--- $f ---"

    rm -f "$ass"

    if ! decac "$f"; then
        echo "[ERREUR] Compilation échouée"
        exit 1
    fi

    if [ ! -f "$ass" ]; then
        echo "[ERREUR] Assembleur non généré"
        exit 1
    fi

    if [ ! -f "$out" ]; then
        echo "[ERREUR] Fichier oracle manquant : $out"
        exit 1
    fi

    resultat=$(ima "$ass") || exit 1
    rm -f "$ass"

    if [ "$resultat" = "$(cat "$out")" ]; then
        echo "[OK]"
    else
        echo "[ERREUR] Résultat incorrect"
        exit 1
    fi
done



#######################################
# INVALID
#######################################
echo "=== Tests gencode INVALID ==="

find src/test/deca/codegen/invalid -name "*.deca" | while read f
do
    echo "--- $f ---"

    if decac "$f"; then
        echo "[ERREUR] Compilation réussie (devait échouer)"
        exit 1
    else
        echo "[OK]"
    fi
done

#######################################
# PERF
#######################################
echo "=== Tests gencode PERF ==="

find src/test/deca/codegen/perf -name "*.deca" | while read f
do
    dir=$(dirname "$f")
    base=$(basename "$f" .deca)
    ass="$dir/$base.ass"

    echo "--- $f ---"

    rm -f "$ass"

    if ! decac "$f"; then
        echo "[ERREUR] Compilation échouée"
        exit 1
    fi

    if [ ! -f "$ass" ]; then
        echo "[ERREUR] Assembleur non généré"
        exit 1
    fi

    rm -f "$ass"
    echo "[OK]"
done


echo "=== Tous les tests gencode sont passés ==="




