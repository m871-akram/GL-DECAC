#!/bin/bash

echo "~~~~ verification des tests invalid ****"
echo ""

tests_total=0
tests_ok=0
tests_fail=0

for fichier in src/test/deca/Trigo/invalid/*.ass; do
    tests_total=$((tests_total + 1))
    nom_test=$(basename "$fichier" .ass)
    
    echo -n "Test $tests_total: $nom_test ... "
    
    # Exécution de ima :
    ima "$fichier" >/dev/null 2>&1
    code_retour=$?
    
    # Un test invalid DOIT échouer
    if [ $code_retour -ne 0 ]; then
        echo "OK (a échoué comme attendu)"
        tests_ok=$((tests_ok + 1))
    else
        echo "ÉCHEC (il a réussi au lieu d'échouer!)"
        tests_fail=$((tests_fail + 1))
    fi
done

echo "Total tests     : $tests_total"
echo "Tests corrects  : $tests_ok (doivent échouer)"
echo "Tests incorrects: $tests_fail (ne devraient pas réussir)"

if [ $tests_fail -eq 0 ] && [ $tests_total -gt 0 ]; then
    echo ""
    echo "Tous les tests invalid sont invalid"
elif [ $tests_total -eq 0 ]; then
    echo ""
    echo "Aucun test invalid trouvé !"
fi