package graphics.sprite;

import engine.Engine;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

public class SpriteBinder extends Engine {

    private static SpriteBinder spriteBinder;
    //Map the index for this texture to
    private HashMap<Integer, Sprite> sprites    = new HashMap<Integer, Sprite>();
    private HashMap<String, Integer> spriteNames = new HashMap<String, Integer>();
    private HashMap<Integer, String> spriteNamesPrime = new HashMap<Integer, String>();

    //File not found sprite
    private Sprite fileNotFound;

    private SpriteBinder(){
        //Initialize our fileNotFound texture!
    }

    public Sprite load(String image){
        if(sprites.containsKey(image)){
            return sprites.get(image);
        }else{
            String path = new File("").getAbsolutePath() + "\\resources\\textures\\" + image;
            File imageFile = new File(path);

            //if not found
            if(!imageFile.exists()){
                return new Sprite(fileNotFound);
            }

            try {
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                int[] pixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);

                int textureID = GL20.glGenTextures();
                Sprite sprite = new Sprite(textureID, pixels, width, height);
                sprites.put(textureID, sprite);
                spriteNames.put(image, textureID);
                spriteNamesPrime.put(textureID, image);

                return sprite;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Sprite(fileNotFound);
    }

    public SpriteSheet loadSheet(String image, int s_width, int s_height){

        LinkedList<Sprite> out = new LinkedList<>();

        String path = new File("").getAbsolutePath() + "\\resources\\textures\\" + image;
        File imageFile = new File(path);

        if(!imageFile.exists()){
            System.err.println("Error file:"+path+" does not exist.");
            path = new File("").getAbsolutePath() + "\\resources\\texturesEnt\\fileNotFound.png";
            imageFile = new File(path);
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            int col = 0;
            int row = 0;

            //If not even throw error.
            if(width % s_width != 0){
                System.out.println("Error loading sprite sheet at that resolution");
                return new SpriteSheet(0, 0, 0, 0, "", new LinkedList<Sprite>());
            }else{
                col = width / s_width;
            }

            if(height % s_height != 0){
                System.out.println("Error loading sprite sheet at that resolution");
                return new SpriteSheet(0, 0, 0, 0, "", new LinkedList<Sprite>());
            }else{
                row = height / s_height;
            }

            //Per spirte of sheet
            //row
            for(int j = 0; j < row; j++){
                //col
                for(int i = 0; i < col; i++){
                    //Build Texture from subset of image
                    int[] pixels = bufferedImage.getRGB(i * s_width, j * s_height, s_width, s_height, null, 0, s_width);
                    ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
                    boolean empty = true;
                    for (int pixel : pixels) {
                        if(pixel != 0){
                            empty = false;
                        }
                        buffer.put((byte) ((pixel >> 16) & 0xFF));
                        buffer.put((byte) ((pixel >> 8) & 0xFF));
                        buffer.put((byte) (pixel & 0xFF));
                        buffer.put((byte) (pixel >> 24));
                    }
                    buffer.flip();

                    //This sprite is blank
                    if(empty){
                        //Add blank
                        out.addLast(null);
                        continue;
                    }

                    int textureID = GL20.glGenTextures();

                    //Load sprite
                    Sprite sprite = new Sprite(textureID, pixels, s_width, s_height);
                    sprites.put(textureID, sprite);

                    out.addLast(sprite);
                }
            }

            return new SpriteSheet(col, row, s_width, s_height, image, out);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SpriteSheet(0, 0, 0, 0, "", new LinkedList<Sprite>());
    }

    public static SpriteBinder getInstance(){
        if(spriteBinder == null){
            spriteBinder = new SpriteBinder();
            spriteBinder.fileNotFound = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889]}"));
        }
        return spriteBinder;
    }

    @Override
    public void onShutdown() {
        for(Sprite sprite : sprites.values()){
            this.delete(sprite);
        }
    }

    public void delete(Sprite sprite){
        this.sprites.remove(sprite);
        String name = null;
        if(this.spriteNamesPrime.containsKey(sprite.getTextureID())){
            name = this.spriteNamesPrime.get(sprite.getTextureID());
            spriteNamesPrime.remove(sprite.getTextureID());
        }
        if(name != null) {
            if (this.spriteNames.containsKey(name)) {
                this.spriteNames.remove(name);
            }
        }
        GL20.glDeleteTextures(sprite.getTextureID());
    }

    public int genTexture() {
        int id = GL20.glGenTextures();
        return id;
    }

    public void map(int id, Sprite sprite){
        sprites.put(id, sprite);
    }

    //Get a sprite based on its texture index.
    public Sprite getSprite(int textureID) {
        if(this.sprites.containsKey(textureID)){
            return this.sprites.get(textureID);
        }
        return spriteBinder.fileNotFound;
    }

    public int getFileNotFoundID() {
        if(this.fileNotFound == null){
            return -1;
        }
        return this.fileNotFound.getTextureID();
    }

//    public void addExternallyGeneratedSprite(int id, Sprite sprite){
//        this.textureIDs.put(id+"", new ImageData(0,0,id));
//    }
}
