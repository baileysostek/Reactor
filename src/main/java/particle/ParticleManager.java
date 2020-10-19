package particle;

import camera.CameraManager;
import entity.component.Attribute;
import graphics.renderer.Handshake;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import models.ModelManager;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.LinkedList;

public class ParticleManager {
    //Handles Rendering, sorting, and updating particle systems.
    //Also will batch together textures into a texture atlas
    private static ParticleManager particleManager;

    private final int MAX_PARTICLES = 10000;


    private final LinkedList<ParticleSystem> systems = new LinkedList<ParticleSystem>();

    private float[] verticies;
    private float[] positions;
    private float[] colors;

    private int vao_id;
    private int vbo_vertex;
    private int vbo_pos;
    private int vbo_rot;
    private int vbo_color;
    private int vbo_scale;
    private int vbo_matrix;

    private int shaderID;

    private boolean startIndicesDirty = false;

    private ParticleManager(){
        //Start up our shader
        shaderID = ShaderManager.getInstance().loadShader("particle");
        // The VBO containing the 4 vertices of the particles.
        // Thanks to instancing, they will be shared by all particles.

        Handshake shape = ModelManager.getInstance().loadModel("cube2.obj").getFirst().getHandshake();
        FloatBuffer positions_data = (FloatBuffer) shape.getAttribute("vPosition");
        verticies = new float[positions_data.remaining()];
        positions_data.get(verticies);

//        verticies = new float[]{
//            -0.5f, -0.5f, 0.0f,
//            0.5f, -0.5f, 0.0f,
//            -0.5f, 0.5f, 0.0f,
//            0.5f, 0.5f, 0.0f,
//        };

        positions = new float[MAX_PARTICLES * 3];
        colors    = new float[MAX_PARTICLES * 4];

        //Create our vao
        vao_id = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao_id);

        //init our buffers
        for(int i = 0; i < MAX_PARTICLES; i++){
            //pos
            positions[i * 3 + 0] = (float) Math.random() * 256;
            positions[i * 3 + 1] = (float) Math.random() * 256;
            positions[i * 3 + 2] = (float) Math.random() * 256;

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
        vbo_pos = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_pos);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, positions, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(1, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the colors of the particles
        vbo_color = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, colors, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(2, 4, GL46.GL_FLOAT, false, 0, 0);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    public void update(double delta){
        // Check if indices are dirty, this happens when a ParticleSystem changes size.
        if(startIndicesDirty){
            recalculateSystemsStartIndices();
            startIndicesDirty = false;
        }
        for(ParticleSystem system : systems){
            updateSystem(system);
        }
    }

    public void render(){

        GL46.glUseProgram(shaderID);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "projection"),false, Renderer.getInstance().getProjectionMatrix());

        GL46.glBindVertexArray(vao_id);

        // 1rst attribute buffer : vertices
        GL46.glEnableVertexAttribArray(0);
        // 2nd attribute buffer : translations
        GL46.glEnableVertexAttribArray(1);
        // 3rd attribute buffer : particles' colors
        GL46.glEnableVertexAttribArray(2);
//        // 4th attribute buffer : particles' scale
//        GL46.glEnableVertexAttribArray(2);

        GL46.glVertexAttribDivisor(0, 0); // particles vertices : always reuse the same 4 vertices -> 0
        GL46.glVertexAttribDivisor(1, 1); // positions : one per quad (its center) -> 1
        GL46.glVertexAttribDivisor(2, 1); // color : one per quad -> 1


        int toRender = getAllocatedParticles();
        GL46.glDrawArraysInstanced(GL46.GL_TRIANGLES, 0, verticies.length / 3, toRender);

        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glDisableVertexAttribArray(2);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);

        GL46.glUseProgram(0);
    }

    public int getMaxParticles(){
        return MAX_PARTICLES;
    }

    public int getAllocatedParticles(){
        int count = 0;
        for(ParticleSystem p : systems){
           count += p.numParticles.getData();
        }
        return count;
    }

    //Returns the number of particles we can still render
    public int getUnallocatedParticles(ParticleSystem self){
        int remaining = MAX_PARTICLES;
        for(ParticleSystem p : systems){
            if(!p.equals(self)) {
                remaining -= p.numParticles.getData();
            }
        }
        return remaining;
    }

    public void markIndicesDirty(){
        this.startIndicesDirty = true;
    }

    public void add(ParticleSystem particleSystem) {
        if(systems.add(particleSystem)){
            updateSystem(particleSystem);
        }
    }

    public void remove(ParticleSystem particleSystem){
        systems.remove(particleSystem);
        //recalculate indices
        recalculateSystemsStartIndices();
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private void updateSystem(ParticleSystem system){
        Particle p = null;
        Vector3f systemPos = system.getPosition();

        int startIndex = system.getStartIndex() * 4;
        int numParticles = system.numParticles.getData();

        float[] positions = new float[numParticles * 3];
        float[] colors    = new float[numParticles * 4];

        for(int i = 0; i < numParticles; i++){
            p = system.getParticle(i);

            positions[i * 3 + 0] = systemPos.x + p.pos.x;
            positions[i * 3 + 1] = systemPos.y + p.pos.y;
            positions[i * 3 + 2] = systemPos.z + p.pos.z;

            colors[i * 4 + 0] = p.col.x;
            colors[i * 4 + 1] = p.col.y;
            colors[i * 4 + 2] = p.col.z;
            colors[i * 4 + 3] = 0.5f;
        }

        //Buffer
        GL46.glBindVertexArray(vao_id);
        // 2nd attribute buffer : positions of particles' centers
        GL46.glEnableVertexAttribArray(1);
        GL46.glBindBuffer( GL46.GL_ARRAY_BUFFER, vbo_pos);
        GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, startIndex * 3, positions);
        GL46.glVertexAttribPointer(1, 3, GL46.GL_FLOAT, false, 0, 0);

        // 3rd attribute buffer : particles' colors
        GL46.glEnableVertexAttribArray(2);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
        GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, startIndex * 4, colors);
        GL46.glVertexAttribPointer(2, 4, GL46.GL_FLOAT, false, 0, 0);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glBindVertexArray(0);
    }

    private void recalculateSystemsStartIndices(){
        int count = 0;
        for(ParticleSystem s : systems){
            s.overrideStartIndex(count);
            count += s.numParticles.getData();
        }
    }

    public static void initialize(){
        if(particleManager == null){
            particleManager = new ParticleManager();
        }
    }

    public static ParticleManager getInstance(){
        return particleManager;
    }
}
