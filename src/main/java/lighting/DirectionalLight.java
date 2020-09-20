package lighting;

import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import input.Keyboard;
import util.Callback;

public class DirectionalLight extends Light {

    private FBO depthBuffer;

    public DirectionalLight(){
        depthBuffer = new FBO();
        setTexture(depthBuffer.getDepthTexture());


        Keyboard.getInstance().addPressCallback(Keyboard.ONE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                setTexture(depthBuffer.getDepthTexture());
                return null;
            }
        });
        Keyboard.getInstance().addPressCallback(Keyboard.TWO, new Callback() {
            @Override
            public Object callback(Object... objects) {
                setTexture(depthBuffer.getTextureID());
                return null;
            }
        });
    }

    @Override
    public void update(double delta){
        LightingManager.getInstance().drawFromMyPerspective(this);
    }

    public FBO getDepthBuffer() {
        return this.depthBuffer;
    }
}
