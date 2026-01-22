import java.util.ArrayList;

public class TestSin {

    private static final float PI = 3.14159265f;
    private static final float PI_2 = PI / 2.0f; 
    private static final float PI_3 = PI / 3.0f;
    private static final float PI_6 = PI / 6.0f;
    
    static class UnTest {
        float x;
        float attenduSin;

        UnTest(float x, float attenduSin) {
            this.x = x;
            this.attenduSin = attenduSin;
        }
    }


    public static void main(String[] args) {
        ArrayList<UnTest> mesTests = new ArrayList<>();


        float sqrt2_2 = 0.70710678f;
        float sqrt3_2 = 0.8660254f;
        
        // 0 et multiples de pi
        mesTests.add(new UnTest(0.0f, 0.0f));
        mesTests.add(new UnTest(PI, 0.0f));
        mesTests.add(new UnTest(2*PI, 0.0f));
        
        mesTests.add(new UnTest(PI_2, 1.0f));
        mesTests.add(new UnTest(3*PI_2, -1.0f));
        
        mesTests.add(new UnTest(PI/4, sqrt2_2));
        mesTests.add(new UnTest(3*PI/4, sqrt2_2));
        mesTests.add(new UnTest(5*PI/4, -sqrt2_2));
        mesTests.add(new UnTest(7*PI/4, -sqrt2_2));
        

         // π/3 (60°) et multiples
        mesTests.add(new UnTest(PI_3, sqrt3_2));
        mesTests.add(new UnTest(2*PI_3, sqrt3_2));
        mesTests.add(new UnTest(4*PI_3, -sqrt3_2));
        mesTests.add(new UnTest(5*PI_3, -sqrt3_2));
        
        mesTests.add(new UnTest(PI_6, 0.5f));
        mesTests.add(new UnTest(5*PI_6, 0.5f));
        mesTests.add(new UnTest(7*PI_6, -0.5f));
        mesTests.add(new UnTest(11*PI_6, -0.5f));
        
        mesTests.add(new UnTest(PI/12, 0.258819f));
        
        mesTests.add(new UnTest(5*PI/12, 0.9659258f)); //75

        // Très petits angles
        mesTests.add(new UnTest(1e-10f, 1e-10f));
        mesTests.add(new UnTest(1e-20f, 1e-20f));
        mesTests.add(new UnTest(-1e-10f, -1e-10f));
        mesTests.add(new UnTest(-0.0f, -0.0f));
        
        // Proches de pi/2
        mesTests.add(new UnTest(PI_2 - 1e-6f, (float)Math.cos(1e-6f)));
        mesTests.add(new UnTest(PI_2 + 1e-6f, (float)Math.cos(1e-6f) ));
        
        // Grands multiples de pi
        mesTests.add(new UnTest(100*PI, 0.0f));
        mesTests.add(new UnTest(100*PI + PI_2, 1.0f));
        
        
        
        float[] angles = {
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
            1.17549435E-35f,
            0.999f,
            -0.999f,
            0.99f,
            -0.99f,
            1e-6f,
            -1e-6f,
            3.14159f,
            6.28318f,
            4.712388f,
            1.570796f,
            -1.570796f,
            0.785398f,
            -0.785398f,
            1.047197f,
            0.523599f
        };
        
    
        for (float x : angles) {
            mesTests.add(new UnTest(x, (float)java.lang.Math.sin(x)));
        }

        for (UnTest t : mesTests) {
            verifierSin(t);
        }

    }

  

    public static void verifierSin(UnTest t) {
        String nomTest = "sin_" + t.x;
        if (Float.isNaN(t.attenduSin)) {
            System.out.printf("%s;%s;%.12E;%s\n", 
                            nomTest, "sin", t.x,"NAN");
        }
        System.out.printf("%s;%s;%.12E;%.12E\n", 
                          nomTest, "sin", t.x, t.attenduSin);
    }
}