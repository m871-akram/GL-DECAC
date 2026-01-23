#!/bin/bash
# Script pour supprimer les fichiers de tests Trigo avec les rep valid et invalid

echo "suppression des fichiers trigo .deca et .ass **"
echo ""

DIR="src/test/deca/Trigo"

if [ ! -d "$DIR" ]; then
    echo "Dossier $DIR n'existe pas"
    exit 0
fi

total=0

while read -r fichier; do
    echo "  Supprime: $(basename "$fichier")"
    rm -f "$fichier"  
    total=$((total + 1))
done < <(find "$DIR" \( -name "*.deca" -o -name "*.ass" -o -name "*.expected" \) -type f)

echo ""
echo "$total fichier(s) supprime(s)"
echo ""

# suppresion des des dossiers vides
find "$DIR" -type d -empty -delete 2>/dev/null
echo "Dossiers vides supprimes"