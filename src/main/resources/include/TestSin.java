void testCos() {
    Math m = new Math();
    
    float x = 0.0;
    float maxErrorULP = 0.0;
    float worstX = 0.0;
    float worstSinValue = 0.0;
    
    println("TEST Sin AVEC ULP ===");
    println("x, sin(x), erreur_ULP");
    
    while (x <= m.TWO_PI) {
        float sinx = m.sin(x);
        
        // Calcul de l'erreur attendue ---
        // On ne connaît pas la valeur exacte de sin(x)...
        // Mais on peut estimer l'erreur relative
        
        // Option 1: Erreur par rapport aux bornes théoriques
        float error = 0.0;
        
        if (x == 0.0 || x == m.PI || x == m.TWO_PI) {
            // Points où sin(x) DOIT être exactement 0
            error = sinx;                               // écart par rapport à 0

        } else if (x == m.PI/2.0) {
            // Point où sin(x) DOIT être 1
            error = sinx - 1.0;

        } else if (x == 3.0*m.PI/2.0) {
            // Point où sin(x) DOIT être -1
            error = sinx + 1.0;  // sinx - (-1.0)

        } else {
            // Pour les autres points, on vérifie juste les bornes
            if (sinx > 1.0) { //erreur
                error = sinx - 1.0;
            } else if (sinx < -1.0) {
                error = -1.0 - sinx;
            }
        }
        
        // Conversion en ULPs ---
        float errorULP = 0.0;
        
        if (error != 0.0) {
            // ULPs = erreur / ULP de la valeur
            errorULP = error / m.ulp(sinx);
        }
        
        // --- Affichage pour toutes les valeurs ---
        println(x, ", ", sinx, ", ", errorULP, " ULPs");
        
        // --- Suivi du pire cas ---
        if (errorULP > maxErrorULP) {
            maxErrorULP = errorULP;
            worstX = x;
            worstSinValue = sinx;
        }
        
        x = x + 0.1;  // pas
    }
    
    println("Erreur max: ", maxErrorULP, " ULPs");
    println("À x = ", worstX);
    println("sin(x) = ", worstSinValue);
}