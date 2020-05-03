package input.Chroma;

import org.jglr.jchroma.utils.ColorRef;

public class ColorUtils {

    public static ColorRef lerp(ColorRef start, ColorRef dest, float value){
//        0 = 100% start    1 = 100% dest
        return new ColorRef(
                (int)Math.floor((start.getRed() * (1.0 - value)) + (dest.getRed() * value)),
                (int)Math.floor((start.getGreen() * (1.0 - value)) + (dest.getGreen() * value)),
                (int)Math.floor((start.getBlue() * (1.0 - value)) + (dest.getBlue() * value)));
    }
}
