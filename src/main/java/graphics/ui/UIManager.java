package graphics.ui;

import engine.Reactor;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;

import static org.lwjgl.system.MemoryUtil.NULL;

public class UIManager{

    private static UIManager uiManager;
    protected static long vg;

    private LinkedList<ColorRender> backgroundColors = new LinkedList<ColorRender>();
    private LinkedList<Renderable> drawCalls = new LinkedList<>();
    private LinkedList<ColorRender> foregroundColors = new LinkedList<ColorRender>();

    private HashMap<Integer, Integer> textureCache = new HashMap<Integer, Integer>();
    private HashMap<String, Vector4f> hexColorCache = new HashMap<>();

    private final Pattern regex = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");

    private final Vector4f WHITE = new Vector4f(255);

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

//        NVGColor colorB = NVGColor.create();
//        NanoVG.nvgRGBA((byte)255, (byte)255, (byte)255, (byte)255, colorB);
//        NanoVG.nvgFillColor(Reactor.getVg(), colorB);
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
            NanoVG.nvgClosePath(vg);
        }

        NanoVG.nvgText(Reactor.getVg(), 4, 4, "FPS:" + Reactor.getFPS() + " Batches:" + Renderer.getInstance().getBatches());

        for(Renderable r : drawCalls){
            if(r instanceof TextRender){
                TextRender text = ((TextRender)r);
                NVGColor color = NVGColor.create();
                NanoVG.nvgRGBA((byte)text.color.x, (byte)text.color.y, (byte)text.color.z, (byte)text.color.w, color);
                NanoVG.nvgFillColor(Reactor.getVg(), color);
                NanoVG.nvgText(Reactor.getVg(), text.posX, text.posY, text.text);
                continue;
            }
            if(r instanceof TextSize){
                TextSize textSize = ((TextSize) r);
                NanoVG.nvgFontSize(Reactor.getVg(), textSize.size);
                continue;
            }
            if(r instanceof TextFont){
                TextFont textFont = ((TextFont) r);
                NanoVG.nvgFontFace(Reactor.getVg(), textFont.fontFace);
                continue;
            }
            if(r instanceof TextAlignmentDirective){
                TextAlignmentDirective alignment = ((TextAlignmentDirective) r);
                NanoVG.nvgTextAlign(Reactor.getVg(), alignment.alignment);
                continue;
            }
            if(r instanceof LineRender){
                LineRender line = ((LineRender) r);

                NanoVG.nvgBeginPath(vg);

                NanoVG.nvgMoveTo(vg, line.p1x, line.p1y);
                NanoVG.nvgLineTo(vg, line.p2x, line.p2y);

                NVGColor color = NVGColor.create();
                NanoVG.nvgRGBA((byte)line.color.x, (byte)line.color.y, (byte)line.color.z, (byte)line.color.w, color);
                NanoVG.nvgStrokeColor(vg, color);
                NanoVG.nvgStrokeWidth(vg,line.thickness);
                NanoVG.nvgStroke(vg);

                NanoVG.nvgClosePath(vg);

                continue;
            }
            if(r instanceof SpriteRender){
                SpriteRender sprite = (SpriteRender) r;
                NVGPaint img = NVGPaint.create();

                NanoVG.nvgTranslate(vg, sprite.posx, sprite.posy);
                NanoVG.nvgRotate(vg, sprite.angle);
                NanoVG.nvgBeginPath(vg);
                NanoVG.nvgImagePattern(vg, 0, 0, sprite.width, sprite.height, 0, sprite.textureID, sprite.alpha, img);
                NanoVG.nvgRect(vg, 0, 0, sprite.width, sprite.height);
                NanoVG.nvgFillPaint(vg, img);
                NanoVG.nvgFill(vg);

                NanoVG.nvgClosePath(vg);
                NanoVG.nvgRotate(vg, -sprite.angle);
                NanoVG.nvgTranslate(vg, -sprite.posx, -sprite.posy);


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
                NanoVG.nvgClosePath(vg);
                continue;
            }

            if(r instanceof RectangleRender){
                RectangleRender rect = ((RectangleRender)r);
                NanoVG.nvgBeginPath(vg);
                NVGColor color = NVGColor.create();
                NanoVG.nvgRGBA((byte)rect.color.x, (byte)rect.color.y, (byte)rect.color.z, (byte)rect.color.w, color);
                NanoVG.nvgFillColor(Reactor.getVg(), color);
                NanoVG.nvgRoundedRect(Reactor.getVg(), rect.x, rect.y, rect.width, rect.height, rect.corner);
                NanoVG.nvgFill(vg);
                NanoVG.nvgClosePath(vg);
            }
        }

        //Render foreground
        for(ColorRender colorRender : foregroundColors){
            NVGColor colorC = NVGColor.create();
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRGBA((byte)colorRender.color.x, (byte)colorRender.color.y, (byte)colorRender.color.z, (byte)colorRender.color.w, colorC);
            NanoVG.nvgFillColor(Reactor.getVg(), colorC);
            NanoVG.nvgRect(Reactor.getVg(), colorRender.posx, colorRender.posy, colorRender.width, colorRender.height);
            NanoVG.nvgFill(vg);
            NanoVG.nvgClosePath(vg);
        }


        NanoVG.nvgEndFrame(Reactor.getVg());

        backgroundColors.clear();
        foregroundColors.clear();
        drawCalls.clear();
    }

    // Text functions
    public void drawText(float posx, float posy, String string){
        drawCalls.addLast(new TextRender(posx, posy, string));
    }

    public void drawText(float posx, float posy, String string, Vector4f color){
        drawCalls.addLast(new TextRender(posx, posy, string).setColor(color));
    }

    public void setTextSize(float size){
        drawCalls.addLast(new TextSize(size));
    }

    public void setTextFont(String fontFamily){
        if(FontLoader.getInstance().hasFont(fontFamily)) {
            drawCalls.addLast(new TextFont(fontFamily));
        }
    }

    public void setTextAlignment(TextAlignment ... textAlignment){
        int total = TextAlignment.TOP_LEFT.alignment;

        if(textAlignment.length > 0){
            total = 0;
            for(TextAlignment alignment : textAlignment){
                total = total | alignment.alignment;
            }
        }

        drawCalls.addLast(new TextAlignmentDirective(total));
    }

    public void drawLine(float p1x, float p1y, float p2x, float p2y, Vector4f color) {
        drawCalls.addLast(new LineRender(p1x,  p1y,  p2x,  p2y).setColor(color));
    }

    public void drawLine(float p1x, float p1y, float p2x, float p2y, Vector4f color, float thickness) {
        drawCalls.addLast(new LineRender(p1x,  p1y,  p2x,  p2y).setColor(color).setLineWidth(thickness));
    }

    //Basic image
    public void drawImage(float x, float y, float width, float height, int textureID){
        drawCalls.addLast(new SpriteRender(x, y, width, height, textureID));
    }

    public void drawImage(float x, float y, float width, float height, Sprite sprite){
        drawCalls.addLast(new SpriteRender(x, y, width, height, sprite));
    }

    public void drawImage(float x, float y, int textureID){
        Sprite s = SpriteBinder.getInstance().getSprite(textureID);
        drawCalls.addLast(new SpriteRender(x, y, s.getWidth(), s.getHeight(), textureID));
    }

    public void drawImage(float x, float y, Sprite sprite){
        drawCalls.addLast(new SpriteRender(x, y, sprite.getWidth(), sprite.getHeight(), sprite));
    }

    //Image with angle
    public void drawImageRotated(float x, float y, float width, float height, int textureID, float angle){
        drawCalls.addLast(new SpriteRender(x, y, width, height, textureID).setAngle(angle));
    }

    public void drawImageRotated(float x, float y, float width, float height, Sprite sprite, float angle){
        drawCalls.addLast(new SpriteRender(x, y, width, height, sprite).setAngle(angle));
    }

    public void drawImageRotated(float x, float y, int textureID, float angle){
        Sprite s = SpriteBinder.getInstance().getSprite(textureID);
        drawCalls.addLast(new SpriteRender(x, y, s.getWidth(), s.getHeight(), textureID).setAngle(angle));
    }

    public void drawImageRotated(float x, float y, Sprite sprite, float angle){
        drawCalls.addLast(new SpriteRender(x, y, sprite.getWidth(), sprite.getHeight(), sprite).setAngle(angle));
    }

    //Image with alpha
    public void drawImageWithAlpha(float x, float y, float width, float height, int textureID, float alpha){
        drawCalls.addLast(new SpriteRender(x, y, width, height, textureID).setAlpha(alpha));
    }

    public void drawImageWithAlpha(float x, float y, float width, float height, Sprite sprite, float alpha){
        drawCalls.addLast(new SpriteRender(x, y, width, height, sprite).setAlpha(alpha));
    }

    public void drawImageWithAlpha(float x, float y, int textureID, float alpha){
        Sprite s = SpriteBinder.getInstance().getSprite(textureID);
        drawCalls.addLast(new SpriteRender(x, y, s.getWidth(), s.getHeight(), textureID).setAlpha(alpha));
    }

    public void drawImageWithAlpha(float x, float y, Sprite sprite, float alpha){
        drawCalls.addLast(new SpriteRender(x, y, sprite.getWidth(), sprite.getHeight(), sprite).setAlpha(alpha));
    }

    //Image with angle and alpha
    public void drawImageRotatedWithAlpha(float x, float y, float width, float height, int textureID, float angle, float alpha){
        drawCalls.addLast(new SpriteRender(x, y, width, height, textureID).setAlpha(alpha).setAngle(angle));
    }

    public void drawImageRotatedWithAlpha(float x, float y, float width, float height, Sprite sprite, float angle, float alpha){
        drawCalls.addLast(new SpriteRender(x, y, width, height, sprite).setAlpha(alpha).setAngle(angle));
    }

    public void drawImageRotatedWithAlpha(float x, float y, int textureID, float angle, float alpha){
        Sprite s = SpriteBinder.getInstance().getSprite(textureID);
        drawCalls.addLast(new SpriteRender(x, y, s.getWidth(), s.getHeight(), textureID).setAlpha(alpha).setAngle(angle));
    }

    public void drawImageRotatedWithAlpha(float x, float y, Sprite sprite, float angle, float alpha){
        drawCalls.addLast(new SpriteRender(x, y, sprite.getWidth(), sprite.getHeight(), sprite).setAlpha(alpha).setAngle(angle));
    }

    //TODO gradients
    //Background colors
    public void drawColorBackground(float posx, float posy, float width, float height, Vector4f col){
        backgroundColors.addLast(new ColorRender(posx, posy, width, height, col));
    }

    public void fillColorBackground(Vector4f col){
        backgroundColors.addLast(new ColorRender(0, 0, Renderer.getWIDTH(), Renderer.getHEIGHT(), col));
    }

    public void drawColorForeground(float posx, float posy, float width, float height, Vector4f col){
        foregroundColors.addLast(new ColorRender(posx, posy, width, height, col));
    }

    public void fillColorForeground(Vector4f col){
        foregroundColors.addLast(new ColorRender(0,0, Renderer.getWIDTH(), Renderer.getHEIGHT(), col));
    }

    public void drawRect(float x, float y, float width, float height, Vector4f color){
        drawCalls.addLast(new RectangleRender(x, y, width, height).setColor(color));
    }

    public void drawRect(float x, float y, float width, float height, Vector4f color, float cornerRadius){
        drawCalls.addLast(new RectangleRender(x, y, width, height).setColor(color).setCornerRoundness(cornerRadius));
    }

    //Color Utils
    public Vector4f getDrawColor(String hexColor, float alpha){
        if(!this.hexColorCache.containsKey(hexColor)){

            if(!hexColor.startsWith("#")){
                hexColor = "#"+hexColor;
            }

            if(!regex.matcher(hexColor).matches() || hexColor.length() < 3){
                return new Vector4f(255);
            }

            //Remove the Hash character
            String removedHash = hexColor.substring(1);

            Vector4f color = new Vector4f(255);

            if(hexColor.length() == 3){
                color.x = Integer.parseInt(removedHash.substring(0, 1), 16);
                color.y = Integer.parseInt(removedHash.substring(1, 2), 16);
                color.z = Integer.parseInt(removedHash.substring(2, 3), 16);
            }else{
                color.x = Integer.parseInt(removedHash.substring(0, 2), 16);
                color.y = Integer.parseInt(removedHash.substring(2, 4), 16);
                color.z = Integer.parseInt(removedHash.substring(4, 6), 16);
            }

            // Alpha
            color.w = Math.max(Math.min(alpha, 1), 0) * 255f;

            this.hexColorCache.put(hexColor, color);
        }
        return this.hexColorCache.get(hexColor);
    }

    public Vector4f getDrawColor(String hexColor){
        return getDrawColor(hexColor, 1f);
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
        Vector4f color = WHITE;

        TextRender(float posX, float posY, String text){
            this.posX = posX;
            this.posY = posY;
            this.text = text;
        }

        public Renderable setColor(Vector4f color) {
            this.color = color;
            return this;
        }
    }

    private class TextSize implements Renderable{
        float size;

        public TextSize(float size) {
            this.size = size;
        }
    }

    private class TextFont implements Renderable{
        String fontFace;

        public TextFont(String fontFace) {
            this.fontFace = fontFace;
        }
    }

    private class TextAlignmentDirective implements Renderable{
        int alignment;

        public TextAlignmentDirective(int alignment) {
            this.alignment = alignment;
        }
    }

    private class SpriteRender implements Renderable{
        float posx;
        float posy;
        float width;
        float height;

        boolean invertX = false;
        boolean invertY = false;

        float angle = 0;
        float alpha = 1;

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

        public SpriteRender setAlpha(float alpha){
            this.alpha = alpha;
            return this;
        }

        public SpriteRender setAngle(float angle){
            this.angle = alpha;
            return this;
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
        float thickness = 5.5f;
        Vector4f color = WHITE;

        public LineRender(float p1x, float p1y, float p2x, float p2y) {
            this.p1x = p1x;
            this.p1y = p1y;
            this.p2x = p2x;
            this.p2y = p2y;
        }

        public LineRender setColor(Vector4f color){
            this.color = color;
            return this;
        }
        public LineRender setLineWidth(float width){
            this.thickness = width;
            return this;
        }
    }

    private class RectangleRender implements Renderable{
        float x;
        float y;
        float width;
        float height;
        float corner = 0f;

        Vector4f color = new Vector4f(255);

        public RectangleRender(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }


        public RectangleRender setColor(Vector4f color){
            this.color = color;
            return this;
        }
        public RectangleRender setCornerRoundness(float corner){
            this.corner = corner;
            return this;
        }
    }
}
