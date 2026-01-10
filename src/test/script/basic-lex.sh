#! /bin/sh

# Auteur : gl51
# Version initiale : 01/01/2026

# Base pour un script de test de la lexicographie.
# On teste un fichier valide et un fichier invalide.
# Il est conseillé de garder ce fichier tel quel, et de créer de
# nouveaux scripts (en s'inspirant si besoin de ceux fournis).

# Il faudrait améliorer ce script pour qu'il puisse lancer test_lex
# sur un grand nombre de fichiers à la suite.

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :


#################################

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

# /!\ test valide lexicalement, mais invalide pour l'étape A.
# test_lex peut au choix afficher les messages sur la sortie standard
# (1) ou sortie d'erreur (2). On redirige la sortie d'erreur sur la
# sortie standard pour accepter les deux (2>&1)

#################################
# Fonctions utilitaires
#################################

# Test lexical valide : aucune erreur ne doit être signalée
test_lex_valide () {
    fichier="$1"

    if test_lex "$fichier" 2>&1 | grep -q -e "$fichier:[0-9]"
    then
        echo "[ERREUR] Échec lexical inattendu pour $fichier"
        exit 1
    else
        echo "[OK] Lexicalement valide : $fichier"
    fi
}

# Test lexical invalide : une erreur doit être détectée
test_lex_invalide () {
    fichier="$1"

    if test_lex "$fichier" 2>&1 | grep -q -e "$fichier:[0-9]"
    then
        echo "[OK] Échec lexical attendu pour $fichier"
    else
        echo "[ERREUR] Erreur lexicale non détectée pour $fichier"
        exit 1
    fi
}

#################################
# Tests lexicaux valides
#################################

echo "=== Tests lexicaux valides ==="

for cas_test in src/test/deca/lexical/valid/*.deca
do
    test_lex_valide "$cas_test"
done

#################################
# Tests lexicaux invalides
#################################

echo "=== Tests lexicaux invalides ==="

for cas_test in src/test/deca/lexical/invalid/*.deca
do
    test_lex_invalide "$cas_test"
done

echo "=== Tous les tests lexicaux sont réussi ==="

