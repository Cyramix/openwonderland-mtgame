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

import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.renderer.ColorRGBA;
import javolution.util.FastList;
import com.jme.system.DisplaySystem;
import com.jme.renderer.Renderer;
import com.jme.renderer.Camera;
import com.jme.math.Vector3f;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public abstract class RenderBuffer {
    /**
     * The types of rendering supported
     */
    public enum Target {
        TEXTURE_1D,
        TEXTURE_2D,
        TEXTURE_CUBEMAP,
        SHADOWMAP,
        ONSCREEN
    }
    
    /**
     * The target for this render buffer.
     */
    private Target target = Target.ONSCREEN;
    
    /**
     * The width and height of this buffer
     */
    private int height = 0;
    private int width = 0;
    
    /**
     * The Camera Component used to render into this buffer
     */
    private CameraComponent cameraComponent = null;
   
    /**
     * A flag that indicates whether we are rendering all scenes or
     * managing manually.
     */
    private boolean manageRenderScenes = false;
    
    /**
     * A callback for applications that want to be notified after a
     * render.
     */
    private RenderUpdater renderUpdater = null;

    /**
     * A callback for applications that want to be notified when the
     * render buffer is ready.
     */
    private BufferUpdater bufferUpdater = null;

    /**
     * The lists of Spatials to render into this buffer
     * These lists are only accessed if manage render scenes is true.
     */
    protected FastList<RenderComponent> renderComponentList = new FastList();
    private FastList<Spatial> managedRenderList = new FastList();
    protected FastList<PassComponent> managedPassList = new FastList();

    /**
     * The background color
     */
    protected ColorRGBA backgroundColor = new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f);

    /**
     * The order number for this buffer.
     */
    private int order = 0;

    /**
     * The number of render passes for this buffer
     */
    private int numPasses = 1;

    /**
     * The currently active portal
     */
    private Portal portal = null;
    private Camera portalCamera = null;
    protected Vector3f lastLoc = null;
    protected Vector3f lastDir = null;
    protected Vector3f lastUp = null;
    protected Vector3f lastLeft = null;
    
    /**
     * The constructor
     */
    public RenderBuffer(Target target, int width, int height) {
        this(target, width, height, 0);
    }

    /**
     * The constructor
     */
    public RenderBuffer(Target target, int width, int height, int order) {
        this.target = target;
        this.height = height;
        this.width = width;
        this.order = order;
    }

    /**
     * This gets called to clear the buffer
     */
    public abstract void clear(Renderer renderer);

    /**
     * This gets called to make this render buffer current for rendering
     */
    public abstract boolean makeCurrent(DisplaySystem diaplay, Renderer jMERenderer);

    /**
     * These are used to render the given opaque, transparent, and ortho objects
     */
    public abstract void preparePass(Renderer renderer, FastList<Spatial> renderList, FastList<PassComponent> passList, int pass);
    public abstract void completePass(Renderer renderer, int pass);
    public abstract void renderOpaque(Renderer renderer);
    public abstract void renderPass(Renderer renderer);
    public abstract void renderTransparent(Renderer renderer);
    public abstract void renderOrtho(Renderer renderer);

    /**
     * This is called when a frame has completed
     */
    public abstract void release();

    /**
     * This is called when the buffer needs to be swaped
     */
    public abstract void swap();

    /**
     * Set the Buffer Updater
     */
    public void setBufferUpdater(BufferUpdater updater) {
        bufferUpdater = updater;
    }

    /**
     * Get the Buffer Updater
     */
    public BufferUpdater getBufferUpdater() {
        return (bufferUpdater);
    }

    /**
     * Set the Render Updater
     */
    public void setRenderUpdater(RenderUpdater updater) {
        renderUpdater = updater;
    }
    
    /**
     * Set the Render Updater
     */
    public RenderUpdater getRenderUpdater() {
        return (renderUpdater);
    }

    /**
     * Set the Portal
     */
    public void setPortal(Portal p, Vector3f lLoc, Vector3f lDir, Vector3f lUp, Vector3f lLeft) {
        portal = p;
        lastLoc = lLoc;
        lastDir = lDir;
        lastUp = lUp;
        lastLeft = lLeft;
    }

    /**
     * Get the Portal
     */
    public Portal getPortal() {
        return (portal);
    }

    /**
     * Get the Portal Camera
     */
    public Camera getPortalCamera() {
        return (portalCamera);
    }

    /**
     * Sets the manage render scenes flag.  If true, the application
     * will set which spatials need to be rendered.  If it is false
     * the system will render all known spatials automatically.  The
     * default is false.
     */
    public void setManageRenderScenes(boolean manage) {
        synchronized (renderComponentList) {
            if (manageRenderScenes != manage) {
                manageRenderScenes = manage;
            }
        }
    }

    /**
     * Gets the manage render scenes flag
     * @param manage
     */
    public boolean getManageRenderScenes() {
        return (manageRenderScenes);
    }

    /**
     * This adds a PassComponent to the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void addPassComponent(PassComponent rc) {
        synchronized (renderComponentList) {
            managedPassList.add(rc);
        }
    }

    /**
     * This removes a PassComponent from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removePassComponent(PassComponent rc) {
        synchronized (renderComponentList) {
            managedPassList.remove(rc);
        }
    }

    /**
     * This removes a PassComponent from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removePassComponent(int index) {
        synchronized (renderComponentList) {
            managedPassList.remove(index);
        }
    }

    /**
     * This returns the current number of passes
     */
    public int numPassComponents() {
        int size = 0;
        synchronized (renderComponentList) {
            size = managedPassList.size();
        }
        return (size);
    }
    
    /**
     * This adds a RenderComponent to the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void addRenderScene(RenderComponent rc) {
        synchronized (renderComponentList) {
            renderComponentList.add(rc);
        }
    }

    /**
     * Get the list of managed render component spatials
     */
    protected FastList<Spatial> getManagedRenderList() {
        managedRenderList.clear();

        for (int i=0; i<renderComponentList.size(); i++) {
            managedRenderList.add(renderComponentList.get(i).getSceneRoot());
        }
        return (managedRenderList);
    }
      
    /**
     * This removes a RenderComponent from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removeRenderScene(RenderComponent rc) {
        synchronized (renderComponentList) {
            renderComponentList.remove(rc);
        }
    }
           
    /**
     * This removes a RenderComponent from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removeRenderScene(int index) {
        synchronized (renderComponentList) {
            renderComponentList.remove(index);
        }
    }    
        
    /**
     * This returns the current number of scenes
     */
    public int numRenderScenes() {
        int size = 0;
        synchronized (renderComponentList) {
            size = renderComponentList.size();
        }
        return (size);
    }

    /**
     * Set the number of render passes
     */
    public void setNumRenderPasses(int passes) {
        numPasses = passes;
    }

    /**
     * Get the number of render passes
     */
    public int numRenderPasses() {
        return (numPasses);
    }

    /**
     * Dynamically set the width of this render target
     */
    public void setWidth(int width) {
        this.width = width;
        // TODO: actually change the surface
    }
    
    /**
     * Get the width
     */
    public int getWidth() {
        return (width);
    }
      
    /**
     * Get the height
     */
    public int getHeight() {
        return (height);
    }

    /**
     * Get the order number for this buffer
     * @return
     */
    public int getOrder() {
        return (order);
    }
    
    /**
     * Dynamically set the height of this render target
     */
    public void setHeight(int height) {
        this.height = height;
        // TODO: actually change the surface
    }
    
    /**
     * Get the background color
     */
    public ColorRGBA getBackgroundColor(ColorRGBA color) {
        synchronized (backgroundColor) {
            color.set(backgroundColor);
        }
        return (color);
    }
         
    /**
     * Get the background color
     */
    public void setBackgroundColor(ColorRGBA color) {
        synchronized (backgroundColor) {
            backgroundColor.set(color);
        }
    }
    
    /**
     * Get the target
     */
    public Target getTarget() {
        return (target);
    }
    
    /**
     * Set the CameraComponent
     */
    public void setCameraComponent(CameraComponent cc) {
        cameraComponent = cc;
        // TODO: propogate dynamic change
    }
    
    /**
     * Get the current CameraComponent
     */
    public CameraComponent getCameraComponent() {
        return (cameraComponent);
    }
}
