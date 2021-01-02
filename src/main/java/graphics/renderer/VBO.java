package graphics.renderer;

public class VBO {
    public final int VBO_ID;
    public final int vertexStride;
    public final int dataSize;

    protected VBO(int VBO_ID, int vertexStride, int dataSize) {
        this.VBO_ID = VBO_ID;
        this.vertexStride = vertexStride;
        this.dataSize = dataSize;
    }
}
