package editor.shaderGraph;

public class OutputNode extends ShaderNode{
    public OutputNode() {
        super("Output");
    }

    @Override
    public String[] getGLSLData() {
        return new String[]{
            "data"
        };
    }
}
