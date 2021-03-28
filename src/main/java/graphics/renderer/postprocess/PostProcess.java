package graphics.renderer.postprocess;

import entity.component.Attribute;
import entity.component.Component;

import java.util.LinkedList;

public class PostProcess extends Component {

    private Attribute<Integer> index = new Attribute<Integer>("PostProcess:Index", 0);
//    private Attribute<Integer> index = new Attribute<Integer>("PostProcess:Index", 0);
    private Attribute<LinkedList<PostProcessStage>> stages = new Attribute<LinkedList<PostProcessStage>>("Stages", new LinkedList<>());

    public PostProcess() {
        super("Post Process");
    }

    @Override
    protected LinkedList<Attribute> initialize() {
        LinkedList<Attribute> out = new LinkedList<Attribute>();

        out.add(index);
        out.add(stages);

        //Return out
        return out;
    }

    public void allocateTexture(PostProcessChannels channel, int textureID){
//        if(tex){
//
//        }
    }

    public void addStage(PostProcessConstants constants, Object ... params){
        PostProcessStage stage = new PostProcessStage(constants, params);
        this.stages.getData().add(stage);
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void update(double delta) {

    }

    @Override
    public void onAttributeUpdate(Attribute observed) {

    }
}