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


import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.renderer.TextureRenderer;

import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;

import com.jme.image.Texture.DepthTextureCompareFunc;
import com.jme.image.Texture.DepthTextureCompareMode;
import com.jme.image.Texture.DepthTextureMode;
import com.jme.renderer.Camera;
import com.jme.renderer.AbstractCamera;
import com.jme.renderer.Renderer;
import javolution.util.FastList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import java.util.ArrayList;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class ShadowMapRenderBuffer extends RenderBuffer {
    /**
     * The texture data for offscreen rendering
     */
    private Texture2D shadowMapTexture = null;
    private TextureRenderer renderer = null;

    /**
     * The list of textures to render into
     */
    private ArrayList<Texture> textureList = new ArrayList();

    /**
     * The ArrayList used for rendering into the texture
     */
    private ArrayList renderList = new ArrayList();

    /**
     * A lock for camera updates
     */
    private Object cameraLock = new Object();

    /**
     * The direction for the camera
     */
    private Vector3f cameraDirection = new Vector3f();
    
    /**
     * The position for the camera
     */
    private Vector3f cameraPosition = new Vector3f();
    
    /**
     * The Look At for the camera
     */
    private Vector3f cameraLookAt = new Vector3f();
    
    /**
     * The up axis for the camera
     */
    private Vector3f cameraUp = new Vector3f();
    
    /**
     * The camera used for rendering
     */
    private Camera camera = null;
    
    /**
     * A flag that indicates something in the camera changed
     */
    private boolean cameraChanged = true;
    
    /**
     * A flag indicating parallel projection
     */
    private boolean cameraIsParallel = true;
    
    private static Matrix4f biasMatrix = new Matrix4f(0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.5f, 0.5f, 0.5f,
            1.0f);

    /**
     * A boolean indicating that this RenderBuffer has been initialized
     */
    private boolean initialized = false;


    /**
     * The constructor
     */
    public ShadowMapRenderBuffer(Target target, int width, int height, int order) {
        super(target, width, height, order);
        shadowMapTexture = new Texture2D();
        shadowMapTexture.setApply(Texture.ApplyMode.Modulate);
        shadowMapTexture.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);
        shadowMapTexture.setWrap(Texture.WrapMode.Clamp);
        shadowMapTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        
        shadowMapTexture.setRenderToTextureType(Texture.RenderToTextureType.Depth);
        shadowMapTexture.setMatrix(new Matrix4f());
        shadowMapTexture.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.EyeLinear);
        shadowMapTexture.setDepthCompareMode(DepthTextureCompareMode.RtoTexture);
        shadowMapTexture.setDepthCompareFunc(DepthTextureCompareFunc.GreaterThanEqual);
        shadowMapTexture.setDepthMode(DepthTextureMode.Intensity);

        textureList.add(shadowMapTexture);
    }

    /**
     * Get the shadow map texture
     */
    public Texture getTexture() {
        return (shadowMapTexture);
    }

    /**
     * Set the camera direction
     */
    public void setCameraDirection(Vector3f dir) {
        synchronized (cameraLock) {
            cameraDirection.x = dir.x;
            cameraDirection.y = dir.y;
            cameraDirection.z = dir.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraDirection(Vector3f dir) {
        synchronized (cameraLock) {
            dir.x = cameraDirection.x;
            dir.y = cameraDirection.y;
            dir.z = cameraDirection.z;
        }
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraLookAt(Vector3f pos) {
        synchronized (cameraLock) {
            cameraLookAt.x = pos.x;
            cameraLookAt.y = pos.y;
            cameraLookAt.z = pos.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraLookAt(Vector3f pos) {
        synchronized (cameraLock) {
            pos.x = cameraLookAt.x;
            pos.y = cameraLookAt.y;
            pos.z = cameraLookAt.z;
        }
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraUp(Vector3f up) {
        synchronized (cameraLock) {
            cameraUp.x = up.x;
            cameraUp.y = up.y;
            cameraUp.z = up.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraUp(Vector3f up) {
        synchronized (cameraLock) {
            up.x = cameraUp.x;
            up.y = cameraUp.y;
            up.z = cameraUp.z;
        }
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraPosition(Vector3f pos) {
        synchronized (cameraLock) {
            cameraPosition.x = pos.x;
            cameraPosition.y = pos.y;
            cameraPosition.z = pos.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraPosition(Vector3f pos) {
        synchronized (cameraLock) {
            pos.x = cameraPosition.x;
            pos.y = cameraPosition.y;
            pos.z = cameraPosition.z;
        }
    }

    /**
     * This gets called to make this render buffer current for rendering
     */
    public boolean makeCurrent(DisplaySystem display, Renderer jMERenderer) {
        if (!initialized) {
            createTextureRenderer(display);
            renderer.setupTexture(shadowMapTexture);
            BufferUpdater bu = getBufferUpdater();
            if (bu != null) {
                bu.init(this);
            }
            initialized = true;
        }

        synchronized (cameraLock) {
            if (cameraChanged) {
                camera = renderer.getCamera();
                updateCamera();
                cameraChanged = false;
            } else {
                camera.update();
            }
        }

        renderer.setBackgroundColor(backgroundColor);
        return (true);
    }
        
    private void createTextureRenderer(DisplaySystem display) {
        TextureRenderer.Target tRtarget = TextureRenderer.Target.Texture2D;
        renderer = display.createTextureRenderer(getWidth(), getHeight(), tRtarget);
    }
    
    /**
     * Set the camera attributes
     */
    void updateCamera() {
        camera.setLocation(cameraPosition);
        //camera.setDirection(cameraDirection);
        //camera.setLeft(cameraLeft);
        //camera.setUp(cameraUp);
        
        camera.lookAt(cameraLookAt, cameraUp);
        
        //System.out.println("Position: " + cameraPosition);
        //System.out.println("Up: " + cameraUp);
        if (cameraIsParallel) {
            camera.setParallelProjection(true);
            camera.setFrustum(1.0f, 3000.0f, -75, 75, -75, 75);
        } else {
            camera.setParallelProjection(false);
            camera.setFrustumPerspective(60.0f, getWidth()/getHeight(), 1.0f, 1000.0f);
        }
        
        
        Matrix4f proj = new Matrix4f();
        Matrix4f view = new Matrix4f();
        proj.set(((AbstractCamera)camera).getProjectionMatrix());
        //System.out.println("PROJ MATRIX: " + proj);
        view.set(((AbstractCamera)camera).getModelViewMatrix());
        //System.out.println("VIEW MATRIX: " + view);
        view.multLocal(proj).multLocal(biasMatrix).transposeLocal();
        //System.out.println("MATRIX: " + view);
        shadowMapTexture.getMatrix().set(view);
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
                setRenderList(renderer, managedRenderList);
                setPassList(renderer, managedPassList);
            }
        } else {
            setRenderList(renderer, rl);
            setPassList(renderer, passList);
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

    private void setPassList(Renderer renderer, FastList<PassComponent> list) {
        // TODO
    }

    private void setRenderList(Renderer renderer, FastList<Spatial> list) {
        for (int i=0; i<list.size(); i++) {
            renderList.add(list.get(i));
        }
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
}
