package graphics.ui;

import org.lwjgl.nanovg.NanoVG;

public enum TextAlignment {

    // Direct Mapping
    LEFT(NanoVG.NVG_ALIGN_LEFT),
    RIGHT(NanoVG.NVG_ALIGN_RIGHT),
    TOP(NanoVG.NVG_ALIGN_TOP),
    MIDDLE(NanoVG.NVG_ALIGN_MIDDLE),
    BOTTOM(NanoVG.NVG_ALIGN_BOTTOM),
    BASELINE(NanoVG.NVG_ALIGN_BASELINE),


    // Double
    TOP_LEFT(NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP),
    CENTER(NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE),
    ;

    int alignment;
    TextAlignment(int alignment){
        this.alignment = alignment;
    }

}
