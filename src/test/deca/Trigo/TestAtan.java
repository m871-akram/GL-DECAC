import java.util.ArrayList;

public class TestAtan {

    private static final float PI = 3.14159265f;
    private static final float PI_2 = PI / 2.0f; 
    private static final float PI_3 = PI / 3.0f;
    private static final float PI_6 = PI / 6.0f;
    
    static class UnTest {
        float x;
        float attenduAtan;

        UnTest(float x, float attenduAtan) {
            this.x = x;
            this.attenduAtan = attenduAtan;
        }
    }


    public static void main(String[] args) {
        ArrayList<UnTest> mesTests = new ArrayList<>();
        
        float[] angles = {
            // des petites valeurs
            0.0f, -0.0f, 1e-37f, -1e-37f,
            
            // la , on utilse Taylor 
            0.001f, 0.149f, 0.15f, 0.151f, 0.153f,
            
            // la on utilse CORDIC
            0.3f, 0.5f, 0.70710678f, 0.8660254f, 0.999999f,
            
            1.0f, 1.000001f, 1.1f,
            
            1.570796f, 1.7320508f, 2.0f, 10.0f, 100.0f, 
            1e6f, 1e20f,
            
            // des valeurs negatives
            -0.15f, -0.152f , -0.5f, -1.0f, -1.570796f, -1e10f,           
        };
        
    
        for (float x : angles) {
            mesTests.add(new UnTest(x, (float)java.lang.Math.atan(x)));
        }

        for (UnTest t : mesTests) {
            verifierAtan(t);
        }
    }

  

    public static void verifierAtan(UnTest t) {
        String nomTest = "atan_" + t.x;
        if (Float.isNaN(t.attenduAtan)) {
            System.out.printf("%s;%s;%.6e;%s\n", 
                            nomTest, "atan", t.x,"erreur");
        }
        System.out.printf("%s;%s;%.6e;%.6e\n", 
                          nomTest, "atan", t.x, t.attenduAtan);
    }
}



