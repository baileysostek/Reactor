package editor;

import entity.Entity;
import entity.EntityEditor;
import entity.EntityManager;
import entity.component.Attribute;
import entity.component.EnumAttributeType;
import imgui.*;
import imgui.enums.*;
import input.MousePicker;
import material.Material;
import material.MaterialManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Callback;

import java.util.*;

public class AttributeRenderer{

    private static boolean initialized = false;

    private static final int MATERIAL_PREVIEW_SIZE = 64;

    private static float columnWidth = 0;

    public static void renderAttributes(Collection<Attribute> attributes){
        //First thing we do is build our set of attributes
        LinkedHashMap<String, LinkedList<Attribute>> categories = new LinkedHashMap<>();

        for(Attribute attribute : attributes){
            //Lookup category
            String cat = attribute.getCategory();
            if(!categories.containsKey(cat)){
                categories.put(cat, new LinkedList<Attribute>());
            }
            categories.get(cat).addLast(attribute);
        }


        for(String category : categories.keySet()) {
            LinkedList<Attribute> attSet = categories.get(category);
            ImGui.separator();
            ImGui.text(category);
            ImGui.separator();
            //Loop through each attribute in the list.
            //These will be Key value pairs, where the key is in the left column.
            for (Attribute attribute : attSet) {
                //If this attribute should not be rendered, just skip this one.
                if (!attribute.isVisible()) {
                    continue;
                }

                float baseWidth = ImGui.getColumnWidth();

                //Try find a type
                ImGui.columns(2);
                ImGui.pushID(Editor.getInstance().getNextID());
                if (!initialized) {
                    ImGui.setColumnWidth(0, baseWidth * 0.33f);
                }else{
                    ImGui.setColumnWidth(0, columnWidth);
                }

                columnWidth = ImGui.getColumnWidth();

                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.labelText("", attribute.getName());
                ImGui.popItemWidth();
                ImGui.popID();
                ImGui.nextColumn();
                if (!initialized) {
                    ImGui.setColumnWidth(1, baseWidth * 0.67f);
                }
//            ImGui.pushID(Editor.getInstance().getNextID());
                if (attribute.getData() instanceof Collection) {
                    if(((Collection)attribute.getData()).size() <= 0){
                        ImGui.text("[ Empty ]");
                        ImGui.columns();
                        break;
                    }
                    if (attribute.getData() instanceof LinkedList) {
                        //This can cause direct access of LL members, so we need to create new instance of LL to check for change.
                        LinkedList data = new LinkedList((LinkedList) attribute.getData());
                        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), lookupHeight(attribute));
                        int index = 0;
                        for (Object object : data) {
                            Attribute tmp = new Attribute("" + index, object);
                            tmp.setType(attribute.getType());
                            renderAttribute(tmp);
                            ImGui.sameLine();
                            //Render an add button
//                            ImGui.beginTooltip();
//                            ImGui.setTooltip("Remove:" + tmp.getData());
                            ImGui.pushID(Editor.getInstance().getNextID());
                            if(ImGui.button("Remove", -1, 14)){
                                data.set(index, null);
                                System.out.println("Setting index:"+index+" to null");
                            }else{
                                data.set(index, tmp.getData());
                            }
                            ImGui.popID();
//                            ImGui.endTooltip();
                            index++;
                        }

                        LinkedList out = new LinkedList();
                        //Filter
                        for(Object obj : data){
                            if(obj != null){
                                out.addLast(obj);
                            }
                        }

                        attribute.setData(out);
                    } else {
                        Collection<?> data = (Collection<?>) attribute.getData();
                        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), lookupHeight(attribute));
                        int index = 0;
                        for (Object object : data) {
                            Attribute tmp = new Attribute("" + index, object);
                            tmp.setType(attribute.getType());
                            renderAttribute(tmp);
                            ImGui.sameLine();
                            if(ImGui.button("-", 16, 16)){

                            }
                            object = attribute.getData();
                            index++;
                        }
                    }
                } else if (attribute.getType().equals(EnumAttributeType.COLOR)) {
                    ImGui.beginChild("" + Editor.getInstance().getNextID(), ImGui.getColumnWidth(), ImGui.getColumnWidth() + 32);
                    renderAttribute(attribute);
                } else {
                    ImGui.beginChild("" + Editor.getInstance().getNextID(), ImGui.getColumnWidth(), 16);
                    renderAttribute(attribute);
                }
                ImGui.endChild();
                ImGui.columns();
            }
        }
        ImGui.columns();

        initialized = true;

    }

    private static void renderAttribute(Attribute attribute){
        {
            if(attribute.getData() instanceof Callback){
                if(ImGui.button(attribute.getName(), ImGui.getColumnWidth() - 3, 16)){
                    ((Callback)attribute.getData()).callback();
                }
                return;
            }
            if(attribute.getData() instanceof Enum){
                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                renderEnum(attribute, ((Enum) attribute.getData()).getDeclaringClass());
                ImGui.popItemWidth();
                return;
            }
            if(attribute.getData() instanceof Material){
                Material material = (Material) attribute.getData();

                ImGui.imageButton(material.getMaterialPreview(), ImGui.getColumnWidth(), ImGui.getColumnWidth(), 0, 1, 1, 0);

                boolean rightClick = false;
                boolean leftClick  = false;
                if (ImGui.isItemHovered() && ImGui.getIO().getMouseDown(0)) {
                    leftClick = true;
                }

                if (ImGui.isItemHovered() && ImGui.getIO().getMouseDown(1)) {
                    rightClick = true;
                }

                String popupIDLeft  = Editor.getInstance().getNextID()+"";
                String popupIDRight = Editor.getInstance().getNextID()+"";


                if (leftClick) {
                    ImGui.openPopup(popupIDLeft);
                }

                if (rightClick) {
                    ImGui.openPopup(popupIDRight);
                }

                //POPUP for left click
                ImGui.setNextWindowSize(ImGui.getColumnWidth(), ImGui.getColumnWidth());
                ImVec2 vec2 = new ImVec2();
                ImGui.getWindowPos(vec2);
                ImGui.setNextWindowPos(vec2.x, vec2.y);
                if (ImGui.beginPopup(popupIDLeft)) {
                    for (Material mat : MaterialManager.getInstance().getAllMaterials()) {
                        if (ImGui.imageButton(mat.getMaterialPreview(), ImGui.getColumnWidth(), ImGui.getColumnWidth(), 0, 1, 1, 0)) {
                            for (Entity e : Editor.getInstance().getSelectedEntities()) {
                                e.setMaterial(mat);
                            }
                            ImGui.closeCurrentPopup();
                        }
                    }
                    ImGui.endPopup();
                }

                //Popup for right click
                ImGui.setNextWindowSize(ImGui.getColumnWidth(), ImGui.getColumnWidth());
                vec2 = new ImVec2();
                ImGui.getWindowPos(vec2);
                ImGui.setNextWindowPos(vec2.x, vec2.y);
                if (ImGui.beginPopup(popupIDRight)) {
                    if(ImGui.button("Edit Material", ImGui.getWindowWidth(), 32)){

                    }
                    if(ImGui.button("New Instance", ImGui.getWindowWidth(), 32)){
                        Collection<Entity> entities = Editor.getInstance().getSelectedEntities();
                        if(entities.size() > 0){
                            Entity selected = (Entity) entities.toArray()[0];
                            Material matCopy = MaterialManager.getInstance().generateMaterial(selected.getMaterial());
                            selected.setMaterial(matCopy);
                        }
                    }
                    ImGui.endPopup();
                }


                return;
            }
            if (attribute.getData() instanceof Vector4f) {
                Vector4f data = (Vector4f) attribute.getData();

                if(attribute.getType().equals(EnumAttributeType.COLOR)){
                    float[] color = new float[]{data.x, data.y, data.z, data.w};
                    ImGui.pushID(Editor.getInstance().getNextID());
                    if(ImGui.colorPicker4(attribute.getName(), color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.NoAlpha | ImGuiColorEditFlags.NoSidePreview | ImGuiColorEditFlags.NoSmallPreview | ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoLabel)){
                        attribute.setData(new Vector4f(color[0], color[1], color[2], color[3]));
                    }
                    ImGui.popID();
                }else {
                    ImFloat x = new ImFloat(data.x);
                    ImFloat y = new ImFloat(data.y);
                    ImFloat z = new ImFloat(data.z);
                    ImFloat w = new ImFloat(data.w);

                    ImGui.columns(4);
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                    ImGui.inputFloat("X", x, .1f, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                    ImGui.inputFloat("Y", y, .1f, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 0, 1, 1);
                    ImGui.inputFloat("Z", z, .1f, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 1, 1, 1, 1);
                    ImGui.inputFloat("W", w, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.columns();

                    attribute.setData(new Vector4f(x.get(), y.get(), z.get(), w.get()));
                }

                return;
            }
            if (attribute.getData() instanceof Vector3f) {
                Vector3f data = (Vector3f) attribute.getData();

                if(attribute.getType().equals(EnumAttributeType.COLOR)){
                    float[] color = new float[]{data.x, data.y, data.z};
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth());
                    if(ImGui.colorPicker3(attribute.getName(), color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.NoAlpha | ImGuiColorEditFlags.NoSidePreview | ImGuiColorEditFlags.NoSmallPreview | ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoLabel)){
                        attribute.setData(new Vector3f(color[0], color[1], color[2]));
                    }
                    ImGui.popItemWidth();
                    ImGui.popID();
                }else if(attribute.getType().equals(EnumAttributeType.SLIDERS)){
                    float[] sliderX = new float[]{data.x};
                    float[] sliderY = new float[]{data.y};
                    float[] sliderZ = new float[]{data.z};

                    ImGui.columns(3);
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                    ImGui.sliderFloat("", sliderX, 0, 360);
                    ImGui.popStyleColor();
                    ImGui.popItemWidth();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                    ImGui.sliderFloat("", sliderY, 0, 360);
                    ImGui.popStyleColor();
                    ImGui.popItemWidth();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 0, 1, 1);
                    ImGui.sliderFloat("", sliderZ, 0, 360);
                    ImGui.popStyleColor();
                    ImGui.popItemWidth();
                    ImGui.popID();
                    ImGui.columns();

                    attribute.setData(new Vector3f(sliderX[0], sliderY[0], sliderZ[0]));

                }else {
                    ImFloat x = new ImFloat(data.x);
                    ImFloat y = new ImFloat(data.y);
                    ImFloat z = new ImFloat(data.z);

                    ImGui.columns(3);
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                    ImGui.inputFloat("", x, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popItemWidth();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                    ImGui.inputFloat("", y, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popItemWidth();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 0, 1, 1);
                    ImGui.inputFloat("", z, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popItemWidth();
                    ImGui.popID();
                    ImGui.columns();

                    attribute.setData(new Vector3f(x.get(), y.get(), z.get()));
                }
                return;
            }

            if (attribute.getData() instanceof Vector2f) {
                Vector2f data = (Vector2f) attribute.getData();
                ImFloat x = new ImFloat(data.x);
                ImFloat y = new ImFloat(data.y);

                ImGui.columns(2);
                ImGui.pushID(Editor.getInstance().getNextID());
                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                ImGui.inputFloat("", x, 1, 10);
                ImGui.popItemWidth();
                ImGui.popStyleColor();
                ImGui.popID();
                ImGui.nextColumn();
                ImGui.pushID(Editor.getInstance().getNextID());
                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                ImGui.inputFloat("", y, 1, 10);
                ImGui.popItemWidth();
                ImGui.popStyleColor();
                ImGui.popID();
                ImGui.columns();

                attribute.setData(new Vector2f(x.get(), y.get()));
                return;
            }

            if (attribute.getData() instanceof Boolean) {
                boolean data = (boolean) attribute.getData();
                ImBool value = new ImBool(data);

                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.checkbox(attribute.getName(), value);
                ImGui.popItemWidth();

                attribute.setData(value.get());
                return;
            }

            if (attribute.getData() instanceof Integer) {
                int data = (int) attribute.getData();
                ImInt value = new ImInt(data);

                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.inputInt(attribute.getName(), value);
                ImGui.popItemWidth();

                attribute.setData(value.get());
                return;
            }

            if (attribute.getData() instanceof Float) {
                float data = (float) attribute.getData();
                ImFloat value = new ImFloat(data);

                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.inputFloat(attribute.getName(), value);
                ImGui.popItemWidth();

                attribute.setData(value.get());
                return;
            }

            if (attribute.getData() instanceof Double) {
                double data = (double) attribute.getData();
                ImDouble value = new ImDouble(data);

                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                if(attribute.getType().equals(EnumAttributeType.SLIDERS)){
                    float[] sliderValue = new float[]{(float) data};
                    ImGui.sliderFloat("", sliderValue, 0, 1);
                    attribute.setData((double)sliderValue[0]);
                }else{
                    ImGui.inputDouble(attribute.getName(), value);
                    attribute.setData(value.get());
                }
                ImGui.popItemWidth();

                return;
            }

            if (attribute.getData() instanceof String) {
                String data = (String) attribute.getData();
                ImString value = new ImString(data);

                int flags = ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll ;

                ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                ImGui.inputText(attribute.getName(), value, flags);
                ImGui.popItemWidth();

                attribute.setData(value.get());
                return;
            }
            //End If no type was found, render default string.
            ImGui.inputText(attribute.getName(), new ImString(attribute.getData() + ""));
        }
    }

    private static float lookupHeight(Attribute attribute){

        float modifier = 1f;

        if(attribute.getData() instanceof Collection){
            modifier = (float)((Collection) attribute.getData()).size();
            if(modifier > 0){
                //Could be an issue with large arrays.
                Object element = ((Collection) attribute.getData()).toArray()[0];
                if(element instanceof Material){
                    return ImGui.getColumnWidth() * modifier;
                }
            }
        }

        switch (attribute.getType()){
            case COLOR:{
                return (ImGui.getColumnWidth() + 32) * modifier;
            }
        }

        return 16f * modifier;
    }

    //This can take ANY enum and iterate through each element.
    private static <T extends Enum<T>> void renderEnum(Attribute<T> attribute, Class<T> cls) {
        if (ImGui.beginCombo(attribute.getName(), attribute.getData().toString())){
            int flags = ImGuiSelectableFlags.AllowDoubleClick;
            for (T item : EnumSet.allOf(cls)){
                if(ImGui.selectable(item.toString(), item.equals(attribute.getData()), flags)){
                    attribute.setData(item);
                }
            }
            ImGui.endCombo();
        }

    }

}