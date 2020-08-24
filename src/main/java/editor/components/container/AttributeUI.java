package editor.components.container;

import editor.components.UIComponet;
import entity.component.Attribute;

public class AttributeUI extends UIComponet {

    private Attribute value;

    public AttributeUI(Attribute value){
        this.value = value;
    }

    @Override
    public void onAdd() {

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

    @Override
    public String getName() {
        return null;
    }
}
