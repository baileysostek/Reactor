package graphics.renderer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;
import util.StringUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;

public class ShaderManager {

    //Singleton instance
    public static ShaderManager shaderManager;

    //Store references to all shader programs compiled via this class, so we can delete them on shutdown.
    private HashMap<String, Integer>    shaderInstances       = new HashMap<>();
    private HashMap<String, Shader>     shaders               = new HashMap<>();
    private HashMap<Integer, String>    shaderInstances_prime = new HashMap<>();

    private HashMap<String, Boolean>    enabledArrays        = new HashMap<>();

    //Keep Track of the active shader
    private int activeShader = -1;

    private GLTarget target = GLTarget.GL40;


    private ShaderManager(){

    }

    public void setGLTarget(GLTarget target){
        this.target = target;
    }

    public GLTarget getGLTarget(){
        return this.target;
    }

    public int loadShader(String name){
        //Look at the assets we have available to us, and load a shaders source files
        String info     = StringUtils.load("shaders/" + name + "_properties.json");
        String vertex   = StringUtils.load("shaders/" + name + "_vertex.glsl");
        String fragment = StringUtils.load("shaders/" + name + "_fragment.glsl");

        //Buffer for reading compile status
        int[] compileBuffer = new int[]{ 0 };

        //Now that we have a source, we  need to compile the shaders into GPU code
        int vertexShader   = GL46.glCreateShader(GL46.GL_VERTEX_SHADER);
        GL46.glShaderSource(vertexShader, vertex);
        GL46.glCompileShader(vertexShader);
        GL46.glGetShaderiv(vertexShader, GL46.GL_COMPILE_STATUS, compileBuffer);
        if (compileBuffer[0] == GL46.GL_FALSE) {
            GL46.glGetShaderiv(vertexShader, GL46.GL_INFO_LOG_LENGTH, compileBuffer);
            //Check that log exists
            if (compileBuffer[0] > 0) {
                //Cleanup our broken shader
                GL46.glDeleteShader(vertexShader);
                System.out.println("Vertex Status:" + GL46.glGetShaderInfoLog(vertexShader));
                return -1;
            }
        }else{
            if(compileBuffer[0] == GL46.GL_TRUE){
                System.out.println("Vertex Shader compiled Successfully.");
            }else{
                System.out.println("Vertex Shader compiled in an unknown state. This may cause strange behavior.");
            }
        }

        int fragmentShader = GL46.glCreateShader(GL46.GL_FRAGMENT_SHADER);
        GL46.glShaderSource(fragmentShader, fragment);
        GL46.glCompileShader(fragmentShader);
        GL46.glGetShaderiv(fragmentShader, GL46.GL_COMPILE_STATUS, compileBuffer);
        if (compileBuffer[0] == GL46.GL_FALSE) {
            GL46.glGetShaderiv(fragmentShader, GL46.GL_INFO_LOG_LENGTH, compileBuffer);
            //Check that log exists
            if (compileBuffer[0] > 0) {
                //Cleanup our broken shader
                GL46.glDeleteShader(vertexShader);
                System.out.println("Fragment Status:" + GL46.glGetShaderInfoLog(fragmentShader));
                return -1;
            }
        }else{
            if(compileBuffer[0] == GL46.GL_TRUE){
                System.out.println("Fragment Shader compiled Successfully.");
            }else{
                System.out.println("Fragment Shader compiled in an unknown state. This may cause strange behavior.");
            }
        }

        //Now that we have our shaders compiled, we link them to a shader program.
        int programID = GL46.glCreateProgram();

        //Add the parsed meta data into this
        JsonParser parser = new JsonParser();
        JsonObject shaderMeta = parser.parse(info).getAsJsonObject();
        String version = shaderMeta.get("Version").getAsString();

        //IN's are attributes that need to be bound to the current context.
        JsonObject vertexData = shaderMeta.get("Vertex").getAsJsonObject();
        JsonObject fragmentData = shaderMeta.get("Fragment").getAsJsonObject();
        JsonObject attributesJSON = vertexData.get("Attributes").getAsJsonObject();
        String[] attributes = new String[attributesJSON.size()];
        int index = 0;
        for(String uniformName : attributesJSON.keySet()){
            String attributeName = uniformName;
            attributes[index] = attributeName;
            System.out.println("Adding attribute: " + attributes[index]);
            index++;
        }

        int attributeIndex = 1;
        for(String attribute : attributes){
            GL46.glBindAttribLocation(programID, attributeIndex, attribute);
            attributeIndex++;
        }


        //Combine vertex and fragment shaders into one program
        GL46.glAttachShader(programID, vertexShader);
        GL46.glAttachShader(programID, fragmentShader);

        //Link
        GL46.glLinkProgram(programID);

        //Check link status
        GL46.glGetShaderiv(vertexShader, GL46.GL_LINK_STATUS, compileBuffer);
        if (compileBuffer[0] == GL46.GL_TRUE) {
            System.out.println("Shader Link was successful.");
            //Add this shader to our shader cache
            shaderInstances.put(name, programID);
            shaderInstances_prime.put(programID, name);
        }else{
            System.err.println("Shader Link failed.");
            GL46.glDeleteProgram(programID);
            return -1;
        }

        //These programs are no longer needed, so we can simply clean them up.
        GL46.glDeleteShader(vertexShader);
        GL46.glDeleteShader(fragmentShader);

        //Create a shader data object which will be used to hold data about this shader.
        Shader shader = new Shader(name, programID, version).setAttributes(attributes);

        //get all uniforms
        JsonObject uniformsJSON = vertexData.get("Uniforms").getAsJsonObject();
        for(String uniformName : uniformsJSON.keySet()){
            String uniformType = uniformsJSON.get(uniformName).getAsString();
            shader.addUniform(uniformName, uniformType);
            System.out.println("Adding uniform named: " + uniformName + " type:" + uniformType);
        }
        uniformsJSON = fragmentData.get("Uniforms").getAsJsonObject();
        for(String uniformName : uniformsJSON.keySet()){
            String uniformType = uniformsJSON.get(uniformName).getAsString();
            shader.addUniform(uniformName, uniformType);
            System.out.println("Adding uniform named: " + uniformName + " type:" + uniformType);
        }

        shaders.put(name, shader);

        //Return the program ID, and store this shader's name hashed to its program id. That way we can skip loading in the future
        return programID;
    }

    public boolean hasShader(String name){
        return shaderInstances.containsKey(name);
    }

    //Use shader takes a shader context
    public void useShader(int programID){
        if(shaderInstances_prime.containsKey(programID)){
            GL46.glUseProgram(programID);
            activeShader = programID;
        }else{
            System.err.println("Tried to use a shader programID out of the range of currently available programs.");
        }
    }

    public void loadHandshakeIntoShader(int programID, Handshake handshake){
        if(shaderInstances_prime.containsKey(programID)) {
            Shader shader = shaders.get(shaderInstances_prime.get(programID));
            int index = 1;
            for(String attribute : shader.getAttributes()){
                loop:{
                    //Check to see if this handshake has this attribute.
                    if (!handshake.hasAttribute(attribute)) {
                        System.err.println("This handshake does not contain the attribute: " + attribute);
                        break loop;
                    }

                    //If handshake contains this buffered data.
                    int attribPointer = GL46.glGetAttribLocation(programID, attribute);

                    //If this attribute is out of range of the program IE compiler optomised it out skip over loading into memory.
                    int errorCheck = GL46.glGetError();
                    boolean error = false;
                    while (errorCheck != GL46.GL_NO_ERROR) {
                        System.out.println("GLError:" + errorCheck);
                        if (errorCheck == GL46.GL_INVALID_VALUE) {
                            error = true;
                            System.out.println("Attribute[" + attribute + "] could not be found in Shader[" + shaderInstances_prime.get(programID) + ']');
                            shader.removeAttribute(attribute);
                        }
                        errorCheck = GL46.glGetError();
                    }

                    //If error, skip loading data into this attribute
                    if (error) {
                        System.out.println("Error we breaking.");
                        break loop;
                    }

                    if(attribPointer > 0) {
                        GL46.glEnableVertexAttribArray(attribPointer);
                        GL46.glVertexAttribPointer(attribPointer, handshake.getAttributeSize(attribute), GL46.GL_FLOAT, true, 0, (FloatBuffer) handshake.getAttribute(attribute));
                    }
                }
            }
        }
    }

    //lets a public interface load data into a shader
    public void loadUniformIntoActiveShader(String name, Object uniform,  EnumGLDatatype uniformType){

        //If we dont have this uniform variable, dont do anything.
        if(uniformType == null){
            return;
        }

        int location = GL46.glGetUniformLocation(activeShader, name);

        float[] data = new float[uniformType.getSize()];

        switch(uniformType){
            case VEC2 : {
                Vector2f vector2f = (Vector2f)uniform;
                GL46.glUniform2f(location, vector2f.x(), vector2f.y());
                break;
            }
            case VEC3 : {
                Vector3f vector3f = (Vector3f)uniform;
                data[0] = vector3f.x();
                data[1] = vector3f.y();
                data[2] = vector3f.z();
                GL46.glUniform3fv(location, data);
                break;
            }
            case MAT4: {
                float[] matrix = (float[])uniform;
                GL46.glUniformMatrix4fv(location, false, matrix);
                break;
            }
            case SAMPLER2D : {
                int index = Integer.parseInt(uniform+"");
//                data[0] = index;
                GL46.glUniform1i(location, index);
                break;
            }
            case SAMPLER3D : {
                int index = Integer.parseInt(uniform+"");
//                data[0] = index;
                GL46.glUniform1i(location, index);
                break;
            }
            default:{
                System.out.println("[loadUniformIntoActiveShader]Error: datatype " + uniformType +" is not supported yet.");
            }
        }
    }

    public void loadUniformIntoActiveShader(String name, Object uniform){
        EnumGLDatatype uniformType = shaders.get(shaderInstances_prime.get(activeShader)).getUniform(name);
        loadUniformIntoActiveShader(name, uniform, uniformType);
    }

    public void loadUniformIntoActiveShaderArray(String name, int arrayIndex, Object uniform){
        EnumGLDatatype uniformType = shaders.get(shaderInstances_prime.get(activeShader)).getUniform(name);
        loadUniformIntoActiveShader(name+"["+arrayIndex+"]", uniform, uniformType);
    }

    //Initialize code
    public static void initialize(){
        if(shaderManager == null){
            shaderManager = new ShaderManager();
        }
    }

    //Cleanup our memory. We don't want to have old programs lying around.
    public void shutdown(){
        for(int id : shaderInstances.values()){
            GL46.glDeleteProgram(id);
            System.out.println("Deleted program:" + id);
        }
    }

    //Singleton Design Pattern
    public static ShaderManager getInstance(){
        return shaderManager;
    }
}
