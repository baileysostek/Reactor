package graphics.sprite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import math.Maths;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import serialization.Serializable;
import util.DistanceCalculator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Sprite implements Serializable<Sprite> {
    //Inheritied or generated value
    private int textureID = 0;

    //Serialized values
    private int width     = 0;
    private int height    = 0;
    private int[] pixels;

    //Make an empty sprite
    public Sprite(JsonObject data){
        this.deserialize(data);
    }

    public Sprite(int width, int height) {
        this.textureID = SpriteBinder.getInstance().genTexture();
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];

        for(int i = 0; i < this.pixels.length; i++){
            this.pixels[i] = 0x00000000;
        }

        this.flush();
    }

    public Sprite(int textureID, int[] pixels, int width, int height) {
        this.textureID = textureID;
        this.width = width;
        this.height = height;
        this.pixels = pixels;

        for(int i = 0; i < (width * height); i++){
            if(this.pixels == null){
                this.pixels[i] = 0x00000000;
            }
        }

        this.flush();
    }

    public Sprite(Sprite sprite){
        this.textureID = SpriteBinder.getInstance().genTexture();
        this.width = sprite.getWidth();
        this.height = sprite.getHeight();

        this.pixels = new int[width * height];

        for(int j = 0; j < height; j++){
            for(int i = 0; i < width; i++){
                pixels[i + (j * width)] = sprite.pixels[i + (j * width)];
            }
        }

        this.flush();
    }

    private int[] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }

    public int getTextureID(){
        return this.textureID;
    }

    //Modify calls
    public Sprite rainbow(){
        for(int i = 0;  i < pixels.length; i++){
            //A R G B?
            pixels[i] = ((int) (Math.random() * Integer.MAX_VALUE)) | 0xFF000000;
        }
        flush();
        return this;
    }

    public Sprite setPixelColor(int x, int y, Vector4f color){
        int alpha  = (int) (0xFF * color.w()) << 24;
        int red    = (int) (0xFF * color.x()) << 16;
        int green  = (int) (0xFF * color.y()) << 8;
        int blue   = (int) (0xFF * color.z());

        int out = alpha + red + green + blue;

        this.pixels[Math.max(0, Math.min(x, width - 1)) + (Math.max(0, Math.min(y, height - 1)) * width)] = out;

        return this;
    }

    public Sprite drawLine(int x1, int y1, int x2, int y2, Vector4f color){
        int dx = x2 - x1;
        int dy = y2 - y1;
        int lastY = y1;
        for(int x = x1; x < x2; x++){
            int y = y1 + dy * (x - x1) / dx;
            for(int newY = lastY; y > newY; newY++){
                this.setPixelColor(x, newY, color);
            }
            lastY = y;
        }
        return this;
    }

    public Sprite drawSquare(int x, int y, int width, int height, Vector4f color){
        for(int j = 0; j < height; j++){
            for(int i = 0; i < width; i++){
                this.setPixelColor(x + i, y + j, color);
            }
        }
        return this;
    }

    public Sprite drawCircle(int x, int y, int diameter, Vector4f color){
        for(int j = -diameter; j < diameter; j++){
            for(int i = -diameter; i < diameter; i++){
                if(new Vector2f(i, j).length() <= diameter){
                    this.setPixelColor(x + i, y + j, color);
                }
            }
        }
        return this;
    }

    //This function fills the entire sprite with the color specified.
    public Sprite fill(Vector4f color){
        for(int j = 0; j < height; j++){
            for(int i = 0; i < width; i++){
                this.setPixelColor(i, j, color);
            }
        }
        return this;
    }

    //This function outlines a sprite and returns a new instance of the sprite.
    public Sprite outline(Vector4f color){
        Sprite outlineSearch = new Sprite(this.width + 2, this.height + 2);
        outlineSearch.overlay(this, 1, 1, this.width, this.height);
        outlineSearch.flush();

        Sprite outline = new Sprite(this.width + 2, this.height + 2);
        outline.overlay(this, 1, 1, this.width, this.height);

        for(int j = 0; j < outline.height; j++){
            for(int i = 0; i < outline.width; i++){
                if(isAdjasent(outlineSearch, i, j)){
                    outline.setPixelColor(i, j, color);
                }
            }
        }
        outline.flush();


        return outline;
    }

    //Chgecks if the coord at XY is adjasent to a tile and it itself is not visiable
    private boolean isAdjasent(Sprite sprite, int x, int y){
        //IF there is no color at the current pixel
        if(sprite.pixels[x+(y * sprite.width)] >> 24 == 0){
            //If the overlay sprite has data
            //UP
            if((sprite.pixels[x+(Math.max(0, y - 1) * sprite.width)] >> 24) != 0){
                return true;
            }
            //DOWN
            if((sprite.pixels[x+(Math.min(sprite.height-1, y + 1) * sprite.width)] >> 24) != 0){
                return true;
            }
            //Left
            if((sprite.pixels[Math.max(0, x - 1)+(y * sprite.width)] >> 24) != 0){
                return true;
            }
            //Right
            if((sprite.pixels[Math.min(sprite.width-1, x + 1)+(y * sprite.width)] >> 24) != 0){
                return true;
            }
        }
        return false;
    }


    //This call us used to layer sprites on top of one-another.
    public Sprite overlay(Sprite overlay){
        return this.overlay(overlay, 0, 0, overlay.width, overlay.height);
    }

    //Overlay subset
    public Sprite overlay(Sprite overlay, int x_offset, int y_offset, int width, int height){

        //If overlay is big, resize the base sprite
        if(Math.min(width, overlay.width) > this.width || Math.min(height, overlay.height) > this.height){

        }

        for(int j = 0; j < Math.min(height, overlay.height); j++){
            for(int i = 0; i < Math.min(width, overlay.width); i++){
                int x = i + x_offset;
                int y = j + y_offset;
                //Other pixel
                int otherPixel = overlay.getPixels()[i+(j * overlay.getWidth())];
                //If the overlay sprite has data
                //See if the alpha channel exists
                if((otherPixel >> 24) != 0){
                    //Garbo town
                    this.pixels[x+(y * this.width)] = new Integer(otherPixel);
                }
            }
        }
        return this;
    }

    //Update texture call.
    public void flush(){
        ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) (pixel >> 24));
        }
        buffer.flip();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL13.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL13.GL_NEAREST);

//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_LEVEL, 7);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        SpriteBinder.getInstance().map(this.textureID, this);
    }

    //Hashing the sprite contents
    public String getHash(){
        String out = "";
        for(int i = 0; i < this.pixels.length; i++){
            out += "," + this.pixels[i];
        }
        return out;
    }

    //Serialize sprite call.
    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        out.addProperty("width", width);
        out.addProperty("height", height);
        JsonArray pixels = new JsonArray();
        for(int i : this.pixels){
            pixels.add(new JsonPrimitive(i));
        }
        out.add("pixels", pixels);
        return out;
    }

    @Override
    public Sprite deserialize(JsonObject data) {
        int textureID = SpriteBinder.getInstance().genTexture();
        JsonArray pixels_raw = data.get("pixels").getAsJsonArray();
        int[] pixels = new int[pixels_raw.size()];
        for(int i = 0; i < pixels_raw.size(); i++){
            pixels[i] = pixels_raw.get(i).getAsInt();
        }

        //Set props
        this.textureID = textureID;
        this.pixels    = pixels;
        this.width     = data.get("width").getAsInt();
        this.height    = data.get("height").getAsInt();

        this.flush();

        return this;
    }
}
