package particle;

import camera.CameraManager;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
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
            colors[i * 4 + 3] = 1;
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
        GL46.glVertexAttribPointer(2, 4, GL46.GL_UNSIGNED_BYTE, false, 0, 0);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    public void update(double delta){
        //Update particles
        for(int i = 0; i < getMaxParticles(); i++){
            positions[i * 3 + 1] -= 1 * delta;
        }

    }

    public void render(){

        GL46.glUseProgram(shaderID);

        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
        GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "projection"),false, Renderer.getInstance().getProjectionMatrix());

//        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT);

        GL46.glBindVertexArray(vao_id);

        //Flush CPU buffer to GPU
        //Read buffered data
//        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_pos);
//        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, positions.length, GL46.GL_STREAM_DRAW);
//        GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, positions);
//
//        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
//        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, colors.length, GL46.GL_STREAM_DRAW);
//        GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, colors);


        // 1rst attribute buffer : vertices
        GL46.glEnableVertexAttribArray(0);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_vertex);
        GL46.glVertexAttribPointer(
            0, // attribute. No particular reason for 0, but must match the layout in the shader.
            3, // size
            GL46.GL_FLOAT, // type
            false, // normalized?
            0, // stride
            0 // array buffer offset
        );

        // 2nd attribute buffer : positions of particles' centers
        GL46.glEnableVertexAttribArray(1);
        GL46.glBindBuffer( GL46.GL_ARRAY_BUFFER, vbo_pos);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, positions, GL46.GL_STREAM_DRAW);
        GL46.glVertexAttribPointer(
            1, // attribute. No particular reason for 1, but must match the layout in the shader.
            3, // size : x + y + z + size => 4
            GL46.GL_FLOAT, // type
            false, // normalized?
            0, // stride
            0 // array buffer offset
        );

        // 3rd attribute buffer : particles' colors
        GL46.glEnableVertexAttribArray(2);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_color);
        GL46.glVertexAttribPointer(
            2, // attribute. No particular reason for 1, but must match the layout in the shader.
            4, // size : r + g + b + a => 4
            GL46.GL_UNSIGNED_BYTE, // type
            true, // normalized? *** YES, this means that the unsigned char[4] will be accessible with a vec4 (floats) in the shader ***
            0, // stride
            0 // array buffer offset
        );
        // These functions are specific to glDrawArrays*Instanced*.
        // The first parameter is the attribute buffer we're talking about.
        // The second parameter is the "rate at which generic vertex attributes advance when rendering multiple instances"
        // http://www.opengl.org/sdk/docs/man/xhtml/glVertexAttribDivisor.xml
        GL46.glVertexAttribDivisor(0, 0); // particles vertices : always reuse the same 4 vertices -> 0
        GL46.glVertexAttribDivisor(1, 1); // positions : one per quad (its center) -> 1
        GL46.glVertexAttribDivisor(2, 1); // color : one per quad -> 1

        // Draw the particules !
        // This draws many times a small triangle_strip (which looks like a quad).
        // This is equivalent to :
        // for(i in ParticlesCount) : glDrawArrays(GL_TRIANGLE_STRIP, 0, 4),
        // but faster.
        GL46.glDrawArraysInstanced(GL46.GL_TRIANGLE_STRIP, 0, 4, MAX_PARTICLES);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);

        GL46.glUseProgram(0);
    }

    public int getMaxParticles(){
        return MAX_PARTICLES;
    }

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
        systems.add(particleSystem);
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
