package graphics.renderer;

public class DrawIndex {
    int positionStart  = 0;
    int positionLength = 0;
    int colorStart  = 0;
    int colorLength = 0;

    public DrawIndex(int positionStart, int positionLength, int colorStart, int colorLength){
        this.positionStart  = positionStart;
        this.positionLength = positionLength;
        this.colorStart     = colorStart;
        this.colorLength    = colorLength;
    }
}
