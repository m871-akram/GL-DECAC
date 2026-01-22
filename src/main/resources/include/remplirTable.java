/*
* class pour remplir les tables cos et sin et creer des methodes getSin et getCos , utilse dans la class Math.decah
*/


class remplirTable{
    public static void main(String[] args) {
        float PI_OVER_2 = 1.57079632679489661923f;
        int CACHE_SIZE = 256;
        float CACHE_STEP = PI_OVER_2 / (CACHE_SIZE - 1);
        
        System.out.println("//table sin:");

        for (int i = 0; i < CACHE_SIZE ; i++) {
            float angle = i * CACHE_STEP;
            float sin_i = (float)java.lang.Math.sin(angle);
            System.out.println("float SIN_"+i+ " = " + sin_i + ";");
        }
        
        System.out.println("//table cos:");

        for (int i = 0; i < CACHE_SIZE ; i++) {
            float angle = i * CACHE_STEP;
            float cos_i = (float)java.lang.Math.cos(angle);
            System.out.println("float COS_"+i+ " = " + cos_i + ";");
        }

    }
}