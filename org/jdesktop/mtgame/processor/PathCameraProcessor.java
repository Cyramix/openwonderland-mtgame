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
import com.jme.math.Quaternion;
import com.jme.scene.CameraNode;
import com.jme.renderer.Camera;

import java.awt.event.KeyEvent;

/**
 * A camera which orbits an object
 * 
 * @author Doug Twilleager
 */
public class PathCameraProcessor extends AWTEventProcessorComponent {
    /**
     * The arming conditions for this processor
     */
    private ProcessorArmingCollection collection = null;
    
    /**
     * States for movement
     */
    private static final int STOPPED = 0;
    private static final int RUNNING = 1;
    
    /**
     * Our current state
     */
    private int state = STOPPED;
    
    /**
     * The Node to modify
     */
    private CameraNode target = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The set of positions, directions, ups, and times
     */
    private Vector3f[] positions = null;
    private Quaternion[] rots = null;
    private float[] times = null;
    
    /**
     * The set of current variables
     */
    private Vector3f curPosition = new Vector3f();
    private Quaternion curRot = new Quaternion();
    private long startTime = 0;
    private long totalTime = 0;
    
    /**
     * The default constructor
     */
    public PathCameraProcessor(AWTInputComponent listener, CameraNode cameraNode,
            WorldManager wm, Entity myEntity, Vector3f[] positions, Quaternion[] rots,
            float[] times) {
        super(listener);
        target = cameraNode;
        worldManager = wm;
        setEntity(myEntity);

        this.positions = positions;
        this.rots = rots;
        this.times = times;
        
        curPosition.set(positions[0]);
        curRot.set(rots[0]);
        target.updateFromCamera();
        state = STOPPED;
        
        collection = new ProcessorArmingCollection(this);        
        collection.addCondition(new AwtEventCondition(this));
        collection.addCondition(new NewFrameCondition(this));
    }
    
    public void initialize() {
        setArmingCondition(collection);
    }
    
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = getEvents();

        for (int i=0; i<events.length; i++) {
            if (events[i] instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
        
        if (state == RUNNING) {
            totalTime = System.nanoTime() - startTime;
            float flTime = totalTime/1000000000.0f;
            
            // Find the right interval
            int startIndex = 0;
            int endIndex = 0;
            
            for (int i=0; i<times.length-1; i++) {
                if (flTime > times[i] && flTime < times[i+1]) {
                    startIndex = i;
                    endIndex = i+1;
                    break;
                }
            }
            
            if (startIndex != endIndex) {
                // Find the percentage
                float timeSpread = times[endIndex] - times[startIndex];
                float alpha = flTime - times[startIndex];
                float penetration = alpha/timeSpread;
                //System.out.println("Penetration is: " + penetration);
                curPosition.interpolate(positions[startIndex], positions[endIndex], penetration);
                curRot.slerp(rots[startIndex], rots[endIndex], penetration);
            } 
        }

    }

    private void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_R) {
                curPosition.set(positions[0]);
                curRot.set(rots[0]);
                state = STOPPED;
            }
            if (ke.getKeyCode() == KeyEvent.VK_S) {
                state = RUNNING;
                startTime = System.nanoTime();
            }
        }
    }

    
    /**
     * The commit methods
     */
    public void commit(ProcessorArmingCollection collection) {
        Camera camera = target.getCamera();
        /*
        camera.setLocation(curPosition);
        camera.setDirection(curDirection);
        camera.setUp(curUp);
        camera.setLeft(curLeft);
        target.updateFromCamera();
         * */
        target.setLocalTranslation(curPosition);
        target.setLocalRotation(curRot);
        
        worldManager.addToUpdateList(target);
    }
}
