package models;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import graphics.renderer.*;
import graphics.sprite.Colors;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import material.Material;
import material.MaterialManager;
import math.VectorUtils;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import util.StringUtils;

import java.io.*;
import java.lang.Math;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static org.lwjgl.system.MemoryUtil.*;
import static util.StringUtils.resizeBuffer;

//Singleton design pattern
public class ModelManager {

    //TODO create a scene structure with scenes and scene transitions. Scenes can register a need for specific models, and on transition to scene the entity manager will unload all uneded data, and load new data in

    private static ModelManager modelManager;
    private HashMap<String, Model> cachedModels = new HashMap<>();
    private int modelIDOffset = 0;

    private JsonParser parser = new JsonParser();

    private final Model DEFAULT_MODEL;

    private final Matrix4f IDENTITY_MATRIX       = new Matrix4f().identity();

    private final float[]  IDENTITY_MATRIX_ARRAY = IDENTITY_MATRIX.get(new float[16]);

    private ModelManager(){
        //TODO fix
        DEFAULT_MODEL = loadModel("sphere_smooth.tek").getFirst();
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
                Model model = new Model(this.getNextID(), modelName).deserialize(parser.parse(data).getAsJsonObject());

                cachedModels.put(modelName, model);
                LinkedList<Model> out = new LinkedList();
                out.add(model);

                return out;
            }
            default: {
                //We import a scene based on our model file.

                AIPropertyStore store = Assimp.aiCreatePropertyStore();

//                AIFileIO fileIO = AIFileIO.create().OpenProc((pFileIO, fileName, openMode) -> {
//                    ByteBuffer buffer;
//                    String fileNameUtf8 = memUTF8(fileName);
//                    try {
//                        buffer = StringUtils.loadRaw(fileNameUtf8, 1024 * 150);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException("Could not open file: " + fileNameUtf8);
//                    }
//
//                    return AIFile.create()
//                            .ReadProc((pFile, pBuffer, size, count) -> {
//                                long max = Math.min(buffer.remaining(), size * count);
//                                memCopy(memAddress(buffer) + buffer.position(), pBuffer, max);
//                                return max;
//                            })
//                            .SeekProc((pFile, offset, origin) -> {
//                                if (origin == Assimp.aiOrigin_CUR) {
//                                    buffer.position(buffer.position() + (int) offset);
//                                } else if (origin == Assimp.aiOrigin_SET) {
//                                    buffer.position((int) offset);
//                                } else if (origin == Assimp.aiOrigin_END) {
//                                    buffer.position(buffer.limit() + (int) offset);
//                                }
//                                return 0;
//                            }).FileSizeProc(pFile -> buffer.limit()).address();
//                }).CloseProc((pFileIO, pFile) -> {
//                    AIFile aiFile = AIFile.create(pFile);
//
//                    aiFile.ReadProc().free();
//                    aiFile.SeekProc().free();
//                    aiFile.FileSizeProc().free();
//                });

                AIScene aiScene = Assimp.aiImportFileExWithProperties(resourceName,
                Assimp.aiProcess_Triangulate |
                        Assimp.aiProcess_FlipUVs |
                        Assimp.aiProcess_CalcTangentSpace |
                        Assimp.aiProcess_GenNormals |
                        Assimp.aiProcess_GenUVCoords |
//                                Assimp.aiProcess_OptimizeMeshes|
//                                Assimp.aiProcess_LimitBoneWeights |
                        Assimp.aiProcess_FixInfacingNormals |
                        Assimp.aiProcess_GenBoundingBoxes,
                    null,
                    store
                );

                if (aiScene == null) {
                    System.out.println("Error loading:" + fileExtension);
                    System.err.println(Assimp.aiGetErrorString());
                    LinkedList<Model> out = new LinkedList();
                    out.add(DEFAULT_MODEL);
                    return out;
                }

                LinkedList<Model> out = parseAssimp(modelName, aiScene);
                cachedModels.put(modelName, out.getFirst());
                return out;
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

    private Matrix4f toJOML(AIMatrix4x4 matrix){
        return new Matrix4f(
            matrix.a1(), matrix.b1(), matrix.c1(), matrix.d1(),
            matrix.a2(), matrix.b2(), matrix.c2(), matrix.d2(),
            matrix.a3(), matrix.b3(), matrix.c3(), matrix.d3(),
            matrix.a4(), matrix.b4(), matrix.c4(), matrix.d4()
        );
    }

    private LinkedHashMap<String, Animation> calculateAnimations(AIScene scene, HashMap<String, Joint> joints){
        LinkedHashMap<String, Animation>  animations = new LinkedHashMap<String, Animation>();
        //For Each animation
        for(int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation aiAnimation = AIAnimation.create(scene.mAnimations().get(i));
            String animationName = aiAnimation.mName().dataString();
            System.out.println("Animation found in model data:" + animationName);
            double duration = aiAnimation.mDuration();
            if(!Double.isFinite(duration)){
                duration = 1.0d;
            }
            Animation animation = new Animation(duration, (float)aiAnimation.mTicksPerSecond());
            HashMap<String, LinkedList<KeyFrame>> keyframes = new HashMap<>();
            HashMap<String, LinkedList<AIBone>> bones = new HashMap<>();

            for(String s : joints.keySet()){
                keyframes.put(s, new LinkedList<>());
            }

            //For each bone
            for(int c = 0; c < aiAnimation.mNumChannels(); c++){
                AINodeAnim nodeAnim = AINodeAnim.create(aiAnimation.mChannels().get(c));
                String boneName = nodeAnim.mNodeName().dataString();

                if(joints.containsKey(boneName)) {
                    //For each frame of animation for bone in animation
                    for (int f = 0; f < nodeAnim.mNumPositionKeys(); f++) {
                        double time = nodeAnim.mPositionKeys().get(f).mTime();
                        AIVector3D pos = nodeAnim.mPositionKeys().get(f).mValue();
                        AIQuaternion rot = nodeAnim.mRotationKeys().get(f).mValue();
                        KeyFrame keyFrame = new KeyFrame(time, new Matrix4f().identity().translate(pos.x(), pos.y(), pos.z()).rotate(new Quaternionf(rot.x(), rot.y(), rot.z(), rot.w())));

                        //Insertion Sort
                        int numBones = keyframes.get(boneName).size();
                        if(numBones >= 1) {
                            keyframes.get(boneName).addLast(null);
                            for (int j = 0; j < numBones; j++) {
                                KeyFrame otherFrame = keyframes.get(boneName).get(j);
                                if (keyframes.get(boneName).get(j) != null) {
                                    //if true Our new frame goes here
                                    if (otherFrame.timelinePosition <= keyFrame.timelinePosition) {
                                        for (int k = 0; k < (numBones - j); k++) {
                                            keyframes.get(boneName).set(numBones - k, keyframes.get(boneName).get(numBones - k - 1));
                                        }
                                        keyframes.get(boneName).set(j, keyFrame);
                                        break;
                                    }
                                }
                                //We are the biggest frame
                                keyframes.get(boneName).addLast(keyFrame);
                            }
                        }else{
                            keyframes.get(boneName).add(keyFrame);
                        }
                    }

                    //Flip the bones
                    LinkedList<KeyFrame> flippedFrames = new LinkedList<>();
                    for(KeyFrame frame : keyframes.get(boneName)){
                        flippedFrames.addFirst(frame);
                    }
                    keyframes.put(boneName, flippedFrames);
                }
            }

            animation.importKeyFrames(keyframes);

            animations.put(animationName, animation);
        }

        return animations;
    }

    private Joint calcualteBoneHeierarchy(AINode node, HashMap<String, Joint> joints){
        //Store this name.
        String nodeName = node.mName().dataString();

        //Check if this node is contained within the joints list
        if(joints.containsKey(nodeName)){
            Joint rootJoint = joints.get(nodeName);
            //This is the root joint! now calculate child joints.
            JointHelper(rootJoint, node, joints);
            return joints.get(nodeName);
        }


        for(int i = 0; i < node.mNumChildren(); i++){
            Joint childJoint = calcualteBoneHeierarchy( AINode.create(node.mChildren().get(i)), joints);
            if(childJoint != null){
                return childJoint;
            }
        }

        return null;
    }

    private void JointHelper(Joint parent, AINode possibleChildren, HashMap<String, Joint> joints){
        for(int i = 0; i < possibleChildren.mNumChildren(); i++){
            AINode possibleChildJoint = AINode.create(possibleChildren.mChildren().get(i));
            String possibleChildName = possibleChildJoint.mName().dataString();
            if(joints.containsKey(possibleChildName)){
                Joint childJoint = joints.get(possibleChildName);
                JointHelper(childJoint, possibleChildJoint, joints);
                parent.addChild(childJoint);
            }
        }
    }

    private LinkedList<Model> parseAssimp(String modelPath, AIScene scene){
        LinkedList<Model> out = new LinkedList<>();

        LinkedHashMap<Integer, Material> materials = new LinkedHashMap<>();

        LinkedHashMap<String, Joint> joints = new LinkedHashMap<>();

        String textureDirectory = "";
        if(modelPath.contains("/")){
            textureDirectory = modelPath.substring(0, modelPath.lastIndexOf("/")+1);
        }

        try {
            //Allocate our textures
            int numMaterials = scene.mNumMaterials();
            PointerBuffer aiMaterials = scene.mMaterials();
            for (int i = 0; i < numMaterials; i++) {
                AIMaterial material = AIMaterial.create(aiMaterials.get(i)); // wrap raw pointer in AIMaterial instance

                int materialIndex = i;

                Material mat = MaterialManager.getInstance().generateMaterial(SpriteBinder.getInstance().getFileNotFoundID());

                AIString path = AIString.calloc();
                Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
                String textPath = path.dataString();
                System.out.println("Texture file:" + textureDirectory+textPath);
                if(!textPath.isEmpty()) {
                    mat.setAlbedoID(SpriteBinder.getInstance().load(textureDirectory+textPath, StringUtils.getRelativePath()+"models/").getTextureID());
                }

//                AIString pathNormal = AIString.calloc();
//                Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_NORMALS, 0, pathNormal, (IntBuffer) null, null, null, null, null, null);
//                String textPathNormal = pathNormal.dataString();
//                System.out.println("Normal file:" + textureDirectory+textPathNormal);
//                if(!textPath.isEmpty()) {
//                    mat.setNormalID(SpriteBinder.getInstance().load(textureDirectory+textPathNormal, StringUtils.getRelativePath()+"models/").getTextureID());
//                }
//
//                AIString pathAo = AIString.calloc();
//                Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_LIGHTMAP, 0, pathAo, (IntBuffer) null, null, null, null, null, null);
//                String textPathAo = pathAo.dataString();
//                System.out.println("AO file:" + textureDirectory+textPathAo);
//                if(!textPath.isEmpty()) {
//                    mat.setAmbientOcclusionID(SpriteBinder.getInstance().load(textureDirectory+textPathAo, StringUtils.getRelativePath()+"models/").getTextureID());
//                }

//                AIString pathAo = AIString.calloc();
//                Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_LIGHTMAP, 0, pathAo, (IntBuffer) null, null, null, null, null, null);
//                String textPathAo = pathAo.dataString();
//                System.out.println("AO file:" + textureDirectory+textPathAo);
//                if(!textPath.isEmpty()) {
//                    mat.setAmbientOcclusionID(SpriteBinder.getInstance().load(textureDirectory+textPathAo, StringUtils.getRelativePath()+"models/").getTextureID());
//                }
                if(!materials.containsKey(materialIndex)) {
                    materials.put(materialIndex, mat);
                }

//                // Method 2: Parse material properties manually
//                PointerBuffer properties = material.mProperties(); // array of pointers to AIMaterialProperty structs
//                for ( int j = 0; j < properties.remaining(); j++ ) {
//                    AIMaterialProperty prop = AIMaterialProperty.create(properties.get(j));
//                    // parse property
//                    prop.mKey().dataString();
//                    String name = prop.mKey().dataString();
//                    if(name.contains("file")) {
//                        String propValue = StandardCharsets.UTF_8.decode(prop.mData()).toString();
//                        System.out.println("Texture file:" + propValue);
//                    }else{
//                        System.out.println("Other Prop:" + name + " = " + StandardCharsets.UTF_8.decode(prop.mData()).toString());
//                    }
//                }
            }

            //Load our mesh
            for (int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++) {
                AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshIndex));

                //Store the material
                int materialIndex = mesh.mMaterialIndex();

                //Number of vertices in this mesh
                int numVertices = mesh.mNumFaces() * 3;

                //Put our lists into buffers for this Model.
                float[] vPositions   = new float[numVertices * 3];
                float[] vNormals     = new float[numVertices * 3];
                float[] vTangents    = new float[numVertices * 3];
                float[] vBiTangents  = new float[numVertices * 3];
                float[] vTextures    = new float[numVertices * 2];

                int numBones = mesh.mNumBones();
                PointerBuffer aiBones = mesh.mBones();
                for (int i = 0; i < numBones; i++) {
                    AIBone aiBone = AIBone.create(aiBones.get(i));
                    String name = aiBone.mName().dataString();
                    joints.put(name, new Joint(i, name, this.toJOML(aiBone.mOffsetMatrix())));
                }

                //For each vertex, get all data we need
                for (int i = 0; i < mesh.mNumFaces(); i++) {
                    AIFace face = mesh.mFaces().get(i);
                    for(int faceVertexIndex = 0; faceVertexIndex < face.mNumIndices(); faceVertexIndex++){
                        int index = face.mIndices().get(faceVertexIndex);

                        //Get ref to vector
                        AIVector3D position = mesh.mVertices().get(index);
                        AIVector3D normal = mesh.mNormals().get(index);
                        AIVector3D tangent = null;
                        try{
                            tangent = mesh.mTangents().get(index);
                        }catch (NullPointerException e){
                            System.out.println("Error: Imported model does not have tangent data encorporated.");
                        }
                        AIVector3D bitangent = null;
                        try{
                            tangent = mesh.mBitangents().get(index);
                        }catch (NullPointerException e){
                            System.out.println("Error: Imported model does not have tangent data encorporated.");
                        }

                        AIVector3D texture = null;
                        if (mesh.mTextureCoords(0) != null) {
                            texture = mesh.mTextureCoords(0).get(index);
                        }

                        //Add to our array
                        vPositions[(i * 9) + (faceVertexIndex * 3) + 0] = position.x();
                        vPositions[(i * 9) + (faceVertexIndex * 3) + 1] = position.y();
                        vPositions[(i * 9) + (faceVertexIndex * 3) + 2] = position.z();

                        vNormals[(i * 9) + (faceVertexIndex * 3) + 0] = normal.x();
                        vNormals[(i * 9) + (faceVertexIndex * 3) + 1] = normal.y();
                        vNormals[(i * 9) + (faceVertexIndex * 3) + 2] = normal.z();

                        if(tangent != null) {
                            vTangents[(i * 9) + (faceVertexIndex * 3) + 0] = tangent.x();
                            vTangents[(i * 9) + (faceVertexIndex * 3) + 1] = tangent.y();
                            vTangents[(i * 9) + (faceVertexIndex * 3) + 2] = tangent.z();
                        }else{
                            vTangents[(i * 9) + (faceVertexIndex * 3) + 0] = 0;
                            vTangents[(i * 9) + (faceVertexIndex * 3) + 1] = 0;
                            vTangents[(i * 9) + (faceVertexIndex * 3) + 2] = 1;
                        }

                        if(bitangent != null) {
                            vBiTangents[(i * 9) + (faceVertexIndex * 3) + 0] = bitangent.x();
                            vBiTangents[(i * 9) + (faceVertexIndex * 3) + 1] = bitangent.y();
                            vBiTangents[(i * 9) + (faceVertexIndex * 3) + 2] = bitangent.z();
                        }else{
                            vBiTangents[(i * 9) + (faceVertexIndex * 3) + 0] = 1;
                            vBiTangents[(i * 9) + (faceVertexIndex * 3) + 1] = 0;
                            vBiTangents[(i * 9) + (faceVertexIndex * 3) + 2] = 0;
                        }

                        if (texture != null) {
                            vTextures[(i * 6) + (faceVertexIndex * 2) + 0] = texture.x();
                            vTextures[(i * 6) + (faceVertexIndex * 2) + 1] = texture.y();
                        } else {
                            vTextures[(i * 6) + (faceVertexIndex * 2) + 0] = 0;
                            vTextures[(i * 6) + (faceVertexIndex * 2) + 1] = 0;
                        }

                    }
                }

                HashMap<String, Integer> boneMap = new HashMap<>();
                LinkedHashMap<String, Matrix4f> boneOffsets = new LinkedHashMap<>();
                HashMap<Integer, LinkedList<Integer>> vertBones = new HashMap<>();
                HashMap<Integer, LinkedList<Float>> vertWeights = new HashMap<>();

                //Num Faces * 3 = vertices * 3 = 3 weights per vertex
                float[] vBoneIndices = new float[numVertices * 3];
                float[] vBoneWeights = new float[numVertices * 3];


                for(int boneIndex = 0; boneIndex < mesh.mNumBones(); boneIndex++){
                    AIBone bone = AIBone.create(mesh.mBones().get(boneIndex));
                    String boneName = bone.mName().dataString();
                    boneMap.put(boneName, boneIndex);
                    Matrix4f offset = new Matrix4f().setFromAddress(bone.mOffsetMatrix().address());
                    boneOffsets.put(boneName, offset);

                    //Weight calculations
                    for(int w = 0; w < bone.mNumWeights(); w++){
                        AIVertexWeight weight = bone.mWeights().get(w);
                        int vertexIndex = weight.mVertexId();
                        float boneWeight = weight.mWeight();

                        if(!vertBones.containsKey(vertexIndex)){
                            vertBones.put(vertexIndex, new LinkedList<Integer>());
                            vertWeights.put(vertexIndex, new LinkedList<Float>());
                        }
                        vertBones.get(vertexIndex).addLast(boneIndex);
                        vertWeights.get(vertexIndex).addLast(boneWeight);
                    }
                }

                for(int i = 0; i < mesh.mNumFaces(); i++){
                    AIFace face = mesh.mFaces().get(i);
                    for(int faceSize = 0; faceSize < face.mNumIndices(); faceSize++){
                        int faceIndex = face.mIndices().get(faceSize);

                        //Indices //3 is max joints.
                        int[] indices = new int[]{0, 0, 0};
                        float[] weights = new float[]{1, 0, 0};
                        if(vertBones.containsKey(faceIndex)){
                            LinkedList<Integer> boneMapping = vertBones.get(faceIndex);
                            for(int j = 0; j < Math.min(boneMapping.size(), indices.length); j++){
                                indices[j] = boneMapping.get(j);
                            }
                            LinkedList<Float> boneWeights = vertWeights.get(faceIndex);
                            for(int j = 0; j < Math.min(boneWeights.size(), weights.length); j++){
                                weights[j] = boneWeights.get(j);
                            }
                        }
                        vBoneIndices[(i * 9) + (faceSize * 3) + 0] = indices[0];
                        vBoneIndices[(i * 9) + (faceSize * 3) + 1] = indices[1];
                        vBoneIndices[(i * 9) + (faceSize * 3) + 2] = indices[2];

                        vBoneWeights[(i * 9) + (faceSize * 3) + 0] = weights[0];
                        vBoneWeights[(i * 9) + (faceSize * 3) + 1] = weights[1];
                        vBoneWeights[(i * 9) + (faceSize * 3) + 2] = weights[2];
                    }
                }

                Handshake modelHandshake = new Handshake();
                modelHandshake.addAttributeList("vPosition", vPositions, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vNormal", vNormals, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vTangent", vTangents, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vBitangent", vBiTangents, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vTexture", vTextures, EnumGLDatatype.VEC2);

                modelHandshake.addAttributeList("vBoneIndices", vBoneIndices, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vBoneWeights", vBoneWeights, EnumGLDatatype.VEC3);

//                modelHandshake.addAttributeList("vColor", vNormals, EnumGLDatatype.VEC3);

                AIVector3D min = mesh.mAABB().mMin();
                AIVector3D max = mesh.mAABB().mMax();

                //Get Bone info
                Joint root = calcualteBoneHeierarchy(scene.mRootNode(), joints);

                LinkedHashMap<String, Animation> animations = calculateAnimations(scene, joints);
                if(animations.size() > 0){
                    System.out.println("Bone Check");
                }

                Model model = new Model(this.getNextID(), modelPath, modelHandshake, vPositions.length / 3, new Vector3f[]{new Vector3f(min.x(), min.y(), min.z()), new Vector3f(max.x(), max.y(), max.z())}, root, animations, boneOffsets, joints);
                System.out.println("Material Index link:"+ materialIndex);
                model.setDefaultMaterial(materials.get(materialIndex));
                out.push(model);
            }

        }catch(Exception exception){
            exception.printStackTrace();
            System.out.println("Error parsing data for model:");

            out.clear();
            out.push(DEFAULT_MODEL);

        }

        return out;
    }

    public Model buildPlane(float width, float height, int subdivisionsX, int subdivisionsY){
        float unitWidth  = width  / (float)subdivisionsX;
        float unitHeight = height / (float)subdivisionsY;

        // +2 is for first and last point, so with 0 subdivisions you get 4 points making a simple quad.
        int widthUnits  = subdivisionsY+2;
        int heightUnits = subdivisionsX+2;

        Vector3f[] vertices = new Vector3f[widthUnits * heightUnits];
        Vector3f offset = new Vector3f(widthUnits * unitWidth / 2.0f, 0, heightUnits * unitHeight / 2.0f);

        // Generate our vertses
        for(int j = 0; j < heightUnits; j++){
            for(int i = 0; i < widthUnits; i++){
                vertices[i + (j * widthUnits)] = new Vector3f(i * unitWidth, 0, j * unitHeight).sub(offset);
            }
        }

        //Here we have 6 verts per quad. Each quad is 2 tries = 6 verts.
        Vector3f[] faceData   = new Vector3f[subdivisionsX * subdivisionsY * 6];
        Vector3f[] normalData = new Vector3f[faceData.length];
        Vector2f[] uvData     = new Vector2f[faceData.length];

        // Generate our indices
        for(int j = 0; j < subdivisionsY; j++){
            for(int i = 0; i < subdivisionsX; i++){
                int faceIndex = (i + (j * subdivisionsY));
                boolean inverseFace = ((i + j) % 2) == 1;

                Vector3f vec0 = vertices[(i + 0) + ((j + 0) * widthUnits)];
                Vector3f vec1 = vertices[(i + 1) + ((j + 0) * widthUnits)];
                Vector3f vec2 = vertices[(i + 0) + ((j + 1) * widthUnits)];
                Vector3f vec3 = vertices[(i + 1) + ((j + 1) * widthUnits)];

                Vector2f uv0 = new Vector2f((float)(i + 0) / (float)subdivisionsX, (float)(j + 0) / (float)subdivisionsY);
                Vector2f uv1 = new Vector2f((float)(i + 1) / (float)subdivisionsX, (float)(j + 0) / (float)subdivisionsY);
                Vector2f uv2 = new Vector2f((float)(i + 0) / (float)subdivisionsX, (float)(j + 1) / (float)subdivisionsY);
                Vector2f uv3 = new Vector2f((float)(i + 1) / (float)subdivisionsX, (float)(j + 1) / (float)subdivisionsY);

                if(inverseFace){
                    // [0, 1, 3, 0, 3, 2]
                    faceData[(faceIndex * 6) + 0] = vec3;
                    faceData[(faceIndex * 6) + 1] = vec1;
                    faceData[(faceIndex * 6) + 2] = vec0;
                    faceData[(faceIndex * 6) + 3] = vec2;
                    faceData[(faceIndex * 6) + 4] = vec3;
                    faceData[(faceIndex * 6) + 5] = vec0;

                    uvData[(faceIndex * 6) + 0] = uv3;
                    uvData[(faceIndex * 6) + 1] = uv1;
                    uvData[(faceIndex * 6) + 2] = uv0;
                    uvData[(faceIndex * 6) + 3] = uv2;
                    uvData[(faceIndex * 6) + 4] = uv3;
                    uvData[(faceIndex * 6) + 5] = uv0;
                }else{
                    // [0, 1, 2, 1, 3, 2]
                    faceData[(faceIndex * 6) + 0] = vec2;
                    faceData[(faceIndex * 6) + 1] = vec1;
                    faceData[(faceIndex * 6) + 2] = vec0;
                    faceData[(faceIndex * 6) + 3] = vec2;
                    faceData[(faceIndex * 6) + 4] = vec3;
                    faceData[(faceIndex * 6) + 5] = vec1;

                    uvData[(faceIndex * 6) + 0] = uv2;
                    uvData[(faceIndex * 6) + 1] = uv1;
                    uvData[(faceIndex * 6) + 2] = uv0;
                    uvData[(faceIndex * 6) + 3] = uv2;
                    uvData[(faceIndex * 6) + 4] = uv3;
                    uvData[(faceIndex * 6) + 5] = uv1;
                }
            }
        }

        float[] vBoneIndices = new float[faceData.length];
        float[] vBoneWeights = new float[faceData.length];

        for(int i = 0 ; i < faceData.length / 3; i++){
            //Bone Weights
            vBoneWeights[i * 3 + 0] = 1.0f;
            vBoneWeights[i * 3 + 1] = 0.0f;
            vBoneWeights[i * 3 + 1] = 0.0f;

            //Calculate normals
            Vector3f normal = new Vector3f();
            GeometryUtils.normal(faceData[(i * 3) + 0], faceData[(i * 3) + 1], faceData[(i * 3) + 2], normal);
            normalData[(i * 3) + 0] = normal;
            normalData[(i * 3) + 1] = normal;
            normalData[(i * 3) + 2] = normal;
        }

        Handshake modelHandshake = new Handshake();
        modelHandshake.addAttributeList("vPosition", VectorUtils.streamToFloatArray(faceData), EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vNormal", VectorUtils.streamToFloatArray(normalData), EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vTangent", VectorUtils.streamToFloatArray(faceData), EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vBitangent", VectorUtils.streamToFloatArray(faceData), EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vTexture", VectorUtils.streamToFloatArray(uvData), EnumGLDatatype.VEC2);

        modelHandshake.addAttributeList("vBoneIndices", vBoneIndices, EnumGLDatatype.VEC3);
        modelHandshake.addAttributeList("vBoneWeights", vBoneWeights, EnumGLDatatype.VEC3);


//                modelHandshake.addAttributeList("vColor", vNormals, EnumGLDatatype.VEC3);

//        AIVector3D min = mesh.mAABB().mMin();
//        AIVector3D max = mesh.mAABB().mMax();

        LinkedHashMap<String, Animation> animations = new LinkedHashMap<String, Animation>();
        LinkedHashMap<String, Matrix4f> boneOffsets = new LinkedHashMap<>();
        LinkedHashMap<String, Joint> joints = new LinkedHashMap<>();

        return new Model(this.getNextID(), "generated", modelHandshake, faceData.length / 3, new Vector3f[]{ new Vector3f(-width/2f, -0.5f, -height/2f), new Vector3f(width/2f, 0.5f, height/2f)}, null, animations, boneOffsets, joints);
    }

    private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null)
            throw new IOException("Classpath resource not found: " + resource);
        File file = new File(url.getFile());
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();
            fis.close();
        } else {
            buffer = BufferUtils.createByteBuffer(bufferSize);
            InputStream source = url.openStream();
            if (source == null)
                throw new FileNotFoundException(resource);
            try {
                byte[] buf = new byte[8192];
                while (true) {
                    int bytes = source.read(buf, 0, buf.length);
                    if (bytes == -1)
                        break;
                    if (buffer.remaining() < bytes)
                        buffer = resizeBuffer(buffer, Math.max(buffer.capacity() * 2, buffer.capacity() - buffer.remaining() + bytes));
                    buffer.put(buf, 0, bytes);
                }
                buffer.flip();
            } finally {
                source.close();
            }
        }
        return buffer;
    }

    public Matrix4f getIdentityMatrix() {
        return IDENTITY_MATRIX;
    }

    public float[] getIdentityMatrixArray() {
        return IDENTITY_MATRIX_ARRAY;
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
