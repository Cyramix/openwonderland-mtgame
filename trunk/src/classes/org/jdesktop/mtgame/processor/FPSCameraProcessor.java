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

package org.jdesktop.mtgame.processor;

import org.jdesktop.mtgame.*;
import com.jme.math.Vector3f;
import com.jme.math.Matrix3f;
import com.jme.math.Ray;
import com.jme.math.Quaternion;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jme.intersection.TriangleCollisionResults;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.light.LightNode;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;


/**
 * This is simple camera control which mimics the typical first person shooter
 * camera control
 * 
 * @author Doug Twilleager
 */
public class FPSCameraProcessor extends AWTEventProcessorComponent {
    /**
     * The arming conditions for this processor
     */
    private ProcessorArmingCollection collection = null;
    
    /**
     * First, some common variables
     */
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    
    /**
     * The cumulative rotation in Y and X
     */
    private float rotY = 0.0f;
    private float rotX = 0.0f;
    
    /**
     * This scales each change in X and Y
     */
    private float scaleX = 0.7f;
    private float scaleY = 0.7f;
    private float walkInc = 0.5f;
    
    /**
     * States for movement
     */
    private static final int STOPPED = 0;
    private static final int WALKING_FORWARD = 1;
    private static final int WALKING_BACK = 2;
    private static final int STRAFE_LEFT = 3;
    private static final int STRAFE_RIGHT = 4;
    
    /**
     * Our current state
     */
    private int state = STOPPED;
    
    /**
     * Our current position
     */
    private Vector3f position = new Vector3f(0.0f, 100.0f, -30.0f);
    
    /**
     * A sphere for initial avatar collisions
     */
    private Sphere bounds = new Sphere("Avatar", 10, 10, 1.5f);
    private Vector3f boundsPosition = new Vector3f();
    //private BoundingCollisionResults results = new BoundingCollisionResults();
    private TriangleCollisionResults results = new TriangleCollisionResults();
    
    /**
     * The Y Axis
     */
    private Vector3f yDir = new Vector3f(0.0f, 1.0f, 0.0f);
    
    /**
     * Our current forward direction
     */
    private Vector3f fwdDirection = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f rotatedFwdDirection = new Vector3f();
    
    /**
     * Our current side direction
     */
    private Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f rotatedSideDirection = new Vector3f();
    
    /**
     * The quaternion for our rotations
     */
    private Quaternion quaternion = new Quaternion();
    
    /**
     * This is used to keep the direction rotated
     */
    private Matrix3f directionRotation = new Matrix3f();
    
    /**
     * The Node to modify
     */
    private Node target = null;
    
    /**
     * A boolean to signal that we should have a point light on the camera
     */
    private boolean light = false;
    
    /**
     * The light node for the camera
     */
    LightNode lightNode = null;
    
    /**
     * A boolean to signal that we should adjust the height to follow the ground
     */
    private boolean collide = false;
    
    /**
     * The collision system - if we are tracking collision.
     */
    private CollisionSystem collisionSystem = null;
    
    /**
     * A Ray used for collision
     */
    private Ray ray = new Ray();
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The default constructor
     */
    public FPSCameraProcessor(AWTInputComponent listener, Node cameraNode,
            WorldManager wm, Entity myEntity, boolean collide, boolean light) {
        super(listener);
        target = cameraNode;
        worldManager = wm;
        setEntity(myEntity);
        
        collection = new ProcessorArmingCollection(this);
        collection.addCondition(new AwtEventCondition(this));
        collection.addCondition(new NewFrameCondition(this));
        this.collide = collide;
        this.light = light;
        
        if (collide) {
            collisionSystem = wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
            ray = new Ray();
            ray.origin = position;
            ray.direction = new Vector3f(0.0f, -1.0f, 0.0f);
        }
        
        if (light) {
            lightNode = new LightNode();
            PointLight pointLight = new PointLight();
            pointLight.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
            pointLight.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
            pointLight.setEnabled(true);
            lightNode.setLight(pointLight);
            lightNode.setLocalTranslation(position);
            wm.getRenderManager().addLight(lightNode);
        }
    }
    
    public void initialize() {
        setArmingCondition(collection);
    }
    
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = getEvents();
        boolean updateRotations = false;

        for (int i=0; i<events.length; i++) {
            if (events[i] instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_MOVED) {
                    processRotations(me);
                    updateRotations = true;
                }
            } else if (events[i] instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
        
        if (updateRotations) {
            directionRotation.fromAngleAxis(rotY*(float)Math.PI/180.0f, yDir);
            directionRotation.mult(fwdDirection, rotatedFwdDirection);
            directionRotation.mult(sideDirection, rotatedSideDirection);
            //System.out.println("Forward: " + rotatedFwdDirection);
            quaternion.fromAngles(rotX*(float)Math.PI/180.0f, rotY*(float)Math.PI/180.0f, 0.0f);
        }
        
        updatePosition();
    }
    
    private void processRotations(MouseEvent me) {
        int deltaX = 0;
        int deltaY = 0;
        int currentX = 0;
        int currentY = 0;
        currentX = me.getX();
        currentY = me.getY();

        if (lastMouseX == -1) {
            // First time through, just initialize
            lastMouseX = currentX;
            lastMouseY = currentY;
        } else {
            deltaX = currentX - lastMouseX;
            deltaY = currentY - lastMouseY;
            deltaX = -deltaX;

            rotY += (deltaX * scaleX);
            rotX += (deltaY * scaleY);
            if (rotX > 60.0f) {
                rotX = 60.0f;
            } else if (rotX < -60.0f) {
                rotX = -60.0f;
            }
            lastMouseX = currentX;
            lastMouseY = currentY;
        }
    }
    
    
    private void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_W) {
                state = WALKING_FORWARD;
            }
            if (ke.getKeyCode() == KeyEvent.VK_S) {
                state = WALKING_BACK;
            }
            if (ke.getKeyCode() == KeyEvent.VK_A) {
                state = STRAFE_LEFT;
            }
            if (ke.getKeyCode() == KeyEvent.VK_D) {
                state = STRAFE_RIGHT;
            }
        }
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_W ||
                ke.getKeyCode() == KeyEvent.VK_S ||
                ke.getKeyCode() == KeyEvent.VK_A ||
                ke.getKeyCode() == KeyEvent.VK_D) {
                state = STOPPED;
            }
        }
    }
    
    private void updatePosition() {
        switch (state) {
            case WALKING_FORWARD:
                position.x += (walkInc * rotatedFwdDirection.x);
                position.y += (walkInc * rotatedFwdDirection.y);
                position.z += (walkInc * rotatedFwdDirection.z);
                break;
            case WALKING_BACK:
                position.x -= (walkInc * rotatedFwdDirection.x);
                position.y -= (walkInc * rotatedFwdDirection.y);
                position.z -= (walkInc * rotatedFwdDirection.z);
                break;
            case STRAFE_LEFT:
                position.x += (walkInc * rotatedSideDirection.x);
                position.y += (walkInc * rotatedSideDirection.y);
                position.z += (walkInc * rotatedSideDirection.z);
                break;
            case STRAFE_RIGHT:
                position.x -= (walkInc * rotatedSideDirection.x);
                position.y -= (walkInc * rotatedSideDirection.y);
                position.z -= (walkInc * rotatedSideDirection.z);
                break;  
        }
        
        if (collide) {
            PickInfo pi = collisionSystem.pickAllWorldRay(ray, true, false);
            if (pi.size() != 0) {
                // Grab the first one
                PickDetails pd = pi.get(0);
                position.y = pd.getPosition().y + 2.0f;
            }
        }
    }
    /**
     * The commit methods
     */
    public void commit(ProcessorArmingCollection collection) {
        target.setLocalRotation(quaternion);
        target.setLocalTranslation(position);
        if (light) {
            lightNode.setLocalTranslation(position);
        }
        worldManager.addToUpdateList(target);
    }

}
