#!/bin/bash

echo "Test de performance pour trigo (just le dossier valid)"
echo ""

for fichier in src/test/deca/Trigo/valid/*.ass; do
    # on ignore les fichiers _ulp (car sont juste des binomes )
    [[ "$fichier" == *"_ulp.ass" ]] && continue
    
    nom_test=$(basename "$fichier" .ass)
    
    # le résultat attendu
    ligne=$(grep "^$nom_test;" "src/test/deca/Trigo/mes-tests-trigo.txt" 2>/dev/null)
    [ -z "$ligne" ] && continue
    
    resultat_attendu=$(echo "$ligne" | cut -d';' -f4)  #on recupere le 4eme field
    [ "$resultat_attendu" = "erreur" ] && continue
    
    # Exécution du  test principal (pas le binome _ulp)
    sortie_test=$(ima "$fichier" 2>&1)
    resultat_obtenu=$(echo "$sortie_test" | awk 'END{print $NF}' | tr -d '\r')
    
    # Exécution de  test binome (ULP)
    fichier_ulp="src/test/deca/Trigo/valid/${nom_test}_ulp.ass"
    [ ! -f "$fichier_ulp" ] && continue
    
    sortie_ulp=$(ima "$fichier_ulp" 2>&1)
    ulp_value=$(echo "$sortie_ulp" | awk 'END{print $NF}' | tr -d '\r')
    
    # on calcul l'erreur relative
    erreur_ulp=$(awk -v obtenu="$resultat_obtenu" -v attendu="$resultat_attendu" -v ulp="$ulp_value" '
    BEGIN {
        # Convertion en nombres
        o = obtenu + 0
        a = attendu + 0
        u = ulp + 0
        
        # différence absolue
        diff = o - a
        if (diff < 0) diff = -diff
        
        # Calculer erreur en ULP
        erreur = diff / u
        printf "%.6f", erreur
    }')
    
    echo "Test: $nom_test"
    echo "  Obtenu  : $resultat_obtenu"
    echo "  Attendu : $resultat_attendu"
    echo "  ULP     : $ulp_value"
    echo "  Erreur relative : $erreur_ulp ULP"
    
    # on vérifie si <= 2 ULP
    if [ "$erreur_ulp" = "INF" ]; then
        echo "  ERREUR: ULP = 0"
    else
        # calcul via awk
        if awk -v e="$erreur_ulp" 'BEGIN {exit !(e <= 2.0)}'; then
            echo "  OK (≤ 2 ULP)"
        elif awk -v e="$erreur_ulp" 'BEGIN {exit !(e <= 4.0)}'; then
            echo "  pas mal (>2 ULP et <4 ULP)"
        elif awk -v e="$erreur_ulp" 'BEGIN {exit !(e <= 100.0)}'; then
            echo "  un peu mal (>4 ULP et <100 ULP)"
        else
            echo "  trop mal ( > 100 ULP)"
        fi
    fi
    
    echo ""
done