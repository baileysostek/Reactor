package scene.transition;

import graphics.renderer.DirectDraw;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class FadeOutFadeIn extends Transition{

    private final Vector3f color;
    private Vector4f colorBuffer = new Vector4f();


    public FadeOutFadeIn(float time, Vector3f color) {
        super(time);
        this.color = color;

        super.setCrossoverPoint(time / 2f);

        super.addKeyframeForTrack("alpha", 0, 0f);
        super.addKeyframeForTrack("alpha", time / 2f, 1f);
        super.addKeyframeForTrack("alpha", time, 0f);
    }


    @Override
    public void render() {

        colorBuffer.set(color, super.getKeyframeValue("alpha"));

        DirectDraw.getInstance().Draw2D.fillColorForeground(colorBuffer);
    }
}