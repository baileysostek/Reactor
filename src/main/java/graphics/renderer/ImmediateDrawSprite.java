package graphics.renderer;

import camera.CameraManager;
import graphics.renderer.Handshake;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.SpriteBinder;
import models.ModelManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ImmediateDrawSprite {

    private final int MAX_SPRITES  = 1024;

    private float[] verticies;
    private float[] positions;
    private float[] scales;
    private float[] textureCore;
    private float[] colors;

    private int vao_id;
    private int vbo_vertex;
    private int vbo_pos;
    private int vbo_scale;
    private int vbo_texture;
    private int vbo_colors;

    private int shaderID;

    private LinkedHashMap<Integer, LinkedList<Billboard>> billboards = new LinkedHashMap<>();

    private boolean startIndicesDirty = false;

    protected ImmediateDrawSprite(){
        //Start up our shader
        shaderID = ShaderManager.getInstance().loadShader("billboard");

        //Every sprite will be a quad
        verticies = new float[]{
            -0.5f, -0.5f, 0, // -,- top left
            0.5f, -0.5f, 0, // +,- top right
            -0.5f,  0.5f, 0, // -,+ bottom left
            0.5f,  0.5f, 0, // +,+ bottom right
        };

        textureCore = new float[]{
            0, 0, // -,- top left
            1, 0, // +,- top right
            0, 1, // -,+ bottom left
            1, 1, // +,+ bottom right
        };

        positions = new float[MAX_SPRITES * 3];
        scales    = new float[MAX_SPRITES * 3];
        colors    = new float[MAX_SPRITES * 3];

        //Create our vao
        vao_id = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao_id);

        //init our buffers
        for(int i = 0; i < MAX_SPRITES; i++){
            //pos
            positions[i * 3 + 0] = 0;
            positions[i * 3 + 1] = 0;
            positions[i * 3 + 2] = 0;

            //scale
            scales[i * 3 + 0] = 1;
            scales[i * 3 + 1] = 1;
            scales[i * 3 + 2] = 1;

            //colors
            colors[i * 3 + 0] = 1;
            colors[i * 3 + 1] = 1;
            colors[i * 3 + 2] = 1;
        }


        vbo_vertex = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_vertex);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, verticies, GL46.GL_DYNAMIC_DRAW);
        GL46.glVertexAttribPointer(0, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the positions and sizes of the particles
        vbo_pos = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_pos);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, positions, GL46.GL_DYNAMIC_DRAW);
        GL46.glVertexAttribPointer(1, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the scale of the particles
        vbo_scale = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_scale);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, scales, GL46.GL_DYNAMIC_DRAW);
        GL46.glVertexAttribPointer(2, 3, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the texture coordinates of the particles
        vbo_texture = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_texture);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, textureCore, GL46.GL_DYNAMIC_DRAW);
        GL46.glVertexAttribPointer(3, 2, GL46.GL_FLOAT, false, 0, 0);

        // The VBO containing the color of the billboard
        vbo_colors = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_colors);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, colors, GL46.GL_DYNAMIC_DRAW);
        GL46.glVertexAttribPointer(4, 3, GL46.GL_FLOAT, false, 0, 0);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    public void drawBillboard(Vector3f position, Vector2f scale, int textureID){
        if(!billboards.containsKey(textureID)){
            billboards.put(textureID, new LinkedList<>());
        }
        Billboard billboard = new Billboard(position, scale, new Vector3f(1), textureID);
        billboards.get(textureID).add(billboard);
    }

    public void drawBillboard(Vector3f position, Vector2f scale, Vector3f color, int textureID){
        if(!billboards.containsKey(textureID)){
            billboards.put(textureID, new LinkedList<>());
        }
        Billboard billboard = new Billboard(position, scale, color, textureID);
        billboards.get(textureID).add(billboard);
    }

    public void render(){
        //Buffer data
        for(int textureID : billboards.keySet()){
            int index = 0;
            int toRender = billboards.get(textureID).size();

            for(Billboard billboard : billboards.get(textureID)){
                positions[index * 3 + 0] = billboard.getPosition().x;
                positions[index * 3 + 1] = billboard.getPosition().y;
                positions[index * 3 + 2] = billboard.getPosition().z;

                scales[index * 3 + 0] = billboard.getScale().x;
                scales[index * 3 + 1] = billboard.getScale().y;
                scales[index * 3 + 2] = 1f;

                colors[index * 3 + 0] = billboard.getColor().x;
                colors[index * 3 + 1] = billboard.getColor().y;
                colors[index * 3 + 2] = billboard.getColor().z;

                index++;
            }

            //Actual render
            //Buffer
            GL46.glBindVertexArray(vao_id);

            GL46.glBindBuffer( GL46.GL_ARRAY_BUFFER, vbo_pos);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, positions);

            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_scale);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, scales);

            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_colors);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0, colors);

            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);

            GL46.glBindVertexArray(0);

            //Draw call
            GL46.glUseProgram(shaderID);

            GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "view"), false, CameraManager.getInstance().getActiveCamera().getTransform());
            GL46.glUniformMatrix4fv(GL46.glGetUniformLocation(shaderID, "projection"),false, Renderer.getInstance().getProjectionMatrix());

            //Bind the texture atlas.
            GL46.glActiveTexture(GL46.GL_TEXTURE0);
            GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureID);
            GL46.glUniform1i(GL46.glGetUniformLocation(shaderID, "textureID"), 0);

            //blending
            GL46.glEnable(GL46.GL_BLEND);
            GL46.glDepthMask(false);
            GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE);

            //Bind the VAO
            GL46.glBindVertexArray(vao_id);

            // 1rst attribute buffer : vertices
            GL46.glEnableVertexAttribArray(0);
            // 2nd attribute buffer : translations
            GL46.glEnableVertexAttribArray(1);
            // 3rd attribute buffer : particles' colors
            GL46.glEnableVertexAttribArray(2);
//        // 4th attribute buffer : particles' scale
            GL46.glEnableVertexAttribArray(3);
            GL46.glEnableVertexAttribArray(4);

            GL46.glVertexAttribDivisor(0, 0); // particles vertices : always reuse the same 4 vertices -> 0
            GL46.glVertexAttribDivisor(1, 1); // positions : one per quad (its center) -> 1
            GL46.glVertexAttribDivisor(2, 1); // scale : one per quad -> 1
            GL46.glVertexAttribDivisor(3, 0); // texture coords : four per quad -> 0
            GL46.glVertexAttribDivisor(4, 1); // color one per quad -> 0

            GL46.glDrawArraysInstanced(GL46.GL_TRIANGLE_STRIP, 0, verticies.length / 3, toRender);
        }

        //Disable blend
        GL46.glDisable(GL46.GL_BLEND);
        GL46.glDepthMask(true);

        GL46.glDisableVertexAttribArray(0);
        GL46.glDisableVertexAttribArray(1);
        GL46.glDisableVertexAttribArray(2);
        GL46.glDisableVertexAttribArray(3);
        GL46.glDisableVertexAttribArray(4);

        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);

        GL46.glUseProgram(0);

        //Cleanup this frame
        billboards.clear();
    }

    private class Billboard{
        Vector3f position;
        Vector2f scale;
        Vector3f color;
        int textureID;

        public Billboard(Vector3f position, Vector2f scale, Vector3f color, int textureID) {
            this.position = position;
            this.scale = scale;
            this.textureID = textureID;
            this.color = color;
        }

        public Vector3f getPosition() {
            return position;
        }

        public Vector2f getScale() {
            return scale;
        }

        public Vector3f getColor() {
            return color;
        }

        public int getTextureID() {
            return textureID;
        }

    }
}