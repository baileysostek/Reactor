package math;

import java.text.DecimalFormat;

public class VectorUtils {

    private static DecimalFormat df = new DecimalFormat("#.####");


    public static float getDistance(Vector3f min, Vector3f max){
        return (float) Math.sqrt(((max.x() - min.x())*(max.x() - min.x()))+((max.y() - min.y())*(max.y() - min.y()))+((max.z() - min.z())*(max.z() - min.z())));
    }

    public static String format(float f){
        return df.format(f);
    }

}
