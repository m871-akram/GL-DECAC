"""code python pour remplir la table cordic , et creer une methode deca _getCordic , utilse dans Math.decah"""
import math

for i in range(32):
    print(f"float cordic{i} = {math.atan(2**(-i)):.9g}f;")  #9chifres en total 


print("\nfloat _getCordic(int i) {")
for i in range(32):
    print(f"    {'if' if i==0 else 'else if'} (i == {i}) return cordic{i};")

print("    else {")
print("        float pow2i = 1.0f;")
print("        int j = 0;")
print("        while (j < i) {")
print("            pow2i = pow2i * 0.5f;")
print("            j = j + 1;")
print("        }")
print("        return pow2i;")
print("    }")
print("}")