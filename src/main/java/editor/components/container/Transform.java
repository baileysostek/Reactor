package editor.components.container;

import editor.components.UIComponet;
import entity.interfaces.Transformable;

public class Transform extends UIComponet {

    private Axis position_a  ;
    private Axis rotation_a;
    private Axis scale_a     ;

    private CollapsibleHeader pos_header = new CollapsibleHeader().setLabel("Position");
    private CollapsibleHeader rot_header = new CollapsibleHeader().setLabel("Rotation");
    private CollapsibleHeader scale_header = new CollapsibleHeader().setLabel("Scale");

    private Transformable transformable;

    public Transform(){
        position_a   = new Axis();
        rotation_a   = new Axis().setRange(-1, 1);
        scale_a      = new Axis();
    }

    public Transform(Transformable t){
        transformable = t;

        position_a   = new Axis(t.getPosition()).setRange(-10f, 10f);
        rotation_a   = new Axis(t.getRotation()).setRange((float)(-2f * Math.PI), (float)(2f * Math.PI));
        scale_a      = new Axis(t.getScale());
    }

    @Override
    public void onAdd() {
        //Set slider ranges
        scale_a.setRange(0, 2).setValue(1f);

        //Add grandchildren
        pos_header.addChild(position_a);
        rot_header.addChild(rotation_a);
        scale_header.addChild(scale_a);

        //Add to us
        this.addChild(pos_header);
        this.addChild(rot_header);
        this.addChild(scale_header);
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void self_update(double delta) {

    }

    @Override
    public void self_render() {

    }

    @Override
    public void self_post_render() {

    }
}
