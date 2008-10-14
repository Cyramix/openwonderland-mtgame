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

import java.util.ArrayList;
import java.awt.Canvas;
import java.awt.event.*;

import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.CameraNode;
import com.jme.scene.Spatial;
import com.jme.system.*;
import com.jme.renderer.*;
import com.jme.renderer.pass.Pass;
import com.jme.scene.state.*;
//import com.jmex.awt.SimpleCanvasImpl;
//import com.jmex.awt.JMECanvas;
import com.jme.system.canvas.JMECanvas;
import com.jme.system.canvas.SimpleCanvasImpl;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.Threading;
import com.jmex.awt.jogl.JOGLAWTCanvasConstructor;
import com.jmex.awt.lwjgl.LWJGLAWTCanvasConstructor;
import com.jme.system.lwjgl.LWJGLSystemProvider;
import com.jme.system.lwjgl.LWJGLDisplaySystem;

/**
 * This is the main rendering thread for a screen.  All jME calls must be 
 * made from this thread
 * 
 * @author Doug Twilleager
 */
class Renderer extends Thread {
    /**
     * The RenderManager for this renderer
     */
    private RenderManager renderManager = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The list of processors to run on the frame
     */
    private ArrayList processorsTriggered = new ArrayList();
    
    /**
     * A flag indicating, that some entity has been added or removed
     */
    private boolean entityChanged = false;
    
    /**
     * A lock to acquire when checking/changing entities
     */
    private Object entityLock = new Object();

    /**
     * The list of render updaters
     */
    private ArrayList renderUpdateList = new ArrayList();
    private ArrayList renderUpdateArgs = new ArrayList();
    
    /**
     * The list of scene objects that need their state updated
     */
    private ArrayList updateList = new ArrayList();
    
    /**
     * The list of collision components that need their state updated
     */
    private ArrayList collisionComponents = new ArrayList();
    
    /**
     * The list of camera objects that need their state updated
     */
    private ArrayList cameraUpdateList = new ArrayList();
    
    /**
     * The cached list of known cameras
     */
    private ArrayList renderCameras = new ArrayList();
    
    /**
     * The array list of camera's
     */
    private ArrayList cameras = new ArrayList();
    
    /**
     * A boolean indicating that the camera list has changed
     */
    private boolean camerasChanged = false;
    
    /**
     * The list of all skyboxes
     */
    private ArrayList skyboxes = new ArrayList();
    
    /**
     * A boolean indicating that the skybox list has changed
     */
    private boolean skyboxChanged = false;
    
    /**
     * The current skybox
     */
    private Skybox currentSkybox = null;
    
    /**
     * The current list of scenes
     */
    private ArrayList renderScenes = new ArrayList();
    
    /**
     * The array list of scene's
     */
    private ArrayList scenes = new ArrayList();
    
    /**
     * A boolean indicating that the scene list has changed
     */
    private boolean scenesChanged = false;
          
    /**
     * The current list of renderable passes
     */
    private ArrayList renderPasses = new ArrayList();
    
    /**
     * The array list of passes
     */
    private ArrayList passes = new ArrayList();
    
    /**
     * A boolean indicating that the pass list has changed
     */
    private boolean passesChanged = false;
          
    /**
     * The list of scene objects that need their state updated
     */
    private ArrayList passUpdateList = new ArrayList();
    
    /**
     * The list of scene components to be parsed for picking
     */
    private ArrayList pickScenes = new ArrayList();
    
    /**
     * An object to lock out pick requests when scene updates are happening
     */
    private Object pickLock = new Object();
    
    /**
     * The list of HUD elements
     */
    private HudComponent[] renderHuds = null;
    
    /**
     * The array list of hud's
     */
    private ArrayList huds = new ArrayList();
    
    /**
     * A boolean indicating that the hud list has changed
     */
    private boolean hudsChanged = false;
    
    /**
     * The list of listeners waiting for notifications of scene changes
     */
    private ArrayList nodeListeners = new ArrayList();
    
    /**
     * The screen number for this renderer
     */
    private int screenNumber = -1;
    
    /**
     * This flag tells the renderer when it is done.
     */
    private boolean done = false;
    
    /**
     * The commit process list - to be processed as we can at the
     * end of the render loop.
     */
    private ProcessorComponent[] commitList = null;
    
    /**
     * The committer currently being processed.
     */
    private int currentCommit = 0;
    
    /**
     * The desired framerate, in frames per second.
     */
    private int desiredFrameRate = 60;
    
    /**
     * The frame time needed to achieve the desired frames per second.
     */
    private long desiredFrameTime = -1;
    
    /**
     * A callback for someone interested in the framerate
     */
    private FrameRateListener frameRateListener = null;
    
    /**
     * The Frequency - in number of frames - to update the listener
     */
    private int frameRateListenerFrequency = 0;
    
    /**
     * A countdown variable for the listener
     */
    private int listenerCountdown = 0;
    private long listenerStarttime = 0;
    
    /**
     * The Display System for jME
     */
    private DisplaySystem displaySystem = null;
    
    /**
     * The jME Renderer object
     */
    private com.jme.renderer.Renderer jmeRenderer = null;
    
    /**
     * The current rendering canvas
     */
    private GLCanvas currentCanvas = null;
    private Canvas currentAWTCanvas = null;
    
    /**
     * A boolean that indicates that the canvas is ready
     */
    private boolean canvasReady = false;
    
    /**
     * This is true if we are using JOGL
     */
    private boolean useJOGL = true;
            
    /**
     * The constructor
     */
    Renderer(WorldManager wm, RenderManager rm, int screenNum) {
        worldManager = wm;
        renderManager = rm;
        screenNumber = screenNum;
        desiredFrameTime = 1000000000/desiredFrameRate;
    }
    
    /**
     * Get the renderer started.  This is called from the render manager to get
     * the thread started.
     */
    synchronized void initialize() {
        this.start();
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    } 

    synchronized Canvas createCanvas(int width, int height) {
        // Create the canvas and it's notification object
        JMECanvas canvas = displaySystem.createCanvas(width, height, "AWT", null);
        if (useJOGL) {
            ((GLCanvas)canvas).setAutoSwapBufferMode(false);
        }
        //System.out.println("CANVAS: " + canvas);

        MyImplementor impl = new MyImplementor(this, width, height);
        canvas.setImplementor(impl);
         
        return ((Canvas)canvas);
    }
    
    synchronized void setCurrentCanvas(Canvas canvas) {
        currentAWTCanvas = canvas;
        if (useJOGL) {
            currentCanvas = (GLCanvas) canvas;
        }
    }
    
    /**
     * This is internal initialization done once.
     */
    synchronized void initRenderer() {
        //Create the base jME objects
        try {
            if (!useJOGL) {
                displaySystem = DisplaySystem.getDisplaySystem(LWJGLSystemProvider.LWJGL_SYSTEM_IDENTIFIER);
                //displaySystem = DisplaySystem.getDisplaySystem("LWJGL");
                displaySystem.registerCanvasConstructor("AWT", LWJGLAWTCanvasConstructor.class);
            } else {
                displaySystem = DisplaySystem.getDisplaySystem("JOGL");
                Threading.disableSingleThreading();
                displaySystem.registerCanvasConstructor("AWT", JOGLAWTCanvasConstructor.class);
            }
            //lwjglDisplay = (LWJGLDisplaySystem) displaySystem;
            //joglDisplay = (JOGLDisplaySystem) displaySystem;
        } catch (JmeException e) {
            System.out.println(e);
        }

        // Let the caller know to proceed
        notify();
        
        // Now wait for a canvas...
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        
        // We should be set to go now...
        jmeRenderer = displaySystem.getRenderer();
        //System.out.println("jmeRenderer: " + jmeRenderer);
        canvasReady = true;
    }
    
    synchronized void canvasIsReady() {
        // Just notify the renderer
        notify();
    }
    
    void waitUntilReady() {
        while (!canvasReady) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
    
    /**
     * Get an object from the jME Renderer
     */
    RenderState createRendererState(int type) {
        return (jmeRenderer.createState(type));
    } 
               
    /**
     * Create the jmeCamera
     */
    Camera createJMECamera(int width, int height) {  
        return (jmeRenderer.createCamera(width, height));   
    }
    
    /**
     * The render loop
     */
    public void run() {
        long processTime = -1;
        long frameStartTime = -1;
        long renderTime = -1;
        long totalTime = -1;
        long threadStartTime = System.nanoTime();
        GLContext glContext = null;
        
        initRenderer();
        while (!done) {
            // Snapshot the current time
            frameStartTime = System.nanoTime();
            
            /**
             * Grab any new entities
             */
            synchronized (pickLock) {
                checkForEntityChanges();
            }

            if (renderScenes.size() > 0 || renderCameras.size() > 0) {
                if (useJOGL) {
                    glContext = currentCanvas.getContext();
                    try {
                        glContext.makeCurrent();
                    } catch (javax.media.opengl.GLException e) {
                        System.out.println(e);
                    }
                } else {
                //((LWJGLDisplaySystem)displaySystem).setCurrentCanvas((JMECanvas) currentCanvas);
                    ((LWJGLDisplaySystem)displaySystem).switchContext(currentCanvas);   
                }

                /* 
                 * This block of code handles calling entity processes which are
                 * locked to the renderer - like the current camera.
                 */
                runProcessorsTriggered();
                
                /**
                 * This allows anyone that needs to do some updating in the render
                 * thread be called
                 */
                processRenderUpdates();
                
                /* 
                 * This block handles any state updates needed to any of the graphs
                 */
                synchronized (pickLock) {
                    float flTime = totalTime/1000000000.0f;
                    processCameraUpdates(flTime);
                    processUpdates(flTime);
                    processPassUpdates(flTime);
                    processCollisionUpdates(flTime);
                }

                // First, clear the buffers
                //jmeRenderer.setBackgroundColor(new ColorRGBA(1.0f, 0.0f, 0.0f, 0.0f));
                jmeRenderer.clearQueue();
                jmeRenderer.getQueue().setTwoPassTransparency(true);
                jmeRenderer.clearBuffers();
                                   
                // Render the skybox
                if (currentSkybox != null) {
                    jmeRenderer.draw(currentSkybox);
                }
                
                /*
                 * This block does the actual rendering of the frame
                 */
                for (int i = 0; i < renderScenes.size(); i++) {
                    RenderComponent scene = (RenderComponent) renderScenes.get(i);
                    Node sceneRoot = scene.getSceneRoot();

                    jmeRenderer.draw(sceneRoot);
                }
                
                for (int i = 0; i < renderPasses.size(); i++) {
                    PassComponent pc = (PassComponent) renderPasses.get(i);
                    pc.getPass().renderPass(jmeRenderer);
                }
 
                // This actually does the rendering
                jmeRenderer.renderQueue();
               
                currentCanvas.swapBuffers();
            }
            
            /*
             * Now we track some times, and process the commit lists
             */
            
            // Snapshot the time it took to render
            renderTime = System.nanoTime() - frameStartTime;
            //Calculate the amount of time left to process commits
            processTime = desiredFrameTime - renderTime;
            
            // Process the commit list
            synchronized (pickLock) {
                processCommitList(processTime);
                if (processTime < 0) {
                    //System.out.println("NEED TO ADAPT TO NEGATIVE PROCESS TIME");
                    }
            }

             
            // Let the processes know that we want to do a frame tick
            if (screenNumber == 0) {
                renderManager.triggerNewFrame();
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

            if (frameRateListener != null) {
                listenerCountdown--;
                if (listenerCountdown == 0) {
                    long currentTime = System.nanoTime();
                    long elapsedTime = currentTime - listenerStarttime;
                    float flTime = elapsedTime/1000000000.0f;
                    float framerate = ((float)frameRateListenerFrequency)/flTime;
                    frameRateListener.currentFramerate(framerate);

                    listenerCountdown = frameRateListenerFrequency;
                    listenerStarttime = currentTime;
                }
            }
        }
    }
    
    /**
     * Add a RenderUpdater to the list of objects to update in the render thread.
     */
    void addRenderUpdater(RenderUpdater ru, Object obj) {
        synchronized (renderUpdateList) {
            renderUpdateList.add(ru);
            renderUpdateArgs.add(obj);
        }
    }
    
    /**
     * Process anyone who wants to update in the render thread before rendering
     */
    void processRenderUpdates() {
        synchronized (renderUpdateList) {
            for (int i=0; i<renderUpdateList.size(); i++) {
                RenderUpdater ru = (RenderUpdater) renderUpdateList.get(i);
                Object obj = renderUpdateArgs.get(i);
                ru.update(obj);
            }
            renderUpdateList.clear();
            renderUpdateArgs.clear();
        }
    }
    
    /**
     * Process the scene updates
     */
    void processUpdates(float referenceTime) {

        synchronized (updateList) {
            if (updateList.size() != 0) {
                for (int i = 0; i < updateList.size(); i++) {
                    Spatial s = (Spatial) updateList.get(i);

                    s.updateGeometricState(referenceTime, true);
                    s.updateRenderState();
                }
                updateList.clear();
                notifyNodeChangedListeners();
            }
        }
    }   
    
    /**
     * Process the scene updates
     */
    void processPassUpdates(float referenceTime) {

        synchronized (passUpdateList) {
            if (passUpdateList.size() != 0) {
                for (int i = 0; i < passUpdateList.size(); i++) {
                    Pass p = (Pass) passUpdateList.get(i);
                    p.updatePass(referenceTime);
                }
                passUpdateList.clear();
                notifyNodeChangedListeners();
            }
        }
    }
    
    /**
     * Process the scene updates
     */
    void processCollisionUpdates(float referenceTime) {
        // TODO: remove duplicate updates between this and render components
        synchronized (collisionComponents) {
            if (collisionComponents.size() != 0) {
                for (int i = 0; i < collisionComponents.size(); i++) {
                    CollisionComponent cc = (CollisionComponent) collisionComponents.get(i);

                    Node node = cc.getNode();
                    if (node != null) {
                        node.updateGeometricState(referenceTime, true);
                        node.updateRenderState();
                    }
                    cc.getCollisionSystem().addCollisionComponent(cc);
                }
                collisionComponents.clear();
            }
        }
    }
    
    /**
     * Process the camera updates
     */
    void processCameraUpdates(float referenceTime) {
        CameraComponent cameraComponent = null;
        Node cameraSceneGraph = null;
        Camera camera = null;
        CameraNode cameraNode = null;

        if (cameraUpdateList.size() != 0) {
            for (int i = 0; i < cameraUpdateList.size(); i++) {
                cameraComponent = (CameraComponent) cameraUpdateList.get(i);
                cameraSceneGraph = cameraComponent.getCameraSceneGraph();             
                
                camera = cameraComponent.getCamera();
                cameraNode = cameraComponent.getCameraNode();
                
                if (cameraComponent.isPrimary()) {
                    camera.update();
                    jmeRenderer.setCamera(camera);                
                }

                cameraSceneGraph.updateGeometricState(referenceTime, true);
            }
            cameraUpdateList.clear();
        }
    }
    
    /**
     * Process as many committers as we can, given the amount of process time
     */
    synchronized void processCommitList(long processTime) {
        long currentTime = System.nanoTime();
        long elapsedTime = 0;
        long nextCurrentTime = 0;
        ProcessorComponent pc = null;
        
        if (commitList == null) {
            //System.out.println("Renderer: No Commits");
            return;
        }
        
        // Note: We won't stop in the middle of a chain
        // TODO: Work on partial commits
        while (/*elapsedTime < processTime &&*/ currentCommit != commitList.length) {
            pc = commitList[currentCommit++];
            pc.commit(pc.getCurrentTriggerCollection());
            pc.clearTriggerCollection();
            
            // Process the chain
            pc = pc.getNextInChain();
            while (pc != null) {
                pc.commit(pc.getCurrentTriggerCollection());
                pc.clearTriggerCollection();
                pc = pc.getNextInChain();
            }
   
            nextCurrentTime = System.nanoTime();
            elapsedTime += (nextCurrentTime - currentTime);
            currentTime = nextCurrentTime;
        }
        
        // If we are done, notify the process controller
        if (currentCommit == commitList.length) {
            currentCommit = 0;
            commitList = null;
            notify();
        }
    }
    
    /**
     * Run the processes component commit list
     * For now, we'll just run them on screen 0
     */
    synchronized void runCommitList(ProcessorComponent[] runList) {
        commitList = runList;
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        
    }
        
    /**
     * Add a processor which has triggerd to the Renderer Processor List
     */
    void addTriggeredProcessor(ProcessorComponent pc) {
        synchronized (processorsTriggered) {
            if (!processorsTriggered.contains(pc)) {
                processorsTriggered.add(pc);
            }
        }
    }
    
    /** 
     * Run the processors that have triggered
     */
    void runProcessorsTriggered() {
        ProcessorComponent pc = null;
        ProcessorComponent[] procs = new ProcessorComponent[0];
        
        synchronized (processorsTriggered) {
            // Snapshot the list of those to run.
            procs = (ProcessorComponent[]) processorsTriggered.toArray(procs);
            processorsTriggered.clear();
        }
        
        for (int i = 0; i < procs.length; i++) {
            pc = procs[i];
            pc.compute(pc.getCurrentTriggerCollection());
            pc.commit(pc.getCurrentTriggerCollection());
            pc.clearTriggerCollection();
            worldManager.armProcessorComponent(pc.getArmingCondition());
            
            // Process the chain
            pc = pc.getNextInChain();
            while (pc != null) {
                pc.compute(pc.getCurrentTriggerCollection());
                pc.commit(pc.getCurrentTriggerCollection());
                pc.clearTriggerCollection();
                pc = pc.getNextInChain();
            }
        }
    }
    
    void addEntity(Entity e) {
        EntityComponent ec = null;

        synchronized (entityLock) {
            
            // Lot's of things can have a camera
            if ((ec = e.getComponent(CameraComponent.class)) != null) {
                cameras.add(ec);
                camerasChanged = true;
                entityChanged = true;
            }
            
            if ((ec = e.getComponent(CollisionComponent.class)) != null) {
                synchronized (collisionComponents) {
                    collisionComponents.add(ec);
                }
            }
            
            if ((ec = e.getComponent(SkyboxComponent.class)) != null) {
                synchronized (skyboxes) {
                    skyboxes.add(ec);
                    skyboxChanged = true;
                    entityChanged = true;
                }
            }
                        
            if ((ec = e.getComponent(PassComponent.class)) != null) {
                synchronized (passes) {
                    passes.add(ec);
                    passesChanged = true;
                    entityChanged = true;
                }
            }

            // An entity is one of the following - for now.
            if ((ec = e.getComponent(RenderComponent.class)) != null) {
                processSceneGraph((RenderComponent)ec);
                if (((RenderComponent)ec).getAttachPoint() != null) {
                    processAttachPoint((RenderComponent)ec, true);
                } else {
                    scenes.add(ec);
                    scenesChanged = true;
                    entityChanged = true;
                }
            } else if ((ec = e.getComponent(HudComponent.class)) != null) {
                huds.add(ec);
                hudsChanged = true;
                entityChanged = true;
            }
        }
    }
    
    void removeEntity(Entity e) {
        EntityComponent ec = null;

        synchronized (entityLock) {
            
            // Lot's of things can have a camera
            if ((ec = e.getComponent(CameraComponent.class)) != null) {
                cameras.remove(ec);
                camerasChanged = true;
                entityChanged = true;
            }

                        
            if ((ec = e.getComponent(CollisionComponent.class)) != null) {
                synchronized (collisionComponents) {
                    collisionComponents.remove(ec);
                }
            }
                        
            if ((ec = e.getComponent(SkyboxComponent.class)) != null) {
                synchronized (skyboxes) {
                    skyboxes.remove(ec);
                    skyboxChanged = true;
                    entityChanged = true;
                }
            }
                                   
            if ((ec = e.getComponent(PassComponent.class)) != null) {
                synchronized (passes) {
                    passes.remove(ec);
                    passesChanged = true;
                    entityChanged = true;
                }
            }
            
            // An entity is one of the following - for now.
            if ((ec = e.getComponent(RenderComponent.class)) != null) {
                if (((RenderComponent) ec).getAttachPoint() != null) {
                    processAttachPoint((RenderComponent) ec, false);
                } else {
                    scenes.remove(ec);
                    scenesChanged = true;
                    entityChanged = true;
                }
            } else if ((ec = e.getComponent(HudComponent.class)) != null) {
                huds.remove(ec);
                hudsChanged = true;
                entityChanged = true;
            }
        }
    }
    
    /**
     * Add a component to the list that we process
     * @param c
     */
    void addComponent(EntityComponent c) {
        synchronized (entityLock) {
              
            // Lot's of things can have a camera
            if (c instanceof CameraComponent) {
                cameras.add(c);
                camerasChanged = true;
                entityChanged = true;
            }

            if (c instanceof CollisionComponent) {
                synchronized (collisionComponents) {
                    collisionComponents.add(c);
                }
            }
                                         
            if (c instanceof SkyboxComponent) {
                synchronized (skyboxes) {
                    skyboxes.add(c);
                    skyboxChanged = true;
                    entityChanged = true;
                }
            }
                                              
            if (c instanceof PassComponent) {
                synchronized (passes) {
                    passes.add(c);
                    passesChanged = true;
                    entityChanged = true;
                }
            }
                   
            // An entity is one of the following - for now.
            if (c instanceof RenderComponent) {
                processSceneGraph((RenderComponent)c);
                if (((RenderComponent) c).getAttachPoint() != null) {
                    processAttachPoint((RenderComponent) c, true);
                } else {
                    scenes.add(c);
                    scenesChanged = true;
                    entityChanged = true;
                }
            } else if (c instanceof HudComponent) {
                huds.add(c);
                hudsChanged = true;
                entityChanged = true;
            }
        }
    }
    
    /**
     * Remove a component from the list that we process
     * @param c
     */    
    void removeComponent(EntityComponent c) {
        synchronized (entityLock) {
                   
            // Lot's of things can have a camera
            if (c instanceof CameraComponent) {
                cameras.remove(c);
                camerasChanged = true;
                entityChanged = true;
            }

            
            if (c instanceof CollisionComponent) {
                synchronized (collisionComponents) {
                    collisionComponents.remove(c);
                }
            }
                                         
            if (c instanceof SkyboxComponent) {
                synchronized (skyboxes) {
                    skyboxes.remove(c);
                    skyboxChanged = true;
                    entityChanged = true;
                }
            }
                                                                  
            if (c instanceof PassComponent) {
                synchronized (passes) {
                    passes.remove(c);
                    passesChanged = true;
                    entityChanged = true;
                }
            }
            
            // An entity is one of the following - for now.
            if (c instanceof RenderComponent) {
                if (((RenderComponent) c).getAttachPoint() != null) {
                    processAttachPoint((RenderComponent) c, false);
                } else {
                    scenes.remove(c);
                    scenesChanged = true;
                    entityChanged = true;
                }
            } else if (c instanceof HudComponent) {
                huds.remove(c);
                hudsChanged = true;
                entityChanged = true;
            }
        }
    }
    
    /**
     * Attach a render component with an attach point to the appropriate place
     * Add the highest parent to the update list.
     */
    void processAttachPoint(RenderComponent rc, boolean addComponent) {
        Node sceneRoot = rc.getSceneRoot();
        Node attachPoint = rc.getAttachPoint();

        if (addComponent) {
            attachPoint.attachChild(sceneRoot);
        } else {
            attachPoint.detachChild(sceneRoot);
        }
        
        // Get the highest parent
        sceneRoot = attachPoint;
        Node parent = sceneRoot.getParent();
        while (parent != null) {
            sceneRoot = parent;
            parent = parent.getParent();
        }
        addToUpdateList(sceneRoot);
    }
    
    /**
     * Add a node to be updated
     */
    void addToUpdateList(Spatial sp) {
        synchronized (updateList) {
            updateList.add(sp);
        }
    }
          
    /**
     * Add a node to be updated
     */
    void addToPassUpdateList(Pass p) {
        synchronized (passUpdateList) {
            passUpdateList.add(p);
        }
    }
    
    /**
     * Add a listener to the list of listening for scene changes
     */
    public void addNodeChangedListener(NodeChangedListener l) {
        synchronized (nodeListeners) {
            nodeListeners.add(l);
        }
    }
    
    /**
     * Remove a listener from the list of listening for scene changes
     */
    public void removeNodeChangedListener(NodeChangedListener l) {
        synchronized (nodeListeners) {
            nodeListeners.remove(l);
        }
    } 
    
    /**
     * Notify the node changed listeners.
     */
    void notifyNodeChangedListeners() {
        synchronized (nodeListeners) {
            NodeChangedListener l = null;
            for (int i=0; i<nodeListeners.size(); i++) {
                l = (NodeChangedListener)nodeListeners.get(i);
                l.nodeChanged();
            }
        }
    }
    
    /**
     * Get the jme renderer
     */
    com.jme.renderer.Renderer getJMERenderer() {
        return (jmeRenderer);
    }
    
    /**
     * Check for changes in any entities
     */
    void checkForEntityChanges() {
        synchronized (entityLock) {
            if (entityChanged) {
                if (scenesChanged) {
                    processScenesChanged();
                    scenesChanged = false;
                }
                if (camerasChanged) {
                    processCamerasChanged();
                    camerasChanged = false;
                }
                if (skyboxChanged) {
                    processSkyboxChanged();
                    skyboxChanged = false;
                }
                if (passesChanged) {
                    processPassesChanged();
                    passesChanged = false;
                }
                if (hudsChanged) {
                    processHudsChanged();
                    hudsChanged = false;
                }
                entityChanged = false;
            }
        }
    }
    
    /**
     * Check for camera changes
     */
    void processCamerasChanged() {
        int len = 0;
        CameraComponent camera = null;
        
        // Minimize the number of additions to the cameraUpdateList
        // First, let's look for removals
        len = renderCameras.size();
        for (int i=0; i<len;) {
            camera = (CameraComponent) renderCameras.get(i);
            if (cameras.contains(camera)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                renderCameras.remove(camera);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<cameras.size(); i++) {
            camera = (CameraComponent) cameras.get(i);
            if (!renderCameras.contains(camera)) {
                renderCameras.add(camera);
                cameraUpdateList.add(camera);
            }
        }
        
        // Just a sanity check
        if (cameras.size() != renderCameras.size()) {
            System.out.println("Error, Camera sizes differ");
        }
               
    }
    
        
    /**
     * Check for camera changes
     */
    void processSkyboxChanged() {
        SkyboxComponent sbox = null;

        synchronized (skyboxes) {
            for (int i=0; i<skyboxes.size(); i++) {
                sbox = (SkyboxComponent) skyboxes.get(i);
                if (sbox.getCurrent()) {
                    currentSkybox = sbox.getSkybox();
                    addToUpdateList(currentSkybox);
                    break;
                }
            }
        }
               
    }
    
    /**
     * Check for scene changes
     */
    void processScenesChanged() {
        int len = 0;
        RenderComponent scene = null;
        
        // = (ArrayList)scenes.clone();
        
        // Minimize the numner of additions to the updateList
        // First, let's look for removals
        len = renderScenes.size();
        for (int i=0; i<len;) {
            scene = (RenderComponent) renderScenes.get(i);
            if (scenes.contains(scene)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                renderScenes.remove(scene);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<scenes.size(); i++) {
            scene = (RenderComponent) scenes.get(i);
            if (!renderScenes.contains(scene)) {
                renderScenes.add(scene);
                addToUpdateList(scene.getSceneRoot());
            }
        }
        
        // Just a sanity check
        if (scenes.size() != renderScenes.size()) {
            System.out.println("Error, Scene sizes differ: " + scenes.size() + ", " + renderScenes.size()); 
        }
       
    }

    /**
     * Check for pass changes
     */
    void processPassesChanged() {
        int len = 0;
        PassComponent pass = null;
        
        // Minimize the numner of additions to the updateList
        // First, let's look for removals
        len = renderPasses.size();
        for (int i=0; i<len;) {
            pass = (PassComponent) renderPasses.get(i);
            if (passes.contains(pass)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                renderPasses.remove(pass);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<passes.size(); i++) {
            pass = (PassComponent) passes.get(i);
            if (!renderPasses.contains(pass)) {
                renderPasses.add(pass);
                addToPassUpdateList(pass.getPass());
            }
        }
        
        // Just a sanity check
        if (passes.size() != renderPasses.size()) {
            System.out.println("Error, Pass sizes differ: " + passes.size() + ", " + renderPasses.size()); 
        }
       
    }

    /**
     * Set desired frame rate
     */
    void setDesiredFrameRate(int fps) {
        desiredFrameRate = fps;
        desiredFrameTime = 1000000000/desiredFrameRate;
    }
              
    /**
     * Set a listener for frame rate updates
     */
    void setFrameRateListener(FrameRateListener l, int frequency) {
        listenerCountdown = frequency;
        listenerStarttime = System.nanoTime();
        frameRateListenerFrequency = frequency;
        frameRateListener = l;
    }
    
    /**
     * Turn on key input tracking
     */
    void trackKeyInput(Object listener) {
        currentAWTCanvas.addKeyListener((KeyListener)listener);
    }
    
    /**
     * turn off key input tracking
     */
    void untrackKeyInput(Object listener) {
        currentAWTCanvas.removeKeyListener((KeyListener)listener);
    }
        
    /**
     * turn on mouse input tracking
     */
    void trackMouseInput(Object listener) {
        currentAWTCanvas.addMouseListener((MouseListener) listener);
        currentAWTCanvas.addMouseMotionListener((MouseMotionListener) listener);
        currentAWTCanvas.addMouseWheelListener((MouseWheelListener) listener);
    }
        
    /**
     * turn off mouse input tracking
     */
    void untrackMouseInput(Object listener) {
        currentAWTCanvas.removeMouseListener((MouseListener) listener);
        currentAWTCanvas.removeMouseMotionListener((MouseMotionListener) listener);
        currentAWTCanvas.removeMouseWheelListener((MouseWheelListener) listener);
    }

    /**
     * Check for hud changes
     */
    void processHudsChanged() {
        
    }
    
    class MyImplementor extends SimpleCanvasImpl {
        /**
         * The Renderer to notify
         */
        private Renderer mtrenderer = null;

        public MyImplementor(Renderer renderer, int width, int height) {
            super(width, height);
            this.mtrenderer = renderer;
        }

        public void simpleSetup() {
            if (!useJOGL) {
                mtrenderer.canvasIsReady();
                //System.out.println("In simple setup: ");
            }
            
        }

        public void simpleUpdate() {
            if (useJOGL) {
                mtrenderer.canvasIsReady();
                //System.out.println("In simple update");
            }
        }
    }
    
    /**
     * The jme collision system needs this for locking
     * @return
     */
    Object getCollisionLock() {
        return (pickLock);
    }
    
    /**
     * Do any pre-processing on the scene graph
     */
    void processSceneGraph(RenderComponent sc) {
        Node sg = sc.getSceneRoot();
        
        traverseGraph(sg);
    }
    
    /**
     * Examine this node and travese it's children
     */
    void traverseGraph(Spatial sg) {
   
        examineSpatial(sg);
        if (sg instanceof Node) {
            Node node = (Node)sg;
            for (int i=0; i<node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                traverseGraph(child);
            }
        }
    }
    
    /**
     * Examine the given node
     */
    void examineSpatial(Spatial s) {
        checkForTransparency(s);
    }
    
    /**
     * This mehod checks for transpaency attributes
     */
    void checkForTransparency(Spatial s) {
        BlendState blendState = (BlendState) s.getRenderState(RenderState.RS_BLEND);
        
        if (s.getRenderQueueMode() != com.jme.renderer.Renderer.QUEUE_SKIP) {
            if (blendState != null) {
                if (blendState.isBlendEnabled()) {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_TRANSPARENT);
                //System.out.println("In Transparent Queue: " + s);
                } else {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
                //System.out.println("In Opaque Queue: " + s);
                }
            } else {
                s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_INHERIT);
            //System.out.println("Inheriting (No Blend State): " + s);
            }
        }
    }
}
