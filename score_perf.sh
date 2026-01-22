#!/bin/bash

# --- Configuration ---
# Chemin vers notre compilateur
DECAC="./src/main/bin/decac"
# Dossier contenant les tests de performance fournis
PERF_DIR="src/test/deca/codegen/perf/provided"
# Liste des fichiers tests
TESTS=("ln2.deca" "ln2_fct.deca" "syracuse42.deca")

# --- Valeurs par défaut (gl00 - VieuxCompDeProf) ---
# Utilisez ces valeurs si votre compilateur échoue sur un test
DEFAULT_LN2=17000      # Valeur approximative à ajuster selon le wiki
DEFAULT_LN2_FCT=50000  # Valeur indiquée dans ton énoncé
DEFAULT_SYRACUSE=30000 # Valeur approximative à ajuster selon le wiki

# Initialisation du score total
TOTAL_SCORE=0

echo "=========================================================="
echo "   CALCUL DU SCORE DE PERFORMANCE (GL 2026)"
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
        # On capture la sortie et on cherche la ligne "Temps d'execution"
        OUTPUT=$(ima -s "$ass_file")
        CYCLES=$(echo "$OUTPUT" | grep "Temps d'execution" | grep -oE '[0-9]+$')

        if [ -z "$CYCLES" ]; then
            echo "Erreur lors de l'exécution ima."
        else
            echo "SUCCÈS -> $CYCLES cycles"
            TOTAL_SCORE=$((TOTAL_SCORE + CYCLES))
        fi
    else
        # 3. Gestion de l'échec (Fallback sur les valeurs par défaut)
        echo "ÉCHEC DE COMPILATION"
        echo "   -> Utilisation de la valeur par défaut (gl00)"

        case $test_file in
            "ln2.deca")
                CYCLES=$DEFAULT_LN2
                ;;
            "ln2_fct.deca")
                CYCLES=$DEFAULT_LN2_FCT
                ;;
            "syracuse42.deca")
                CYCLES=$DEFAULT_SYRACUSE
                ;;
        esac
        echo "   -> Ajout de $CYCLES cycles au score"
        TOTAL_SCORE=$((TOTAL_SCORE + CYCLES))
    fi
done

echo "=========================================================="
echo "   SCORE TOTAL À REPORTER SUR LE WIKI : $TOTAL_SCORE"
echo "=========================================================="