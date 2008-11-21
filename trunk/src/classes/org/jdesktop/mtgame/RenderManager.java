/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.mtgame;

import java.awt.Canvas;

import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.light.LightNode;
import com.jme.scene.CameraNode;
import com.jme.renderer.Camera;
import com.jme.scene.Skybox;
import com.jme.renderer.pass.Pass;

/**
 * The RenderManager creates and controls the renderer threads.  It also acts as
 * the management interface to the rest of the system for anything rendering 
 * related.
 * 
 * @author Doug Twilleager
 */
public class RenderManager {

    /**
     * The array of Renderer's - one per screen.
     * TODO: Only 1 screen supported for now
     */
    private Renderer renderer = null;
    
    /**
     * The entity process controller - used for new frame triggers
     */
    private WorldManager worldManager = null;

    /**
     * The default constructor
     */
    RenderManager(WorldManager wm) {
        worldManager = wm;
        // Wait until we have a canvas to render into before doing anything
        renderer = new Renderer(wm, this, 0);
    }

    /**
     * Create a RenderComponent using the jME graph given
     */
    public RenderComponent createRenderComponent(Node node) {
        RenderComponent rc = new RenderComponent(node);
        return (rc);
    }
      
    /**
     * Create a SkyboxComponent using the jME Skybox given
     */
    public SkyboxComponent createSkyboxComponent(Skybox sb, boolean current) {
        SkyboxComponent sc = new SkyboxComponent(sb, current);
        return (sc);
    }
    
    /**
     * Create a RenderComponent using the jME graph given and the attach point
     */
    public RenderComponent createRenderComponent(Node node, Node attachPoint) {
        RenderComponent rc = new RenderComponent(node, attachPoint);
        return (rc);
    }             
        
    /**
     * Create a PassComponent using the jME Pass given
     */
    public PassComponent createPassComponent(Pass pass) {
        PassComponent pc = new PassComponent(pass);
        return (pc);
    }  
    
    /**
     * Create a CameraComponent using the jME graph given and the camera parameters
     */
    public CameraComponent createCameraComponent(Node cameraSG, CameraNode cameraNode, 
            int viewportWidth, int viewportHeight, float fov,
            float aspect, float near, float far, boolean primary){
        
        CameraComponent cc = new CameraComponent(cameraSG, cameraNode, 
                viewportWidth, viewportHeight, fov, aspect, near, far, primary);
        cc.createJMECamera(this);
        return (cc);
    }  
          
    /**
     * Create a CameraComponent using the jME graph given and the camera parameters
     */
    public CameraComponent createCameraComponent(Node cameraSG, CameraNode cameraNode, 
            int viewportWidth, int viewportHeight, float near, float far, 
            float left, float right, float bottom, float top, boolean primary){
        
        CameraComponent cc = new CameraComponent(cameraSG, cameraNode, 
                viewportWidth, viewportHeight, near, far, left, right, bottom, top,
                primary);
        cc.createJMECamera(this);
        return (cc);
    }  
    
    /**
     * Create a RenderBuffer of the given type, width, and height
     */
    public RenderBuffer createRenderBuffer(RenderBuffer.Target target, int width, int height) {
        RenderBuffer rb = null;
        if (target == RenderBuffer.Target.ONSCREEN) {
            rb = new OnscreenRenderBuffer(target, width, height);
        } else if (target == RenderBuffer.Target.TEXTURE_2D) {
            rb = new TextureRenderBuffer(target, width, height);
        } else if (target == RenderBuffer.Target.TEXTURE_CUBEMAP) {
            rb = new CubeMapRenderBuffer(target, width, height);
        } else if (target == RenderBuffer.Target.SHADOWMAP) {
            rb = new ShadowMapRenderBuffer(target, width, height);
        }
        return (rb);
    }
    
    /**
     * Create a render buffer with the characteristics given.
     */
    public void addRenderBuffer(RenderBuffer rb) {
        renderer.addRenderBuffer(rb);
    }
    
    /**
     * This method blocks until the renderer is ready to go
     */
    void waitUntilReady() {
        renderer.waitUntilReady();
    }

    /**
     * Get an object from the renderer
     */
    public RenderState createRendererState(int type) {
        renderer.waitUntilReady();
        return (renderer.createRendererState(type));
    }
         
    /**
     * Add a global light to the scene
     */
    public void addLight(LightNode light) {
        renderer.addLight(light);
    }
    
    /**
     * Remove a global light from the scene
     */
    public void removeLight(LightNode light) {
        renderer.removeLight(light);
    }
    
    /**
     * Return the number of global Lights
     */
    public int numLights() {
        return (renderer.numLights());
    }
    
    /**
     * Get a light at the index specified
     */
    public LightNode getLight(int i) {
        return (renderer.getLight(i));
    }
    
    /**
     * Create the jmeCamera
     */
    Camera createJMECamera(int width, int height) {  
        renderer.waitUntilReady();
        return (renderer.createJMECamera(width, height));   
    }
 
    /**
     * Change the ortho flag for this render component
     */
    void changeOrthoFlag(RenderComponent rc) {
        // Pass the change onto the renderer
        renderer.changeOrthoFlag(rc);
    }
        
    /**
     * Change the lighting information for this render component
     */
    void changeLighting(RenderComponent rc) {
        // Pass the change onto the renderer
        renderer.changeLighting(rc);
    }
    
    /**
     * Add a component to our processing list
     */
    void addComponent(EntityComponent c) {
        // Pass the component onto the renderer
        renderer.addComponent(c);
    }

    /**
     * Remove a component from our processing list
     */
    void removeComponent(EntityComponent c) {
        // Pass the component onto the renderer
        renderer.removeComponent(c);
    }

    /**
     * Add a processor which has triggerd to the Renderer Processor List
     */
    void addTriggeredProcessor(ProcessorComponent pc) {
        renderer.addTriggeredProcessor(pc);
    }

    /**
     * Set the desired frame rate
     */
    public void setDesiredFrameRate(int fps) {
        // Pass the info onto the renderers
        renderer.setDesiredFrameRate(fps);
    }
 
    /**
     * Set the desired frame rate
     */
    public void setMinSamples(int samples) {
        // Pass the info onto the renderers
        renderer.setMinSamples(samples);
    }
    
    /**
     * Set a listener for frame rate updates
     */
    public void setFrameRateListener(FrameRateListener l, int frequency) {
        renderer.setFrameRateListener(l, frequency);
    }
    
    /**
     * Add a listener to the list of listening for scene changes
     */
    void addNodeChangedListener(NodeChangedListener l) {
        renderer.addNodeChangedListener(l);
    }
            
    /**
     * Add a updater to be called in the render thread
     */
    void addRenderUpdater(RenderUpdater ru, Object obj) {
        renderer.addRenderUpdater(ru, obj);
    }
    
    /**
     * Remove a listener from the list of listening for scene changes
     */
    void removeNodeChangedListener(NodeChangedListener l) {
        renderer.removeNodeChangedListener(l);
    }  

    /**
     * Add a node to the update lists
     */
    void addToUpdateList(Spatial spatial) {
        // What about multiple renderers
        renderer.addToUpdateList(spatial);
    }
    
    /**
     * Add a Pass to the update lists
     */
    void addToPassUpdateList(Pass pass) {
        // What about multiple renderers
        renderer.addToPassUpdateList(pass);
    }
    
    /**
     * Run the processes component commit list
     * For now, we'll just run them on screen 0
     */
    void runCommitList(ProcessorComponent[] runList) {
        // This method will block until the renderer processes 
        // the whole list
        renderer.runCommitList(runList);
    }

    /**
     * Start tracking key input.
     */
    void trackKeyInput(Canvas c, Object listener) {
        renderer.trackKeyInput(c, listener);
    }

    /**
     * Stop tracking key input
     */
    void untrackKeyInput(Canvas c, Object listener) {
        renderer.untrackKeyInput(c, listener);
    }

    /**
     * Set the MouseInput to track.  Null means stop tracking
     */
    void trackMouseInput(Canvas c, Object listener) {
        renderer.trackMouseInput(c, listener);
    }

    /**
     * Stop tracking key input
     */
    void untrackMouseInput(Canvas c, Object listener) {
        renderer.untrackMouseInput(c, listener);
    }

    /**
     * Let the entity manager know that a frame has ticked
     */
    void triggerNewFrame() {
        // Don't try to count a frame tick if the epc isn't ready
        if (worldManager != null) {
            worldManager.triggerNewFrame();
        }
    }
        
    /**
     * The jme collision system needs this for locking
     * @return
     */
    Object getCollisionLock() {
        return (renderer.getCollisionLock());
    }

}
