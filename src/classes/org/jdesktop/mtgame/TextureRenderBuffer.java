/*
 * Copyright (c) 2009, Sun Microsystems, Inc. All rights reserved.
 *
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions
 *    are met:
 *
 *  . Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  . Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  . Neither the name of Sun Microsystems, Inc., nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.mtgame;

import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.renderer.TextureRenderer;

import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.util.TextureManager;
import com.jme.scene.state.jogl.JOGLTextureState;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.renderer.RenderContext;
import com.jme.scene.state.jogl.records.TextureRecord;
import com.jme.scene.state.jogl.records.TextureStateRecord;
import com.jme.system.DisplaySystem;
import javolution.util.FastList;
import com.jme.renderer.Renderer;

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
     * A buffer used to read back the contents of the texture buffer
     */
    ByteBuffer textureReadBuffer = null;

    /**
     * The texture data for offscreen rendering
     */
    private Texture texture = null;
    private TextureRenderer renderer = null;

    /**
     * The list of textures to render into
     */
    private ArrayList<Texture> textureList = new ArrayList();

    /**
     * A boolean indicating that this RenderBuffer has been initialized
     */
    private boolean initialized = false;

    /**
     * The ArrayList used for rendering into the texture
     */
    private ArrayList renderList = new ArrayList();

    /**
     * The constructor
     */
    public TextureRenderBuffer(Target target, int width, int height, int order) {
        super(target, width, height, order);
        setTexture(new Texture2D());
    }

    /**
     * Set the target texture
     */
    void setTexture(Texture t) {
        texture = t;
        if (textureList.size() == 0) {
            textureList.add(t);
        } else {
            textureList.set(0, t);
        }
    }

    /**
     * Get the texture used for offscreen rendering
     */
    public Texture getTexture() {
        return (texture);
    }


    /**
     * This gets called to make this render buffer current for rendering
     */
    public boolean makeCurrent(DisplaySystem display, Renderer jMERenderer) {
        GL gl = GLU.getCurrentGL();

        if (!initialized) {
            createTextureRenderer(display);
            createTextureObjects(gl, display);
            BufferUpdater bu = getBufferUpdater();
            if (bu != null) {
                bu.init(this);
            }
            initialized = true;
        }

        renderer.setCamera(getCameraComponent().getCamera());
        getCameraComponent().getCamera().update();
        renderer.setBackgroundColor(backgroundColor);
        return (true);
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

        renderer = display.createTextureRenderer(getWidth(), getHeight(), tRtarget);
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

        textureReadBuffer = BufferUtils.createByteBuffer(getWidth()*getHeight()*4);
    }
    
    /**
     * Setup some state on the texture
     */
    void setupState(DisplaySystem display, Texture t) {   
        // Setup filtering and wrap
        RenderContext<?> context = display.getCurrentContext();
        TextureStateRecord record = (TextureStateRecord) context
                .getStateRecord(RenderState.StateType.Texture);
        TextureRecord texRecord = record.getTextureRecord(t.getTextureId(), t.getType());

        JOGLTextureState.applyFilter(t, texRecord, 0, record);
        JOGLTextureState.applyWrap(t, texRecord, 0, record);
    }

    /**
     * This gets called to clear the buffer
     */
    public void clear(Renderer renderer) {
        // Nothing to do
    }

    /**
     * These are used to render the given opaque, transparent, and ortho objects
     */
    public void preparePass(Renderer renderer, FastList<Spatial> rl, FastList<PassComponent> passList, int pass) {
        renderList.clear();
        if (getManageRenderScenes()) {
            synchronized (renderComponentList) {
                renderList(renderer, getManagedRenderList());
                renderPassList(renderer, managedPassList);
            }
        } else {
            renderList(renderer, rl);
            renderPassList(renderer, passList);
        }
    }

    public void completePass(Renderer renderer, int pass) {
        this.renderer.render(renderList, textureList, true);
    }

    public void renderOpaque(Renderer renderer) {
    }

    public void renderPass(Renderer renderer) {
    }

    public void renderTransparent(Renderer renderer) {
    }

    public void renderOrtho(Renderer renderer) {
    }

    private void renderPassList(Renderer renderer, FastList<PassComponent> list) {
        // TODO
    }

    private void renderList(Renderer renderer, FastList<Spatial> list) {
        renderList.addAll(list);
    }

    /**
     * This is called when a frame has completed
     */
    public void release() {
        // Nothing to do
    }

    /**
     * This is called when the buffer needs to be swaped
     */
    public void swap() {
        // Nothing to do
    }

    /**
     * Get the actual texture data
     */
    public ByteBuffer getTextureData() {
        GL gl = GLU.getCurrentGL();

        JOGLTextureState.doTextureBind(getTexture().getTextureId(), 0, Texture.Type.TwoDimensional);
        gl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_BGR, GL.GL_UNSIGNED_BYTE, textureReadBuffer);
        //System.out.println("GetErroe: " + gl.glGetError());
        return (textureReadBuffer);
    }
}
