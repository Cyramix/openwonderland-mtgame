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

import com.bulletphysics.dynamics.DynamicsWorld;

/**
 * This interface is implemented by anyone who wishes to implement a 
 * physics system.
 *
 * @author Doug Twilleager
 */
public class JBulletPhysicsSystem extends PhysicsSystem implements Runnable {
    /**
     * A boolean for the physics render loop.
     */
    private boolean done = false;
    
    /**
     * The desired framerate, in frames per second.
     */
    private int desiredFrameRate = 60;
    
    /**
     * The CollisionWorld for this simulation
     */
    private DynamicsWorld world = null;
    
    /**
     * The thread for the simulation
     */
    private Thread thread = null;
    
    /**
     * This allows us to start and stop the simulation
     */
    private boolean started = false;
    
    /**
     * Allow the system to initialize
     */
    synchronized void initialize() {
        JBulletDynamicCollisionSystem cs = (JBulletDynamicCollisionSystem)collisionSystem;
        world = cs.getDynamicsWorld();
        thread = new Thread(this);
        thread.setName("JBullet Physics Thread");
        
        thread.start();
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
    
    /**
     * This creates a jbullet physics component
     */
    public JBulletPhysicsComponent createPhysicsComponent(JBulletCollisionComponent cc) {
        JBulletPhysicsComponent pc = new JBulletPhysicsComponent(this, cc);
        // Need to wait for node to initialize, so just return.
        return (pc);
    }
        /**
     * This is internal initialization done once.
     */
    synchronized void initRenderer() {
        // Let the caller know to proceed
        notify();
    }
    
    public void setStarted(boolean s) {
        started = s;
    }
    
    /**
     * The render loop
     */
    public void run() {
        long totalTime = -1;
        long desiredFrameTime = 1000000000/desiredFrameRate;
        long newStartTime = -1;
        long deltaTime = -1;
        long frameStartTime = System.nanoTime();
        float stepTime = 0.0f;
        
        initRenderer();
        while (!done) {
            // Snapshot the current time
            newStartTime = System.nanoTime();
            deltaTime = newStartTime - frameStartTime;
            frameStartTime = newStartTime;
            stepTime = (deltaTime/1000000)/1000.0f;
            
            if (started) {
                synchronized (world) {
                    world.stepSimulation(stepTime);
                }
            }
           
            // Decide if we need to sleep
            totalTime = System.nanoTime() - frameStartTime;
            if (totalTime < desiredFrameTime) {
                // Sleep to hit the frame rate
                try {
                    int sleeptime = (int)(desiredFrameTime - totalTime);
                    int numMillis = sleeptime/1000000;
                    int numNanos = sleeptime - (numMillis*1000000);
                    //System.out.println("Sleeping for " + numMillis + ", " + numNanos);
                    Thread.sleep(numMillis, numNanos);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
