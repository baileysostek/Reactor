package models;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import graphics.renderer.GLTarget;
import graphics.renderer.ShaderManager;
import graphics.renderer.EnumGLDatatype;
import graphics.renderer.Handshake;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;
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

    private final GltfModelReader gltfModelReader = new GltfModelReader();

    private ModelManager(){
        DEFAULT_MODEL = loadModel("sphere_smooth.tek");
    }

    //For now just obj files
    public Model loadModel(String modelName){
        //Check to see if model is cached
        if(cachedModels.containsKey(modelName)){
            return cachedModels.get(modelName);
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
            case "obj": {
                Model model = parseOBJ(this.getNextID(), lines);
                cachedModels.put(modelName, model);

                return model;
            }
            case "tek": {
                Model model = new Model(this.getNextID()).deserialize(parser.parse(data).getAsJsonObject());
                cachedModels.put(modelName, model);
                return model;
            }
            case "gltf":{
                Path inputFile = Paths.get(StringUtils.getRelativePath()+"models/", modelName);
                try {
                    GltfModel gltfModel = gltfModelReader.read(inputFile.toUri());
                    System.out.println(gltfModel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            default:
                System.err.println("No model parsing function defined for file extension type: " + fileExtension);
                return null;
        }
    }

    private Model parseOBJ(int id, String[] lines){
        try {
            //Buffer lists
            LinkedList<Vector3f> verteciesList = new LinkedList<>();
            LinkedList<Vector3f> nomrmalsList = new LinkedList<>();
            LinkedList<Vector3f> facesList = new LinkedList<>();
            LinkedList<Vector2f> textureVectors = new LinkedList<>();
            LinkedList<Vector3f> facesList_normal = new LinkedList<>();
            LinkedList<Vector2f> facesList_texture = new LinkedList<>();

            //Arrays of data
            Vector3f[] vertecies = null;
            Vector3f[] normals = null;
            Vector2f[] textures = null;

            //Lists of raw floats to buffer into shader (VAO / Handshake)
            float[] vPositions = null;
            float[] vNormals = null;
            float[] vTextures = null;

            int faceCount = 0;

            // for .mtl files
            boolean hasTexture = false;
            Vector2f overrideCoords = new Vector2f(0);
            String mtllib = "";
            int matScale = 0;
            String faceMaterial = "";

            HashMap<String, Vector3f> materials = new HashMap<>();
            HashMap<String, Vector2f> materialLocations = new HashMap<>();

            //Parse data from file
            for (String line : lines) {
                //Loading material library
                if (line.startsWith("mtllib ")) {
                    mtllib = line.replace("mtllib ", "");
                    //Now that we have found a material file, lets look up that file and read in all of the materials
                    System.out.println("Looking for lib:" + "models/" + mtllib.trim());
                    String[] matFile = StringUtils.load("models/" + mtllib.trim()).split("\n");
                    String currentMaterial = "";
                    for (String matLine : matFile) {
                        if (matLine.startsWith("newmtl ")) {
                            currentMaterial = matLine.replace("newmtl ", "");
                        }
                        //Get diffuse color
                        if (currentMaterial != null && matLine.startsWith("Kd ")) {
                            String[] colorComponents = matLine.replace("Kd ", "").split(" ");
                            Vector3f color = new Vector3f(Float.parseFloat(colorComponents[0]), Float.parseFloat(colorComponents[1]), Float.parseFloat(colorComponents[2])).mul(255.0f);
                            materials.put(currentMaterial, color);
                        }
                    }
                    matScale = (int) Math.ceil(Math.sqrt(materials.size()));
                    {
                        int i = 0;
                        for (String mat : materials.keySet()) {
                            float xPos = (i % (float) matScale) / (float) matScale;
                            float yPos = (float) Math.floor(i / (float) matScale) / (float) matScale;
                            materialLocations.put(mat, new Vector2f(xPos, yPos));
                            System.out.println("Location:" + new Vector2f(xPos, yPos));
                            i++;
                        }
                    }
                    //Check to see if this models image exists, if not create it here
//                String bitmapFileName = mtllib.trim().replace(".mtl", ".png");
//                Bitmap bmp = AssetManager.getInstance().readImage(bitmapFileName);
//                if(bmp == null){
//                    int[] colors = new int[matScale * matScale];
//                    for(int i = 0; i < colors.length; i++){
//                        colors[i] = 0;
//                    }
//                    bmp = Bitmap.createBitmap(colors, matScale, matScale, Bitmap.Config.ARGB_8888);
//                    int i = 0;
//                    for (String mat : materials.keySet()) {
//                        int xPos = (int) Math.floor(i % (float) matScale);
//                        int yPos = (int) Math.floor(i / (float) matScale);
//                        Vector3f color = materials.get(mat);
//                        bmp.setPixel(xPos, yPos, Color.argb(1, color.x(), color.y(), color.z()));
//                        i++;
//                    }
//                    SpriteManager.getInstance().putTexture(bitmapFileName, bmp);
//                }
                }
                //Loading vertices
                if (line.startsWith("v ") || line.startsWith("vn ") || line.startsWith("vt ")) {
                    if (line.startsWith("v ")) {
                        //vertex
                        line = line.replace("v ", "");
                        String[] components = line.split(" ");
                        Vector3f vector = new Vector3f(Float.parseFloat(components[0]), Float.parseFloat(components[1]), Float.parseFloat(components[2])).mul(1.0f);
                        verteciesList.addFirst(vector); // More memory efficient because we do not need to traverse the whole list to add a new element. Although, this LL interface may hold pointer to end of list.
                    }
                    if (line.startsWith("vn ")) {
                        //vertex
                        line = line.replace("vn ", "");
                        String[] components = line.split(" ");
                        Vector3f vector = new Vector3f(Float.parseFloat(components[0]), Float.parseFloat(components[1]), Float.parseFloat(components[2])).mul(1.0f);
                        nomrmalsList.addFirst(vector); // More memory efficient because we do not need to traverse the whole list to add a new element. Although, this LL interface may hold pointer to end of list.
                    }
                    if (line.startsWith("vt ")) {
                        hasTexture = true;
                        //vertex
                        line = line.replace("vt ", "");
                        String[] components = line.split(" ");
                        Vector2f vector = new Vector2f(Float.parseFloat(components[0]), Float.parseFloat(components[1])).mul(new Vector2f(1, -1));
                        textureVectors.addFirst(vector); // More memory efficient because we do not need to traverse the whole list to add a new element. Although, this LL interface may hold pointer to end of list.
                    }
                } else {
                    if (line.startsWith("usemtl ") && !hasTexture) {
                        //Force our Texture coords to be different
                        String materialName = line.replace("usemtl ", "");
                        faceMaterial = materialName;
                        System.out.println("Setting color to:" + materials.get(materialName));

                    }
                    if (line.startsWith("f ")) {
                        faceCount++;
                        if (vertecies == null) {
                            System.out.println("Vertecies have been loaded, Buffering to array:" + verteciesList.size());
                            vertecies = new Vector3f[verteciesList.size()];
                            int index = verteciesList.size() - 1;
                            for (Vector3f vec : verteciesList) {
                                vertecies[index] = vec;
                                index--;
                            }
                            System.out.println("Normals have been loaded, Buffering to array:" + nomrmalsList.size());
                            normals = new Vector3f[nomrmalsList.size()];
                            index = nomrmalsList.size() - 1;
                            for (Vector3f vec : nomrmalsList) {
                                normals[index] = vec;
                                index--;
                            }
                            System.out.println("Textures have been loaded, Buffering to array:" + textureVectors.size());
                            textures = new Vector2f[textureVectors.size()];
                            index = textureVectors.size() - 1;
                            for (Vector2f vec : textureVectors) {
                                textures[index] = vec;
                                index--;
                            }
                        }
                    }
                }

                //Loading vertices
                if (line.startsWith("f ")) {
                    //vertex
                    line = line.replace("f ", "");
                    String[] components = line.split(" ");
                    int vertexIndex = 0;

                    //First Face Buffer
                    Vector3f firstFace = new Vector3f(0);
                    Vector3f firstNormal = new Vector3f(0);
                    Vector2f firstTexture = new Vector2f(0);

                    //Last Face Buffer
                    Vector3f lastFace = new Vector3f(0);
                    Vector3f lastNormal = new Vector3f(0);
                    Vector2f lastTexture = new Vector2f(0);

                    for (String component : components) {

                        //If this index is >= 3 this face is a poly-face and we need to insert from chace
                        if(vertexIndex >= 3){
                            //Add the first point
                            facesList.addLast(firstFace);
                            facesList_normal.addLast(firstNormal);
                            facesList_texture.addLast(firstTexture);

                            //Add the last point
                            facesList.addLast(lastFace);
                            facesList_normal.addLast(lastNormal);
                            facesList_texture.addLast(lastTexture);

                            //Now add the new point.
                        }

                        String[] componentParts = component.split("/");
                        if (componentParts.length == 3) {
                            int index = Integer.parseInt(componentParts[0].trim()); //Index   //Always
                            int textureIndex = 1;
                            if (!componentParts[1].trim().isEmpty()) {
                                textureIndex = Integer.parseInt(componentParts[1].trim()); //texture //Sometimes
                            }
                            int normalVector = Integer.parseInt(componentParts[2].trim()); //Normal  //Always
                            //Calculate this face
                            lastFace = vertecies[index - 1];
                            lastNormal = normals[normalVector - 1];

                            facesList.addLast(lastFace);
                            facesList_normal.addLast(lastNormal);

                            //Textures
                            if ((textureIndex - 1) < textures.length) {
                                lastTexture = textures[textureIndex - 1];
                                facesList_texture.addLast(lastTexture);
                            } else {
                                //We dont have a texture but we do have material channels
                                lastTexture = materialLocations.get(faceMaterial);
                                facesList_texture.addLast(lastTexture);
                                System.out.println("Using override texture index:" + materialLocations.get(faceMaterial));
                            }


                        } else if (componentParts.length == 2) {
                            component = component.replaceAll("/", "//");
                            componentParts = component.split("/");
                            int index = Integer.parseInt(componentParts[0].trim()); //Index   //Always
                            int textureIndex = 1;
                            if (!componentParts[1].trim().isEmpty()) {
                                textureIndex = Integer.parseInt(componentParts[1].trim()); //texture //Sometimes
                            }
                            int normalVector = Integer.parseInt(componentParts[2].trim()); //Normal  //Always

                            lastFace = vertecies[index - 1];
                            lastNormal = normals[normalVector - 1];

                            facesList.addLast(lastFace);
                            facesList_normal.addLast(lastNormal);

                            //Textures
                            if ((textureIndex - 1) < textures.length) {
                                lastTexture = textures[textureIndex - 1];
                                facesList_texture.addLast(lastTexture);
                            } else {
                                //We dont have a texture but we do have material channels
                                lastTexture = materialLocations.get(faceMaterial);
                                facesList_texture.addLast(lastTexture);
                                System.out.println("Using override texture index:" + materialLocations.get(faceMaterial));
                            }
                        }

                        //Buffer the last results
                        if(vertexIndex == 0){
                            firstFace = lastFace;
                            firstNormal = lastNormal;
                            firstTexture = lastTexture;
                        }

                        vertexIndex++;
                    }
                }
            }

            //Put our lists into buffers for this Model.
            vPositions = new float[facesList.size() * 3];
            vNormals = new float[facesList.size() * 3];
            vTextures = new float[facesList.size() * 2];


            System.out.println("Building Model:");
            int index = 0;
            for(Vector3f face : facesList){
                vPositions[(index * 3) + 0] = face.x();
                vPositions[(index * 3) + 1] = face.y();
                vPositions[(index * 3) + 2] = face.z();
                index++;
            }

            index = 0;
            for(Vector2f texture : facesList_texture) {
                vTextures[(index * 2) + 0] = texture.x();
                vTextures[(index * 2) + 1] = 1 - (texture.y() * -1);
                index++;
            }

            index = 0;
            for(Vector3f normal : facesList_normal) {
                vNormals[(index * 3) + 0] = normal.x();
                vNormals[(index * 3) + 1] = normal.y();
                vNormals[(index * 3) + 2] = normal.z();
                index++;
            }

            //Done
            System.out.println("Built");

            Handshake modelHandshake = new Handshake();
            modelHandshake.addAttributeList("vPosition", vPositions, EnumGLDatatype.VEC3);
            modelHandshake.addAttributeList("vColor", vNormals, EnumGLDatatype.VEC3);
            modelHandshake.addAttributeList("vNormal", vNormals, EnumGLDatatype.VEC3);
            modelHandshake.addAttributeList("vTexture", vTextures, EnumGLDatatype.VEC2);

            System.out.println("AABB");
            for (Vector3f vec : getAABB(verteciesList)) {
                System.out.println(vec);
            }

            return new Model(id, modelHandshake, faceCount * 3, getAABB(verteciesList));
        }catch(Exception e){
            e.printStackTrace();
            return DEFAULT_MODEL;
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
