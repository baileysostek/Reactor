package math;

import org.joml.Vector3f;

import java.text.DecimalFormat;

public class VectorUtils {

    private static DecimalFormat df = new DecimalFormat("#.####");

    public static String format(float f){
        return df.format(f);
    }

    public static float maxComponent(Vector3f size){
        float biggestDimension = Math.max(Math.max(size.x, size.y), size.z) / 2f;

        return biggestDimension;
    }

}
