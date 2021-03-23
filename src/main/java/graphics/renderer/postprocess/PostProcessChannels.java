package graphics.renderer.postprocess;

public enum PostProcessChannels {
    SCENE_TEXTURE(true),
    SCENE_WORLD_POSITION_TEXTURE(true),
    SCENE_DEPTH_TEXTURE(true),
    SCENE_NORMAL_TEXTURE(true),

    TEXTURE_CHANNEL_0(false),
    TEXTURE_CHANNEL_1(false),
    TEXTURE_CHANNEL_2(false),
    TEXTURE_CHANNEL_3(false),
    TEXTURE_CHANNEL_4(false),
    TEXTURE_CHANNEL_5(false),
    TEXTURE_CHANNEL_6(false),
    TEXTURE_CHANNEL_7(false),
    TEXTURE_CHANNEL_8(false),
    TEXTURE_CHANNEL_9(false);

    protected boolean resrved;
    private PostProcessChannels(boolean isResrved){
        this.resrved = isResrved;
    }

    public boolean isResrved(){
        return resrved;
    }
}
