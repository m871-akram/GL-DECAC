import java.util.ArrayList;

public class TestUlp {
    public static void main(String[] args) {
        ArrayList<UnTest> mesTests = new ArrayList<>();

        float[] angles = {
                0.0f,
                0.12f,
                -0.45f,
                0.88f,
                0.000001f,  // très petit
                -0.4f,
                0.258819f,
                0.3420201f,
                -0.6427876f,
                0.5f,
                -0.8660254f,
                0.99999994f,
                -0.99999994f,
                1.17549435E-38f,
                0.999f,
                -0.999f,
                0.99f,
                -0.99f,
                1e-6f,      // très petit positif
                -1e-6f,     // très petit négatif
                3.14159f,
                6.28318f,
                4.712388f,
                1.570796f,
                -1.570796f,
                0.785398f,
                -0.785398f,
                1.047197f,
                0.523599f,
                10000,         //gros valeurs
                1000000000,
                10,
                242424,
                3.4028235E33f,
                -3.4028235E33f,
                0.0f,
                -0.0f,
                1.0f,             // Puissance de 2
                2.0f,
                0.5f,
                // valeurs proches de 1
                0.99999994f,
                1.0000001f,
                2.8f,  //valeurs arbitraires
                5.9f,
                -4.1f,
                -2.4f,
                11.5f,
                -343.2f
        };


        for (float x : angles) {
            mesTests.add(new UnTest(x, (float) java.lang.Math.ulp(x)));
        }

        for (UnTest t : mesTests) {
            verifierUlp(t);
        }
    }

    public static void verifierUlp(UnTest t) {
        String nomTest = "ulp_b_" + t.x;
        if (Float.isNaN(t.attenduUlp)) {
            System.out.printf("%s;%s;%.6e;%s\n",
                    nomTest, "ulp", t.x, "erreur");
        }
        System.out.printf("%s;%s;%.6e;%.6e\n",
                nomTest, "ulp", t.x, t.attenduUlp);
    }

    static class UnTest {
        float x;
        float attenduUlp;

        UnTest(float x, float attenduUlp) {
            this.x = x;
            this.attenduUlp = attenduUlp;
        }
    }
}