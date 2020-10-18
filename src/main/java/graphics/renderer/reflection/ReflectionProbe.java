package graphics.renderer.reflection;

import entity.Entity;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL46;

import java.nio.ByteBuffer;

public class ReflectionProbe extends Entity {

    private Vector4f color = new Vector4f(0.2f, 0.5f, 1.0f, 1);
    private final int length = 32;
    private int reflectionCubeTextureID;
    private int size = 256;

    //TODO refactor to manager class because they will all be same.
    private Matrix4f[] relativeTransforms = new Matrix4f[6];

    public ReflectionProbe(){
        //Front
        for(int i = 0;  i < relativeTransforms.length; i++){
            relativeTransforms[i] = new Matrix4f().identity();
            Vector2i pitchYaw = faceMap(i);

            relativeTransforms[i] = relativeTransforms[i].rotate((float) Math.toRadians(180), new Vector3f(0, 0, 1));
            relativeTransforms[i] = relativeTransforms[i].rotate((float) Math.toRadians(pitchYaw.x), new Vector3f(1, 0, 0));
            relativeTransforms[i] = relativeTransforms[i].rotate((float) Math.toRadians(pitchYaw.y), new Vector3f(0, 1, 0));
//        Vector3f negativeCameraPos = new Vector3f(-center.x, -center.y, -center.z);
//        relativeTransforms[i].translate(negativeCameraPos, viewMatrix, viewMatrix);

            relativeTransforms[i] = relativeTransforms[i].mul(ReflectionManager.getInstance().getProjectionMatrix());
        }

        super.setVisible(false);

//        super.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
//        super.setScale(0.25f);

        initProbe();
    }

    private Vector2i faceMap(int index){
        switch (index) {
            case 0:
                return new Vector2i(0, 90);
            case 1:
                return new Vector2i(0, -90);
            case 2:
                return new Vector2i(-90, 180);
            case 3:
                return new Vector2i(90, 180);
            case 4:
                return new Vector2i(0, 180);
            case 5:
                return new Vector2i(0, 0);
        }
        return new Vector2i(0, 0);
    }

    @Override
    public void onAdd(){
        ReflectionManager.getInstance().addReflectionProbe(this);
    }

    @Override
    public void onRemove(){
        ReflectionManager.getInstance().removeReflectionProbe(this);
    }

    private void initProbe(){
        reflectionCubeTextureID = SpriteBinder.getInstance().genTexture();
        initCubeMap();
    }

    @Override
    public void renderInEditor(boolean selected){

    }

    public ReflectionProbe setSize(int size){
        this.size = size;
        initCubeMap();
        return this;
    }

    private void initCubeMap(){
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, reflectionCubeTextureID);

        String[] names = new String[]{"RIGHT", "LEFT", "TOP", "BOTTOM", "BACK", "FRONT"};

        for(int i = 0; i < 6; i++){
            Sprite sprite = SpriteBinder.getInstance().load("sky/"+names[i].toLowerCase()+".png");

            int[] pixels = sprite.getPixels();
            ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) (pixel >> 24));
            }
            buffer.flip();

            GL46.glTexImage2D(GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL46.GL_RGBA, sprite.getWidth(), sprite.getHeight(), 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, buffer);
        }

        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);

        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, 0);
    }



}
