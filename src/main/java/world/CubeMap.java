package world;

import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import org.lwjgl.opengl.GL46;

import java.nio.ByteBuffer;

public class CubeMap {
    private int textureID;
    private int size = 256;

    public CubeMap(){
        textureID = SpriteBinder.getInstance().genTexture();
        initCubeMap();
    }

    public CubeMap(int size){
        textureID = SpriteBinder.getInstance().genTexture();
        this.size = size;
        initCubeMap();
    }

    public CubeMap setSize(int size){
        this.size = size;
        initCubeMap();
        return this;
    }

    private void initCubeMap(){
        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureID);

        //For Each of the six faces
        for(int i = 0; i < 6; i++){
            GL46.glTexImage2D(GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL46.GL_RGBA, size, size, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        }

        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);

        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_R, GL46.GL_CLAMP_TO_EDGE);

        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, 0);
    }

}
