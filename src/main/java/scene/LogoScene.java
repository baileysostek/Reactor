package scene;

import engine.Reactor;
import graphics.animation.Timeline;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import graphics.ui.UIManager;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Callback;

public class LogoScene extends Scene{

    private Sprite sprite;
    private Timeline fadeIntimeline;

    private float inDuration   = 0.33f;
    private float stayDuration = 1f;
    private float outDuration  = 0.33f;

    private final Scene nextScene;

    public LogoScene(String logoFile, Scene nextScene){
        this.sprite = SpriteBinder.getInstance().load(logoFile);
        this.nextScene = nextScene;
    }

    public LogoScene setFadeInDuration(float duration){
        this.inDuration = duration;
        return this;
    }

    public LogoScene setFadeOutDuration(float duration){
        this.outDuration = duration;
        return this;
    }

    public LogoScene setStayDuration(float duration){
        this.stayDuration = duration;
        return this;
    }

    @Override
    public void onLoad() {
        fadeIntimeline = new Timeline();

        fadeIntimeline.setDuration(this.inDuration + this.stayDuration + outDuration);

        fadeIntimeline.addKeyFrame("fade_alpha", 0, 1);
        fadeIntimeline.addKeyFrame("fade_alpha", this.inDuration, 0);
        fadeIntimeline.addKeyFrame("fade_alpha", this.inDuration + stayDuration, 0);
        fadeIntimeline.addKeyFrame("fade_alpha", fadeIntimeline.getDuration(), 1);

        fadeIntimeline.addCallback(fadeIntimeline.getDuration(), new Callback() {
            @Override
            public Object callback(Object... objects) {
                SceneManager.getInstance().setScene(nextScene, SceneManager.getInstance().FadeToBlack());
                return null;
            }
        });

        fadeIntimeline.start();
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void update(double delta) {
        if(fadeIntimeline.isRunning()){
            fadeIntimeline.update(delta);
        }
    }

    @Override
    public void render() {
        //Bagkround color
        UIManager.getInstance().fillColorBackground(new Vector4f(0, 0, 0, 1));

        //Draw logo in center of screen
        UIManager.getInstance().drawImage(((Renderer.getWIDTH() - sprite.getWidth())/2f), ((Renderer.getHEIGHT() - sprite.getHeight())/2f), sprite.getWidth(), sprite.getHeight(), sprite);

        //
        UIManager.getInstance().fillColorForeground(new Vector4f(0, 0, 0, fadeIntimeline.getValueOf("fade_alpha")));
    }

    @Override
    public void onPress(Vector2f pos) {

    }

    @Override
    public void onMove(Vector2f delta) {

    }

    @Override
    public void onRelease(Vector2f pos) {

    }
}
