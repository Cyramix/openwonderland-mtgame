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

import java.util.ArrayList;
import java.awt.Canvas;
import java.awt.event.*;

import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.Geometry;
import com.jme.scene.shape.Quad;
import com.jme.scene.Spatial.CullHint;
import com.jme.math.Vector3f;
import com.jme.light.LightNode;
import com.jme.system.*;
import com.jme.renderer.*;
import com.jme.renderer.pass.Pass;
import com.jme.scene.state.*;
import com.jme.system.canvas.JMECanvas;
import com.jme.system.canvas.SimpleCanvasImpl;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.GL;
import javax.media.opengl.Threading;
import com.jmex.awt.jogl.JOGLAWTCanvasConstructor;
import com.jmex.awt.lwjgl.LWJGLAWTCanvasConstructor;
import com.jme.system.lwjgl.LWJGLSystemProvider;
import com.jme.system.lwjgl.LWJGLDisplaySystem;
import java.lang.reflect.Method;
import javolution.util.FastMap;
import javolution.util.FastList;
import java.lang.Exception;

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
     * The array list of scene's
     */
    private ArrayList lights = new ArrayList();
    
    /**
     * A boolean indicating that the scene list has changed
     */
    private boolean lightsChanged = false;
          
    /**
     * The global light state to be applied to all objects
     */
    private ArrayList globalLights = new ArrayList();
    
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
     * The array list of orthographic projection render components waiting
     * to have their status changed
     */
    private ArrayList<OrthoOp> orthos = new ArrayList();
    
    /**
     * A boolean indicating that the orthos list has changed
     */
    private boolean orthosChanged = false;

    /**
     * A hashmap of geometry objects to track via geometry lod
     */
    private FastMap geometryLODMap = new FastMap();

    /**
     * The list of geometry lod's to update each frame
     */
    private ArrayList geometryLODs = new ArrayList();
       
    /**
     * The array list of render components waiting
     * to have their lighting changed
     */
    private ArrayList componentLighting = new ArrayList();
    
    /**
     * A boolean indicating that some component lighting has changed
     */
    private boolean componentLightingChanged = false;
    
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
     * This flag tells the renderer whether or not to run
     */
    private boolean running = true;
    
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
     * The samples to use for multisampling
     */
    private int minSamples = 0;
    
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
     * The Buffer Controller
     */
    private BufferController bufferController = null;

    /**
     * The list of all render buffers (off and on screen) in order
     */
    private RenderBuffer currentScreenBuffer = null;

    /**
     * The lists of opaque, transparent, and ortho spatials to draw
     */
    private FastList<Spatial> renderList = new FastList<Spatial>();
    private FastList<PassComponent> passList = new FastList<PassComponent>();
    private FastList<Spatial> transparentList = new FastList<Spatial>();
    private FastList<Spatial> orthoList = new FastList<Spatial>();

    /**
     * The list of RenderTechniques
     */
    private FastList<RenderTechnique> renderTechniques = new FastList<RenderTechnique>();

    /**
     * The current rendering canvas
     */
    private GLCanvas currentCanvas = null;
    private GLContext glContext = null;
    
    /**
     * This is true if we are using JOGL
     */
    private boolean useJOGL = true;
        
    /**
     * A flag indicating that the renderer has been initialized
     */
    private boolean initialized = false;
    
    ColorRGBA bgColor = new ColorRGBA();

    /**
     * A lock to aquire when making jme scene graph changes
     */
    private Object jmeSGLock = new Object();

    /**
     * A class to hold collision component actions
     */
    class CollisionComponentOp {
        CollisionComponent cc = null;
        boolean add = false;

        CollisionComponentOp(CollisionComponent cc, boolean add) {
            this.cc = cc;
            this.add = add;
        }
    }

    /**
     * A class to hold render update operations
     */
    class RenderUpdaterOp {
        RenderUpdater updater = null;
        Object arg = null;
        boolean wait = false;
        boolean done = false;

        RenderUpdaterOp(RenderUpdater up, Object arg, boolean wait) {
            updater = up;
            this.arg = arg;
            this.wait = wait;
        }
    }

    /**
     * A class to hold render component actions
     */
    class RenderComponentOp {
        RenderComponent rc = null;
        boolean add = false;

        RenderComponentOp(RenderComponent rc, boolean add) {
            this.rc = rc;
            this.add = add;
        }
    }

    public enum ListType {
        Opaque,
        Transparent,
        Ortho
    }

    /**
     * A class to hold ortho actions
     */
    class OrthoOp {
        RenderComponent rc = null;
        boolean on = false;

        OrthoOp(RenderComponent rc, boolean on) {
            this.rc = rc;
            this.on = on;
        }
    }

    /**
     * The constructor
     */
    Renderer(WorldManager wm, RenderManager rm, int screenNum) {
        worldManager = wm;
        renderManager = rm;
        screenNumber = screenNum;
        desiredFrameTime = 1000000000/desiredFrameRate;
        bufferController = new DefaultBufferController();
        setName("MTGame Renderer");
    }
    
    /**
     * Get the renderer started.  This is called from the render manager to get
     * the thread started.
     */
    void initialize() {
        this.start();
        while (!initialized) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    } 

    /**
     * Create a canvas using the info in the RenderBuffer object
     */
    GLCanvas createCanvas(RenderBuffer rb) {
        int width = rb.getWidth();
        int height = rb.getHeight();
       
        // Create the canvas and it's notification object
        JMECanvas canvas = displaySystem.createCanvas(width, height, "AWT", null);
        if (useJOGL) {
            ((GLCanvas)canvas).setAutoSwapBufferMode(false);
        }
        //System.out.println("CANVAS: " + canvas);

        MyImplementor impl = new MyImplementor(this, rb, (Canvas)canvas, width, height);
        canvas.setImplementor(impl);
         
        return ((GLCanvas)canvas);
    }

    
    void addRenderBuffer(RenderBuffer rb) {
        if (!initialized) {
            initialize();     
        }

        // For onscreen canvases, create the canvas, but don't add
        // it to the buffer list until it has been mapped.  For all
        // others, add them to the buffer list.
        if (rb.getTarget() == RenderBuffer.Target.ONSCREEN) {
            ((OnscreenRenderBuffer) rb).setCanvas(createCanvas(rb));
        } else {
            bufferController.addBuffer(rb);
        }
    }
    
    /**
     * This is internal initialization done once.
     */
    void initRenderer() {
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
            displaySystem.setMinSamples(minSamples);
            markAsRenderThread(true);
        //lwjglDisplay = (LWJGLDisplaySystem) displaySystem;
        //joglDisplay = (JOGLDisplaySystem) displaySystem;
        } catch (JmeException e) {
            System.out.println(e);
        }
        initialized = true;
    }
    
    void addOnscreenBuffer(RenderBuffer rb) {
        if (jmeRenderer == null) {
            jmeRenderer = displaySystem.getRenderer();
            jmeRenderer.getQueue().setTwoPassTransparency(false);
        }
        bufferController.addBuffer(rb);
    }
    
    void waitUntilReady() {
        while (jmeRenderer == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Mark the current thread as one that can update the scene graph
     * @param isRenderThread
     */
    void markAsRenderThread(boolean isRenderThread) {
        displaySystem.setRenderThread(isRenderThread);
    }

    boolean isRenderThread() {
        return displaySystem.isRenderThread();
    }

    /**
     * Process all buffer and entity updates
     */
    private void processInternalUpdates() {
        //checkForRenderBuffers();
        synchronized (pickLock) {
            checkForEntityChanges();
        }     
    }
    
    /**
     * Process all jME related updates.  These happen once per canvas
     */
    void processJMEUpdates(float updateTime) {
        /**
         * This allows anyone that needs to do some updating in the render
         * thread be called
         */
        processRenderUpdates();

        /* 
         * This block handles any state updates needed to any of the graphs
         */
        synchronized (pickLock) {  
            processCameraUpdates(updateTime);
            processUpdates(updateTime);
            processPassUpdates(updateTime);
            processCollisionUpdates(updateTime);
        }

        Vector3f position = jmeRenderer.getCamera().getLocation();
        for (int i=0; i<geometryLODs.size(); i++) {
            GeometryLOD lod = (GeometryLOD) geometryLODs.get(i);
            lod.applyShader(position);
        }
    }
    
    void printGraph(Spatial s) {
        System.out.println("Rendering: " + s);
        System.out.println("Rendering B: " + s.getWorldBound());
        System.out.println("Rendering T: " + s.getWorldTranslation());
        System.out.println("Rendering R: " + s.getWorldRotation());
        System.out.println("Rendering S: " + s.getWorldScale());

        if (Node.class.isInstance(s)) {
            Node n = (Node) s;
            for (int i = 0; i < n.getQuantity(); i++) {
                printGraph(n.getChild(i));
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
     * Get an object from the jME Renderer
     */
    RenderState createRendererState(RenderState.StateType type) {
        return (jmeRenderer.createState(type));
    }
               
    /**
     * Create the jmeCamera
     */
    Camera createJMECamera(int width, int height) {  
        return (jmeRenderer.createCamera(width, height));   
    }
    
    void setRunning(boolean flag) {
        running = flag;
    }
    
    boolean getRunning() {
        return (running);
    }
    
    // We need to call this method reflectively because it isn't available in Java 5
    // BTW: we don't support Java 5 on Linux, so this is okay.
    private static boolean isLinux = System.getProperty("os.name").equals("Linux");
    private static Method isAWTLockHeldByCurrentThreadMethod;
    
    static {
        if (isLinux) {
            try {
                Class awtToolkitClass = Class.forName("sun.awt.SunToolkit");
                isAWTLockHeldByCurrentThreadMethod =
                        awtToolkitClass.getMethod("isAWTLockHeldByCurrentThread");
            } catch (ClassNotFoundException ex) {
            } catch (NoSuchMethodException ex) {
            }
        }
    }
  
    void releaseSwingLock() {
        // Linux-specific workaround: On Linux JOGL holds the SunToolkit AWT lock in mtgame commit methods.
        // In order to avoid deadlock with any threads which are already holding the AWT lock and which
        // want to acquire the lock on the dirty rectangle so they can draw (e.g Embedded Swing threads)
        // we need to temporarily release the AWT lock before we lock the dirty rectangle and then reacquire
        // the AWT lock afterward.
        if (isAWTLockHeldByCurrentThreadMethod != null) {
            try {
                Boolean ret = (Boolean) isAWTLockHeldByCurrentThreadMethod.invoke(null);
                if (ret.booleanValue()) {
                    glContext.release();
                }
            } catch (Exception ex) {
            }
        }
    }
    
    void acquireSwingLock() {
        // Linux-specific workaround: Reacquire the lock if necessary.
        if (glContext != null) {
            glContext.makeCurrent();
        }
    }
      
    /**
     * The render loop
     */
    public void run() {
        long processTime = -1;
        long frameStartTime = -1;
        long renderTime = -1;
        long totalTime = -1;
        
                    
        initRenderer();   
        while (!done) {
            // Pause running if flag is set
            while (!running) {
                try {
                    Thread.sleep(333, 0);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }             
            }
            
            // Snapshot the current time
            frameStartTime = System.nanoTime();

            processInternalUpdates();

            /* 
             * This block of code handles calling entity processes which are
             * locked to the renderer - like the current camera.
             */
            runProcessorsTriggered();
                
            // Ready to update and render.  Don't let anyone in.
            synchronized (jmeSGLock) {
                // Process any jME updates.  We need to make an onscreen
                // canvas current for this, so we pick the last one on the
                // list.
                bufferController.startFrame(jmeRenderer);
                if (bufferController.anyBuffers()) {
                    currentScreenBuffer = bufferController.getCurrentOnscreenBuffer();
                    if (currentScreenBuffer != null &&
                        currentScreenBuffer.makeCurrent(displaySystem, jmeRenderer)) {
                        processJMEUpdates(totalTime / 1000000000.0f);
                        bufferController.renderScene(displaySystem, jmeRenderer, this);
                        bufferController.endFrame(jmeRenderer);
                        currentScreenBuffer.release();
                    }
                }
            }
            /*
             * Now we track some times, and process the commit lists
             */
            
            // Snapshot the time it took to render
            renderTime = System.nanoTime() - frameStartTime;
            //Calculate the amount of time left to process commits
            processTime = desiredFrameTime - renderTime;
            
            // Process the commit list

            if (bufferController.anyBuffers()) {
                currentScreenBuffer = bufferController.getCurrentOnscreenBuffer();
                if (currentScreenBuffer != null &&
                    currentScreenBuffer.makeCurrent(displaySystem, jmeRenderer)) {

                    synchronized (pickLock) {
                        processCommitList(processTime);
                        if (processTime < 0) {
                            //System.out.println("NEED TO ADAPT TO NEGATIVE PROCESS TIME");
                        }
                    }
                    currentScreenBuffer.release();
                }
            }
                       
            // Let the processes know that we want to do a frame tick
            renderManager.triggerNewFrame();

            //System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory());
            //System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
            //System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
          
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
     * get the renderlist from the render techniques, given the render buffer
     */
    FastList<Spatial> getRenderList(RenderBuffer rb) {
        FastList<Spatial> spatialList = null;

        renderList.clear();
        for (int j = 0; j < renderTechniques.size(); j++) {
            RenderTechnique rt = renderTechniques.get(j);
            rt.startFrame(rb);

            spatialList = rt.getSpatials(rb);
            if (spatialList != null) {
                renderList.addAll(renderList.size(), spatialList);
            }
        }

        if (currentSkybox != null) {
            renderList.add(currentSkybox);
        }

        return (renderList);
    }

    /**
     * Get the current passlist
     */
    FastList<PassComponent> getPassList(RenderBuffer rb) {
        return (passList);
    }

    /**
     * Notify the render techniques that this buffer render is done
     */
    void endFrame(RenderBuffer rb) {
        for (int j = 0; j < renderTechniques.size(); j++) {
            renderTechniques.get(j).endFrame(rb);
        }
    }
    /**
     * Populate the render queue by taking the list of spatials
     * and drawing them.
     */
    private void populateRenderQueue(FastList<Spatial> list) {
        for (int i=0; i<list.size(); i++) {
            jmeRenderer.draw(list.get(i));
        }
    }

    /**
     * Merge two sorted lists.  The first is the source, the second is
     * the destination.  The type dictates how to compare.
     */
    private void mergeSpatialList(FastList<Spatial> source, FastList<Spatial> dest, ListType type) {
        int j=0;

        for (int i=0; i<source.size(); i++) {
            Spatial src = source.get(i);
            for (j=0; j<dest.size(); j++) {
                Spatial dst = dest.get(j);
                if (type == ListType.Opaque) {
                    if (src.queueDistance < dst.queueDistance) {
                        continue;
                    }
                } else if (type == ListType.Transparent) {
                    if (src.queueDistance > dst.queueDistance) {
                        continue;
                    }
                } else {
                    if (src.getZOrder() > src.getZOrder()) {
                        continue;
                    }
                }
            }
            if (dest.size() == 0) {
                dest.add(src);
            } else {
                dest.add(j-1, src);
            }
        }
    }

    private void printList(FastList<Spatial> list, ListType type) {
        float value = 0.0f;

        switch (type) {
            case Opaque:
                System.out.println("====================== OPAQUE LIST ==================");
                break;
            case Transparent:
                System.out.println("====================== TRANSPARENT LIST ==================");
                break;
            case Ortho:
                System.out.println("====================== ORTHO LIST ==================");
                break;
        }

        for (int i=0; i<list.size(); i++) {
            if (type == ListType.Ortho) {
                value = list.get(i).getZOrder();
            } else {
                value = list.get(i).queueDistance;
            }
            System.out.println("Spatial: " + list.get(i) + ", " + value);
        }
    }

    /**
     * Add a RenderUpdater to the list of objects to update in the render thread.
     */
    void addRenderUpdater(RenderUpdater ru, Object obj, boolean wait) {
        if (wait && Thread.currentThread() == this) {
            ru.update(obj);
        } else {
            RenderUpdaterOp ruop = new RenderUpdaterOp(ru, obj, wait);
            synchronized (renderUpdateList) {
                renderUpdateList.add(ruop);
            }
            if (wait) {
                while (!ruop.done) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }
     
    /**
     * Change the ortho flag for this render component
     */
    void changeOrthoFlag(RenderComponent rc) {
        synchronized (entityLock) {
            synchronized (orthos) {
                OrthoOp oop = new OrthoOp(rc, rc.getOrtho());
                orthos.add(oop);
                orthosChanged = true;
                entityChanged = true;
            }
        }
    }
           
    /**
     * Change the lighting info for this render component
     */
    void changeLighting(RenderComponent rc) {
        synchronized (entityLock) {
            synchronized (componentLighting) {
                componentLighting.add(rc);
                componentLightingChanged = true;
                entityChanged = true;
            }
        }
    }
    
    /**
     * Process anyone who wants to update in the render thread before rendering
     */
    void processRenderUpdates() {
        RenderUpdaterOp[] rus = null;

        synchronized (renderUpdateList) {
            rus = new RenderUpdaterOp[renderUpdateList.size()];
            for (int i = 0; i < renderUpdateList.size(); i++) {
                rus[i] = (RenderUpdaterOp) renderUpdateList.get(i);
            }
            renderUpdateList.clear();
        }

        for (int i = 0; i < rus.length; i++) {
            try {
                rus[i].updater.update(rus[i].arg);
                if (rus[i].wait) {
                    rus[i].done = true;
                }
            } catch (Exception e) {
                System.out.println("MTGame: Exception Caught in renderer update: " + e);
                e.printStackTrace();
            }
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
                    CollisionComponentOp ccop = (CollisionComponentOp) collisionComponents.get(i);

                    if (ccop.add) {
                        Node node = ccop.cc.getNode();
                        if (node != null) {
                            node.updateGeometricState(referenceTime, true);
                            node.updateRenderState();
                        }
                        ccop.cc.getCollisionSystem().addCollisionComponent(ccop.cc);
                    } else {
                        ccop.cc.getCollisionSystem().removeCollisionComponent(ccop.cc);
                    }
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

        if (cameraUpdateList.size() != 0) {
            for (int i = 0; i < cameraUpdateList.size(); i++) {
                cameraComponent = (CameraComponent) cameraUpdateList.get(i);
                cameraSceneGraph = cameraComponent.getCameraSceneGraph();             
                cameraSceneGraph.updateGeometricState(referenceTime, true);
            }
            cameraUpdateList.clear();
        }
    }
    
    /**
     * Process as many committers as we can, given the amount of process time
     */
    void processCommitList(long processTime) {
        long currentTime = System.nanoTime();
        long elapsedTime = 0;
        long nextCurrentTime = 0;
        ProcessorComponent pc = null;
        
        synchronized (this) {
            if (commitList == null) {
                //System.out.println("Renderer: No Commits");
                return;
            }

            // Note: We won't stop in the middle of a chain
            // TODO: Work on partial commits
            while (/*elapsedTime < processTime &&*/currentCommit != commitList.length) {
                pc = commitList[currentCommit++];
                if (pc.getSwingSafe()) {
                    releaseSwingLock();
                    try {
                        pc.commit(pc.getCurrentTriggerCollection());
                    } catch (Exception e) {
                        System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                        e.printStackTrace();
                    }
                    acquireSwingLock();
                } else {
                    try {
                        pc.commit(pc.getCurrentTriggerCollection());
                    } catch (Exception e) {
                        System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                        e.printStackTrace();
                    }
                }
                pc.clearTriggerCollection();

                // Process the chain
                pc = pc.getNextInChain();
                while (pc != null) {
                    if (pc.getSwingSafe()) {
                        releaseSwingLock();
                        try {
                            pc.commit(pc.getCurrentTriggerCollection());
                        } catch (Exception e) {
                            System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                            e.printStackTrace();
                        }
                        acquireSwingLock();
                    } else {
                        try {
                            pc.commit(pc.getCurrentTriggerCollection());
                        } catch (Exception e) {
                            System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                            e.printStackTrace();
                        }
                    }
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
    }
    
    /**
     * Run the processes component commit list
     * For now, we'll just run them on screen 0
     */
    void runCommitList(ProcessorComponent[] runList) {
        synchronized (this) {
            commitList = runList;
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
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
            if (pc.getSwingSafe()) {
                releaseSwingLock();
                try {
                    pc.commit(pc.getCurrentTriggerCollection());
                } catch (Exception e) {
                    System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                    e.printStackTrace();
                }
                acquireSwingLock();
            } else {
                try {
                    pc.commit(pc.getCurrentTriggerCollection());
                } catch (Exception e) {
                    System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                    e.printStackTrace();
                }
            }
            pc.clearTriggerCollection();
            if (pc.getEntityProcessController() != null) {
                worldManager.armProcessorComponent(pc.getArmingCondition());
            }
            
            // Process the chain
            pc = pc.getNextInChain();
            while (pc != null) {
                pc.compute(pc.getCurrentTriggerCollection());
                if (pc.getSwingSafe()) {
                    releaseSwingLock();
                    try {
                        pc.commit(pc.getCurrentTriggerCollection());
                    } catch (Exception e) {
                        System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                        e.printStackTrace();
                    }
                    acquireSwingLock();
                } else {
                    try {
                        pc.commit(pc.getCurrentTriggerCollection());
                    } catch (Exception e) {
                        System.out.println("MTGame: Exception Caught in renderer commit: " + e);
                        e.printStackTrace();
                    }
                }
                pc.clearTriggerCollection();
                pc = pc.getNextInChain();
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
                    CollisionComponentOp ccop = new CollisionComponentOp((CollisionComponent)c, true);
                    collisionComponents.add(ccop);
                }
            }
                                         
            if (c instanceof SkyboxComponent) {
                synchronized (skyboxes) {
                    Spatial sg = ((SkyboxComponent)c).getSkybox();
                    BlendState bs = (BlendState)sg.getRenderState(RenderState.StateType.Blend);
                    traverseGraph(sg, false, bs);
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
                synchronized (scenes) {
                    processSceneGraph((RenderComponent)c, true);
                    RenderComponentOp rcop = new RenderComponentOp((RenderComponent)c, true);
                    scenes.add(rcop);
                    scenesChanged = true;
                    entityChanged = true;
                }
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
                    CollisionComponentOp ccop = new CollisionComponentOp((CollisionComponent)c, false);
                    collisionComponents.add(ccop);
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
                synchronized (scenes) {
                    processSceneGraph((RenderComponent)c, false);
                    RenderComponentOp rcop = new RenderComponentOp((RenderComponent)c, false);
                    scenes.add(rcop);
                    scenesChanged = true;
                    entityChanged = true;
                }
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
     * Add a geometry lod to track.
     */
    public void addGeometryLOD(GeometryLOD lod) {
        synchronized (geometryLODMap) {
            geometryLODMap.put(lod.getGeometry(), lod);
        }
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
     * Add a global light to the scene
     */
    public void addLight(LightNode light) {
        synchronized (entityLock) {
            synchronized (lights) {
                lights.add(light);
                lightsChanged = true;
                entityChanged = true;
            }
        }
    }
    
    /**
     * Remove a global light from the scene
     */
    public void removeLight(LightNode light) {
        synchronized (entityLock) {
            synchronized (lights) {
                lights.remove(light);
                lightsChanged = true;
                entityChanged = true;
            }
        }
    }
    
    /**
     * Return the number of global Lights
     */
    public int numLights() {
        int num = 0;
        synchronized (entityLock) {
            synchronized (lights) {
                num = lights.size();
            }
        }
        return (num);
    }
    
    /**
     * Get a light at the index specified
     */
    public LightNode getLight(int i) {
        LightNode light = null;
        
        synchronized (entityLock) {
            synchronized (lights) {
                light = (LightNode)lights.get(i);
            }
        }
        return (light);
    }
    
    /**
     * Change the lights settings
     */
    private void processLightsChanged() {
        // The list of lights have changed, reset the light list.
        globalLights.clear();
        for (int i=0; i<lights.size(); i++) {
            globalLights.add(lights.get(i));
        }
        
        // Now go through all the renderScenes and apply the light state
        for (int i=0; i<renderScenes.size(); i++) {
            RenderComponent scene = (RenderComponent) renderScenes.get(i);
            scene.updateLightState(globalLights);
            scene.getSceneRoot().updateRenderState();
        }
    }
    
    /**
     * Check for component lighting changes
     */
    void processComponentLightingChanged() {
        synchronized (componentLighting) {
            for (int i=0; i<componentLighting.size(); i++) {
                RenderComponent rc = (RenderComponent) componentLighting.get(i);
                rc.updateLightState(globalLights);
                rc.getSceneRoot().updateRenderState();
            }
            componentLighting.clear();
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
                if (orthosChanged) {
                    processOrthosChanged();
                    orthosChanged = false;
                }
                if (componentLightingChanged) {
                    processComponentLightingChanged();
                    componentLightingChanged = false;
                }
                if (lightsChanged) {
                    processLightsChanged();
                    lightsChanged = false;
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
            currentSkybox = null;
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

        for (int i = 0; i < scenes.size(); i++) {
            RenderComponentOp rcop = (RenderComponentOp) scenes.get(i);
            if (rcop.add) {
                addToRenderTechnique(rcop.rc);
                renderScenes.add(rcop.rc);
                addToUpdateList(rcop.rc.getSceneRoot());
            } else {
                renderScenes.remove(rcop.rc);
                removeFromRenderTechnique(rcop.rc);
            }
        }
        scenes.clear();
    }

    /**
     * Add the given render component to the appropriate RenderTechnique
     */
    private void addToRenderTechnique(RenderComponent rc) {
        RenderTechnique rt = null;
        int i=0;

        // First find the technique
        for (i=0; i<renderTechniques.size(); i++) {
            rt = renderTechniques.get(i);
            if (rc.getRenderTechniqueName().equals(rt.getName())) {
                rt.addRenderComponent(rc);
                break;
            }
        }

        if (i == renderTechniques.size()) {
            // Load the technique
            try {
                rt = (RenderTechnique)Class.forName(rc.getRenderTechniqueName()).newInstance();
            } catch (InstantiationException e) {
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println(e);
            } catch (IllegalAccessException e) {
                System.out.println(e);
            }

            if (rt != null) {
                renderTechniques.add(rt);
                rt.initialize();
                rt.addRenderComponent(rc);
            }
        }
    }

    /**
     * Add the given render component to the appropriate RenderTechnique
     */
    private void removeFromRenderTechnique(RenderComponent rc) {
        RenderTechnique rt = null;
        int i=0;

        // First find the technique
        for (i=0; i<renderTechniques.size(); i++) {
            rt = renderTechniques.get(i);
            if (rc.getRenderTechniqueName().equals(rt.getName())) {
                rt.removeRenderComponent(rc);
                break;
            }
        }

        if (i == renderTechniques.size()) {
            System.out.println("ERROR: RenderTechnique Not Found");
        }
    }

    private void processGraphAddition(Node node) {
        synchronized (geometryLODMap) {
            processGeometryLod(node, true);
        }
    }

    private void processGraphRemove(Node node) {
        synchronized (geometryLODMap) {
            processGeometryLod(node, false);
        }
    }

    /**
     * Examine this node and travese it's children
     */
    void processGeometryLod(Spatial sg, boolean add) {
        GeometryLOD lod = (GeometryLOD)geometryLODMap.get(sg);
        if (lod != null) {
            if (add) {
                geometryLODs.add(lod);
            } else {
                geometryLODs.remove(lod);
            }
        }

        if (sg instanceof Node) {
            Node node = (Node)sg;
            for (int i=0; i<node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                processGeometryLod(child, add);
            }
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
        len = passList.size();
        for (int i=0; i<len;) {
            pass = (PassComponent) passList.get(i);
            if (passes.contains(pass)) {
                // move on to the next
                i++;
            } else {
                // remove the scene, this will shift things down
                passList.remove(pass);
                len--;
            }
        }
        
        // Now let's look for additions
        for (int i=0; i<passes.size(); i++) {
            pass = (PassComponent) passes.get(i);
            if (!passList.contains(pass)) {
                passList.add(pass);
                addToPassUpdateList(pass.getPass());
            }
        }
        
        // Just a sanity check
        if (passes.size() != passList.size()) {
            System.out.println("Error, Pass sizes differ: " + passes.size() + ", " + passList.size());
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
     * Set the desired frame rate
     */
    public void setMinSamples(int samples) {
        minSamples = samples;
        if (displaySystem != null) {
            displaySystem.setMinSamples(samples);
        }
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
     * Set the BufferController
     */
    void setBufferController(BufferController bc) {
        bufferController = bc;
    }
    
    /**
     * Turn on key input tracking
     */
    void trackKeyInput(Canvas c, Object listener) {
        c.addKeyListener((KeyListener)listener);
    }
    
    /**
     * turn off key input tracking
     */
    void untrackKeyInput(Canvas c, Object listener) {
        c.removeKeyListener((KeyListener)listener);
    }
        
    /**
     * turn on mouse input tracking
     */
    void trackMouseInput(Canvas c, Object listener) {
        c.addMouseListener((MouseListener) listener);
        c.addMouseMotionListener((MouseMotionListener) listener);
        c.addMouseWheelListener((MouseWheelListener) listener);
    }
        
    /**
     * turn off mouse input tracking
     */
    void untrackMouseInput(Canvas c, Object listener) {
        c.removeMouseListener((MouseListener) listener);
        c.removeMouseMotionListener((MouseMotionListener) listener);
        c.removeMouseWheelListener((MouseWheelListener) listener);
    }

    /**
     * Check for ortho changes
     */
    void processOrthosChanged() {
        synchronized (orthos) {
            for (int i=0; i<orthos.size(); i++) {
                OrthoOp oop = orthos.get(i);
                BlendState bs = (BlendState)oop.rc.getSceneRoot().getRenderState(RenderState.StateType.Blend);
                traverseGraph(oop.rc.getSceneRoot(), oop.on, bs);
                addToUpdateList(oop.rc.getSceneRoot());
            }
            orthos.clear();
        }
    }
    
    class MyImplementor extends SimpleCanvasImpl {
        /**
         * The Renderer to notify
         */
        private Renderer mtrenderer = null;
        private RenderBuffer renderBuffer = null;
        private Canvas canvas = null;
        private boolean first = true;

        public MyImplementor(Renderer renderer, RenderBuffer rb, Canvas c, int width, int height) {
            super(width, height);
            this.canvas = c;
            this.mtrenderer = renderer;
            renderBuffer = rb;
        }

        public void simpleSetup() {
            //System.out.println("In simple setup: ");
            mtrenderer.addOnscreenBuffer(renderBuffer);
            BufferUpdater bu = renderBuffer.getBufferUpdater();
            if (bu != null) {
                bu.init(renderBuffer);
            }
        //System.out.println("In simple setup: ");
        }

        public void simpleUpdate() {
            //System.out.println("In simple update: ");
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
     * The jme lock used during graph updates
     */
    Object getJMESGLock() {
        return (jmeSGLock);
    }
    
    /**
     * Do any pre-processing on the scene graph
     */
    void processSceneGraph(RenderComponent sc, boolean add) {
        Node sg = sc.getSceneRoot();

        synchronized (jmeSGLock) {
            if (add) {
                sc.updateLightState(globalLights);
                processGraphAddition(sg);
                if (sc.getAttachPoint() != null) {
                    processAttachPoint(sc, true);
                }
                addToUpdateList(sg);
            } else {
                processGraphRemove(sg);
                if (sc.getAttachPoint() != null) {
                    processAttachPoint(sc, false);
                }
            }

            BlendState bs = (BlendState)sg.getRenderState(RenderState.StateType.Blend);
            traverseGraph(sg, sc.getOrtho(), bs);
        }
    }
    
    /**
     * Examine this node and travese it's children
     */
    void traverseGraph(Spatial sg, boolean ortho, BlendState bs) {
   
        examineSpatial(sg, ortho, bs);
        if (sg instanceof Node) {
            Node node = (Node)sg;
            for (int i=0; i<node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                BlendState cbs = (BlendState)child.getRenderState(RenderState.StateType.Blend);
                if (cbs == null) {
                    traverseGraph(child, ortho, bs);
                } else {
                    traverseGraph(child, ortho, cbs);
                }
            }
        }
    }
    
    /**
     * Examine the given node
     */
    void examineSpatial(Spatial s, boolean ortho, BlendState bs) {
        setRenderQueue(s, ortho, bs);
    }
    
    /**
     * This mehod checks for transpaency attributes
     */
    void setRenderQueue(Spatial s, boolean ortho, BlendState bs) {
        if (ortho) {
            s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_ORTHO);
        } else {
            if (bs != null) {
                if (bs.isBlendEnabled()) {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_TRANSPARENT);
                } else {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
                }
            } else {
                s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
            }
        }
    }
}
