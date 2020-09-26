package graphics.renderer;

import graphics.sprite.SpriteBinder;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

/**
 * Created by Bailey on 11/17/2017.
 */
public class DepthBuffer {
    int id;

    int depthTexture;
    int depthBuffer;

    private int WIDTH;
    private int HEIGHT;

    public DepthBuffer(){
        this.WIDTH = Renderer.getInstance().getWIDTH();
        this.HEIGHT = Renderer.getInstance().getHEIGHT();
        init();
    }

    public DepthBuffer(int width, int height){
        this.WIDTH = width;
        this.HEIGHT = height;
        init();
    }

    private void init(){
        //Check that FBO's are enabled on this system
        if(GL.getCapabilities().GL_EXT_framebuffer_object){
            id = GL46.glGenFramebuffers();
            GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, id);

            depthTexture = SpriteBinder.getInstance().genTexture();

            createDepthBufferAttachment();
        }
        unbindFrameBuffer();
    }

    public void bindFrameBuffer(){
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, id);
    }

    public void unbindFrameBuffer(){
        GL46.glBindFramebuffer(GL46.GL_FRAMEBUFFER, 0);
    }

    public final void resize(int width, int height){
        this.WIDTH  = width;
        this.HEIGHT = height;
        createDepthBufferAttachment();
    }

    private int createDepthBufferAttachment(){
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, depthTexture);
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_DEPTH_COMPONENT16, WIDTH, HEIGHT, 0,
                GL46.GL_DEPTH_COMPONENT, GL46.GL_FLOAT, (ByteBuffer) null);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_CLAMP_TO_EDGE);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_CLAMP_TO_EDGE);
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
