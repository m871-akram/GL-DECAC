public class remplirTable {
    public static void main(String[] args) {
        float PI_OVER_2 = 1.5707964f;
        int CACHE_SIZE = 512;
        float CACHE_STEP = PI_OVER_2 / (CACHE_SIZE - 1);
        
        float[] sinValues = new float[CACHE_SIZE];
        float[] cosValues = new float[CACHE_SIZE];
        
        // Pré-calcul des valeurs avec Java
        for (int i = 0; i < CACHE_SIZE; i++) {
            sinValues[i] = (float) Math.sin(i * CACHE_STEP);
            cosValues[i] = (float) Math.cos(i * CACHE_STEP);
        }

        System.out.println("class TableSinCos {");

        // Génération pour le Sinus
        System.out.println("    float _getSinValue(int index) {");
        generateBinarySearch(0, CACHE_SIZE - 1, sinValues, "        ");
        System.out.println("    }");

        // Génération pour le Cosinus
        System.out.println("\n    float _getCosValue(int index) {");
        generateBinarySearch(0, CACHE_SIZE - 1, cosValues, "        ");
        System.out.println("    }");

        System.out.println("}");
    }

    /**
     * Génère récursivement l'arbre de recherche binaire
     */
    public static void generateBinarySearch(int min, int max, float[] values, String indent) {
        if (min == max) {
            // Cas de base : on a trouvé l'index exact
            System.out.printf("%sreturn %.8Ef;\n", indent, values[min]);
            return;
        }

        int mid = (min + max) / 2;
        
        // Division du domaine de recherche en deux
        System.out.println(indent + "if (index <= " + mid + ") {");
        generateBinarySearch(min, mid, values, indent + "    ");
        System.out.println(indent + "} else {");
        generateBinarySearch(mid + 1, max, values, indent + "    ");
        System.out.println(indent + "}");
    }
}