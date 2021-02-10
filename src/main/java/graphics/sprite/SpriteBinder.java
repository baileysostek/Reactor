package graphics.sprite;

import camera.CameraManager;
import graphics.renderer.*;
import models.Model;
import models.ModelManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.opengl.GL46;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import skybox.SkyboxManager;
import util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class SpriteBinder {

    private static SpriteBinder spriteBinder;
    //Map the index for this texture to
    private HashMap<Integer, Sprite> sprites    = new HashMap<Integer, Sprite>();
    private HashMap<String, Integer> spriteNames = new HashMap<String, Integer>();
    private HashMap<Integer, String> spriteNamesPrime = new HashMap<Integer, String>();

    //Cubemap textures and sprite
    private HashSet<Integer> cubemapTextures = new HashSet<>();
    private Sprite cubemapTarget;

    private LinkedList<Integer> extraIDS = new LinkedList<Integer>();

    //File not found sprite
    private Sprite fileNotFound;
    private Sprite normalDefault;
    private Sprite metallicDefault;
    private Sprite roughnessDefault;
    private Sprite aoDefault;

    private Handshake cube;

    private SpriteBinder(){
        cube = new Handshake();

        float SIZE = 500f;

        float[] defaultCubeMap = new float[]{
            -SIZE,  SIZE, -SIZE,
            -SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            -SIZE,  SIZE, -SIZE,
            SIZE,  SIZE, -SIZE,
            SIZE,  SIZE,  SIZE,
            SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
            SIZE, -SIZE,  SIZE
        };
        cube.addAttributeList("aPos", defaultCubeMap, EnumGLDatatype.VEC3);
    }

    private void postInit(){
        spriteBinder.fileNotFound       = new Sprite(StringUtils.parseJson("{'width':16,'height':16,'pixels':[-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-9568145,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-13828050,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-9568145,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-13828050,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-261889,-261889,-261889,-261889,-261889,-261889,-261889,-261889]}"));
        spriteBinder.normalDefault      = new Sprite(StringUtils.parseJson("{'width':1,'height':1,'pixels':[-8355841]}"));
        spriteBinder.metallicDefault    = new Sprite(StringUtils.parseJson("{'width':1,'height':1,'pixels':[-16777216]}"));
        spriteBinder.roughnessDefault   = new Sprite(StringUtils.parseJson("{'width':1,'height':1,'pixels':[-1]}"));
        spriteBinder.aoDefault          = new Sprite(StringUtils.parseJson("{'width':1,'height':1,'pixels':[-1]}"));

        cubemapTarget = new Sprite(1, 1);
    }

    public Sprite load(String image){
        if(spriteNames.containsKey(image)){
            return sprites.get(spriteNames.get(image));
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

    public int loadSVG(String svgName, float contentScaleX, float contentScaleY, float dpi){
        String data = StringUtils.load(svgName);

        NSVGImage svg;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            svg = NanoSVG.nsvgParse(data, "px", dpi);

            if (svg == null) {
                throw new IllegalStateException("Failed to parse SVG.");
            }
        } finally {
//            MemoryUtil.memFree(svgData);
        }

        float svgWidth  = svg.width();
        float svgHeight = svg.height();

        int width  = (int)(svgWidth * contentScaleX);
        int height = (int)(svgHeight * contentScaleY);

        long rast = NanoSVG.nsvgCreateRasterizer();
        if (rast <= 0) {
            throw new IllegalStateException("Failed to create SVG rasterizer.");
        }

        ByteBuffer image = MemoryUtil.memAlloc(width * height * 4);

        NanoSVG.nsvgRasterize(rast, svg, 0, 0, 1, image, width, height, width * 4);

        NanoSVG.nsvgDeleteRasterizer(rast);

        int texID = this.genTexture();

        GL46.glBindTexture(GL46.GL_TEXTURE_2D, texID);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR_MIPMAP_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);

        //heres where the Color comes in.
        premultiplyAlpha(image, width, height, width * 4);

        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, image);

        ByteBuffer input_pixels = image;

        int        input_w      = width;
        int        input_h      = height;
        int        mipmapLevel  = 0;
        while (1 < input_w || 1 < input_h) {
            int output_w = Math.max(1, input_w >> 1);
            int output_h = Math.max(1, input_h >> 1);

            ByteBuffer output_pixels = MemoryUtil.memAlloc(output_w * output_h * 4);
            STBImageResize.stbir_resize_uint8_generic(
                input_pixels, input_w, input_h, input_w * 4,
                output_pixels, output_w, output_h, output_w * 4,
                4, 3, STBImageResize.STBIR_FLAG_ALPHA_PREMULTIPLIED,
                STBImageResize.STBIR_EDGE_CLAMP,
                STBImageResize.STBIR_FILTER_MITCHELL,
                STBImageResize.STBIR_COLORSPACE_SRGB
            );

            MemoryUtil.memFree(input_pixels);

            GL46.glTexImage2D(GL46.GL_TEXTURE_2D, ++mipmapLevel, GL46.GL_RGBA, output_w, output_h, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, output_pixels);

            input_pixels = output_pixels;
            input_w = output_w;
            input_h = output_h;
        }
        MemoryUtil.memFree(input_pixels);

        return texID;
    }

    private static ByteBuffer downloadSVG(String spec) {
        ByteBuffer buffer = MemoryUtil.memAlloc(128 * 1024);
        try {
            URL url = new URL(spec);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept-Encoding", "gzip");
            InputStream is = con.getInputStream();
            if ("gzip".equals(con.getContentEncoding())) {
                is = new GZIPInputStream(is);
            }

            try (ReadableByteChannel rbc = Channels.newChannel(is)) {
                int c;
                while ((c = rbc.read(buffer)) != -1) {
                    if (c == 0) {
                        buffer = MemoryUtil.memRealloc(buffer, (buffer.capacity() * 3) >> 1);
                    }
                }
            }
        } catch (IOException e) {
            MemoryUtil.memFree(buffer);
            throw new RuntimeException(e);
        }
        buffer.put((byte)0);
        buffer.flip();

        return buffer;
    }

    private static void premultiplyAlpha(ByteBuffer image, int w, int h, int stride) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte)Math.round(((image.get(i + 0) & 0xFF) * alpha) + 0xFF));
                image.put(i + 1, (byte)Math.round(((image.get(i + 1) & 0xFF) * alpha) + 0xFF));
                image.put(i + 2, (byte)Math.round(((image.get(i + 2) & 0xFF) * alpha) + 0xFF));
            }
        }
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

    public int generateCubeMap(Vector4f color){
        int textureID = this.genTexture();

        //Register cubemapTexture
        cubemapTextures.add(textureID);

        updateCubeMap(textureID, color);

        extraIDS.addLast(textureID);

        return textureID;
    }


    public void updateCubeMap(int textureID, Vector4f color) {
        if(cubemapTextures.contains(textureID)) {
            GL46.glActiveTexture(GL46.GL_TEXTURE0);
            GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, textureID);

            GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
            GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);

            cubemapTarget.setPixelColor(0, 0, color);

            for (int i = 0; i < 6; i++) {
                int[] pixels = cubemapTarget.getPixels();
                ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);

                for (int pixel : pixels) {

//                    pixel = (r.nextInt()) | 0xFF000000;

                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) (pixel >> 24));
                }
                buffer.flip();

                GL46.glTexImage2D(GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL46.GL_RGBA, cubemapTarget.getWidth(), cubemapTarget.getHeight(), 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, buffer);
            }

//        GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);
        }
    }

    public int loadCubeMap(String location){
        int textureID = this.genTexture();

        GL46.glActiveTexture(GL46.GL_TEXTURE0);
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

        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);

        extraIDS.addLast(textureID);

        return textureID;
    }

    public int loadCubeMapHDR(String location){
        int hdrTextureID = this.genTexture();

        IntBuffer width     = BufferUtils.createIntBuffer(1);
        IntBuffer height    = BufferUtils.createIntBuffer(1);
        IntBuffer channels  = BufferUtils.createIntBuffer(1);

        String resourcePath = StringUtils.getRelativePath()+ "textures/IBL/" + location;

        System.out.println("Resource:" + resourcePath);

        FloatBuffer textureData = STBImage.stbi_loadf(resourcePath , width, height, channels, 3);

        //TODO add if has data in buffer.
        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, hdrTextureID);
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGB16F, width.get(0), height.get(0), 0, GL46.GL_RGB, GL46.GL_FLOAT, textureData);

        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);

        STBImage.stbi_image_free(textureData);

        //Create our cubemap texture with no data inside it.
        int envCubemap = this.genTexture();
        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, envCubemap);
        for(int i = 0; i < 6; i++){
            GL46.glTexImage2D(GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL46.GL_RGB32F, 512, 512, 0, GL46.GL_RGB, GL46.GL_FLOAT, (FloatBuffer) null);
        }
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_R, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, 0);

        //PBR create projection and view matrices for the six cubemap faces
        Matrix4f captureProjection = new Matrix4f().perspective((float) Math.toRadians(90f), 1.0f, 0.1f, 10f);

        Matrix4f[] captureViews = new Matrix4f[]{
            new Matrix4f().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f( 1f, 0f, 0f), new Vector3f(0f, -1f,  0f)),
            new Matrix4f().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(-1f, 0f, 0f), new Vector3f(0f, -1f,  0f)),
            new Matrix4f().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f,  1f, 0f), new Vector3f(0f,  0f,  1f)),
            new Matrix4f().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, -1f, 0f), new Vector3f(0f,  0f, -1f)),
            new Matrix4f().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f,  1f), new Vector3f(0f, -1f,  0f)),
            new Matrix4f().lookAt(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -1f), new Vector3f(0f, -1f,  0f)),
        };

        //Render HDR equirectangular environment map to cubemap equivalent
        int equirectangularShader = ShaderManager.getInstance().loadShader("equirectangular");
        ShaderManager.getInstance().useShader(equirectangularShader);
        ShaderManager.getInstance().loadUniformIntoActiveShader("equirectangularMap", 0);
        ShaderManager.getInstance().loadUniformIntoActiveShader("projection", captureProjection);
        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, hdrTextureID);

        //PBR setup framebuffer used to blur our images
        int cubeFBO = GL46.glGenFramebuffers();
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, cubeFBO);
        GL46.glDrawBuffer(GL46.GL_COLOR_ATTACHMENT0);

        int cubeRBO = GL46.glGenRenderbuffers();
        GL46.glBindRenderbuffer(GL46.GL_RENDERBUFFER, cubeRBO);
        GL46.glRenderbufferStorage(GL46.GL_RENDERBUFFER, GL46.GL_DEPTH_COMPONENT24, 512, 512);
        GL46.glFramebufferRenderbuffer(GL46.GL_FRAMEBUFFER, GL46.GL_DEPTH_ATTACHMENT, GL46.GL_RENDERBUFFER, cubeRBO);

        GL46.glViewport(0, 0, 512, 512);
        for(int i = 0; i < 6; i++){
            GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, envCubemap, 0);
            ShaderManager.getInstance().loadUniformIntoActiveShader("view", captureViews[i]);
            renderCube(equirectangularShader);
        }
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);

        //PBR create texture for convoluted cubemap
        int irradianceMap = this.genTexture();
        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, irradianceMap);
        for(int i = 0; i < 6; i++){
            GL46.glTexImage2D(GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL46.GL_RGB16F, 32, 32, 0, GL46.GL_RGB, GL46.GL_FLOAT, (ByteBuffer) null);
        }
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_WRAP_R, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_CUBE_MAP, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);

        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, cubeFBO);

        //PBR convolute to create an irradiance cubemap
        int irradianceShader = ShaderManager.getInstance().loadShader("irradiance");
        ShaderManager.getInstance().useShader(irradianceShader);
        ShaderManager.getInstance().loadUniformIntoActiveShader("environmentMap", 0);
        ShaderManager.getInstance().loadUniformIntoActiveShader("projection", captureProjection);
        GL46.glActiveTexture(GL46.GL_TEXTURE0);

        GL46.glBindTexture(GL46.GL_TEXTURE_CUBE_MAP, envCubemap);
        GL46.glViewport(0, 0, 32, 32);
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, cubeFBO);
        for(int i = 0; i < 6; i++){
            ShaderManager.getInstance().loadUniformIntoActiveShader("view", captureViews[i]);
            GL46.glFramebufferTexture2D(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, GL46.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, irradianceMap, 0);
            GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);
            renderCube(irradianceShader);
        }
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);


        GL46.glDeleteFramebuffers(cubeFBO);
        GL46.glDeleteRenderbuffers(cubeRBO);

        extraIDS.addLast(irradianceMap);
        extraIDS.addLast(hdrTextureID);
        extraIDS.addLast(envCubemap);
        GL46.glViewport(0, 0, Renderer.getWIDTH(), Renderer.getHEIGHT());

        return irradianceMap;
    }

    public void renderCube(int shader){
        GL46.glDisable(GL46.GL_CULL_FACE);


        ShaderManager.getInstance().loadHandshakeIntoShader(shader, cube);
        //Render
        GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, 36);

        GL46.glUseProgram(0);

        //Overall GL config
        GL46.glEnable(GL46.GL_CULL_FACE);
    }

    public static SpriteBinder getInstance(){
        if(spriteBinder == null){
            spriteBinder = new SpriteBinder();
            spriteBinder.postInit(); }
        return spriteBinder;
    }

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

    public Sprite copySprite(int albedoID) {
        return new Sprite( this.sprites.get(albedoID));
    }

    public String lookupFileFromTextureID(int textureID) {
        if(this.spriteNamesPrime.containsKey(textureID)) {
            return this.spriteNamesPrime.get(textureID);
        }
        return null;
    }


//    public void addExternallyGeneratedSprite(int id, Sprite sprite){
//        this.textureIDs.put(id+"", new ImageData(0,0,id));
//    }
}
