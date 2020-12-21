//package graphics.renderer;
//
//import camera.CameraManager;
//import graphics.sprite.SpriteBinder;
//import org.lwjgl.opengl.GL46;
//
//public class SkyboxRenderer {
//    private int skyShaderID = 0;
//    private final Handshake handshake;
//
//    private final float SIZE = 500f;
//
//    private final float[] positionsF;
//
//    private final int textureID;
//
//    protected SkyboxRenderer(){
//        skyShaderID = ShaderManager.getInstance().loadShader("sky");
//
//        textureID = SpriteBinder.getInstance().loadCubeMap("sky1");
//
//        handshake = new Handshake();
//        positionsF = new float[]{
//            -SIZE,  SIZE, -SIZE,
//            -SIZE, -SIZE, -SIZE,
//            SIZE, -SIZE, -SIZE,
//            SIZE, -SIZE, -SIZE,
//            SIZE,  SIZE, -SIZE,
//            -SIZE,  SIZE, -SIZE,
//
//            -SIZE, -SIZE,  SIZE,
//            -SIZE, -SIZE, -SIZE,
//            -SIZE,  SIZE, -SIZE,
//            -SIZE,  SIZE, -SIZE,
//            -SIZE,  SIZE,  SIZE,
//            -SIZE, -SIZE,  SIZE,
//
//            SIZE, -SIZE, -SIZE,
//            SIZE, -SIZE,  SIZE,
//            SIZE,  SIZE,  SIZE,
//            SIZE,  SIZE,  SIZE,
//            SIZE,  SIZE, -SIZE,
//            SIZE, -SIZE, -SIZE,
//
//            -SIZE, -SIZE,  SIZE,
//            -SIZE,  SIZE,  SIZE,
//            SIZE,  SIZE,  SIZE,
//            SIZE,  SIZE,  SIZE,
//            SIZE, -SIZE,  SIZE,
//            -SIZE, -SIZE,  SIZE,
//
//            -SIZE,  SIZE, -SIZE,
//            SIZE,  SIZE, -SIZE,
//            SIZE,  SIZE,  SIZE,
//            SIZE,  SIZE,  SIZE,
//            -SIZE,  SIZE,  SIZE,
//            -SIZE,  SIZE, -SIZE,
//
//            -SIZE, -SIZE, -SIZE,
//            -SIZE, -SIZE,  SIZE,
//            SIZE, -SIZE, -SIZE,
//            SIZE, -SIZE, -SIZE,
//            -SIZE, -SIZE,  SIZE,
//            SIZE, -SIZE,  SIZE
//        };
//        handshake.addAttributeList("position", positionsF, EnumGLDatatype.VEC3);
//    }
//
//    public void render(){
//        // Vertex data
//        ShaderManager.getInstance().useShader(skyShaderID);
//
//        //Overall GL config
//        GL46.glDisable(GL46.GL_CULL_FACE);
//
//
//        ShaderManager.getInstance().loadHandshakeIntoShader(skyShaderID, handshake);
//
//        //Mess with uniforms
//        float[] viewMatrix = CameraManager.getInstance().getActiveCamera().getTransform();
//        viewMatrix[12] = 0;
//        viewMatrix[13] = 0;
//        viewMatrix[14] = 0;
//
//        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(skyShaderID, "viewMatrix"), false, viewMatrix);
//        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(skyShaderID, "projectionMatrix"),false, Renderer.getInstance().getProjectionMatrix());
//
//        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureID);
//
//        //Render
//        GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, positionsF.length / EnumGLDatatype.VEC3.sizePerVertex);
//
//        GL46.glUseProgram(0);
//
//        //Overall GL config
//        GL46.glEnable(GL46.GL_CULL_FACE);
//    }
//
//    public int getTextureID() {
//        return this.textureID;
//    }
//}
