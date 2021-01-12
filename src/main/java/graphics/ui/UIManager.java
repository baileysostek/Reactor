package graphics.ui;

import engine.Reactor;
import graphics.renderer.Renderer;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.system.MemoryUtil.NULL;

public class UIManager{

    private static UIManager uiManager;
    private long vg;

    private LinkedList<TextRender> textRenders = new LinkedList();

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
        NanoVG.nvgFontFace(Reactor.getVg(), "roboto");
        NanoVG.nvgTextAlign(Reactor.getVg(), NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP);

        for(TextRender text : textRenders){
            NanoVG.nvgText(Reactor.getVg(), text.posX, text.posY, text.text);
        }

        NanoVG.nvgText(Reactor.getVg(), 4, 4, "FPS:" + Reactor.getFPS());

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
