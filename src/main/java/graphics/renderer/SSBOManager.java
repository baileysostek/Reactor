package graphics.renderer;

import org.lwjgl.opengl.GL46;

import java.util.LinkedHashMap;

public class SSBOManager {
    private static SSBOManager singleton;
    private static int ssboLocations = 0;

    private LinkedHashMap<Integer, SSBO>   ssboInstances = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> namedSSBOs = new LinkedHashMap<>();

    private SSBOManager() {

    }

    public SSBO getSSBO(String name){
        if(this.namedSSBOs.containsKey(name)) {
            return this.ssboInstances.get(this.namedSSBOs.get(name));
        }
        return null;
    }

    public SSBO generateSSBO(String name, EnumGLDatatype datatype){
        int ssboID = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, ssboID);

        SSBO ssbo = new SSBO(ssboID, getUniqueLocation(), datatype);

        ssboInstances.put(ssboID, ssbo);

        this.namedSSBOs.put(name, ssboID);

        return ssbo;
    }

    public boolean hasSSBO(String ssboName){
        return this.namedSSBOs.containsKey(ssboName);
    }

    public static void initialize() {
        if (singleton == null) {
            singleton = new SSBOManager();
        }
    }

    public static SSBOManager getInstance() {
        return singleton;
    }

    public int getUniqueLocation() {
        int out = ssboLocations;
        ssboLocations++;
        return out;
    }
}