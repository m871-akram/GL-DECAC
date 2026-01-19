#!/usr/bin/env python3
import math

# Fonction récursive pour générer la recherche binaire
def generate_binary_search(min_idx, max_idx, values, indent):
    if min_idx == max_idx:
        # Cas de base : on retourne la valeur au format scientifique
        val = float(values[min_idx])
        print(f"{indent}return {val:.9E}f;")
        return

    mid = (min_idx + max_idx) // 2
    print(f"{indent}if (i <= {mid}) {{")
    generate_binary_search(min_idx, mid, values, indent + "    ")
    print(f"{indent}}} else {{")
    generate_binary_search(mid + 1, max_idx, values, indent + "    ")
    print(f"{indent}}}")

print("class CordicTable {")

# Pré-calcul des valeurs
pow2_values = [2**(-i) for i in range(32)]
atan_values = [math.atan(2**(-i)) for i in range(32)]

# Génération de _getPow2
print("\n    float _getPow2(int i) {")
generate_binary_search(0, 31, pow2_values, "        ")
print("    }")

# Génération de _getCordic
print("\n    float _getCordic(int i) {")
generate_binary_search(0, 31, atan_values, "        ")
print("    }")

print("}")