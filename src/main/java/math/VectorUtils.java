package math;

import java.text.DecimalFormat;

public class VectorUtils {

    private static DecimalFormat df = new DecimalFormat("#.####");

    public static String format(float f){
        return df.format(f);
    }

}
