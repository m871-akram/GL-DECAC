import java.util.ArrayList;

public class TestAcosAsin {

    private static final float PI = 3.14159265f;
    private static final float PI_2 = PI / 2.0f; 
    private static final float PI_3 = PI / 3.0f;
    private static final float PI_6 = PI / 6.0f;
    
    static class UnTest {
        float x;
        float attenduAsin;
        float attenduAcos;

        UnTest(float x, float attenduAsin , float attenduAcos) {
            this.x = x;
            this.attenduAsin = attenduAsin;
            this.attenduAcos = attenduAcos;
        }
    }


    public static void main(String[] args) {
        ArrayList<UnTest> mesTests = new ArrayList<>();

        mesTests.add(new UnTest(0.0f,  0.0f,      PI_2));
        mesTests.add(new UnTest(1.0f,  PI_2,   0.0f));
        mesTests.add(new UnTest(-1.0f, -PI_2,  PI));
        mesTests.add(new UnTest(0.5f,  PI_6,   PI_3));
        mesTests.add(new UnTest(-0.5f, -PI_6,  PI - PI_3));
        mesTests.add(new UnTest(0.70710678f, PI/4, PI/4));  // √2/2
        mesTests.add(new UnTest(-0.70710678f, -PI/4, PI - PI/4));
        mesTests.add(new UnTest(0.8660254f, PI/3, PI/6));   // √3/2
        mesTests.add(new UnTest(-0.8660254f, -PI/3, PI - PI/6));

        // cas de NAN 
        mesTests.add(new UnTest(1.0000001f, Float.NaN, Float.NaN));
        mesTests.add(new UnTest(-1.0000001f, Float.NaN, Float.NaN));
        mesTests.add(new UnTest(Float.POSITIVE_INFINITY, Float.NaN, Float.NaN));
        mesTests.add(new UnTest(Float.NEGATIVE_INFINITY, Float.NaN, Float.NaN));
        mesTests.add(new UnTest(Float.NaN, Float.NaN, Float.NaN));



        // quelques valeurs calculées par Java
        float[] values = {
            0.12f, 
            -0.45f, 
            0.88f , 
            0.000001f , 
            -0.4f , 
            -1.1f , 
            -0.0f, 
            0.99999994f,
            -0.99999994f, 
            Float.MIN_VALUE, 
            1.17549435E-38f,
            //valeurs proches de 1
            0.9999999f,
            0.99999999f,
            0.999f,
            0.99f,
            0.9f,
            // valeurs proches de -1
            -0.9999999f,
            -0.99999999f,
            -0.999f,
            -0.99f,
            -0.9f,
            // valeurs oroche de 0
            1e-10f,
            -1e-10f,
            1e-20f,
            //valeurs speciales 
            Float.MIN_VALUE,
            Float.MIN_NORMAL,
            0.12f, 
            -0.45f, 
            0.88f, 
            0.000001f, 
            -0.4f,
            0.258819f,      // sin(π/12)
            0.9659258f,     // sin(75°)
            0.3420201f,     // sin(20°)
            -0.6427876f,    // sin(-40°)
            0.99999994f,
            -0.99999994f
            };


        for (float x : values) {
            mesTests.add(new UnTest(x, (float)java.lang.Math.asin(x) , (float)java.lang.Math.acos(x)));
        }

        System.out.println("#nom;fonction;valeur;Resultat");
        int total =0;
        for (UnTest t : mesTests) {
            total += 1;
            verifierAsin(t);
            verifierAcos(t);
        }
        System.out.println("# on a "+ total + "test pour les fonctions Asin et Acos");
        System.out.println("");
    }

  

    public static void verifierAsin(UnTest t) {
        String nomTest = "asin_" + t;
        if (Float.isNaN(t.attenduAsin)) {
            System.out.printf("%s;%s;%s;%s\n", 
                            nomTest, "asin", t.x,"NAN");
        }
        System.out.printf("%s;%s;%f;%f\n", 
                          nomTest, "asin", t.x, t.attenduAsin);
    }

    public static void verifierAcos(UnTest t) {
        String nomTest = "acos_" + t;
        if (Float.isNaN(t.attenduAcos)) {
            System.out.printf("%s;%s;%s;%s\n", 
                            nomTest, "acos", t.x,"NAN");
        }
        System.out.printf("%s;%s;%f;%f\n", 
                          nomTest, "acos", t.x, t.attenduAcos);
    }
}