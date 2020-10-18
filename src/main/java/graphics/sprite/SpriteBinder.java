package graphics.sprite;

import engine.Engine;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL46;
import util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class SpriteBinder extends Engine {

    private static SpriteBinder spriteBinder;
    //Map the index for this texture to
    private HashMap<Integer, Sprite> sprites    = new HashMap<Integer, Sprite>();
    private HashMap<String, Integer> spriteNames = new HashMap<String, Integer>();
    private HashMap<Integer, String> spriteNamesPrime = new HashMap<Integer, String>();

    private LinkedList<Integer> extraIDS = new LinkedList<Integer>();

    //File not found sprite
    private Sprite fileNotFound;
    private Sprite normalDefault;
    private Sprite metallicDefault;
    private Sprite roughnessDefault;
    private Sprite aoDefault;

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

                int textureID = GL46.glGenTextures();
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

                    int textureID = GL46.glGenTextures();

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

    public int loadCubeMap(String location){
        int textureID = this.genTexture();

        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureID);

        String[] names = new String[]{"RIGHT", "LEFT", "TOP", "BOTTOM", "BACK", "FRONT"};

        for(int i = 0; i < 6; i++){
            Sprite sprite = SpriteBinder.getInstance().load(location+"/"+names[i].toLowerCase()+".png");

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

        extraIDS.addLast(textureID);

        return textureID;
    }

    public int generateCubeMap(Vector4f color){
        int textureID = this.genTexture();

        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureID);

        for(int i = 0; i < 6; i++){
            Sprite sprite = new Sprite(1, 1);
            sprite.setPixelColor(0, 0, color);
            sprite.flush();

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

        extraIDS.addLast(textureID);

        return textureID;
    }

    public int generateCubeMap(Sprite sprite){
        int textureID = this.genTexture();

        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureID);

        for(int i = 0; i < 6; i++){
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

        extraIDS.addLast(textureID);

        return textureID;
    }

    public static SpriteBinder getInstance(){
        if(spriteBinder == null){
            spriteBinder = new SpriteBinder();
            spriteBinder.fileNotFound       = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889]}"));
            spriteBinder.normalDefault      = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841,-8355841]}"));
            spriteBinder.metallicDefault    = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216]}"));
            spriteBinder.roughnessDefault   = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1]}"));
            spriteBinder.aoDefault          = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1]}"));
        }
        return spriteBinder;
    }

    @Override
    public void onShutdown() {
        for(Sprite sprite : sprites.values()){
            this.delete(sprite);
        }

        for(int i : extraIDS){
            GL46.glDeleteTextures(i);
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
        GL46.glDeleteTextures(sprite.getTextureID());
    }

    public int genTexture() {
        int id = GL46.glGenTextures();
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

    public int getDefaultNormalMap() {
        if(this.normalDefault == null){
            return -1;
        }
        return this.normalDefault.getTextureID();
    }

    public Integer getDefaultMetallicMap() {
        if(this.metallicDefault == null){
            return -1;
        }
        return this.metallicDefault.getTextureID();
    }

    public Integer getDefaultRoughnessMap() {
        if(this.roughnessDefault == null){
            return -1;
        }
        return this.roughnessDefault.getTextureID();
    }

    public Integer getDefaultAmbientOcclusionMap() {
        if(this.aoDefault == null){
            return -1;
        }
        return this.aoDefault.getTextureID();
    }

//    public void addExternallyGeneratedSprite(int id, Sprite sprite){
//        this.textureIDs.put(id+"", new ImageData(0,0,id));
//    }
}
