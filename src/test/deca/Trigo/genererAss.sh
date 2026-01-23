#!/bin/bash


DECAC="./src/main/bin/decac"
TEST_LIST="src/test/deca/Trigo/mes-tests-trigo.txt"

echo "--- Generation des .ass ---"

while IFS=';' read -r nom fonction valeur resultat
do
    [[ -z "$nom" ]] || [[ "$nom" == \#* ]] && continue
    
    if [[ "$resultat" == "erreur" ]]; then
        dossier="invalid"
    else
        dossier="valid"
    fi
    
    target="src/test/deca/Trigo/$dossier/$nom.deca"
    
    echo "Compilation : $nom"
    $DECAC "$target"             #la ligne de commande
    if [ $? -ne 0 ]; then
        echo "ERREUR dans $target"
        exit 1
    fi

    # Gestion du binome ULP pour les tests valides(pour invalid n'existe pas )
    if [ "$dossier" == "valid" ]; then
        ulp_target="src/test/deca/Trigo/valid/${nom}_ulp.deca"
        if [ -f "$ulp_target" ]; then
            $DECAC "$ulp_target"
        fi
    fi

done < "$TEST_LIST"

echo "--- Termine"