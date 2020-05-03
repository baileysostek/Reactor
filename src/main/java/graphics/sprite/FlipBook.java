package graphics.sprite;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import graphics.animation.Timeline;
import util.StringUtils;


public class FlipBook extends Sprite {

    int frameWidth;
    int frameHeight;
    int frameCount;
    int atlasWidth;
    int atlasHeight;
    int spritesPerRow;
    int spritesPerCol;

    private float duration;

    private Timeline timeline;

    protected FlipBook(int textureID, int atlasWidth, int atlasHeight, String filePath) {
        super(textureID, atlasWidth, atlasHeight);
        //Record atlas width and height
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;

        //Replace .png
        filePath = filePath.replaceAll(".png", ".json");

        try {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(StringUtils.load("\\images\\"+filePath)).getAsJsonObject();

            this.frameWidth  = object.get("frame_width").getAsInt();
            this.frameHeight = object.get("frame_height").getAsInt();
            this.frameCount  = object.get("frame_count").getAsInt();

            this.duration    = object.get("duration").getAsFloat();

            this.spritesPerRow = (this.atlasWidth  / this.frameWidth );
            this.spritesPerCol = (this.atlasHeight / this.frameHeight);

            this.setUiScale(frameWidth, frameHeight);
            this.setTextureScale(((float)this.frameWidth / (float)this.atlasWidth), ((float)this.frameHeight / (float)this.atlasHeight));

            timeline = new Timeline();
            timeline.setDuration(duration);
            timeline.start();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setLoopPoint(int frameNumber){
//        timeline.setAnimationEnd(EnumAnimationEnd.BACKTRACK);
//        timeline.setBacktrackPoint(new Double((float)frameNumber / (float)this.frameCount));
    }

    @Override
    public int getTextureID(){
//        int frameNumber = (int) Math.floor((frameCount) * timeline.value());
//        super.setTextureOffset(frameNumber % spritesPerRow , (int) Math.floor(frameNumber / spritesPerCol));
        return super.getTextureID();
    }

    public Timeline getTimeline(){
        return this.timeline;
    }


}
