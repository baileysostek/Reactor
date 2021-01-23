package graphics.ui;

import engine.Reactor;
import graphics.renderer.Renderer;
import graphics.renderer.Window;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import input.MousePicker;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.system.MemoryUtil.NULL;

public class UIManager{

    private static UIManager uiManager;
    protected static long vg;

    private LinkedList<TextRender> textRenders = new LinkedList();

    private LinkedList<SpriteRender> spriteRenders = new LinkedList<>();

    private UIManager(){
        vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
        if (vg == NULL) {
            throw new RuntimeException("Could not init nanovg.");
        }else{
            System.out.println("NanoVG handle:" + vg);
        }

//        for(int i = 0; i < 1; i++){
//            spriteRenders.add(new SpriteRender(100 + (i * 16), 100 + (i * 16), 256, 256, SpriteBinder.getInstance().load("OrangeTree_BaseColor.png")));
//        }
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

        for(TextRender text : textRenders){
            NanoVG.nvgText(Reactor.getVg(), text.posX, text.posY, text.text);
        }

        NanoVG.nvgText(Reactor.getVg(), 4, 4, "FPS:" + Reactor.getFPS());

        int index = 0;

        for(SpriteRender sprite : spriteRenders){
            NVGPaint img = NVGPaint.create();

            NanoVG.nvgBeginPath(vg);

            NanoVG.nvgImagePattern(vg, sprite.posx, sprite.posy, sprite.width, sprite.height, 0, sprite.textureID, 1f, img);
            NanoVG.nvgRect(vg, sprite.posx, sprite.posy, sprite.width, sprite.height);
            NanoVG.nvgFillPaint(vg, img);
            NanoVG.nvgFill(vg);

            index++;
        }


//        NanoVG.nvgFontSize(Reactor.getVg(), 128);
//        NanoVG.nvgText(Reactor.getVg(), 0, 256, "Oh also there is font in this Josiah");
////
//        NanoVG.nvgFontSize(Reactor.getVg(), 24);
//        NanoVG.nvgText(Reactor.getVg(), 0, 364, "FPS:" + Reactor.getFPS());

//        NVGColor colorC = NVGColor.create();
//        NanoVG.nvgRGBA((byte)255, (byte)0, (byte)0, (byte)255, colorC);
//        NanoVG.nvgFontSize(Reactor.getVg(), 70);
//        NanoVG.nvgFillColor(Reactor.getVg(), colorC);
//        NanoVG.nvgText(Reactor.getVg(), 0, 512, "Also the background is RED");


        NanoVG.nvgEndFrame(Reactor.getVg());

        textRenders.clear();

    }

    public void drawString(float posx, float posy, String string){
        textRenders.add(new TextRender(posx, posy, string));
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
}

class TextRender{
    float posX;
    float posY;
    String text;

    TextRender(float posX, float posY, String text){
        this.posX = posX;
        this.posY = posY;
        this.text = text;
    }
}

class SpriteRender{
    float posx;
    float posy;
    float width;
    float height;

    boolean invertX = false;
    boolean invertY = false;

    Sprite sprite;

    int textureID;

    SpriteRender(float x, float y, float width, float height, Sprite sprite){
        this.posx = x;
        this.posy = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;

        this.textureID = NanoVGGL3.nvglCreateImageFromHandle(UIManager.vg, sprite.getTextureID(), 16, 16, NanoVGGL3.NVG_IMAGE_NODELETE);
    }

}
