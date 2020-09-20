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
            id = GL32.glGenFramebuffers();
            GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, id);

            depthTexture = SpriteBinder.getInstance().genTexture();

            createDepthBufferAttachment();
        }
        unbindFrameBuffer();
    }

    public void bindFrameBuffer(){
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, id);
    }

    public void unbindFrameBuffer(){
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
    }

    public final void resize(int width, int height){
        this.WIDTH  = width;
        this.HEIGHT = height;
        createDepthBufferAttachment();
    }

    private int createDepthBufferAttachment(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, WIDTH, HEIGHT, 0,
                GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture, 0);

        return depthTexture;
    }


    public void cleanUp(){
        GL30.glDeleteFramebuffers(id);
        GL30.glDeleteRenderbuffers(depthBuffer);
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
