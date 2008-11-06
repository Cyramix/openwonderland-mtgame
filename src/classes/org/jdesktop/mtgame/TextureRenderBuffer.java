/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.renderer.TextureRenderer;

import java.nio.IntBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.util.TextureManager;
import com.jme.scene.state.jogl.JOGLTextureState;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.renderer.RenderContext;
import com.jme.scene.state.jogl.records.TextureRecord;
import com.jme.scene.state.jogl.records.TextureStateRecord;
import com.jme.system.DisplaySystem;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class TextureRenderBuffer extends RenderBuffer {    
    /**
     * The constructor
     */
    public TextureRenderBuffer(Target target, int width, int height) {
        super(target, width, height);
        setTexture(new Texture2D());
    }

    /**
     * Initialize this RenderBuffer.  This is called from the renderer
     * before the buffer is rendered into.
     */
    void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents) {
        GL gl = GLU.getCurrentGL();

        synchronized (getRBLock()) {
            if (!isInitialized()) {
                createTextureRenderer(display);
                createTextureObjects(gl, display);
                updateRenderList(skybox, renderComponents);
                setInitialized(true);
            }
        }
    }
    
    /**
     * Create the jME texture objects, and prep them for rendering
     */
    private void createTextureObjects(GL gl, DisplaySystem display) {
        // First do the common render target
        assignTextureId(gl, getTexture(), Texture.Type.TwoDimensional);
        allocateTextureData(gl, getTexture(), Texture.Type.TwoDimensional);
        setupState(display, getTexture());
    }
        
    private void createTextureRenderer(DisplaySystem display) {
        TextureRenderer.Target tRtarget = TextureRenderer.Target.Texture2D;

        setTextureRenderer(display.createTextureRenderer(getWidth(), getHeight(), tRtarget));
        getTextureRenderer().setBackgroundColor(getBackgroundColor());
    }
    
    /**
     * Manage the texture id.
     */
    void assignTextureId(GL gl, Texture t, Texture.Type type) {
        IntBuffer ibuf = BufferUtils.createIntBuffer(1);

        if (t.getTextureId() != 0) {
            ibuf.put(t.getTextureId());
            gl.glDeleteTextures(ibuf.limit(), ibuf); // TODO Check <size>
            ibuf.clear();
        }

        // Create the texture
        gl.glGenTextures(ibuf.limit(), ibuf); // TODO Check <size>
        t.setTextureId(ibuf.get(0));
        TextureManager.registerForCleanup(t.getTextureKey(), t.getTextureId());

        JOGLTextureState.doTextureBind(t.getTextureId(), 0, type);
    }
    
        
    /**
     * Allocate the texture data, based upon what we are doing.
     */
    void allocateTextureData(GL gl, Texture t, Texture.Type type) {
        int components = GL.GL_RGBA8;
	int format = GL.GL_RGBA;
	int dataType = GL.GL_UNSIGNED_BYTE;
        Texture.RenderToTextureType rttType = Texture.RenderToTextureType.RGBA;

        t.setRenderToTextureType(rttType);

        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, components, getWidth(), getHeight(), 0,
                format, dataType, null);
        if (t.getMinificationFilter().usesMipMapLevels()) {
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);
        }
    }
    
    /**
     * Setup some state on the texture
     */
    void setupState(DisplaySystem display, Texture t) {   
        // Setup filtering and wrap
        RenderContext<?> context = display.getCurrentContext();
        TextureStateRecord record = (TextureStateRecord) context
                .getStateRecord(RenderState.RS_TEXTURE);
        TextureRecord texRecord = record.getTextureRecord(t.getTextureId(), t.getType());

        JOGLTextureState.applyFilter(t, texRecord, 0, record);
        JOGLTextureState.applyWrap(t, texRecord, 0, record);
    }
    
    /**
     * Render the current RenderList into this buffer
     */
    void render(Renderer r) {
        GL gl = GLU.getCurrentGL();
        com.jme.renderer.Renderer jmeRenderer = r.getJMERenderer();
        
        getTextureRenderer().setCamera(getCameraComponent().getCamera());
        getCameraComponent().getCamera().update();
        getTextureRenderer().render(getRenderList(), getTextureList(), true);
    }
}
