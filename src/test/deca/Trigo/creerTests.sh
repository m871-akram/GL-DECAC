#!/bin/bash
# Script pour creer les fichiers de tests TRIGO(a partir du text mes-tests-trigo)

echo "CREATION DES FICHIERS TRIGO ~~~~~~~"
echo ""

mkdir -p "src/test/deca/Trigo/valid"
mkdir -p "src/test/deca/Trigo/invalid"
# Compteurs des fichiers
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
    

    # on determine le bon dossier(valid/invalid)
    if [[ "$resultat" == "NaN" ]]; then
        dossier="invalid"
        type_test="(invalid)"
    else
        dossier="valid"
        type_test="(valid)"
    fi
    

    # Fichier principal
    fichier="src/test/deca/Trigo/$dossier/$nom.deca"
    
    # On ecrit le code Deca
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

echo "    println(m.$fonction($valeur));" >> "$fichier"
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
    
done < "src/test/deca/Trigo/mes-tests-trigo.txt"


echo ""
echo "le nombre de tests dans le fichier: $total_fichiers"
echo "le nombre des fichiers .deca crees:  $fichiers_crees"
