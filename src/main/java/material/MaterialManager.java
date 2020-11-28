package material;

import java.util.HashMap;

public class MaterialManager {

    private static MaterialManager materialManager;

    private static HashMap<Integer, Material> materials = new HashMap<>();

    private MaterialManager(){

    }

    public static void initialize(){
        if(materialManager == null){
            materialManager = new MaterialManager();
        }
    }

    public static MaterialManager getInstance(){
        return materialManager;
    }

}
