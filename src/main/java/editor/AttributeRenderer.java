package editor;

import entity.component.Attribute;
import entity.component.EnumAttributeType;
import imgui.*;
import imgui.enums.ImGuiCol;
import imgui.enums.ImGuiColorEditFlags;
import imgui.enums.ImGuiInputTextFlags;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;

public class AttributeRenderer{

    public static void renderAttributes(Collection<Attribute> attributes){
        //Loop through each attribute in the list.
        //These will be Key value pairs, where the key is in the left column.
        for (Attribute attribute : attributes) {

            //If this attribute should not be rendered, just skip this one.
            if(!attribute.isVisible()){
                continue;
            }

            //Try find a type
            ImGui.columns(2);
            ImGui.pushID(Editor.getInstance().getNextID());
            ImGui.pushItemWidth(ImGui.getColumnWidth());
            ImGui.labelText("", attribute.getName());
            ImGui.popItemWidth();
            ImGui.popID();
            ImGui.nextColumn();
//            ImGui.pushID(Editor.getInstance().getNextID());
            ImGui.beginChild(""+Editor.getInstance().getNextID(), ImGui.getColumnWidth(), 16 );
            loop:{
                if (attribute.getData() instanceof Vector4f) {
                    Vector4f data = (Vector4f) attribute.getData();

                    if(attribute.getType().equals(EnumAttributeType.COLOR)){
                        float[] color = new float[]{data.x, data.y, data.z};
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.colorPicker3(attribute.getName(), color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.NoAlpha | ImGuiColorEditFlags.NoSidePreview | ImGuiColorEditFlags.NoSmallPreview | ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoLabel);
                        attribute.setData(new Vector3f(color[0], color[1], color[2]));
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


                    break loop;
                }
                if (attribute.getData() instanceof Vector3f) {
                    Vector3f data = (Vector3f) attribute.getData();

                    if(attribute.getType().equals(EnumAttributeType.COLOR)){
                        float[] color = new float[]{data.x, data.y, data.z};
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.pushItemWidth(ImGui.getColumnWidth());
                        ImGui.colorPicker3(attribute.getName(), color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.NoAlpha | ImGuiColorEditFlags.NoSidePreview | ImGuiColorEditFlags.NoSmallPreview | ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoLabel);
                        ImGui.popItemWidth();
                        attribute.setData(new Vector3f(color[0], color[1], color[2]));
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


                    break loop;
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

                    break loop;
                }

                if (attribute.getData() instanceof Boolean) {
                    boolean data = (boolean) attribute.getData();
                    ImBool value = new ImBool(data);

                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.checkbox(attribute.getName(), value);
                    ImGui.popItemWidth();

                    attribute.setData(value.get());

                    break loop;
                }

                if (attribute.getData() instanceof Integer) {
                    int data = (int) attribute.getData();
                    ImInt value = new ImInt(data);

                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.inputInt(attribute.getName(), value);
                    ImGui.popItemWidth();

                    attribute.setData(value.get());

                    break loop;
                }

                if (attribute.getData() instanceof Float) {
                    float data = (float) attribute.getData();
                    ImFloat value = new ImFloat(data);

                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.inputFloat(attribute.getName(), value);
                    ImGui.popItemWidth();

                    attribute.setData(value.get());

                    break loop;
                }

                if (attribute.getData() instanceof String) {
                    String data = (String) attribute.getData();
                    ImString value = new ImString(data);

                    int flags = ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll ;

                    ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                    ImGui.inputText(attribute.getName(), value, flags);
                    ImGui.popItemWidth();

                    attribute.setData(value.get());

                    break loop;
                }
                //End If no type was found, render default string.
                ImGui.inputText(attribute.getName(), new ImString(attribute.getData() + ""));
            }
            ImGui.endChild();
            ImGui.columns();
        }
        ImGui.columns();
    }
}