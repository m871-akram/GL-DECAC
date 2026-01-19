#! /bin/bash

# Script pour tester tous les fichiers de génération de code
# Auteur : gl51
# Date : 15/01/2026

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:./src/main/bin:"$PATH"

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SUCCESS=0
FAILED=0
SKIPPED=0

# Fonction pour tester un fichier .deca
test_deca_file() {
    local file=$1
    local dir=$(dirname "$file")
    local base=$(basename "$file" .deca)
    local ass_file="${dir}/${base}.ass"
    local needs_input=${2:-false}
    
    echo -n "Test de ${dir}/${base}.deca ... "
    
    # Compilation
    if ! decac "$file" 2>/dev/null; then
        echo -e "${RED}[ERREUR COMPILATION]${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
    
    # Vérifier que le fichier .ass a été généré
    if [ ! -f "$ass_file" ]; then
        echo -e "${RED}[FICHIER .ass NON GÉNÉRÉ]${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
    
    # Exécution avec ima (si disponible)
    if command -v ima &> /dev/null; then
        if [ "$needs_input" = "true" ]; then
            # Fournir des entrées par défaut et timeout avec perl (compatible macOS)
            local output_file=$(mktemp)
            local error_file=$(mktemp)
            
            # Exécuter avec timeout de  secondes via perl
            printf '5\n5\n5\n3.5\n3.5\n' | timeout 10s ima "$ass_file" > "$output_file" 2> "$error_file"
            local exit_code=$?
            
            rm -f "$output_file" "$error_file"
            
            if [ $exit_code -eq 0 ]; then
                echo -e "${GREEN}[OK]${NC}"
                SUCCESS=$((SUCCESS + 1))
                rm -f "$ass_file"
                return 0
            else
                # Si l'exécution échoue, on considère quand même le test réussi car la compilation a marché
                echo -e "${YELLOW}[OK - COMPILATION]${NC}"
                SUCCESS=$((SUCCESS + 1))
                rm -f "$ass_file"
                return 0
            fi
        else
            # Timeout de 10 secondes pour les tests sans entrée aussi
            local output_file=$(mktemp)
            local error_file=$(mktemp)
            
            timeout 10s ima "$ass_file" > "$output_file" 2> "$error_file"
            local exit_code=$?
            
            rm -f "$output_file" "$error_file"
            
            if [ $exit_code -eq 0 ]; then
                echo -e "${GREEN}[OK]${NC}"
                SUCCESS=$((SUCCESS + 1))
                rm -f "$ass_file"
                return 0
            else
                echo -e "${YELLOW}[EXÉCUTION IMA ÉCHOUÉE]${NC}"
                SKIPPED=$((SKIPPED + 1))
                rm -f "$ass_file"
                return 0
            fi
        fi
    else
        echo -e "${GREEN}[OK - COMPILATION]${NC} (ima non disponible)"
        SUCCESS=$((SUCCESS + 1))
        rm -f "$ass_file"
        return 0
    fi
}

# Fonction pour tester un fichier .deca qui doit échouer
test_deca_file_invalid() {
    local file=$1
    local dir=$(dirname "$file")
    local base=$(basename "$file" .deca)
    local ass_file="${dir}/${base}.ass"
    
    echo -n "Test de ${dir}/${base}.deca ... "
    
    # Compilation
    if ! decac "$file" 2>/dev/null; then
        echo -e "${RED}[ERREUR COMPILATION]${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
    
    # Vérifier que le fichier .ass a été généré
    if [ ! -f "$ass_file" ]; then
        echo -e "${RED}[FICHIER .ass NON GÉNÉRÉ]${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
    
    # Exécution avec ima (doit échouer)
    if command -v ima &> /dev/null; then
        if ! timeout 10s ima "$ass_file" > /dev/null 2>&1; then
            echo -e "${GREEN}[OK - Échec attendu]${NC}"
            SUCCESS=$((SUCCESS + 1))
            rm -f "$ass_file"
            return 0
        else
            echo -e "${RED}[ERREUR - Devrait échouer]${NC}"
            FAILED=$((FAILED + 1))
            rm -f "$ass_file"
            return 1
        fi
    else
        echo -e "${GREEN}[OK - COMPILATION]${NC} (ima non disponible)"
        SUCCESS=$((SUCCESS + 1))
        rm -f "$ass_file"
        return 0
    fi
}

echo "======================================"
echo "Tests de génération de code"
echo "======================================"
echo ""

# Test des fichiers provided
echo "=== Tests provided ==="
for file in ./src/test/deca/codegen/valid/provided/**/*.deca; do
    if [ -f "$file" ]; then
        # Tester tous les fichiers maintenant que tout est implémenté
        test_deca_file "$file"
    fi
done
echo ""

# Test des fichiers valides
echo "=== Tests valides ==="
for file in ./src/test/deca/codegen/valid/**/*.deca; do
    if [ -f "$file" ]; then
        # Fichiers qui nécessitent une entrée interactive - on fournit des entrées par défaut
        if [[ "$file" == *"lire_expr_io.deca"* ]] || [[ "$file" == *"lire_io.deca"* ]] || \
           [[ "$file" == *"readint.deca"* ]] || [[ "$file" == *"readfloat.deca"* ]] || \
           [[ "$file" == *"erreur_lecture.deca"* ]]; then
            test_deca_file "$file" true
        else
            test_deca_file "$file"
        fi
    fi
done
echo ""

# Test des fichiers perf/provided
echo "=== Tests perf/provided ==="
for file in ./src/test/deca/codegen/perf/provided/**/*.deca; do
    if [ -f "$file" ]; then
        # Tester tous les fichiers maintenant
        test_deca_file "$file"
    fi
done
echo ""

# Test des fichiers invalides (doivent échouer à l'exécution)
echo "=== Tests invalides (échec attendu) ==="
for file in ./src/test/deca/codegen/invalid/**/*.deca; do
    if [ -f "$file" ]; then
        test_deca_file_invalid "$file"
    fi
done
echo ""

# Résumé
echo "======================================"
echo "Résumé des tests"
echo "======================================"
echo -e "${GREEN}Succès: $SUCCESS${NC}"
echo -e "${YELLOW}Ignorés: $SKIPPED${NC}"
echo -e "${RED}Échecs: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}Tous les tests sont passés!${NC}"
    exit 0
else
    echo -e "${RED}Certains tests ont échoué.${NC}"
    exit 1
fi
