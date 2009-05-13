/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame.util;

import org.jdesktop.mtgame.*;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.RenderState;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import com.jme.util.geom.BufferUtils;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class Mirror {
    /**
     * The width and height of this buffer
     */
    private float height = 0.0f;
    private float width = 0.0f;
    private int textureWidth = 0;
    private int textureHeight = 0;
    private float aspect = 0.0f;

    /**
     * Vectors for the camera
     */
    private Vector3f pos = new Vector3f();
    private Vector3f dir = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f side = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private Quaternion quat = new Quaternion();
    
    /**
     * The Camera Component used to render into this buffer
     */
    private CameraComponent cameraComponent = null;
    
    /**
     * The background color
     */
    private ColorRGBA backgroundColor = new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f);

    /**
     * The RenderBuffer
     */
    RenderBuffer rb = null;
    
    /**
     * The constructor
     */
    public Mirror(WorldManager wm, float mirrorWidth, float mirrorHeight,
            Vector3f position, Vector3f direction, Vector3f up, Vector3f side,
            int texWidth, int texHeight) {
        height = mirrorHeight;
        width = mirrorWidth;
        textureWidth = texWidth;
        textureHeight = texHeight;
        pos.set(position);
        dir.set(direction);
        this.side.set(side);
        this.up.set(up);
        aspect = width/height;

        Entity e = new Entity("Mirror ");
        createRenderBuffer(wm, e);
        createMirrorNode(wm, e);
        wm.addEntity(e);
    }

    void createRenderBuffer(WorldManager wm, Entity e) {
        rb = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, textureWidth, textureHeight);
        CameraNode cn = new CameraNode("MyCamera", null);
        Node cameraSG = new Node();
        cameraSG.attachChild(cn);
        cameraSG.setLocalTranslation(pos);
        quat.fromAxes(side, up, dir);
        cameraSG.setLocalRotation(quat);

        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn,
                textureWidth, textureHeight, 45.0f, aspect, 1.0f, 1000.0f, true);
        rb.setCameraComponent(cc);
        wm.getRenderManager().addRenderBuffer(rb);
        e.addComponent(CameraComponent.class, cc);
    }

    void createMirrorNode(WorldManager wm, Entity e) {
        Node mirror = new Node();
        Quad quad = new Quad("Mirror Quad", width, height);

        FloatBuffer fb = BufferUtils.createVector2Buffer(quad.getVertexCount());
        fb.rewind();
        fb.put(1.0f).put(1.0f);
        fb.put(1.0f).put(0.0f);
        fb.put(0.0f).put(0.0f);
        fb.put(0.0f).put(1.0f);
        TexCoords tc = new TexCoords(fb);

        quad.setTextureCoords(tc);

        mirror.attachChild(quad);
        //mirror.attachChild(new AxisRods("Axis", true, 10.0f, 0.2f));
        mirror.setLocalTranslation(pos);
        quat.fromAxes(side, up, dir);
        mirror.setLocalRotation(quat);

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        quad.setRenderState(buf);

        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setEnabled(true);
        ts.setTexture(((TextureRenderBuffer)rb).getTexture(), 0);
        quad.setRenderState(ts);

        RenderComponent mirrorRC = wm.getRenderManager().createRenderComponent(mirror);
        mirrorRC.setOrtho(false);
        mirrorRC.setLightingEnabled(false);
        e.addComponent(RenderComponent.class, mirrorRC);
    }
}
