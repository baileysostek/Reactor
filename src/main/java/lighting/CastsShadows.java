package lighting;

import graphics.renderer.FBO;

public interface CastsShadows {
    float[] getLightspaceTransform();
    FBO getDepthBuffer();
    Light getLight();
}
