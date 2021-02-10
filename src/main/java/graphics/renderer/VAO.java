package graphics.renderer;

import entity.Entity;
import entity.component.Attribute;
import input.Keyboard;
import material.MaterialManager;
import models.Animation;
import models.Joint;
import models.Model;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL46;
import particle.Particle;

import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class VAO {

    // The largest stride that a VBO can have per vertex is 4 for some reason, so we need to divide matrices into units of 4 or 3.
    private static final int MAX_VBO_ALLOCATION = 4;
    private final int VAO_DI;

    private LinkedHashMap<String, EnumGLDatatype> uniforms = new LinkedHashMap<>();
    private HashMap<String, Float> uniformDefaults = new HashMap<>();

    /*
        NOTE the only thing that is crucial is that index 0 of this array contains the triangulated model data such that VBO[0] / 3 = #of trinagles to render.
     */
    private LinkedHashMap<String, VBO> vbo_indices  = new LinkedHashMap<>();
    private LinkedHashMap<String, VBO> vbo_uniforms = new LinkedHashMap<>();

    //Used for internal lookup when reconstructing complex uniforms
    private LinkedHashMap<VBO, VBO> vbo_lookup = new LinkedHashMap<>();

    // The number of faces stored in the indices list of this VAO, should be numIndices / 3
    private int numFaces = 0;

    // User for animation data
    private boolean animated = false;

    //Store our animations
    Model model;

    public VAO(Model model){
        //Create our vao
        VAO_DI = VAOManager.getInstance().genVertexArrays();
        GL46.glBindVertexArray(VAO_DI);

        this.model = model;

        //Our attributes
        String[] attributes = new String[]{
            "vPosition",
            "vNormal",
            "vTangent",
            "vBitangent",
            "vTexture",
            "vBoneIndices",
            "vBoneWeights",
        };

        //Any uniform loads go here.
        registerUniform("transform", EnumGLDatatype.MAT4);
        registerUniform("jointIndices", EnumGLDatatype.VEC3);
        registerUniform("boneWeights", EnumGLDatatype.VEC3, 1f / (float)EnumGLDatatype.VEC3.sizePerVertex);

        //Lets see if we are animated
        this.animated = model.animations.size() > 0;

        //Load some data in there!
        Handshake shape = model.getHandshake();

        int index = 0;

        for(String name : attributes) {
            //IF this attribute is contained in our Handshake
            if(shape.hasAttribute(name)) {
                //Get the attribute
                FloatBuffer buffered_data = ((FloatBuffer) shape.getAttribute(name)).asReadOnlyBuffer();
                float[] floatBuffer = new float[buffered_data.remaining()];
                buffered_data.get(floatBuffer);

                int size = shape.getBytesPerVertex(name);

                //Load our float data into a VBO that is attached to our VAO.
                int vbo_data = GL46.glGenBuffers();
                GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_data);
                GL46.glBufferData(GL46.GL_ARRAY_BUFFER, floatBuffer, GL46.GL_STATIC_DRAW);
                GL46.glVertexAttribPointer(index, size, GL46.GL_FLOAT, false, 0, 0);

                //make a VBO to hold our data
                VBO vbo = new VBO(vbo_data, size, shape.getAttributeDataLength(name), name);

                System.out.println("Allocating space for [" + name +"] at index:" + (index));
                vbo_indices.put(name, vbo);

                //Check if this is the first time, if it is define the face count.
                if (index == 0) {
                    //NOTE we assume that index 0 is the VBO that storing indices of this model.
                    //NumFaces = totalNumberOfVertices / 3(vertices per face)
                    numFaces = vbo.dataSize / 3;
                }
            }else{
                System.err.println("[VAO] ERROR: Requested attribute:" + name + " however that is not an attribute stored in models handshakes. Is this supposed to be a uniform?");
            }
            index++;
        }

        //Load Uniforms
        for(String name : new LinkedList<String>(this.uniforms.keySet())) {
            int uniformSize = this.uniforms.get(name).sizePerVertex;
            if(uniformSize > MAX_VBO_ALLOCATION){
                //Determine how many uniforms we need to use.
                //TODO pass this as hint to shader compiler.

                //This figures out the smallest number of vector channels we can use.
                int uniformSizeSquared = (int) Math.ceil(Math.sqrt(uniformSize));

                //We are going to pad the end of the uniform name with _# (example) mat4 transform = vec4 transform_1, transform_2, transform_3, transform_4
                for(int i = 0; i < uniformSizeSquared; i++){
                    //Load our float data into a VBO that is attached to our VAO.
                    int vbo_data = GL46.glGenBuffers();
                    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_data);
                    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, new float[]{}, GL46.GL_STREAM_DRAW);
                    GL46.glVertexAttribPointer(index + i, uniformSizeSquared, GL46.GL_FLOAT, false, 0, 0);

                    //make a VBO to hold our data
                    VBO vbo = new VBO(vbo_data, uniformSizeSquared, -1, name);

                    //Now we register this uniform.
                    String uniqueName = name+"_"+i;
                    registerUniform(uniqueName, EnumGLDatatype.findSuitable(uniformSizeSquared));

                    //Now we need to register this VBO in our lookup table so that when uploading uniform data we will be able to get the base uniform type IE mat4 mat3 sampler[] ect.
                    System.out.println("Allocating space for [" + name +"] at index:" + (index + i));
                    vbo_lookup.put(vbo, new VBO(-1, this.uniforms.get(name).sizePerVertex, -1, name));

                    //Buffer this.
                    vbo_uniforms.put(uniqueName, vbo);
                }

                //Increase domain of index by uniformSizeSquared - an offset of 1 because we increment by 1 in the last step.
                index += (uniformSizeSquared - 1);

                //We dont want to try to enable an array element that is not present so we need to remove the base uniform.
                this.uniforms.remove(name);

                System.out.println("size:" + uniformSizeSquared);

            }else{
                //Load a normal uniform
                System.out.println("Allocating space for [" + name +"] at index:" + index);
                VBO dataBuffer = new VBO(-1, this.uniforms.get(name).sizePerVertex, -1, name);
                if(uniformDefaults.containsKey(name)){
                    float defaultValue = uniformDefaults.get(name);
                    dataBuffer.setDefaultValue(defaultValue);
                    System.out.println("Default Value for [" + name +"] = " + defaultValue);
                }
                vbo_lookup.put(allocateUniform(name, index), dataBuffer);
            }
            index++;
        }

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    private VBO allocateUniform(String name, int index){
        //Load our float data into a VBO that is attached to our VAO.
        int vbo_data = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_data);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, new float[]{}, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(index, this.uniforms.get(name).sizePerVertex, GL46.GL_FLOAT, false, 0, 0);

        //make a VBO to hold our data
        VBO vbo = new VBO(vbo_data, this.uniforms.get(name).sizePerVertex, -1, name);

        if(uniformDefaults.containsKey(name)){
            float defaultValue = uniformDefaults.get(name);
            vbo.setDefaultValue(defaultValue);
            System.out.println("Default Value for [" + name +"] = " + defaultValue);
        }

        //Buffer this.
        vbo_uniforms.put(name, vbo);

        return vbo;
    }

    private void registerUniform(String uniformName, EnumGLDatatype datatype){
        this.uniforms.put(uniformName, datatype);
        this.uniformDefaults.put(uniformName, 0f);
    }

    private void registerUniform(String uniformName, EnumGLDatatype datatype, float defaultData){
        this.uniforms.put(uniformName, datatype);
        this.uniformDefaults.put(uniformName, defaultData);
    }

    /*
        Prepare for this VAO to be rendered.
     */
    public void render(Collection<Entity> toRender){
        // --------------  Prepare / Update Buffers  ----------------
        //Bind our VAOID into GL
        GL46.glBindVertexArray(VAO_DI);

        int renderCount = toRender.size();

        //Blacklist
        LinkedList<String> blacklist = new LinkedList<>();

        //Iterate through all of the VBOs we have allocated.
        for(VBO vbo : vbo_uniforms.values()){
            //First check if we have loaded this VBO out of order in a past iteration, this happens when we are loading a linked VAO or 'complex' VAO
            if(blacklist.contains(vbo.name)){
                continue; // Skip this itteration
            }

            //Determine the true size of this VBO
            int trueUniformSize = this.vbo_lookup.get(vbo).vertexStride;

            //Load that data!
            String attributeLookup = this.vbo_lookup.get(vbo).name;

            if(trueUniformSize > MAX_VBO_ALLOCATION){
                //We gotta do some funky stuff to construct this vbo.
                //This figures out the smallest number of vector channels we can use. We will then check that these elements are all present on our lookup.
                int uniformSizeSquared = (int) Math.ceil(Math.sqrt(trueUniformSize));

                //Name
                String attributeName = vbo.name;

                //This is a 2D array of our matrix data that we will load into sqrt(mat.size)# VBOS
                float[][] uniformData = new float[uniformSizeSquared][renderCount * uniformSizeSquared];

                int entityIndex = 0;
                for(Entity e : toRender){
                    //Prevision our variable names here.
                    Object entityData;
                    float[] data;

                    //Transform is reserved
                    if(attributeName.equals("transform")) {
                        //TODO maybe transpose
                        entityData = e.getTransform();
                        data = VAOManager.getInstance().attributeToFloat(entityData, trueUniformSize);
                    }else if(e.hasAttribute(attributeName)){
                        entityData = e.getAttribute(attributeName).getData();
                        data = VAOManager.getInstance().attributeToFloat(entityData, trueUniformSize);
                    }else{
                        data = new float[trueUniformSize];
                    }

                    for(int i = 0; i < uniformSizeSquared; i++){
                        for(int j = 0; j < uniformSizeSquared; j++){
                            uniformData[i][(entityIndex * uniformSizeSquared) + j] = data[(i * uniformSizeSquared) + j];
                        }
                    }

                    entityIndex++;

                }

                //Load the data into our VBOs
                for(int i = 0; i < uniformSizeSquared; i++){
                    String trueName = attributeName + "_" + i;
                    VBO trueVBO = vbo_uniforms.get(trueName);

                    //Add to the blacklist
                    blacklist.add(attributeName);

                    //Default uniform load.
                    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, trueVBO.VBO_ID);
                    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, uniformData[i], GL46.GL_STREAM_DRAW);
                }

            }else{
                //Default uniform load.
                GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo.VBO_ID);
                //Lets build our buffer!

                float[] uniformData = new float[renderCount * vbo.vertexStride];
                //TODO check that the entitiy has an attribute of name name to load here.
                int entityIndex = 0;

                float[] data = new float[vbo.vertexStride];

                for (Entity e : toRender) {
                    if(e.hasAttribute(attributeLookup)){
                        Object entityData = e.getAttribute(attributeLookup).getData();
                        data = VAOManager.getInstance().attributeToFloat(entityData, trueUniformSize);
                        for(int i = 0; i < vbo.vertexStride; i++){
                            uniformData[(entityIndex * vbo.vertexStride) + i] = data[i];
                        }
//                        System.out.println("Loading data for attribute: " + attributeLookup);
                    }else{
                        for(int i = 0; i < vbo.vertexStride; i++){
                            uniformData[(entityIndex * vbo.vertexStride) + i] = vbo.defaultValue;
                        }
//                        System.out.println("Loading default data for attribute: " + attributeLookup + " = " + vbo.defaultValue);
                    }


                    entityIndex++;
                }

                GL46.glBufferData(GL46.GL_ARRAY_BUFFER, uniformData, GL46.GL_STREAM_DRAW);
            }

        }

        //IF this is an animated model load the bone transforms
        loop:{
            if (animated) {
//                for (Animation animation : model.animations.values()) {
//
//                    LinkedHashMap<String, Matrix4f> posePositions = animation.getBoneTransformsForTime(model.getTime());
//                    //Load from animation
//                    int maxBones = 50;
//                    int index = 0;
//                    for(String boneName : posePositions.keySet()){
//                        Matrix4f posePosition = posePositions.get(boneName);
//                        ShaderManager.getInstance().loadUniformIntoActiveShaderArray("jointTransforms", index, posePosition.mul(new Matrix4f(model.joints.get(boneName).getLocalBindTransform()).invert()));
//                        index++;
//                    }
//                }
                HashMap<String, Matrix4f> frames = model.getAnimatedBoneTransforms();
                applyPoseToJoints(frames, model.rootJoint, new Matrix4f().identity());

                for(Joint joint : model.joints.values()){
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("jointTransforms", joint.getIndex(), joint.getAnimationTransform());
                }

                break loop;
            } else {
                int maxBones = 50;
                for (int i = 0; i < maxBones; i++) {
                    ShaderManager.getInstance().loadUniformIntoActiveShaderArray("jointTransforms", i, new Matrix4f().identity());
                }
            }
        }

        // --------------       RENDERING CODE       ----------------
        //Keep track of our load index
        int index = 0;

        //Iterate through our attributes and enable them
        for(String attribute : vbo_indices.keySet()){
            // 1rst attribute buffer : vertices
            GL46.glEnableVertexAttribArray(index);
            //Now we need to tell OpenGL how big to stride each instance of draw over this VBO
            GL46.glVertexAttribDivisor(index, 0);
            index++;
        }
        for(String uniform : vbo_uniforms.keySet()){
            // 1rst attribute buffer : vertices
            GL46.glEnableVertexAttribArray(index);
            //Now we need to tell OpenGL how big to stride each instance of draw over this VBO
            GL46.glVertexAttribDivisor(index,1);
            index++;
        }

        //Actual draw call.
        GL46.glDrawArraysInstanced(GL46.GL_TRIANGLES, 0, numFaces, renderCount);

        // --------------        CLEANUP CODE        ----------------
        //Free up our indices.
        index = 0;
        for(String attribute : vbo_indices.keySet()){
            GL46.glDisableVertexAttribArray(index);
            index++;
        }
        for(String uniform : vbo_uniforms.keySet()){
            GL46.glDisableVertexAttribArray(index);
            index++;
        }

        //unbind us.
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    public int getID() {
        return VAO_DI;
    }

//    private void calculateBonePositionHelper(Joint root, Matrix4f parentTransform, HashMap<String, Matrix4f> frames, HashMap<String, Matrix4f> out) {
//        if(frames.containsKey(root.getName())) {
//            Matrix4f animationOffset = new Matrix4f(frames.get(root.getName()));
////            Matrix4f currentTransform = new Matrix4f(animationOffset).mul(parentTransform);
//
//            for (Joint childJoint : root.getChildren()) {
//                calculateBonePositionHelper(childJoint, animationOffset, frames, out);
//            }
//
//            out.put(root.getName(), new Matrix4f(parentTransform).mul(new Matrix4f(animationOffset)));
////            out.put(root.getName(), new Matrix4f(parentTransform).invert().mul(currentTransform));
//        }
//    }


    private void applyPoseToJoints(HashMap<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
        if(currentPose.containsKey(joint.getName())) {
            Matrix4f currentLocalTransform = currentPose.get(joint.getName());
            Matrix4f currentTransform = new Matrix4f(parentTransform).mul(currentLocalTransform);
            for (Joint childJoint : joint.getChildren()) {
                applyPoseToJoints(currentPose, childJoint, currentTransform);
            }
            currentTransform = currentTransform.mul(joint.getLocalBindTransform());
            joint.setAnimationTransform(currentTransform);
        }
    }

}

