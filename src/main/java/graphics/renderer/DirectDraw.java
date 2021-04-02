package graphics.renderer;

import editor.Editor;
import engine.Reactor;
import entity.Entity;
import entity.EntityManager;
import graphics.ui.UIManager;
import math.VectorUtils;
import models.AABB;
import models.Joint;
import org.joml.*;
import org.lwjgl.opengl.GL46;
import platform.EnumDevelopment;
import platform.PlatformManager;

import java.lang.Math;
import java.util.HashMap;
import java.util.LinkedList;

public class DirectDraw {
    private static DirectDraw directDraw;

    //ImmediateDraw
    private ImmediateDrawLine     drawerLine;
    private ImmediateDrawTriangle drawTriangle;
    private ImmediateDrawSprite   drawSprite;

    public DirectDraw3D Draw3D;
    public UIManager Draw2D;

    private DirectDraw(){
        //Setup our draw instances.

        drawerLine = new ImmediateDrawLine();
        drawTriangle = new ImmediateDrawTriangle();
        drawSprite = new ImmediateDrawSprite();

        Draw2D = UIManager.getInstance();
        Draw3D = DirectDraw3D.getInstance();
    }


    //Draw
    protected void render(){
        drawerLine.render();
        if(Reactor.isDev()) {
            GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT);
        }
        drawTriangle.render();
        drawSprite.render();
    }

    //Singleton stuff.
    public static void initialize(){
        if(directDraw == null){
            directDraw = new DirectDraw();
        }
    }

    public static DirectDraw getInstance(){
        return directDraw;
    }
}
