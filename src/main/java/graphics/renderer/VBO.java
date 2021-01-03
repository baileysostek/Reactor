package graphics.renderer;

public class VBO {
    public final int VBO_ID;
    public final int vertexStride;
    public final int dataSize;
    public final String name;

    protected VBO(int VBO_ID, int vertexStride, int dataSize, String name) {
        this.VBO_ID = VBO_ID;
        this.vertexStride = vertexStride;
        this.dataSize = dataSize;
        this.name = name;
    }
}
