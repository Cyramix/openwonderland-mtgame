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

import java.awt.Canvas;
import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.image.TextureCubeMap;

import java.nio.IntBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.util.TextureManager;
import com.jme.scene.state.jogl.JOGLTextureState;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.renderer.RenderContext;
import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.scene.state.jogl.records.TextureRecord;
import com.jme.scene.state.jogl.records.TextureStateRecord;
import com.jme.system.DisplaySystem;
import com.jme.math.Vector3f;
import javolution.util.FastList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class CubeMapRenderBuffer extends RenderBuffer {
    /**
     * The cube map texture
     */
    private TextureCubeMap texture = null;

    /**
     * A camera for the cubemap render
     */
    private Camera cubeMapCamera = null;
    private Vector3f negativeX = new Vector3f(-1.0f, 0.0f, 0.0f);
    private Vector3f positiveX = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f negativeY = new Vector3f(0.0f, -1.0f, 0.0f);
    private Vector3f positiveY = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f negativeZ = new Vector3f(0.0f, 0.0f, -1.0f);
    private Vector3f positiveZ = new Vector3f(0.0f, 0.0f, 1.0f);

    /**
     * A boolean indicating that this RenderBuffer has been initialized
     */
    private boolean initialized = false;

    /**
     * The camera to be restored after rendering
     */
    private Camera saveCamera = null;

    /**
     * The constructor
     */
    public CubeMapRenderBuffer(Target target, int width, int height, int order) {
        super(target, width, height, order);
        setNumRenderPasses(6);
        texture = new TextureCubeMap();
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
            createTextureObjects(gl, display);
            BufferUpdater bu = getBufferUpdater();
            if (bu != null) {
                bu.init(this);
            }
            initialized = true;
        }
        return (true);
    }
    
    /**
     * Create the jME texture objects, and prep them for rendering
     */
    private void createTextureObjects(GL gl, DisplaySystem display) {
        // First do the common render target
        assignTextureId(gl, texture, Texture.Type.CubeMap);
        allocateTextureData(gl, texture, Texture.Type.CubeMap);
        setupState(display, texture);
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
        int width = getWidth();
        int height = getHeight();

        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, components, width, height, 0,
                format, dataType, null);
        if (t.getMinificationFilter().usesMipMapLevels()) {
            gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
            gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
            gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
            gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
            gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
            gl.glGenerateMipmap(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        }

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
        int width = getWidth();
        int height = getHeight();

        CameraComponent cc = getCameraComponent();
        Camera ccCamera = cc.getCamera();
        ccCamera.update();

        if (cubeMapCamera == null) {
            cubeMapCamera = renderer.createCamera(width, height);
            cubeMapCamera.setFrustumPerspective(cc.getFieldOfView(), cc.getAspectRatio(),
                    cc.getNearClipDistance(), cc.getFarClipDistance());
        }
        saveCamera = renderer.getCamera();
        renderer.setCamera(cubeMapCamera);
        renderer.setBackgroundColor(backgroundColor);
        cubeMapCamera.setLocation(ccCamera.getLocation());
        JOGLTextureState.doTextureBind(texture.getTextureId(), 0, Texture.Type.CubeMap);
    }

    /**
     * These are used to render the given opaque, transparent, and ortho objects
     */
    public void preparePass(Renderer renderer, FastList<Spatial> renderList, FastList<PassComponent> passList, int pass) {
        switch (pass) {
            case 0:
                // Render Negative X
                cubeMapCamera.setDirection(negativeX);
                cubeMapCamera.setUp(new Vector3f(0.0f, 1.0f, 0.0f));
                cubeMapCamera.setLeft(new Vector3f(0.0f, 0.0f, 1.0f));
                break;
            case 1:
                // Render Positive X
                cubeMapCamera.setDirection(positiveX);
                cubeMapCamera.setLeft(new Vector3f(0.0f, 0.0f, -1.0f));
                break;
            case 2:
                // Render Negative Y
                cubeMapCamera.setDirection(negativeY);
                cubeMapCamera.setUp(new Vector3f(0.0f, 0.0f, -1.0f));
                cubeMapCamera.setLeft(new Vector3f(1.0f, 0.0f, 0.0f));
                break;
            case 3:
                // Render Positive Y
                cubeMapCamera.setDirection(positiveY);
                cubeMapCamera.setUp(new Vector3f(0.0f, 0.0f, 1.0f));
                break;
            case 4:
                // Render Negative Z
                cubeMapCamera.setDirection(negativeZ);
                cubeMapCamera.setUp(new Vector3f(0.0f, 1.0f, 0.0f));
                cubeMapCamera.setLeft(new Vector3f(-1.0f, 0.0f, 0.0f));
                break;
            case 5:
                // Render Positive Z
                cubeMapCamera.setDirection(positiveZ);
                cubeMapCamera.setLeft(new Vector3f(1.0f, 0.0f, 0.0f));
                break;
        }
        cubeMapCamera.update();
        renderer.setCamera(cubeMapCamera);
        renderer.clearBuffers();

        renderer.clearQueue();
        if (getManageRenderScenes()) {
            synchronized (renderComponentList) {
                renderList(renderer, getManagedRenderList());
                renderPassList(renderer, managedPassList);
            }
        } else {
            renderList(renderer, renderList);
            renderPassList(renderer, passList);
        }
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
        for (int i=0; i<list.size(); i++) {
            PassComponent pc = (PassComponent) list.get(i);
            pc.getPass().renderPass(renderer);
        }
    }

    private void renderList(Renderer renderer, FastList<Spatial> list) {
        for (int i=0; i<list.size(); i++) {
            renderer.draw(list.get(i));
        }
    }

    public void completePass(Renderer renderer, int pass) {
        int width = getWidth();
        int height = getHeight();
        GL gl = GLU.getCurrentGL();

        renderer.renderQueue();
        
        switch (pass) {
            case 0:
                // Copy Negative X
                gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);
                break;
            case 1:
                // Copy Positive X
                gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);
                break;
            case 2:
                // Copy Negative Y
                gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL.GL_RGBA, 0, 0, width, height, 0);
                break;
            case 3:
                // Copy Positive Y
                gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL.GL_RGBA, 0, 0, width, height, 0);
                break;
            case 4:
                // Copy Negative Z
                gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL.GL_RGBA, 0, 0, width, height, 0);
                break;
            case 5:
                // Copy Positive Z
                gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL.GL_RGBA, 0, 0, width, height, 0);
                renderer.setCamera(saveCamera);
                break;
        }
    }

    /**
     * This is called when a frame has completed
     */
    public void release() {

    }

    /**
     * This is called when the buffer needs to be swaped
     */
    public void swap() {

    }
}
