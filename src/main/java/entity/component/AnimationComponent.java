package entity.component;

import models.Animation;
import models.Model;

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

    //Our list of animations
    private HashMap<String, Animation> animations;


    public AnimationComponent(Model model){
        super("Animation");
        animations = model.getAnimations();
        animationNames.setData(new LinkedList<String>(animations.keySet()));
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
}
