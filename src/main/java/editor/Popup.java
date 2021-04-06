package editor;

import imgui.ImGui;

public abstract class Popup {

    private boolean open = false;
    private String name;

    public Popup(String name) {
        this.name = name;
        if(Editor.getInstance() != null){
            Editor.getInstance().registerPopup(this);
        }
    }

    public void open(){
        this.open = true;
    }

    public void close(){
        this.open = false;
    }

    public abstract void render();

    public String getTitle(){
        return this.name;
    }

    public boolean isOpen(){
        return open;
    }
}