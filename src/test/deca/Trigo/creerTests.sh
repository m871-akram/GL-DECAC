#!/bin/bash
# Script pour creer les fichiers de tests TRIGO

echo "***************** CREATION DES FICHIERS TRIGO ***************"
echo ""

mkdir -p "src/test/deca/Trigo/valid"
mkdir -p "src/test/deca/Trigo/invalid"
# Compteurs
total_fichiers=0
fichiers_crees=0

# fichier de tests
while IFS=';' read -r nom fonction valeur resultat
do
    # Ignorer les lignes vides ou commentaires
    if [[ -z "$nom" ]] || [[ "$nom" == \#* ]]; then
        continue
    fi
    
    total_fichiers=$((total_fichiers + 1))
    

    # Determiner le dossier
    if [[ "$resultat" == "NaN" ]]; then
        dossier="invalid"
        type_test="(invalid)"
    else
        dossier="valid"
        type_test="(valid)"
    fi
    

    # Fichier principal
    fichier="src/test/deca/Trigo/$dossier/$nom.deca"
    
    # On ecrit le code Deca avec en-tête
    cat > "$fichier" << EOF
// Description:
//    Test TRIGO: $fonction($valeur)
//
// Resultats attendus: $resultat
//
// Historique:
//    cree le $(date +%d/%m/%Y)

#include "Math.decah"
{
    Math m = new Math();
EOF
        
if [[ "$fonction" == *"("* ]] && [[ "$fonction" == *")"* ]]; then
    # Expression complexe
    echo "    println($fonction);" >> "$fichier"
else
    # Fonction simple
    echo "    println(m.$fonction($valeur));" >> "$fichier"
fi

echo "}" >> "$fichier"

echo "$nom.deca $type_test cree: $fichier"
fichiers_crees=$((fichiers_crees + 1))
    
    # Fichier ULP (est fait seulement pour les tests valides)
    if [[ "$resultat" != "NaN" ]]; then
        fichier_ulp="src/test/deca/Trigo/$dossier/${nom}_ulp.deca"
        
        cat > "$fichier_ulp" << EOF
// Description:
//    Calcul ULP pour: $valeur
//
// Resultats attendus: ulp($valeur)
//
// Historique:
//    cree le $(date +%d/%m/%Y)

#include "Math.decah"
{
    Math m = new Math();
    println(m.ulp($valeur));
}
EOF


echo "${nom}_ulp.deca cree: $fichier_ulp"
fichiers_crees=$((fichiers_crees + 1))
fi
    
done < "src/test/deca/Trigo/TestsSin.txt"

# un Resume
echo ""
echo "***************** RESUME *******************"
echo "Tests dans le fichier: $total_fichiers"
echo "Fichiers .deca crees:  $fichiers_crees"
