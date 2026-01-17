void testCos() {
    Math m = new Math();
    
    float x = 0.0;
    float maxErrorULP = 0.0;
    float worstX = 0.0;
    float worstSinValue = 0.0;
    
    println("TEST Sin AVEC ULP ===");
    println("x, sin(x), erreur_ULP");
    
    while (x <= TWO_PI) {
        float sinx = m.sin(x);
        float error = 0.0;
        
        if (x == 0.0 || x == PI || x == TWO_PI) {
            // Points où sin(x) DOIT être exactement 0
            error = sinx;                               // écart par rapport à 0

        } else if (x == PI/2.0) {
            // Point où sin(x) DOIT être 1
            error = sinx - 1.0;

        } else if (x == 3.0*PI_OVER_2) {
            // Point où sin(x) DOIT être -1
            error = sinx + 1.0;  // sinx - (-1.0)

        } else {
            if (sinx > 1.0) { //erreur
                error = sinx - 1.0;
            } else if (sinx < -1.0) {
                error = -1.0 - sinx;
            }
        }
        
        // Conversion en ULP
        float errorULP = 0.0;
        
        if (error != 0.0) {
            errorULP = error / m.ulp(sinx);
        }
        
        // Affichage pour toutes les valeurs
        println(x, ", ", sinx, ", ", errorULP, " ULPs");
        
        //  Suivi du pire cas
        if (errorULP > maxErrorULP) {
            maxErrorULP = errorULP;
            worstX = x;
            worstSinValue = sinx;
        }
        
        x = x + 0.001;  // pas
    }
    
    println("Erreur max: ", maxErrorULP, " ULPs");
    println("À x = ", worstX);
    println("sin(x) = ", worstSinValue);
}