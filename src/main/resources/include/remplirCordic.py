"""code python pour remplir la table cordic , et creer une methode deca _getCordic , utilse dans Math.decah"""
import math

for i in range(32):
    print(f"float cordic{i} = {math.atan(2**(-i)):.9g}f;")  #9chifres en total 


print("\nfloat _getCordic(int i) {")
for i in range(32):
    print(f"    {'if' if i==0 else 'else if'} (i == {i}) return cordic{i};")
