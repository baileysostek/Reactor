package models;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import graphics.renderer.GLTarget;
import graphics.renderer.ShaderManager;
import graphics.renderer.EnumGLDatatype;
import graphics.renderer.Handshake;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import util.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
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
//                                Assimp.aiProcess_JoinIdenticalVertices |
                        Assimp.aiProcess_Triangulate |
//                                Assimp.aiProcess_FlipWindingOrder |
//                                Assimp.aiProcess_GenSmoothNormals|
                                Assimp.aiProcess_FlipUVs |
                                Assimp.aiProcess_CalcTangentSpace |
                                Assimp.aiProcess_GenNormals |
                                Assimp.aiProcess_GenUVCoords |
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

    private LinkedList<Animation> calculateAnimations(AIScene scene, HashMap<String, Joint> joints){
        LinkedList<Animation> animations = new LinkedList<>();
        //For Each animation
        for(int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation aiAnimation = AIAnimation.create(scene.mAnimations().get(i));
            Animation animation = new Animation(aiAnimation.mDuration());
            HashMap<String, LinkedList<KeyFrame>> keyframes = new HashMap<>();

            for(String s : joints.keySet()){
                keyframes.put(s, new LinkedList<>());
            }

            //For each bone
            for(int c = 0; c < aiAnimation.mNumChannels(); c++){
                AINodeAnim nodeAnim = AINodeAnim.create(aiAnimation.mChannels().get(c));
                String boneName = nodeAnim.mNodeName().dataString();

                if(joints.containsKey(boneName)) {
                    //For each frame of animation for bone in animatin
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

            animations.addLast(animation);
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

        HashMap<String, Joint> joints = new HashMap<>();

        try {
            //Allocate our textures
            int numMaterials = scene.mNumMaterials();
            PointerBuffer aiMaterials = scene.mMaterials();
            for (int i = 0; i < numMaterials; i++) {
                AIMaterial material = AIMaterial.create(aiMaterials.get(i)); // wrap raw pointer in AIMaterial instance

                // Method 1: use aiGetMaterial<property> functions, e.g.
                int texCount = Assimp.aiGetMaterialTextureCount(material, Assimp.aiTextureType_DIFFUSE);

                // Method 2: Parse material properties manually
                PointerBuffer properties = material.mProperties(); // array of pointers to AIMaterialProperty structs
                for ( int j = 0; j < properties.remaining(); j++ ) {
                    AIMaterialProperty prop = AIMaterialProperty.create(properties.get(j));
                    // parse property
                    prop.mKey().dataString();
                    String name = prop.mKey().dataString();
                    if(name.contains("file")) {
                        String propValue = StandardCharsets.UTF_8.decode(prop.mData()).toString();
                        System.out.println("Texture file:" + propValue);
                    }
                }
            }

            //Load our mesh
            for (int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++) {
                AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshIndex));

                //Put our lists into buffers for this Model.
                float[] vPositions = new float[mesh.mNumFaces() * 3 * 3];
                float[] vNormals = new float[mesh.mNumFaces() * 3 * 3];
                float[] vTangents = new float[mesh.mNumFaces() * 3 * 3];
                float[] vBiTangents = new float[mesh.mNumFaces() * 3 * 3];
                float[] vTextures = new float[mesh.mNumFaces() * 3 * 2];

                int numBones = mesh.mNumBones();
                PointerBuffer aiBones = mesh.mBones();
                for (int i = 0; i < numBones; i++) {
                    AIBone aiBone = AIBone.create(aiBones.get(i));
                    String name = aiBone.mName().dataString();
                    joints.put(name, new Joint(i, name, this.toJOML(aiBone.mOffsetMatrix())));
                }

                //For each vertex, get all data we need
                for (int i = 0; i < mesh.mNumVertices(); i++) {
                    //Get ref to vector
                    AIVector3D position = mesh.mVertices().get(i);
                    AIVector3D normal = mesh.mNormals().get(i);
                    AIVector3D tangent = mesh.mTangents().get(i);
                    AIVector3D bitangent = mesh.mBitangents().get(i);

                    AIVector3D texture = null;
                    if (mesh.mTextureCoords(0) != null) {
                        texture = mesh.mTextureCoords(0).get(i);
                    }

                    //Add to our array
                    vPositions[(i * 3) + 0] = position.x();
                    vPositions[(i * 3) + 1] = position.y();
                    vPositions[(i * 3) + 2] = position.z();

                    vNormals[(i * 3) + 0] = normal.x();
                    vNormals[(i * 3) + 1] = normal.y();
                    vNormals[(i * 3) + 2] = normal.z();

                    vTangents[(i * 3) + 0] = tangent.x();
                    vTangents[(i * 3) + 1] = tangent.y();
                    vTangents[(i * 3) + 2] = tangent.z();

                    vBiTangents[(i * 3) + 0] = bitangent.x();
                    vBiTangents[(i * 3) + 1] = bitangent.y();
                    vBiTangents[(i * 3) + 2] = bitangent.z();

                    if (texture != null) {
                        vTextures[(i * 2) + 0] = texture.x();
                        vTextures[(i * 2) + 1] = texture.y();
                    } else {
                        vTextures[(i * 2) + 0] = 0;
                        vTextures[(i * 2) + 1] = 0;
                    }
                }

                Handshake modelHandshake = new Handshake();
                modelHandshake.addAttributeList("vPosition", vPositions, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vNormal", vNormals, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vTangent", vTangents, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vBitangent", vBiTangents, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vColor", vNormals, EnumGLDatatype.VEC3);
                modelHandshake.addAttributeList("vTexture", vTextures, EnumGLDatatype.VEC2);

                AIVector3D min = mesh.mAABB().mMin();
                AIVector3D max = mesh.mAABB().mMax();

                //Get Bone info
                Joint root = calcualteBoneHeierarchy(scene.mRootNode(), joints);

                //Determine Animations
                calculateAnimations(scene, joints);

                Model model = new Model(this.getNextID(), modelPath, modelHandshake, vPositions.length / 3, new Vector3f[]{new Vector3f(min.x(), min.y(), min.z()), new Vector3f(max.x(), max.y(), max.z())});
                model.setJoints(new LinkedList<>(joints.values()));
                model.setRootJoint(root);
                LinkedList<Animation> animations = calculateAnimations(scene, joints);
                if (animations.size() > 0) {
                    model.animation = animations.getFirst();
                }
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
