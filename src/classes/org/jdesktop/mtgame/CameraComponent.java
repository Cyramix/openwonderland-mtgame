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

import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;

/**
 * This Entity Component encapsulates camera control data and calculations.
 * 
 * @author Doug Twilleager
 */
public class CameraComponent extends EntityComponent {
    /**
     * A flag that indicates that this is the primary camera
     */
    private boolean primary = false;
    
    /**
     * The viewport width and height
     */
    private int width = -1;
    private int height = -1;
    
    /**
     * The field of view
     */
    private float fieldOfView = 45.0f;
    
    /**
     * The aspect ratio
     */
    private float aspectRatio = 1.0f;
   
    /**
     * The near and far clip planes
     */
    private float nearClip = 1.0f;
    private float farClip = 1000.0f;
    
    /**
     * Parallel projection data
     */
    private float left = 0.0f;
    private float right = 0.0f;
    private float bottom = 0.0f;
    private float top = 0.0f;
    
    /**
     * The jME Camera object
     * Note: This is created by the renderer
     */
    private Camera camera = null;
    
    /**
     * The scene graph which contains the CameraNode
     */
    private Node cameraSceneGraph = null;
    
    /**
     * A reference to the CameraNode
     */
    private CameraNode cameraNode = null;
    
    /**
     * A boolen indicating whether or not we are using parallel projection
     */
    private boolean isParallel = false;
    
    /**
     * The constructor
     */
    CameraComponent(Node cSG, CameraNode cNode, int viewportWidth, 
                    int viewportHeight, float fov, float aspect, float near, 
                    float far, boolean primary) {
        cameraSceneGraph = cSG;
        cameraNode = cNode;
        width = viewportWidth;
        height = viewportHeight;
        fieldOfView = fov;
        aspectRatio = aspect;
        nearClip = near;
        farClip = far;
        this.primary = primary;
        this.isParallel = false;
    }
         
    /**
     * The constructor
     */
    CameraComponent(Node cSG, CameraNode cNode, int viewportWidth, 
                    int viewportHeight, float near, float far, float left, 
                    float right, float bottom, float top, boolean primary) {
        cameraSceneGraph = cSG;
        cameraNode = cNode;
        width = viewportWidth;
        height = viewportHeight;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        nearClip = near;
        farClip = far;
        this.primary = primary;
        this.isParallel = true;
    }
    
    /**
     * Create the jmeCamera
     */
    void createJMECamera(RenderManager rm) {                      
        camera = rm.createJMECamera(width, height);
        if (!isParallel) {
            camera.setFrustumPerspective(fieldOfView, aspectRatio, nearClip, farClip);
        } else {
            camera.setFrustum(nearClip, farClip, left, right, top, bottom);       
        }
        camera.setParallelProjection(isParallel);
        cameraNode.setCamera(camera);    
    }
    
    
    /**
     * Set the viewport width and height
     */
    public void setViewport(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Get the vieport width
     */
    public int getViewportWidth() {
        return (width);
    }
    
    /**
     * Get the Viewport height
     */
    public int getViewportHeight() {
        return (height);
    }
    
    /**
     * Set the Field of View
     */
    public void setFieldOfView(float fov) {
        fieldOfView = fov;
    }
    
    /**
     * Get the field of view
     */
    public float getFieldOfView() {
        return (fieldOfView);
    }
    
    /**
     * Set the aspect ratio
     */
    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
    }
    
    /**
     * Get the aspect ratio
     */
    public float getAspectRatio() {
        return (aspectRatio);
    }
    
    /**
     * Set the near and far clip distances
     */
    public void setClipDistances(float near, float far) {
        nearClip = near;
        farClip = far;
    }
    
    /**
     * Get the near clip distance
     */
    public float getNearClipDistance() {
        return (nearClip);
    }
    
    /**
     * Get the far clip distance
     */
    public float getFarClipDistance() {
        return (farClip);
    }
    
    /**
     * Set the camera scene graph
     */
    public void setCameraSceneGraph(Node sg) {
        cameraSceneGraph = sg;
    }
    
    /**
     * Get the camera scene graph
     */
    public Node getCameraSceneGraph() {
        return (cameraSceneGraph);
    }
    
    /**
     * Set the CameraNode reference
     */
    public void setCameraNode(CameraNode cn) {
        cameraNode = cn;
    }
    
    /**
     * Get the CameraNode
     */
    public CameraNode getCameraNode() {
        return (cameraNode);
    }
    
    /**
     * Set the primary camera flag
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
    
    /**
     * Get the primary flag
     */
    public boolean isPrimary() {
        return (primary);
    }
    
    /**
     * Set the jME Camera
     * Note: This is used by the renderer
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    
    /**
     * Get the jME Camera
     */
    public Camera getCamera() {
        return (camera);
    }
}
