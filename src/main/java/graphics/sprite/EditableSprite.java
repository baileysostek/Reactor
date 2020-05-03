package graphics.sprite;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class EditableSprite {

    private int textureID = 0;
    private int width = 0;
    private int height = 0;

    BufferedImage bufferedImage;

    public EditableSprite(int width, int height){
        textureID = GL20.glGenTextures();
        SpriteBinder.getInstance().addExternalID(textureID);
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        updateSprite();
    }

    public void setRGB(int x, int y, int r, int g, int b){
        bufferedImage.setRGB(x, y, (r * 65536) + (g * 256) + (b));
    }

    public void updateSprite(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL13.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL13.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);

        int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
        ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) (0xFF));
//            buffer.put((byte) (0xFFFFFFFF));
        }
        buffer.flip();

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    public Sprite toSprite(){
        return new Sprite(textureID, width, height);
    }

}
