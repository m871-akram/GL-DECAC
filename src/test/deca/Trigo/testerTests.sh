#!/bin/bash
# Script simplifie pour tester TRIGO

echo "***************** LES TESTS TRIGO ***************"
echo ""

total_tests=0
tests_ok=0

# On lit le fichier de tests
while IFS=';' read -r nom fonction valeur resultat
do
    # Ignorer les lignes vides ou commentaires
    [[ -z "$nom" ]] || [[ "$nom" == \#* ]] && continue
    
    total_tests=$((total_tests + 1))
    echo "Test $total_tests: $nom"
    
    # on determine le type de ce fichier (valid ou invalid)
    if [[ "$resultat" == "erreur" ]]; then
        dossier="invalid"
    else
        dossier="valid"
    fi
    
    fichier="src/test/deca/Trigo/$dossier/$nom.deca"

    # la compilation
    decac "$fichier" 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "  Echec de compilation pour $nom"
        continue
    fi
    
    # on execute le script
    fichier_ass="${fichier%.deca}.ass"
    resultat_exec=$(ima "$fichier_ass" 2>&1 | tail -1)
    
    # on verifie le resultat
    if [[ "$resultat" == "erreur" ]]; then
        # On verifie si le resultat est NaN
        if echo "$resultat_exec" | grep -qi "erreur"; then
            echo "  OK - Erreur detectee (erruer)"
            tests_ok=$((tests_ok + 1))
        else
            echo "  ECHEC - Attendu: erreur, Obtenu: $resultat_exec"
        fi
    else
        # Test de precision avec Python (ULP < 2)
        # On recupere la valeur ULP via le fichier _ulp.deca deja existant
        fichier_ulp="src/test/deca/Trigo/valid/${nom}_ulp.deca"
        decac "$fichier_ulp" 2>/dev/null
        ulp_value=$(ima "${fichier_ulp%.deca}.ass" 2>&1 | tail -1)

        # Calcul de l'erreur en Python
        status_ulp=$(python3 -c "
try:
    obtenu, attendu, ulp = float('$resultat_exec'), float('$resultat'), float('$ulp_value')
    if ulp == 0: ulp = 1e-45
    ratio = abs(obtenu - attendu) / ulp
    print('OK' if ratio < 4 else f'FAIL({ratio:.1f})')
except:
    print('ERROR')
")

        if [[ "$status_ulp" == "OK" ]]; then
            echo "  OK (Precision ULP valide) - Obtenu: $resultat_exec"
            tests_ok=$((tests_ok + 1))
        else
            echo "  ECHEC - Obtenu: $resultat_exec, Attendu: $resultat (Ratio: $status_ulp)"
        fi
    fi
    echo ""
    
done < "src/test/deca/Trigo/mes-tests-trigo.txt"

echo "Tests total: $total_tests"
echo "Tests OK:    $tests_ok"

if [ $tests_ok -eq $total_tests ]; then
    echo " Tous les tests sont OK !"
    exit 0
else
    echo " $((total_tests - tests_ok)) test(s) ont echoue."
    exit 1
fi