package graphics.renderer;

import camera.Camera;
import graphics.sprite.SpriteBinder;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL46.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.GL_TEXTURE_BORDER_COLOR;

/**
 * Created by Bailey on 11/17/2017.
 */
public class FBO {
    int id;
    int textureID;
    int depthTexture;
    int depthBuffer;

    private int WIDTH;
    private int HEIGHT;
    private int originalWidth;
    private int originalHeight;

    //If we unbind a frame buffer from within a framebuffer, we want to return the last item on the stack, not just 0
    private static LinkedList<Integer> bufferStack = new LinkedList<>();

    public FBO(){
        this.WIDTH = Renderer.getInstance().getWIDTH();
        this.HEIGHT = Renderer.getInstance().getHEIGHT();
        //Check that FBO's are enabled on this system
        if(GL.getCapabilities().GL_EXT_framebuffer_object){
            id = GL46.glGenFramebuffers();
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, id);
            bufferStack.push(id);
            GL46.glDrawBuffer(GL46.GL_COLOR_ATTACHMENT0);

            textureID = SpriteBinder.getInstance().genTexture();
            depthTexture = SpriteBinder.getInstance().genTexture();

            createTextureAttachment();
            createDepthBufferAttachment();
        }
        unbindFrameBuffer();
    }

    public FBO(int width, int height){
        this.WIDTH = width;
        this.HEIGHT = height;
        //Check that FBO's are enabled on this system
        if(GL.getCapabilities().GL_EXT_framebuffer_object){
            id = GL46.glGenFramebuffers();
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, id);
            bufferStack.push(id);
            GL46.glDrawBuffer(GL46.GL_COLOR_ATTACHMENT0);

            textureID = SpriteBinder.getInstance().genTexture();
            depthTexture = SpriteBinder.getInstance().genTexture();

            createTextureAttachment();
            createDepthBufferAttachment();
        }
        unbindFrameBuffer();
    }

    public void bindFrameBuffer(){
//        GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, id);
        bufferStack.push(id);
        GL46.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_STENCIL_BUFFER_BIT);
        double[] buffer = new double[4];
        GL46.glGetDoublev(GL46.GL_VIEWPORT, buffer);
        originalWidth  = (int) buffer[2];
        originalHeight = (int) buffer[3];
        GL46.glViewport(0, 0, WIDTH, HEIGHT);
    }

    public void unbindFrameBuffer(){
        bufferStack.pop();
        GL46.glViewport(0, 0, originalWidth, originalHeight);
        GL46.glFlush();
        int newBufferID = 0;
        if(bufferStack.size() > 0){
            newBufferID = bufferStack.peek();
        }
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, newBufferID);
    }

    public final void resize(int width, int height){
        this.WIDTH  = width;
        this.HEIGHT = height;
        createTextureAttachment();
        createDepthBufferAttachment();
    }

    private int createTextureAttachment(){
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureID);
        ByteBuffer buffer = ByteBuffer.allocateDirect((this.WIDTH * this.HEIGHT) * 4);
        for (int i = 0; i < (this.WIDTH * this.HEIGHT); i++) {
            buffer.put((byte) 0x00);
            buffer.put((byte) 0x00);
            buffer.put((byte) 0x00);
            buffer.put((byte) 0x00);
        }
        buffer.flip();
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGB, this.WIDTH, this.HEIGHT, 0, GL46.GL_RGB, GL46.GL_UNSIGNED_BYTE, buffer);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_LINEAR);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
        GL46.glFramebufferTexture(GL46.GL_FRAMEBUFFER, GL46.GL_COLOR_ATTACHMENT0, textureID, 0);
        return textureID;
    }

    private int createDepthBufferAttachment(){
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, depthTexture);
        ByteBuffer buffer = ByteBuffer.allocateDirect((this.WIDTH * this.HEIGHT) * 4);
        for (int i = 0; i < (this.WIDTH * this.HEIGHT); i++) {
            buffer.put((byte) 0x00);
            buffer.put((byte) 0x00);
            buffer.put((byte) 0x00);
            buffer.put((byte) 0x00);
        }
        buffer.flip();
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_DEPTH_COMPONENT32, this.WIDTH, this.HEIGHT, 0, GL46.GL_DEPTH_COMPONENT, GL46.GL_FLOAT, buffer);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_BORDER);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_BORDER);
        GL46.glTexParameterfv(GL46.GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        GL46.glFramebufferTexture(GL46.GL_FRAMEBUFFER, GL46.GL_DEPTH_ATTACHMENT, depthTexture, 0);

        return depthTexture;
    }


    public void cleanUp(){
        GL46.glDeleteFramebuffers(id);
        GL46.glDeleteRenderbuffers(depthBuffer);
    }

    public int getFBOID(){
        return id;
    }

    public int getTextureID(){
        return textureID;
    }

    public int getDepthTexture(){
        return this.depthTexture;
    }


    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }
}
