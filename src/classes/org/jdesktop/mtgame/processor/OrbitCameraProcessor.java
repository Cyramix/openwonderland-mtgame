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

package org.jdesktop.mtgame.processor;

import org.jdesktop.mtgame.*;
import com.jme.math.Vector3f;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.scene.Node;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

/**
 * A camera which orbits an object
 * 
 * @author Doug Twilleager
 */
public class OrbitCameraProcessor extends AWTEventProcessorComponent {
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
    private float rotY = 180.0f;
    private float rotX = 0.0f;
    
    /**
     * The current translation in X and Z
     */
    private float transX = 0.0f;
    private float transZ = 0.0f;
    
    /**
     * This scales each change in X and Y
     */
    private float scaleX = 0.5f;
    private float scaleY = 0.5f;
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
    private Vector3f position = new Vector3f(0.0f, 5.0f, -100.0f);
    private Vector3f rotatedPosition = new Vector3f();
    
    /**
     * The Y Axis
     */
    private Vector3f upDir = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f rotatedUpDirection = new Vector3f();
    
    /**
     * Our current forward direction
     */
    private Vector3f lookDirection = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f rotatedLookDirection = new Vector3f();
    
    /**
     * Our current side direction
     */
    private Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f rotatedSideDirection = new Vector3f();
    
    /**
     * The quaternion for our rotations
     */
    private Quaternion quaternion = new Quaternion();
    private Vector3f translation = new Vector3f();
    
    /**
     * This is used to keep the direction rotated
     */
    private Matrix3f directionRotation = new Matrix3f();
    
    /**
     * The Node to modify
     */
    private Node target = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The default constructor
     */
    public OrbitCameraProcessor(AWTInputComponent listener, Node cameraNode,
            WorldManager wm, Entity myEntity) {
        super(listener);
        target = cameraNode;
        worldManager = wm;
        
        collection = new ProcessorArmingCollection(this);        
        collection.addCondition(new AwtEventCondition(this));
        collection.addCondition(new NewFrameCondition(this));
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
                if (me.getID() == MouseEvent.MOUSE_DRAGGED) {                   
                    if ((me.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                        processTranslate(me);
                    } else if ((me.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                        processRotation(me);
                    }
                }
                if (me.getID() == MouseEvent.MOUSE_RELEASED) {
                    // This does a reset of tracking
                    lastMouseX = -1;
                }
            } else if (events[i] instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }

        quaternion.fromAngles(rotX * (float) Math.PI / 180.0f, rotY * (float) Math.PI / 180.0f, 0.0f);
        quaternion.mult(lookDirection, rotatedLookDirection);
        quaternion.mult(sideDirection, rotatedSideDirection);
        quaternion.mult(upDir, rotatedUpDirection);
        updatePosition();
        quaternion.mult(position, rotatedPosition);
        // Now move transX amount in side direction and transZ in look direction
        rotatedPosition.x += (transZ*rotatedLookDirection.x + transX*rotatedSideDirection.x);
        rotatedPosition.z += (transZ*rotatedLookDirection.z + transX*rotatedSideDirection.z);
        translation.x = -rotatedPosition.x;
        translation.y = -rotatedPosition.y;
        translation.z = -rotatedPosition.z;
    }
    
    private void processRotation(MouseEvent me) {
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
            deltaY = -deltaY;
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
    
    private void processTranslate(MouseEvent me) {
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
            deltaY = -deltaY;
            deltaX = -deltaX;

            transX += (deltaX * 0.1f);
            transZ += (deltaY * 0.1f);
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
                position.x += (walkInc * lookDirection.x);
                position.y += (walkInc * lookDirection.y);
                position.z += (walkInc * lookDirection.z);
                break;
            case WALKING_BACK:
                position.x -= (walkInc * lookDirection.x);
                position.y -= (walkInc * lookDirection.y);
                position.z -= (walkInc * lookDirection.z);
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

    }
    
    public void getLookDirection(Vector3f out) {
        out.set(rotatedLookDirection);
    }
    
    public void getSideDirection(Vector3f out) {
        out.set(rotatedSideDirection);
    }
    
    public void getUpDirection(Vector3f out) {
        out.set(rotatedUpDirection);
    }
    
    public void getPosition(Vector3f out) {
        out.set(rotatedPosition);
    }
    
    /**
     * The commit methods
     */
    public void commit(ProcessorArmingCollection collection) {
        target.setLocalRotation(quaternion);
        target.setLocalTranslation(rotatedPosition);
        //System.out.println("Rotation: " + quaternion);
        //System.out.println("Translation: " + translation);
        //boundsPosition.set(position.x, position.y - 1.5f, position.z);
        //bounds.setLocalTranslation(boundsPosition);
        //worldManager.addToUpdateList(bounds);
        worldManager.addToUpdateList(target);
    }
}
