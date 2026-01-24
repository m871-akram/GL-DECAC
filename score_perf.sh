#!/bin/bash

# --- Configuration ---
# Chemin vers votre compilateur
DECAC="./src/main/bin/decac"
# Dossier contenant les tests de performance fournis
PERF_DIR="src/test/deca/codegen/perf/provided"
# Liste des fichiers tests
TESTS=("ln2.deca" "ln2_fct.deca" "syracuse42.deca")

# Initialisation
TOTAL_SCORE=0
IS_COMPLETE=true  # Indicateur pour savoir si le score est valide

echo "=========================================================="
echo "   CALCUL DU SCORE DE PERFORMANCE (VERSION STRICTE)"
echo "=========================================================="

# Vérification de l'exécutable decac
if [ ! -f "$DECAC" ]; then
    echo "Erreur : Le compilateur $DECAC est introuvable."
    echo "Veuillez lancer 'mvn package' ou vérifier le chemin."
    exit 1
fi

# Boucle sur les fichiers
for test_file in "${TESTS[@]}"; do
    full_path="$PERF_DIR/$test_file"
    ass_file="${full_path%.deca}.ass"

    echo -n "Test de $test_file ... "

    # 1. Compilation avec l'option -n (optimisation: pas de check)
    $DECAC -n "$full_path" > /dev/null 2>&1

    if [ $? -eq 0 ]; then
        # 2. Exécution avec ima -s pour récupérer les cycles
        # Note : On filtre sur "Nombre de instructions" ou la sortie standard de votre ima
        OUTPUT=$(ima -s "$ass_file")

        # Adaptation pour capturer le chiffre à la fin de la ligne pertinente
        # (J'ai remis le grep standard, vérifiez si votre ima sort "Temps d'execution" ou "Nombre de instructions")
        CYCLES=$(echo "$OUTPUT" | grep -iE "cycles|instructions" | grep -oE '[0-9]+' | tail -1)

        if [ -z "$CYCLES" ]; then
            echo "ERREUR (ima n'a pas retourné de cycles lisibles)."
            IS_COMPLETE=false
        else
            echo "SUCCÈS -> $CYCLES cycles"
            TOTAL_SCORE=$((TOTAL_SCORE + CYCLES))
        fi
    else
        # 3. Gestion de l'échec (Strict : Pas de défaut)
        echo "ÉCHEC DE COMPILATION"
        IS_COMPLETE=false
    fi
done

echo "=========================================================="
if [ "$IS_COMPLETE" = true ]; then
    echo "   SCORE TOTAL À REPORTER SUR LE WIKI : $TOTAL_SCORE"
else
    echo "   SCORE PARTIEL : $TOTAL_SCORE"
    echo "   ATTENTION : Ce score est incomplet car certains tests ont échoué."
    echo "   (Ne pas reporter ce score s'il manque des tests)"
fi
echo "=========================================================="