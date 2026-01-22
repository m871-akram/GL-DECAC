#!/bin/bash

echo "**** CALCUL ERREUR EN ULP (avec awk) *******"
echo ""

for fichier in src/test/deca/Trigo/valid/*.ass; do
    # Ignorer les fichiers _ulp
    [[ "$fichier" == *"_ulp.ass" ]] && continue
    
    nom_test=$(basename "$fichier" .ass)
    
    # Chercher résultat attendu
    ligne=$(grep "^$nom_test;" "src/test/deca/Trigo/TestsAtan.txt" 2>/dev/null)
    [ -z "$ligne" ] && continue
    
    resultat_attendu=$(echo "$ligne" | cut -d';' -f4)
    [ "$resultat_attendu" = "erreur" ] && continue
    
    # Exécuter test principal
    sortie_test=$(ima "$fichier" 2>&1)
    resultat_obtenu=$(echo "$sortie_test" | awk 'END{print $NF}' | tr -d '\r')
    
    # Exécuter test ULP
    fichier_ulp="src/test/deca/Trigo/valid/${nom_test}_ulp.ass"
    [ ! -f "$fichier_ulp" ] && continue
    
    sortie_ulp=$(ima "$fichier_ulp" 2>&1)
    ulp_value=$(echo "$sortie_ulp" | awk 'END{print $NF}' | tr -d '\r')
    
    # Faire le calcul avec awk
    erreur_ulp=$(awk -v obtenu="$resultat_obtenu" -v attendu="$resultat_attendu" -v ulp="$ulp_value" '
    BEGIN {
        # Convertir en nombres
        o = obtenu + 0
        a = attendu + 0
        u = ulp + 0
        
        # Calculer différence absolue
        diff = o - a
        if (diff < 0) diff = -diff
        
        # Calculer erreur en ULP
        if (u != 0) {
            erreur = diff / u
            printf "%.4f", erreur
        } else {
            print "INF"
        }
    }')
    
    # Afficher résultat
    echo "Test: $nom_test"
    echo "  Obtenu  : $resultat_obtenu"
    echo "  Attendu : $resultat_attendu"
    echo "  ULP     : $ulp_value"
    echo "  Erreur  : $erreur_ulp ULP"
    
    # Vérifier si ≤ 2 ULP
    if [ "$erreur_ulp" = "INF" ]; then
        echo " ERREUR: ULP = 0"
    else
        # Comparer avec awk
        if awk -v e="$erreur_ulp" 'BEGIN {exit !(e <= 2.0)}'; then
            echo "  OK (≤ 2 ULP)"
        elif awk -v e="$erreur_ulp" 'BEGIN {exit !(e <= 4.0)}'; then
            echo "  Pas mal (≤ 4 ULP)"
        else
            echo "  mal (> 4 ULP)"
        fi
    fi
    
    echo ""
done