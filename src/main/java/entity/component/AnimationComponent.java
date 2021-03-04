package entity.component;

import com.google.gson.JsonObject;
import graphics.renderer.DirectDraw;
import graphics.sprite.Colors;
import models.Animation;
import models.Model;
import models.ModelManager;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;

public class AnimationComponent extends Component{

    //This Components attributes
    private Attribute<LinkedList<String>> animationNames = new Attribute<LinkedList<String>>("Animations", new LinkedList<>()).setType(EnumAttributeType.PICKER);
    private Attribute<Double> deltaTime     = new Attribute<Double>("Delta", 0d).setType(EnumAttributeType.SLIDERS);
    private Attribute<Float> duration      = new Attribute<Float>("Duration", 1f);
    private Attribute<Float> playbackRate  = new Attribute<Float>("Play Rate", 1f);
    //Used to offset this animation
    private Attribute<Double> animationIndex  = new Attribute<Double>("animationIndex", 0d).setVisible(false);

    //Used to represent bones and bone transforms
    private Attribute<LinkedList<String>> bones = new Attribute<LinkedList<String>>("bones", new LinkedList<>());

    //Our list of animations
    private HashMap<String, Animation> animations;

    //Hold reference to model
    private Model model;


    public AnimationComponent(){
        super("Animation");
    }

    public void setModel(Model model){
        this.model = model;
        animations = model.getAnimations();
        animationNames.setData(new LinkedList<String>(animations.keySet()));
        duration.setData((float) animations.get(animationNames.getData().getFirst()).getDuration() / (float) animations.get(animationNames.getData().getFirst()).getFramesPerSecond());

        //Get the bones from the animation
        bones.setData(new LinkedList<>(model.getBoneNames()));
    }

    @Override
    protected LinkedList<Attribute> initialize() {
        LinkedList<Attribute> out = new LinkedList<Attribute>();

        //Add the attributes
        out.add(animationNames);
        out.add(deltaTime);
        out.add(duration);
        out.add(playbackRate);
        out.add(animationIndex);
        out.add(bones);

        //Do initial state check
        onAttributeUpdate(null);

        //Return out
        return out;
    }

    @Override
    public void update(double delta) {
        //This is the frame update
        deltaTime.setData((deltaTime.getData() + (delta * playbackRate.getData())) % duration.getData());
        if(deltaTime.getData() < 0){
            deltaTime.setData(1f - deltaTime.getData());
        }
        animationIndex.setData(deltaTime.getData() / duration.getData());

        //Parent model to bone
//        Vector3f pos = model.getAnimatedBoneTransform("Armature|Armature.001|mixamo.com|Layer0", "mixamorig:RightHand", animationIndex.getData()).getTranslation(new Vector3f());

//        DirectDraw.getInstance().drawLine(new Vector3f(super.getParent().getPosition()), pos.add(super.getParent().getPosition()), Colors.MAGENTA);

    }

    @Override
    public String getName() {
        return "Animation!";
    }

    @Override
    public void onRemove(){

    }

    @Override
    public void onAttributeUpdate(Attribute observed) {

    }

    public double getAnimationIndex(){
        return animationIndex.getData();
    }

    public String getCurrentAnimation(){
        if(animationNames.getData().size() > 0) {
            return animationNames.getData().getFirst();
        }
        return "T-Pose";
    }

    @Override
    public AnimationComponent deserialize(JsonObject data){
        if(data.has("model")){
            String modelName = data.get("model").getAsString();
            this.setModel(ModelManager.getInstance().loadModel(modelName).getFirst());
        }
        return this;
    }
}
