package particle;

import camera.CameraManager;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

import java.nio.FloatBuffer;
import java.util.LinkedList;

public class ParticleManager {
    //Handles Rendering, sorting, and updating particle systems.
    //Also will batch together textures into a texture atlas
    private static ParticleManager particleManager;

    private final int MAX_PARTICLES = 100000;


    private final LinkedList<ParticleSystem> systems = new LinkedList<ParticleSystem>();

    private float[] verticies;
    private float[] positions;
    private float[] colors;

    private int vao_id;
    private int vbo_vertex;
    private int vbo_pos;
    private int vbo_color;

    private int shaderID;

    private ParticleManager(){
        //Start up our shader
        shaderID = ShaderManager.getInstance().loadShader("particle");
        // The VBO containing the 4 vertices of the particles.
        // Thanks to instancing, they will be shared by all particles.
        verticies = new float[]{
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            -0.5f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
        };

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
        GL46.glDrawArraysInstanced(GL46.GL_TRIANGLE_STRIP, 0, 4, toRender);

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

    private FloatBuffer storeDataInFloatBuffer(float[] data){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public void add(ParticleSystem particleSystem) {
        if(systems.add(particleSystem)){
            updateSystem(particleSystem);
        }
    }

    private void updateSystem(ParticleSystem system){

        Particle p = null;
        Vector3f systemPos = system.getPosition();

        int startIndex = system.getStartIndex();

        float[] positions = new float[system.numParticles.getData() * 3];
        float[] colors    = new float[system.numParticles.getData() * 4];

        for(int i = 0; i < system.numParticles.getData(); i++){
            p = system.getParticle(i);

            positions[i * 3 + 0] =  systemPos.x;
            positions[i * 3 + 1] =  systemPos.y;
            positions[i * 3 + 2] =  systemPos.z;

            colors[i * 4 + 0] = system.getColor().x;
            colors[i * 4 + 1] = system.getColor().y;
            colors[i * 4 + 2] = system.getColor().z;
            colors[i * 4 + 3] = 1f;
        }

        //Buffer
        GL46.glBindVertexArray(vao_id);
        // 2nd attribute buffer : positions of particles' centers
        GL46.glEnableVertexAttribArray(1);
        GL46.glBindBuffer( GL46.GL_ARRAY_BUFFER, vbo_pos);
        GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, startIndex, positions);
        GL46.glVertexAttribPointer(1, 3, GL46.GL_FLOAT, false, 0, 0);


        // 3rd attribute buffer : particles' colors
        GL46.glEnableVertexAttribArray(2);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
        GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, startIndex, colors);
        GL46.glVertexAttribPointer(2, 4, GL46.GL_FLOAT, false, 0, 0);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glBindVertexArray(0);
    }

    public void remove(ParticleSystem particleSystem){
        systems.remove(particleSystem);
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
