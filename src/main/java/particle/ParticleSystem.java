package particle;

import camera.CameraManager;
import com.google.gson.JsonObject;
import entity.Entity;
import entity.component.Attribute;
import entity.component.AttributeUtils;
import entity.component.EnumAttributeType;
import graphics.renderer.DirectDraw;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.SpriteBinder;
import models.Model;
import models.ModelManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL46;
import util.Callback;

import java.util.Collection;
import java.util.LinkedList;

public class ParticleSystem extends Entity {
    //Entity implementation for a collection of particles, contains information about what properties effect this particle system.

    //Attributes are variables controlled in editor.
    Attribute<Integer> numParticles;

    //Color
    Attribute<LinkedList<Vector3f>> startColors;
    Attribute<ColorInterpolation> deriveStartColor;
//    Attribute<LinkedList<Vector3f>> endColors;
//    Attribute<ColorInterpolation> deriveEndColor;

    //Texture
    Attribute<Boolean> useMaskTexture;

    //Lifespan
        //Curve of lifepsan values, Min, Max, Interpolation

    //Size
        //Curve of Size values, Start{min max}, End{min max}, Interpolation

    //Rotation
        //Curve of Rotation values, Start{min max}, End{min max}, Interpolation
    Attribute<Boolean> alwaysFaceCamera;

    //Opacity
        //Curve of Opacity values, Start{min max}, End{min max}, Interpolation
    Attribute<EnumBlendMode> particleBlendMode;

    //Texture



    //Physics
        //Gravity
    Attribute<Vector3f> gravity;
        //Collision Detection
        //

    //Burst Loop or Not
    Attribute<EmissionType> emissionType;

    //Emission
    Attribute<EmissionShape> emissionShape;
//    Attribute<ColorInterpolation> deriveEndColor;

    //Transition
    Attribute<ColorTransition> startToEndTransition;
    Attribute<Integer> startIndex;

    //Buttons
    Attribute<Callback> playButton;
    Attribute<Callback> pauseButton;

    private Model particleMesh;

    public float lifetime = 8.0f;
    private float time = 0;
    private int burstCount = 0;
    private boolean canBurst = false;
    private boolean needsUpdate = true;
    private boolean paused = false;

    private Particle[] particles;

    // Buffers for actual VAO data
    private float[] verticies;
    private float[] normals;
    private float[] positions;
    private float[] scales;
    private float[] colors;
    private float[] textureCore;

    private int vao_id;
    private int vbo_vertex;
    private int vbo_normal;
    private int vbo_pos;
    private int vbo_rot;
    private int vbo_color;
    private int vbo_scale;
    private int vbo_texture;

    private int shaderID;

    private int textureID;

    public ParticleSystem(){
        //Attribute config
        LinkedList<Vector3f> startColorsList = new LinkedList<Vector3f>(){};
        startColorsList.add(new Vector3f(1, 0, 0));
        startColorsList.add(new Vector3f(0, 1, 0));
        startColorsList.add(new Vector3f(0, 0, 1));
        startColors = new Attribute<LinkedList<Vector3f>>("startColor", startColorsList).setType(EnumAttributeType.COLOR);
        startColors.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                updateSystem();
                return null;
            }
        });

        deriveStartColor = new Attribute<ColorInterpolation>("startInterpolationType" , ColorInterpolation.RANDOM);
        deriveStartColor.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                updateSystem();
                return null;
            }
        });

        useMaskTexture = new Attribute<Boolean>("Use Mask Texture" , false);

//        LinkedList<Vector3f> endColorsList = new LinkedList<Vector3f>(){};
//        endColorsList.add(new Vector3f(1, 0, 0));
//        endColorsList.add(new Vector3f(0, 1, 0));
//        endColorsList.add(new Vector3f(0, 0, 1));
//        endColors = new Attribute<LinkedList<Vector3f>>("endColors", endColorsList).setType(EnumAttributeType.COLOR);
//        endColors.subscribe(new Callback() {
//            @Override
//            public Object callback(Object... objects) {
//            updateSystem();
//            return null;
//            }
//        });

        startIndex = new Attribute<Integer>("startIndex", 0).setShouldBeSerialized(false);
        startIndex.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                ParticleManager.getInstance().markIndicesDirty();
                return null;
            }
        });

        //How many we want
        numParticles = new Attribute<Integer>("numParticles", 100);
        ParticleSystem that = this;
        numParticles.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
            int remaining = ParticleManager.getInstance().getUnallocatedParticles(that);
            if(numParticles.getData() > remaining){
                numParticles.setData(remaining);
            }
            if(numParticles.getData() < 0){
                numParticles.setData(0);
            }
            updateSystem();
            return null;
            }
        });

        //Emission type
        emissionType  = new Attribute<EmissionType>( "Emission Type" , EmissionType.CONTINUOUS);
        emissionShape = new Attribute<EmissionShape>("Emission Shape", EmissionShape.CUBE);
        emissionShape.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                updateSystem();
                System.out.println("Test");
                return null;
            }
        });

        //physics
        gravity  = new Attribute<Vector3f>( "Gravity" , new Vector3f(0, -109.8f, 0));

        //Buttons
        playButton = new Attribute<Callback>("Play", new Callback() {
            @Override
            public Object callback(Object... objects) {
                start();
                return null;
            }
        }).setShouldBeSerialized(false);

        pauseButton = new Attribute<Callback>("Pause", new Callback() {
            @Override
            public Object callback(Object... objects) {
                pause();
                return null;
            }
        }).setShouldBeSerialized(false);

        // Add attributes
        this.addAttribute(numParticles);
        this.addAttribute(startColors);
        this.addAttribute(startIndex);
//        this.addAttribute(endColors);
        this.addAttribute(useMaskTexture);

        // Physics
        this.addAttribute(gravity);

        // Enums
        this.addAttribute(emissionType);
        this.addAttribute(emissionShape);

        // Buttons
        this.addAttribute(playButton);
        this.addAttribute(pauseButton);

        // Force update
        this.getAttribute("updateInEditor").setData(true);

        // Add subscription to scale
        super.getAttribute("scale").subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
            updateSystem();
            return null;
            }
        });

        particleMesh = ModelManager.getInstance().loadModel(new String[]{"quad2.obj", "icosahedron.fbx", "Mole_Mesh_1.fbx"}[(int) Math.floor(Math.random() * 3)]).getFirst();

        // Set system based on initial params
        updateSystem();
    }

    public void onAdd(){
        //Number of particles in use.
        calculateStartIndex();
        ParticleManager.getInstance().add(this);
    }

    private void calculateStartIndex(){
        startIndex.setData(ParticleManager.getInstance().getAllocatedParticles());
    }

    protected void overrideStartIndex(int allocated){
        startIndex.setData(allocated);
    }

    public void onRemove(){
        ParticleManager.getInstance().remove(this);
    }

    private void updateSystem(){
        needsUpdate = true;
        this.particles = new Particle[numParticles.getData()];
        for(int i = 0; i < particles.length; i++){
            particles[i] = new Particle(this);
        }

        //Start up our shader
        shaderID = ShaderManager.getInstance().loadShader("particle");
        // The VBO containing the 4 vertices of the particles.
        // Thanks to instancing, they will be shared by all particles.

//        Handshake shape = ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst().getHandshake();
//        FloatBuffer positions_data = ((FloatBuffer) shape.getAttribute("vPosition")).asReadOnlyBuffer();
//        verticies = new float[positions_data.remaining()];
//        positions_data.get(verticies);


        //Texture atlas for rendering this system
        textureID = SpriteBinder.getInstance().load("particles/flame_04.png").getTextureID();

        verticies = new float[]{
                -0.5f, -0.5f, 0, // -,- top left
                0.5f, -0.5f, 0, // +,- top right
                -0.5f,  0.5f, 0, // -,+ bottom left
                0.5f,  0.5f, 0, // +,+ bottom right
        };

        verticies = particleMesh.getHandshake().getAttributeRaw("vPosition");

        textureCore = new float[]{
                0, 0, // -,- top left
                1, 0, // +,- top right
                0, 1, // -,+ bottom left
                1, 1, // +,+ bottom right
        };

        positions = new float[numParticles.getData() * 3];
        normals   = particleMesh.getHandshake().getAttributeRaw("vNormal");
        scales    = new float[numParticles.getData() * 3];
        colors    = new float[numParticles.getData() * 4];

        //Create our vao
        vao_id = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao_id);

        //init our buffers
        for(int i = 0; i < numParticles.getData(); i++){
            //pos
            positions[i * 3 + 0] = (float) Math.random() * 256;
            positions[i * 3 + 1] = (float) Math.random() * 256;
            positions[i * 3 + 2] = (float) Math.random() * 256;

            //scale
            scales[i * 3 + 0] = 1;
            scales[i * 3 + 1] = 1;
            scales[i * 3 + 2] = 1;

            //col
            colors[i * 4 + 0] = (float) Math.random();
            colors[i * 4 + 1] = (float) Math.random();
            colors[i * 4 + 2] = (float) Math.random();
            colors[i * 4 + 3] = 1f;
        }


        vbo_vertex = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_vertex);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, verticies, GL46.GL_STATIC_DRAW);
        GL46.glVertexAttribPointer(0, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the positions and sizes of the particles
        vbo_normal = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_normal);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, normals, GL46.GL_STATIC_DRAW);
        GL46.glVertexAttribPointer(1, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the positions and sizes of the particles
        vbo_pos = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_pos);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, positions, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(2, 3, GL46.GL_FLOAT, false, 0, 0);


        // The VBO containing the colors of the particles
        vbo_color = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, colors, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(3, 4, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the scale of the particles
        vbo_scale = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_scale);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, scales, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(4, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the texture coordinates of the particles
        vbo_texture = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_texture);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, textureCore, GL46.GL_STATIC_DRAW);
        GL46.glVertexAttribPointer(5, 2, GL46.GL_FLOAT, false, 0, 0);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);

    }

    public void start(){
        paused = false;
        time = lifetime;
        canBurst = true;
        burstCount = 0;
    }

    public void pause(){
        paused = true;
    }

    public void stop(){
        this.paused = true;
        for (Particle p : particles) {
            p.reset();
        }
        canBurst = false;
        burstCount = 0;
    }

    public void update(double delta){
        if(!paused) {
            //Burst
            time += delta;
            if(time > lifetime){
                canBurst = true;
                burstCount++;
            }else{
                canBurst = false;
            }
            time %= lifetime;

            for (Particle p : particles) {
                p.update(delta);
            }

            Particle p = null;

            int numParticles = this.numParticles.getData();

            float[] positions = new float[numParticles * 3];
            float[] scales    = new float[numParticles * 3];
            float[] colors    = new float[numParticles * 4];

            Vector4f pos = new Vector4f(1);

            for(int i = 0; i < numParticles; i++){
                p = this.getParticle(i);

                if(p.isLive()) {

                    p.velocity.add(new Vector3f(gravity.getData()).mul((float) delta));

                    pos.x = p.pos.x;
                    pos.y = p.pos.y;
                    pos.z = p.pos.z;

                    pos = pos.mul(this.getTransform());

                    if(pos.y < 0){
                        pos.y = 0;
                        p.setLive(false);
                    }

                    positions[i * 3 + 0] = pos.x;
                    positions[i * 3 + 1] = pos.y;
                    positions[i * 3 + 2] = pos.z;

                    scales[i * 3 + 0] = p.scale.x;
                    scales[i * 3 + 1] = p.scale.y;
                    scales[i * 3 + 2] = p.scale.z;

                    colors[i * 4 + 0] = p.col.x;
                    colors[i * 4 + 1] = p.col.y;
                    colors[i * 4 + 2] = p.col.z;
                    colors[i * 4 + 3] = 1f;
                }
            }



            //Buffer
            GL46.glBindVertexArray(vao_id);
            // 2nd attribute buffer : positions of particles' centers
//        GL46.glEnableVertexAttribArray(1);
            GL46.glBindBuffer( GL46.GL_ARRAY_BUFFER, vbo_pos);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, positions);
//        GL46.glVertexAttribPointer(1, 3, GL46.GL_FLOAT, false, 0, 0);

            // 3rd attribute buffer : particles' colors
//        GL46.glEnableVertexAttribArray(2);
            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, colors);
//        GL46.glVertexAttribPointer(2, 4, GL46.GL_FLOAT, false, 0, 0);

//        GL46.glEnableVertexAttribArray(3);
            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_scale);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, scales);
//        GL46.glVertexAttribPointer(3, 3, GL46.GL_FLOAT, false, 0, 0);

            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
//        GL46.glDisableVertexAttribArray(1);
//        GL46.glDisableVertexAttribArray(2);
//        GL46.glDisableVertexAttribArray(3);
            GL46.glBindVertexArray(0);

        }
    }

    protected void render(){
        GL46.glUseProgram(shaderID);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "projection"),false, Renderer.getInstance().getProjectionMatrix());

        //Bind the texture atlas.
        GL46.glActiveTexture(GL46.GL_TEXTURE0);
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureID);
        GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "textureID"), 0);

        //blending
//        GL46.glEnable(GL46.GL_BLEND);
//        GL46.glDepthMask(false);
//        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE);

        //Bind the VAO
        GL46.glBindVertexArray(vao_id);

        // 1rst attribute buffer : vertices
        GL46.glEnableVertexAttribArray(0);
        // 2nd attribute buffer : translations
        GL46.glEnableVertexAttribArray(1);
        // 2nd attribute buffer : translations
        GL46.glEnableVertexAttribArray(2);
        // 3rd attribute buffer : particles' colors
        GL46.glEnableVertexAttribArray(3);
//        // 4th attribute buffer : particles' scale
        GL46.glEnableVertexAttribArray(4);

        GL46.glEnableVertexAttribArray(5);

        GL46.glVertexAttribDivisor(0, 0); // particles vertices : always reuse the same 4 vertices -> 0
        GL46.glVertexAttribDivisor(1, 0); // positions : one per quad (its center) -> 1
        GL46.glVertexAttribDivisor(2, 1); // positions : one per quad (its center) -> 1
        GL46.glVertexAttribDivisor(3, 1); // color : one per quad -> 1
        GL46.glVertexAttribDivisor(4, 1); // scale : one per quad -> 1

        GL46.glVertexAttribDivisor(5, 0); // texture coords : four per quad -> 0

        int toRender = numParticles.getData();
        GL46.glDrawArraysInstanced(GL46.GL_TRIANGLES, 0, verticies.length / 3, toRender);

        //Disable blend
        GL46.glDisable(GL46.GL_BLEND);
        GL46.glDepthMask(true);

        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glDisableVertexAttribArray(2);
        GL46.glDisableVertexAttribArray(3);
        GL46.glDisableVertexAttribArray(4);
        GL46.glDisableVertexAttribArray(5);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);

        GL46.glUseProgram(0);
    }

    protected boolean canRespawn(){
        switch (emissionType.getData()){
            case CONTINUOUS:{
                return true;
            }
            case BURST_LOOP:{
                return canBurst;
            }
            case BURST_SINGLE:{
                if(burstCount <= 1) {
                    return canBurst;
                }else{
                    return false;
                }
            }
            default:{
                return true;
            }
        }
    }

    protected Vector3f determineStartColor(){
        switch (deriveStartColor.getData()){
            case DISCRETE:{
                int index = (int) Math.floor(startColors.getData().size() * Math.random());
                return startColors.getData().get(index);
            }
            case RANDOM:{
                //If there are not enough colors to do a random, return the first.
                if(startColors.getData().size() < 2){
                    return startColors.getData().getFirst();
                }

                //Pick two random colors.
                int index_first  = (int) Math.floor(startColors.getData().size() * Math.random());
                int index_second = (int) Math.floor(startColors.getData().size() * Math.random());
                while(index_first == index_second){
                    index_second = (int) Math.floor(startColors.getData().size() * Math.random());
                }

                Vector3f color1 = new Vector3f(startColors.getData().get(index_first));
                Vector3f color2 = new Vector3f(startColors.getData().get(index_second));

                return new Vector3f(new Vector3f(color1).add(color2)).lerp(color1.lerp(color2, (float) Math.random()).normalize(), (float) Math.random());
            }
        }
        return new Vector3f(0);
    }

    protected Vector3f determineEndColor(){
        return new Vector3f(0);
    }

    protected Vector3f determineStartPosition(){
        switch (emissionShape.getData()){
            case POINT:{
                return new Vector3f(0);
            }
            case CUBE:{
                return new Vector3f((float)(Math.random() - 0.5f),(float)(Math.random() - 0.5f), (float)(Math.random() - 0.5f)).mul(2).mul(super.getScale());
            }
            case PLANE:{
                return new Vector3f((float)(Math.random() - 0.5f),(float)(Math.random() - 0.5f), 0).mul(2).mul(super.getScale());
            }
            case SPHERE:{
                while (true) {
                    Vector3f cube = new Vector3f((float)(Math.random() - 0.5f),(float)(Math.random() - 0.5f), (float)(Math.random() - 0.5f)).mul(2).mul(super.getScale());
                    if ( cube.length() <= super.getScale().x ){
                        return cube;
                    }
                }
            }
            case CYLINDER:{
                while (true) {
                    Vector3f cube = new Vector3f((float)(Math.random() - 0.5f),0, (float)(Math.random() - 0.5f)).mul(2).mul(super.getScale());
                    if ( cube.length() <= super.getScale().x ){
                        cube.y = (float)(Math.random() - 0.5f) * (2f * super.getScale().y);
                        return cube;
                    }
                }
            }
        }
        return new Vector3f(0);
    }

    public boolean needsUpdate(){
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate){
        this.needsUpdate = needsUpdate;
    }

    public Particle getParticle(int index){
        return particles[index];
    }

    public int getStartIndex() {
        return startIndex.getData();
    }

    @Override
    public void renderInEditor(boolean selected){
        switch (this.emissionShape.getData()){
            case RING:{

            }

            default: {
                DirectDraw.getInstance().Draw3D.drawRing(this.getPosition(), new Vector2f(super.getScale().x), new Vector3f(1, 0, 0), 32, new Vector3f(1));
                DirectDraw.getInstance().Draw3D.drawRing(this.getPosition(), new Vector2f(super.getScale().y), new Vector3f(0, 1, 0), 32, new Vector3f(1));
                DirectDraw.getInstance().Draw3D.drawRing(this.getPosition(), new Vector2f(super.getScale().z), new Vector3f(0, 0, 1), 32, new Vector3f(1));
            }
        }

        DirectDraw.getInstance().Draw3D.drawBillboard(new Vector3f(this.getPosition()), new Vector2f(1), ParticleManager.getInstance().getParticleSystemSVG());

    }

    public void setStartColor(Vector3f color){
        this.startColors.getData().clear();
        this.startColors.getData().add(color);
        this.updateSystem();
    }

    public void setStartColors(Collection<Vector3f> colors){
        this.startColors.getData().clear();
        this.startColors.setData(new LinkedList<>(colors));
        this.updateSystem();
    }

    @Override
    public JsonObject serialize(){
        return super.serialize();
    }

    @Override
    public ParticleSystem deserialize(JsonObject data) {
        super.deserialize(data);

        startColors    = AttributeUtils.synchronizeWithParent(startColors  , this);
        emissionShape  = AttributeUtils.synchronizeWithParent(emissionShape, this);
        emissionType   = AttributeUtils.synchronizeWithParent(emissionType , this);
        numParticles   = AttributeUtils.synchronizeWithParent(numParticles , this);
        useMaskTexture = AttributeUtils.synchronizeWithParent(useMaskTexture , this);
        gravity        = AttributeUtils.synchronizeWithParent(gravity , this);

        this.updateSystem();
        return this;
    }
}
