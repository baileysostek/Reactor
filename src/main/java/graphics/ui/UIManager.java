package graphics.ui;

import engine.Reactor;
import graphics.renderer.Renderer;
import graphics.renderer.Window;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import input.MousePicker;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.system.MemoryUtil.NULL;

public class UIManager{

    private static UIManager uiManager;
    protected static long vg;

    private LinkedList<TextRender> textRenders = new LinkedList();

//    private LinkedList<SpriteRender> spriteRenders = new LinkedList<>();
    private LinkedList<ColorRender> backgroundColors = new LinkedList<ColorRender>();
//    private LinkedList<ColorRender> foregroundColors = new LinkedList<ColorRender>();
//    private LinkedList<LineRender> lines = new LinkedList<LineRender>();

    private LinkedList<Renderable> drawCalls = new LinkedList<>();

    private HashMap<Integer, Integer> textureCache = new HashMap<Integer, Integer>();

    private UIManager(){
        vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
        if (vg == NULL) {
            throw new RuntimeException("Could not init nanovg.");
        }else{
            System.out.println("NanoVG handle:" + vg);
        }
    }

    public void render(){
        //        //UIs
        NanoVG.nvgBeginFrame(Reactor.getVg(), Renderer.getWIDTH(), Renderer.getHEIGHT(), 1);

//        NVGColor colorA = NVGColor.create();
//        NanoVG.nvgRGBA((byte)255, (byte)0, (byte)0, (byte)64, colorA);
//        NanoVG.nvgFillColor(Reactor.getVg(), colorA);
//        NanoVG.nvgFill(Reactor.getVg());

        NVGColor colorB = NVGColor.create();
        NanoVG.nvgRGBA((byte)255, (byte)255, (byte)255, (byte)255, colorB);
        NanoVG.nvgFillColor(Reactor.getVg(), colorB);
        NanoVG.nvgFontSize(Reactor.getVg(), 24);
        NanoVG.nvgFontFace(Reactor.getVg(), "roboto_mono");
        NanoVG.nvgTextAlign(Reactor.getVg(), NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP);

        //Render background
        for(ColorRender colorRender : backgroundColors){
            NVGColor colorC = NVGColor.create();
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRGBA((byte)colorRender.color.x, (byte)colorRender.color.y, (byte)colorRender.color.z, (byte)colorRender.color.w, colorC);
            NanoVG.nvgFillColor(Reactor.getVg(), colorC);
            NanoVG.nvgRect(Reactor.getVg(), colorRender.posx, colorRender.posy, colorRender.width, colorRender.height);
            NanoVG.nvgFill(vg);
        }

        //Render the text
        for(TextRender text : textRenders){
            NanoVG.nvgText(Reactor.getVg(), text.posX, text.posY, text.text);
        }

        NanoVG.nvgText(Reactor.getVg(), 4, 4, "FPS:" + Reactor.getFPS() + " Batches:" + Renderer.getInstance().getBatches());

        for(Renderable r : drawCalls){
            if(r instanceof LineRender){
                LineRender line = ((LineRender) r);
                NVGColor color = NVGColor.create();
                NanoVG.nvgBeginPath(vg);
                NanoVG.nvgRGBA((byte)line.color.x, (byte)line.color.y, (byte)line.color.z, (byte)line.color.w, color);
//            NanoVG.nvgFillColor(vg, color);
                NanoVG.nvgStrokeColor(vg, color);
                NanoVG.nvgStrokeWidth(vg, 3.0f);
                NanoVG.nvgStroke(vg);
                NanoVG.nvgMoveTo(vg, line.p1x, line.p1y);
                NanoVG.nvgLineTo(vg, line.p2x, line.p2y);
                NanoVG.nvgFill(vg);
                NanoVG.nvgMoveTo(vg, 0, 0);
                continue;
            }
            if(r instanceof SpriteRender){
                SpriteRender sprite = (SpriteRender) r;
                NVGPaint img = NVGPaint.create();

                NanoVG.nvgBeginPath(vg);

                NanoVG.nvgImagePattern(vg, sprite.posx, sprite.posy, sprite.width, sprite.height, 0, sprite.textureID, 1f, img);
                NanoVG.nvgRect(vg, sprite.posx, sprite.posy, sprite.width, sprite.height);
                NanoVG.nvgFillPaint(vg, img);
                NanoVG.nvgFill(vg);
                continue;
            }
            if(r instanceof ColorRender){
                ColorRender colorRender = (ColorRender) r;
                NVGColor colorC = NVGColor.create();
                NanoVG.nvgBeginPath(vg);
                NanoVG.nvgRGBA((byte)colorRender.color.x, (byte)colorRender.color.y, (byte)colorRender.color.z, (byte)colorRender.color.w, colorC);
                NanoVG.nvgFillColor(Reactor.getVg(), colorC);
                NanoVG.nvgRect(Reactor.getVg(), colorRender.posx, colorRender.posy, colorRender.width, colorRender.height);
                NanoVG.nvgFill(vg);
            }
        }


        NanoVG.nvgEndFrame(Reactor.getVg());

        textRenders.clear();
        backgroundColors.clear();
        drawCalls.clear();
    }

    public void drawString(float posx, float posy, String string){
        textRenders.addLast(new TextRender(posx, posy, string));
    }

    public void drawLine(float p1x, float p1y, float p2x, float p2y, Vector4f color) {
        drawCalls.addLast(new LineRender(p1x,  p1y,  p2x,  p2y, color));
    }

    public void drawImage(float x, float y, float width, float height, int textureID){
        drawCalls.addLast(new SpriteRender(x, y, width, height, textureID));
    }

    public void drawImage(float x, float y, float width, float height, Sprite sprite){
        drawCalls.addLast(new SpriteRender(x, y, width, height, sprite));
    }

    public void drawColorBackground(float posx, float posy, float width, float height, Vector4f col){
        backgroundColors.addLast(new ColorRender(posx, posy, width, height, col));
    }

    public void fillColorBackground(Vector4f col){
        backgroundColors.addLast(new ColorRender(0, 0, Renderer.getWIDTH(), Renderer.getHEIGHT(), col));
    }

    public void drawColorForeground(float posx, float posy, float width, float height, Vector4f col){
        drawCalls.addLast(new ColorRender(posx, posy, width, height, col));
    }

    public void fillColorForeground(Vector4f col){
        drawCalls.addLast(new ColorRender(0,0, Renderer.getWIDTH(), Renderer.getHEIGHT(), col));
    }

    public static UIManager getInstance() {
        return uiManager;
    }

    public long getNanoVG(){
        return vg;
    }

    public static  void initialize() {
        if(uiManager == null){
            uiManager = new UIManager();
        }
    }

    public float getCurrentTextSize() {
        return 24;
    }


    private void addToTextStack(){

    }

    private interface Renderable{

    }

    private class TextRender implements Renderable{
        float posX;
        float posY;
        String text;

        TextRender(float posX, float posY, String text){
            this.posX = posX;
            this.posY = posY;
            this.text = text;
        }
    }

    private class SpriteRender implements Renderable{
        float posx;
        float posy;
        float width;
        float height;

        boolean invertX = false;
        boolean invertY = false;

        int textureID;

        SpriteRender(float x, float y, float width, float height, Sprite sprite){
            this.posx = x;
            this.posy = y;
            this.width = width;
            this.height = height;

            //TOOD maybe this needs to be cleaned up
            if(textureCache.containsKey(sprite.getTextureID())){
                this.textureID = textureCache.get(sprite.getTextureID());
            }else {
                int id = NanoVGGL3.nvglCreateImageFromHandle(UIManager.vg, sprite.getTextureID(), (int) width, (int) height, NanoVGGL3.NVG_IMAGE_NODELETE);
                this.textureID = id;
                textureCache.put(sprite.getTextureID(), this.textureID);
            }
        }

        SpriteRender(float x, float y, float width, float height, int sprite){
            this.posx = x;
            this.posy = y;
            this.width = width;
            this.height = height;

            if(textureCache.containsKey(sprite)){
                this.textureID = textureCache.get(sprite);
            }else {
                int id = NanoVGGL3.nvglCreateImageFromHandle(UIManager.vg, sprite, (int)width, (int)height, NanoVGGL3.NVG_IMAGE_NODELETE);
                this.textureID = id;
                textureCache.put(sprite, this.textureID);
            }
        }


    }

    private class ColorRender implements Renderable{
        float posx;
        float posy;
        float width;
        float height;
        Vector4f color;

        ColorRender(float x, float y, float width, float height, Vector4f color){
            this.posx = x;
            this.posy = y;
            this.width = width;
            this.height = height;
            this.color = new Vector4f(color).mul(255).round();
        }
    }

    private class LineRender implements Renderable{
        float p1x;
        float p1y;
        float p2x;
        float p2y;
        Vector4f color;

        public LineRender(float p1x, float p1y, float p2x, float p2y, Vector4f color) {
            this.p1x = p1x;
            this.p1y = p1y;
            this.p2x = p2x;
            this.p2y = p2y;
            this.color = color;
        }
    }
}
