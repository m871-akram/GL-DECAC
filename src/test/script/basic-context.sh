#! /bin/sh

# Auteur : gl51
# Version initiale : 01/01/2026

# Test minimaliste de la vérification contextuelle.
# Le principe et les limitations sont les mêmes que pour basic-synt.sh

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"



#################################
# Fonctions utilitaires
#################################

# Test contextuel valide : aucune erreur ne doit être signalée
test_context_valide () {
    fichier="$1"

    if test_context "$fichier" 2>&1 | grep -q -e "$fichier:[0-9][0-9]*"
    then
        echo "[ERREUR] Échec contextuel inattendu pour $fichier"
        exit 1
    else
        echo "[OK] Contextuellement valide : $fichier"
    fi
}

# Test contextuel invalide : une erreur doit être détectée
test_context_invalide () {
    fichier="$1"

    if test_context "$fichier" 2>&1 | grep -q -e "$fichier:[0-9][0-9]*"
    then
        echo "[OK] Échec contextuel attendu pour $fichier"
    else
        echo "[ERREUR] Erreur contextuel non détectée pour $fichier"
        exit 1
    fi
}


#################################
# Tests Contextuels valides
#################################

echo "=== Tests Contextuels valides ==="

for cas_test in src/test/deca/context/valid/*.deca
do
    test_context_valide "$cas_test"
done

#################################
# Tests Contextuels invalides
#################################

echo "=== Tests Contextuels invalides ==="

for cas_test in src/test/deca/context/invalid/*.deca
do
    test_context_invalide "$cas_test"
done

echo "=== Tous les tests Contextuels sont réussi ==="



