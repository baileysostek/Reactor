package models;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import graphics.renderer.GLTarget;
import graphics.renderer.ShaderManager;
import graphics.renderer.EnumGLDatatype;
import graphics.renderer.Handshake;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;
import util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

//Singleton design pattern
public class ModelManager {

    //TODO create a scene structure with scenes and scene transitions. Scenes can register a need for specific models, and on transition to scene the entity manager will unload all uneded data, and load new data in

    private static ModelManager modelManager;
    private HashMap<String, Model> cachedModels = new HashMap<>();
    private int modelIDOffset = 0;

    private JsonParser parser = new JsonParser();

    private final Model DEFAULT_MODEL;

    private ModelManager(){
        //TODO fix
        DEFAULT_MODEL = loadModel("sphere_smooth.tek").get(0);
    }

    //For now just obj files
    public LinkedList<Model> loadModel(String modelName){
        //Check to see if model is cached
        if(cachedModels.containsKey(modelName)){
            LinkedList<Model> out = new LinkedList();
            out.add(cachedModels.get(modelName));
            return out;
        }

        //Check to see that a file extension has been specified
        if(!modelName.contains(".")){
            System.err.println("Tried to load model: " + modelName + " however no file extension is specified. This information is needed to correctly parse the file.");
        }

        String resourceName = StringUtils.getRelativePath() + "models/" + modelName;

        String data = StringUtils.load("models/" + modelName);
        String[] lines = data.split("\n");
        String fileExtension = modelName.split("\\.")[1];

        //TODO replace with log manager call
        System.out.println("Successfully loaded file: " + modelName + " File extension: " + fileExtension + " : Lines:" + lines.length);

        switch(fileExtension){
            case "tek": {
                Model model = new Model(this.getNextID()).deserialize(parser.parse(data).getAsJsonObject());
                cachedModels.put(modelName, model);
                LinkedList<Model> out = new LinkedList();
                out.add(model);
                return out;
            }
            default: {
                //We import a scene based on our model file.

                AIPropertyStore store = Assimp.aiCreatePropertyStore();
                Assimp.aiSetImportPropertyFloat(store, Assimp.AI_CONFIG_IMPORT_ASE_RECONSTRUCT_NORMALS, 0);
                Assimp.aiSetImportPropertyFloat(store, Assimp.AI_CONFIG_IMPORT_IFC_SMOOTHING_ANGLE, 0);

                AIScene aiScene = Assimp.aiImportFileExWithProperties(resourceName,
//                                Assimp.aiProcess_JoinIdenticalVertices |
                         Assimp.aiProcess_Triangulate |
//                                Assimp.aiProcess_GenSmoothNormals|
                                Assimp.aiProcess_FlipUVs
//                                Assimp.aiProcess_CalcTangentSpace |
//                                Assimp.aiProcess_LimitBoneWeights |
//                                 Assimp.aiProcess_FixInfacingNormals |
//                                 Assimp.aiProcess_GenBoundingBoxes,
                                 ,
                        null,
                        store
                );

                if (aiScene == null) {
                    System.out.println("Error loading:" + fileExtension);
                    LinkedList<Model> out = new LinkedList();
                    out.add(DEFAULT_MODEL);
                    return out;
                }

                return parseAssimp(aiScene);
            }
        }
    }

    public Vector3f[] getAABB(LinkedList<Vector3f>  verteciesList){
        Vector3f min = new Vector3f(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
        Vector3f max = new Vector3f(Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE);

        for(Vector3f vertex : verteciesList){

            if(vertex.x()>max.x()){
                max.x = (vertex.x());
            }
            if(vertex.y()>max.y()){
                max.y = (vertex.y());
            }
            if(vertex.z()>max.z()){
                max.z = (vertex.z());
            }
            if(vertex.x()<min.x()){
                min.x = (vertex.x());
            }
            if(vertex.y()<min.y()){
                min.y = (vertex.y());
            }
            if(vertex.z()<min.z()){
                min.z = (vertex.z());
            }
        }

        Vector3f[] out = new Vector3f[]{min, max};

        return out;
    }

    private LinkedList<Model> parseAssimp(AIScene scene){
        LinkedList<Model> out = new LinkedList<>();

        for(int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++){
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshIndex));

            //Put our lists into buffers for this Model.
            float[] vPositions = new float[mesh.mNumFaces() * 3 * 3];
            float[] vNormals = new float[mesh.mNumFaces() * 3 * 3];
            float[] vTextures = new float[mesh.mNumFaces() * 3 * 2];

            //For each vertex, get all data we need
            for(int i = 0; i < mesh.mNumVertices(); i++){
                //Get ref to vector
                AIVector3D position = mesh.mVertices().get(i);
                AIVector3D normal   = mesh.mNormals().get(i);
                AIVector3D texture = null;
                if(mesh.mTextureCoords(0) != null) {
                    texture = mesh.mTextureCoords(0).get(i);
                }

                //Add to our array
                vPositions[(i * 3) + 0] = position.x();
                vPositions[(i * 3) + 1] = position.y();
                vPositions[(i * 3) + 2] = position.z();

                vNormals[(i * 3) + 0] = normal.x();
                vNormals[(i * 3) + 1] = normal.y();
                vNormals[(i * 3) + 2] = normal.z();

                if(texture != null) {
                    vTextures[(i * 2) + 0] = texture.x();
                    vTextures[(i * 2) + 1] = texture.y();
                }else{
                    vTextures[(i * 2) + 0] = 0;
                    vTextures[(i * 2) + 1] = 0;
                }
            }

            Handshake modelHandshake = new Handshake();
            modelHandshake.addAttributeList("vPosition", vPositions, EnumGLDatatype.VEC3);
            modelHandshake.addAttributeList("vNormal", vNormals, EnumGLDatatype.VEC3);
            modelHandshake.addAttributeList("vColor", vNormals, EnumGLDatatype.VEC3);
            modelHandshake.addAttributeList("vTexture", vTextures, EnumGLDatatype.VEC2);

            AIVector3D min = mesh.mAABB().mMin();
            AIVector3D max = mesh.mAABB().mMax();

            //TODO refactor to grouped model.
            out.push(new Model(this.getNextID(), modelHandshake, mesh.mNumFaces() * 3, new Vector3f[]{new Vector3f(min.x(), min.y(), min.z()), new Vector3f(max.x(), max.y(), max.z())}));

        }

        return out;
    }


    public static void initialize(){
        if(modelManager == null){
            modelManager = new ModelManager();
        }
    }

    public static ModelManager getInstance(){
        return modelManager;
    }

    public int getNextID(){
        int out = this.cachedModels.size()+modelIDOffset;
        modelIDOffset++;
        return out;
    }

    public void incrementCachedModels() {
        modelIDOffset++;
    }
}
