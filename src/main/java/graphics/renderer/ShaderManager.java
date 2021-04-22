package graphics.renderer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import editor.Editor;
import engine.Reactor;
import entity.Entity;
import entity.component.Attribute;
import logging.LogManager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;
import util.Callback;
import util.EnumFileAction;
import util.StringUtils;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ShaderManager {

    //Singleton instance
    public static ShaderManager shaderManager;

    //Store references to all shader programs compiled via this class, so we can delete them on shutdown.
    private HashMap<String, Integer>    shaderInstances       = new HashMap<>();
    private HashMap<String, Shader>     shaders               = new HashMap<>();
    private HashMap<Integer, String>    shaderInstances_prime = new HashMap<>();
    private HashMap<Integer, Callback>  predrawCallbacks      = new HashMap<>();
    private HashMap<Integer, Callback>  postDrawCallbacks     = new HashMap<>();

    //Keep Track of the active shader
    private int activeShader = -1;

    private GLTarget target = GLTarget.GL40;

    //The default shader
    private static int defaultShader;

    //The definition for our shaders directory
    private static final String SHADER_DIRECTORY = "shaders";

    //This is where we store the list of recompilations that need to happen at the start of next frame
    private LinkedList<String> recompilations = new LinkedList<>();

    //Lock for locking our entity set
    private Lock lock;

    private static String DEFAULT_SHADER_PROGRAM = "pbr";

    public static void setDefaultShader(String name){
        DEFAULT_SHADER_PROGRAM = name;
    }

    private ShaderManager(){
        lock = new ReentrantLock();
    }

    protected void predraw(int id){
        Callback predraw = predrawCallbacks.get(id);
        if(predraw != null) {
            predraw.callback();
        }
    }

    public void onPredraw(int id, Callback callback){
        predrawCallbacks.put(id, callback);
    }

    protected void postdraw(int id){
        Callback postdraw = postDrawCallbacks.get(id);
        if(postdraw != null) {
            postdraw.callback();
        }
    }

    public void onPostdraw(int id, Callback callback){
        postDrawCallbacks.put(id, callback);
    }

    public void update(double delta){
        if(recompilations.size() > 0){
            lock.lock();
            for(String shader : recompilations){
                loadShader(shader);
            }
            recompilations.clear();
            lock.unlock();
        }
    }

    public void setupCallbackListeners(){
        if(Reactor.isDev()){
            Editor.getInstance().getFileBrowser().registerCallback("/" + SHADER_DIRECTORY, EnumFileAction.MODIFY, new Callback() {
                @Override
                public Object callback(Object... objects) {
                    String shaderSource = (String) objects[0];
                    //Make sure this is not a temporary file.
                    if(!shaderSource.endsWith("~")){
                        String parsedName = parseShaderName(shaderSource);
                        if(hasShader(parsedName)){
                            lock.lock();
                            if(!recompilations.contains(parsedName)){
                                recompilations.add(parsedName);
                            }
                            lock.unlock();
                        }
                    }
                    return null;
                }
            });
        }
    }

    private String parseShaderName(String source){
        return source.replace("/" + SHADER_DIRECTORY + "/", "").replace("_fragment.glsl", "").replace("_vertex.glsl", "").replace("_properties.json", "");
    }

    public void setGLTarget(GLTarget target){
        this.target = target;
    }

    public GLTarget getGLTarget(){
        return this.target;
    }

    public int loadShader(String name){
        // Say what we are looking for and doing
        System.out.println("Compiling shader: " + name);
        //Look at the assets we have available to us, and load a shaders source files
        String info     = StringUtils.load(SHADER_DIRECTORY + "/" + name + "_properties.json");
        String vertex   = StringUtils.load(SHADER_DIRECTORY + "/" + name + "_vertex.glsl");
        String fragment = StringUtils.load(SHADER_DIRECTORY + "/" + name + "_fragment.glsl");

        //Buffer for reading compile status
        int[] compileBuffer = new int[]{ 0 };

        //Try to compile the Vertex shader
        System.out.println("Try compile vertex shader.");

        //Now that we have a source, we  need to compile the shaders into GPU code
        int vertexShader   = GL46.glCreateShader(GL46.GL_VERTEX_SHADER);
        GL46.glShaderSource(vertexShader, vertex);
        GL46.glCompileShader(vertexShader);
        GL46.glGetShaderiv(vertexShader, GL46.GL_COMPILE_STATUS, compileBuffer);
        if (compileBuffer[0] == GL46.GL_FALSE) {
            GL46.glGetShaderiv(vertexShader, GL46.GL_INFO_LOG_LENGTH, compileBuffer);
            //Check that log exists
            if (compileBuffer[0] > 0) {
                String errorMesssage = GL46.glGetShaderInfoLog(vertexShader);
                String lineNumber = errorMesssage.substring(errorMesssage.indexOf("(")+ 1, errorMesssage.indexOf(")"));
                System.err.println("Error compiling fragment shader| " + StringUtils.getPathToResources() + SHADER_DIRECTORY + "/" + name + "_vertex.glsl:" + lineNumber + " | " + GL46.glGetShaderInfoLog(vertexShader));
                //Cleanup our broken shader
                GL46.glDeleteShader(vertexShader);
                return -1;
            }
        }else{
            if(compileBuffer[0] == GL46.GL_TRUE){
                System.out.println("Vertex Shader compiled Successfully.");
            }else{
                System.out.println("Vertex Shader compiled in an unknown state. This may cause strange behavior.");
            }
        }

        //Try to compile the Vertex shader
        System.out.println("Try compile fragment shader.");

        int fragmentShader = GL46.glCreateShader(GL46.GL_FRAGMENT_SHADER);
        GL46.glShaderSource(fragmentShader, fragment);
        GL46.glCompileShader(fragmentShader);
        GL46.glGetShaderiv(fragmentShader, GL46.GL_COMPILE_STATUS, compileBuffer);
        if (compileBuffer[0] == GL46.GL_FALSE) {
            GL46.glGetShaderiv(fragmentShader, GL46.GL_INFO_LOG_LENGTH, compileBuffer);
            //Check that log exists
            if (compileBuffer[0] > 0) {
                String errorMesssage = GL46.glGetShaderInfoLog(fragmentShader);
//                String lineNumber = errorMesssage.substring(errorMesssage.indexOf("(")+ 1, errorMesssage.indexOf(")"));
//                System.err.println("Error compiling fragment shader| " + StringUtils.getRelativePath() + "shaders/" + name + "_fragment.glsl:" + lineNumber + " | " + GL46.glGetShaderInfoLog(fragmentShader));
                System.err.println("Error compiling fragment shader| " + StringUtils.getPathToResources() + SHADER_DIRECTORY + "/" + name + "_fragment.glsl:" + errorMesssage + " | " + GL46.glGetShaderInfoLog(fragmentShader));
                //Cleanup our broken shader
                GL46.glDeleteShader(vertexShader);
                GL46.glDeleteShader(fragmentShader);
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

        System.out.println("Linking Vertex and Fragment shaders to Program...");

        //Add the parsed meta data into this
        JsonParser parser = new JsonParser();
        JsonObject shaderMeta = parser.parse(info).getAsJsonObject();
        String version = shaderMeta.get("Version").getAsString();
        System.out.println("Shader Version:" + version);

        //IN's are attributes that need to be bound to the current context.
        JsonObject vertexData = shaderMeta.get("Vertex").getAsJsonObject();
        JsonObject fragmentData = shaderMeta.get("Fragment").getAsJsonObject();
        JsonObject attributesJSON = vertexData.get("Attributes").getAsJsonObject();
        String[] attributes = new String[attributesJSON.size()];
        int index = 0;
        for(String uniformName : attributesJSON.keySet()){
            String attributeName = uniformName;
            attributes[index] = attributeName;
            System.out.println("Adding IN attribute to vertex shader:" + attributes[index]);
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
            //Add this shader to our shader cache
//            if(shaderInstances.containsKey(name)){
//                int oldProgramID = shaderInstances.get(name);
//                System.out.println("Shader:" + name + " already loaded at index:" + oldProgramID);
//                MaterialManager.getInstance().updateShaderIndex(oldProgramID, programID);
//                GL46.glDeleteProgram(oldProgramID);
//            }
            shaderInstances.put(name, programID);
            shaderInstances_prime.put(programID, name);
            System.out.println("Shader Link was successful.");
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
            System.out.println("Adding vertex uniform named: " + uniformName + " type:" + uniformType);
        }
        uniformsJSON = fragmentData.get("Uniforms").getAsJsonObject();
        for(String uniformName : uniformsJSON.keySet()){
            String uniformType = uniformsJSON.get(uniformName).getAsString();
            shader.addUniform(uniformName, uniformType);
            System.out.println("Adding fragment uniform named: " + uniformName + " type:" + uniformType);
        }

        //SSBO
        if(vertexData.has("SSBO")) {
            JsonObject ssboJson = vertexData.get("SSBO").getAsJsonObject();
            for (String ssboName : ssboJson.keySet()) {
                JsonObject ssboData = ssboJson.get(ssboName).getAsJsonObject();
                System.out.println("Adding SSBO:" + ssboData);
            }
        }

        // Add the shader to our shader cache
        shaders.put(name, shader);

        LogManager.getInstance().logLine();

        //Return the program ID, and store this shader's name hashed to its program id. That way we can skip loading in the future
        return programID;
    }

    public void loadAttributesIntoShader(Collection<Attribute> attributes){
        
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
            //TODO fix?
//            System.err.println("Tried to use a shader programID out of the range of currently available programs.");
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
                        System.err.println("Error loading data into shader:" + shaderInstances_prime.get(programID) + ". This shader expects Attribute: " + attribute + " however it is not present in the handshake.");
                        break loop;
                    }

                    int errorCheck = GL46.glGetError();
                    while (errorCheck != GL46.GL_NO_ERROR) {
                        System.out.println("Pre error:" + errorCheck);
                        errorCheck = GL46.glGetError();
                    }

                    //If handshake contains this buffered data.
                    int attribPointer = GL46.glGetAttribLocation(programID, attribute);

                    //If this attribute is out of range of the program IE compiler optimised it out skip over loading into memory.
                    errorCheck = GL46.glGetError();
                    boolean error = false;
                    while (errorCheck != GL46.GL_NO_ERROR) {
                        System.out.println("GLError:" + errorCheck);
                        if (errorCheck == GL46.GL_INVALID_VALUE ) {
                            error = true;
                            System.out.println("Attribute[" + attribute + "] could not be found in Shader[" + shaderInstances_prime.get(programID) + ']');
                            shader.removeAttribute(attribute);
                        }
                        if(errorCheck == GL46.GL_INVALID_OPERATION){

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
                        GL46.glVertexAttribPointer(attribPointer, handshake.getBytesPerVertex(attribute), GL46.GL_FLOAT, true, 0, (FloatBuffer) handshake.getAttribute(attribute));
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

        int errorCheck = GL46.glGetError();
        while (errorCheck != GL46.GL_NO_ERROR) {
//            System.out.println("Pre load uniform:" + errorCheck);
            errorCheck = GL46.glGetError();
        }

        int location = GL46.glGetUniformLocation(activeShader, name);

        errorCheck = GL46.glGetError();
        while (errorCheck != GL46.GL_NO_ERROR) {
            System.out.println("post get location:" + errorCheck);
            errorCheck = GL46.glGetError();
        }


        float[] data = new float[uniformType.getSize()];

        switch(uniformType){
            case INT : {
                GL46.glUniform1i(location, (int)uniform);
                break;
            }
            case BOOL : {
                GL46.glUniform1i(location, ((boolean)uniform) ? 1 : 0);
                break;
            }
            case FLOAT : {
                GL46.glUniform1f(location, (float)uniform);
                break;
            }
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
                if(uniform instanceof float[]) {
                    float[] matrix = (float[]) uniform;
                    GL46.glUniformMatrix4fv(location, false, matrix);
                }else{
                    float[] matrix = new float[16];
                    ((Matrix4f)uniform).get(matrix);
                    GL46.glUniformMatrix4fv(location, false, matrix);
                }
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

        errorCheck = GL46.glGetError();
        while (errorCheck != GL46.GL_NO_ERROR) {
            System.out.println("Load uniform error:" + errorCheck);
            errorCheck = GL46.glGetError();
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
            defaultShader = shaderManager.loadShader(DEFAULT_SHADER_PROGRAM);
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

    public int getDefaultShader() {
        return defaultShader;
    }

    public String lookupName(int id) {
        if(shaderInstances_prime.containsKey(id)){
            return shaderInstances_prime.get(id);
        }
        return "unknown";
    }

    public void loadAttributesFromEntity(Entity entity) {

        Collection<String> uniforms = shaders.get(shaderInstances_prime.get(activeShader)).getUniforms();

        for(String uniform : uniforms){
            if (entity.hasAttribute(uniform)) {
                ShaderManager.getInstance().loadUniformIntoActiveShader(uniform, entity.getAttribute(uniform).getData());
            }
        }

    }

    public int getActiveShader() {
        return activeShader;
    }
}
