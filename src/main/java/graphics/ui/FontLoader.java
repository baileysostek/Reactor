package graphics.ui;

import engine.Singleton;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryUtil;
import util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Queue;

public class FontLoader{
    private HashMap<String, ByteBuffer> loadedFonts = new HashMap<>();

    private static FontLoader fontLoader;

    private FontLoader(){

    }

    public void loadFont(String filePath, String id){
        int result = 0;
        try {
            ByteBuffer fontBuffer = StringUtils.loadRaw(StringUtils.getRelativePath() + filePath, 450 * 1024);
            result = NanoVG.nvgCreateFontMem(UIManager.getInstance().getNanoVG(), id, fontBuffer, 0);
            loadedFonts.put(id, fontBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result < 0){
            System.out.println("Font load error:" + result);
        }else{
            System.out.println("Loaded font.");
        }
    }

    public boolean hasFont(String id){
        return loadedFonts.containsKey(id);
    }

    public void cleanup(){
        for(ByteBuffer data : loadedFonts.values()){
            MemoryUtil.memFree(data);
        }
    }

    public static void initialize() {
        if(fontLoader == null){
            fontLoader = new FontLoader();
        }
    }

    public static FontLoader getInstance() {
        return fontLoader;
    }

}
