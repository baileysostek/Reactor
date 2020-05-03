package graphics.sprite;

import engine.Engine;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class SpriteBinder extends Engine {

    private static SpriteBinder spriteBinder;
    private HashMap<String, ImageData> textureIDs = new HashMap<String, ImageData>();

    private SpriteBinder(){

    }

    public Sprite load(String image){
        ImageData textureID = new ImageData(0, 0, 0);
        if(textureIDs.containsKey(image)){
            textureID = textureIDs.get(image);
            //TODO
            //Somehow this seems to never trigger for recurrent sprites.
        }else{
            String path = new File("").getAbsolutePath() + "\\resources\\textures\\" + image;
            File imageFile = new File(path);

            if(!imageFile.exists()){
                System.err.println("Error file:"+path+" does not exist.");
                path = new File("").getAbsolutePath() + "\\resources\\texturesEnt\\fileNotFound.png";
                imageFile = new File(path);
            }

            textureID.textureID = GL20.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID.textureID);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL13.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL13.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);

            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
                ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
                for (int pixel : pixels) {
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) (pixel >> 24));
                }
                buffer.flip();

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                textureID.width = width;
                textureID.height = height;
                textureIDs.put(image, textureID);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Sprite(textureID.textureID, textureID.width, textureID.height);
    }

    public Animation loadAnimation(String filePath){
        Animation animation = new Animation(0, 0, 0);
        return animation;
    }

    public FlipBook loadFlipBook(String filePath){
        Sprite atlas = load(filePath);
        FlipBook flipBook = new FlipBook(atlas.getTextureID(), atlas.getWidth(), atlas.getHeight(), filePath);
        return flipBook;
    }

    public static SpriteBinder getInstance(){
        if(spriteBinder == null){
            spriteBinder = new SpriteBinder();
        }
        return spriteBinder;
    }

    @Override
    public void onShutdown() {
        for(ImageData i : textureIDs.values()){
            GL20.glDeleteTextures(i.textureID);
        }
    }

    public void addExternalID(int id){
        this.textureIDs.put(id+"", new ImageData(0,0,id));
    }

    private class ImageData {
        public int width;
        public int height;
        public int textureID;

        public ImageData(int width, int height, int textureID){
            this.width = width;
            this.height = height;
            this.textureID = textureID;
        }
    }
}
