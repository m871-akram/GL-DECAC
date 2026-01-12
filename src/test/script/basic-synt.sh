#! /bin/sh

# Auteur : gl51
# Version initiale : 01/01/2026

# Test minimaliste de la syntaxe.
# On lance test_synt sur un fichier valide, et les tests invalides.

# dans le cas du fichier valide, on teste seulement qu'il n'y a pas eu
# d'erreur. Il faudrait tester que l'arbre donné est bien le bon. Par
# exemple, en stoquant la valeur attendue quelque part, et en
# utilisant la commande unix "diff".
#
# Il faudrait aussi lancer ces tests sur tous les fichiers deca
# automatiquement. Un exemple d'automatisation est donné avec une
# boucle for sur les tests invalides, il faut aller encore plus loin.

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"


############################
# Fonctions utilitaires
############################

# Test syntaxique invalide : erreur est attendue
test_synt_invalide () {
    fichier="$1"

    if test_synt "$fichier" 2>&1 | grep -q -e "$fichier:[0-9][0-9]*:"
    then
        echo "[OK] Échec attendu pour $fichier"
    else
        echo "[ERREUR] Succès inattendu pour $fichier"
        exit 1
    fi
}

# Test syntaxique valide : aucune erreur ne doit apparaître
test_synt_valide () {
    fichier="$1"

    if test_synt "$fichier" 2>&1 | grep -q -e ':[0-9][0-9]*:'
    then
        echo "[ERREUR] Échec inattendu pour $fichier"
        exit 1
    else
        echo "[OK] Succès attendu pour $fichier"
    fi
}


############################
# Tests valides
############################

echo "=== Tests syntaxiques valides ==="

for cas_test in src/test/deca/syntax/valid/**/*.deca
do
    test_synt_valide "$cas_test"
done

echo "=== Tous les tests syntaxiques sont réussi ==="

############################
# Tests invalides
############################

echo "=== Tests syntaxiques invalides ==="

for cas_test in src/test/deca/syntax/invalid/**/*.deca
do
    test_synt_invalide "$cas_test"
done
