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

import com.bulletphysics.dynamics.DynamicsWorld;
import java.util.HashSet;
import java.util.Set;

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
     * The set of listeners for time step events
     */
    private Set<TimeStepListener> listenerSet = new HashSet();

    /**
     * The current time step of the simulation
     */
    private int timeStep = 0;

    /**
     * Allow the system to initialize
     */
    synchronized void initialize() {
        JBulletDynamicCollisionSystem cs = (JBulletDynamicCollisionSystem)collisionSystem;
        world = cs.getDynamicsWorld();

        if (System.getProperty("mtgame.runPhysicsInRenderer") != null) {
            System.out.println("MT Game Info: Running Physics in Renderer Thread");
            worldManager.getRenderManager().addPhysicsSystem(this);
        } else {
            thread = new Thread(this);
            thread.setName("JBullet Physics Thread");
        
            thread.start();
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
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

            simStep(stepTime);

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

    void quit() {
        done = true;
    }

    /**
     * Adds a new timestep listener. If the listener already exists, this method
     * does nothing.
     * @param listener The listener to add
     */
    public void addTimeStepListener(TimeStepListener listener) {
        synchronized (listenerSet) {
            listenerSet.add(listener);
        }
    }

    /**
     * Removes a timestep listener. If this listener does not exist, this method
     * does nothing.
     * @param listener The listener to remove
     */
    public void removeTimeStepListener(TimeStepListener listener) {
        synchronized (listenerSet) {
            listenerSet.remove(listener);
        }
    }

    /**
     * Notifies all of the listeners of the time step event
     */
    private void fireTimeStepEvent(TimeStepEvent tse) {
        synchronized (listenerSet) {
            for (TimeStepListener l : listenerSet) {
                l.timeStepActionPerformed(tse);
            }
        }
    }

    /**
     * An event class to store the current timestep information
     */
    public class TimeStepEvent {
        public DynamicsWorld world = null;
        public long timeStep = 0;
        public float startTime = 0.0f;

        public TimeStepEvent(DynamicsWorld world, long timeStep, float startTime) {
            this.world = world;
            this.timeStep = timeStep;
            this.startTime = startTime;
        }
    }

    /**
     * Listener interface for callbacks during each time step
     */
    public interface TimeStepListener {
        /**
         * Invoked before the simulation is stepped, given the world dynamics
         * object, the current time step, and the frame start time
         * @param tse The time step event
         */
        public void timeStepActionPerformed(TimeStepEvent tse);
    }

    void simStep(float time) {
        if (started) {
            // Call listeners to update state on the simulation thread.
            TimeStepEvent tse = new TimeStepEvent(world, timeStep, time);
            fireTimeStepEvent(tse);

            synchronized (world) {
                world.stepSimulation(time);
            }

            // Increment the time step
            timeStep++;
        }
    }
}
