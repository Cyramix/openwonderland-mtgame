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
     * The array list of orthographic projection render components waiting
     * to have their status changed
     */
    private ArrayList orthos = new ArrayList();
    
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
     * The list of Canvases to render into
     */
    private ArrayList canvasList = new ArrayList();
    private boolean canvasChanged = false;
    private Canvas[] renderCanvases = new Canvas[0];
        
    /**
     * The list of Buffers to render into
     */
    private ArrayList bufferList = new ArrayList();
    private boolean buffersChanged = false;
    private RenderBuffer[] offscreenRenderList = new RenderBuffer[0];
    
    /**
     * The current rendering canvas
     */
    private GLCanvas currentCanvas = null;
    private Canvas currentAWTCanvas = null;
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
    Canvas createCanvas(RenderBuffer rb) {
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
         
        return ((Canvas)canvas);
    }

    
    void addRenderBuffer(RenderBuffer rb) {
        if (!initialized) {
            initialize();     
        }
        
        switch (rb.getTarget()) {
            case ONSCREEN:
                rb.setCanvas(createCanvas(rb));
                break;
            case TEXTURE_1D:
            case TEXTURE_2D:
            case TEXTURE_CUBEMAP:
            case SHADOWMAP:
                //createTextureRenderer(rb);
                break; 
        }
        synchronized (bufferList) {
            bufferList.add(rb);
            buffersChanged = true;
        }
    }
    
    RenderBuffer findRenderBuffer(Canvas c) {
        RenderBuffer rb = null;
        
        synchronized (bufferList) {
            for (int i=0; i<bufferList.size(); i++) {
                rb = (RenderBuffer) bufferList.get(i);
                if (rb.getCanvas() == c) {
                    break;
                }
            }
        }
        return (rb);
    }
    
    boolean setCurrentCanvas(Canvas canvas) {
        boolean doRender = true;
        GL gl = null;
        
        currentAWTCanvas = canvas;
        if (useJOGL) {
            currentCanvas = (GLCanvas) canvas;
            glContext = currentCanvas.getContext();
            try {
                glContext.makeCurrent();
            } catch (javax.media.opengl.GLException e) {
                System.out.println(e);
            }
            gl = glContext.getGL();
            if (displaySystem.getMinSamples() == 0) {
                gl.glDisable(GL.GL_MULTISAMPLE);
            } else {
                gl.glEnable(GL.GL_MULTISAMPLE);
            }
        } else {
            //((LWJGLDisplaySystem)displaySystem).setCurrentCanvas((JMECanvas) currentCanvas);
            ((LWJGLDisplaySystem) displaySystem).switchContext(currentCanvas);
        }
        
        RenderBuffer rb = findRenderBuffer(canvas); 
        CameraComponent cc = rb.getCameraComponent();
        if (cc != null) {
            Camera camera = cc.getCamera();
            if (rb.getWidth() != canvas.getWidth() ||
                rb.getHeight() != canvas.getHeight()) {
                rb.setWidth(canvas.getWidth());
                rb.setHeight(canvas.getHeight());
                camera.resize(canvas.getWidth(), canvas.getHeight());
            }
            camera.update();
            jmeRenderer.setCamera(camera);
            rb.getBackgroundColor(bgColor);
            jmeRenderer.setBackgroundColor(bgColor);
        } else {
            doRender = false;
        }
        return (doRender);
    }
    
    void releaseCurrentCanvas() {
        // Release the AWT lock
        glContext.release();    
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
        //lwjglDisplay = (LWJGLDisplaySystem) displaySystem;
        //joglDisplay = (JOGLDisplaySystem) displaySystem;
        } catch (JmeException e) {
            System.out.println(e);
        }
        initialized = true;
    }
    
    void addCanvas(Canvas c) {
        synchronized (bufferList) {
            synchronized (canvasList) {
                if (jmeRenderer == null) {
                    jmeRenderer = displaySystem.getRenderer();
                }
                canvasList.add(c);
                canvasChanged = true;
                buffersChanged = true;
            }
        }
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
     * Update the list of canvases for rendering
     */
    void updateCanvasList() {
        renderCanvases = new Canvas[canvasList.size()];
        for (int i=0; i<canvasList.size(); i++) {
            renderCanvases[i] = (Canvas) canvasList.get(i);
        }
    }
    
    /**
     * Update the list of offscreen buffers to render into
     */
    void updateOffscreenList() {
        int numOffscreenBuffers = 0;
        int index = 0;
        RenderBuffer rb = null;

        for (int i = 0; i < bufferList.size(); i++) {
            rb = (RenderBuffer) bufferList.get(i);
            if (rb.getTexture() != null) {
                numOffscreenBuffers++;
            }
        }
                
        offscreenRenderList = new RenderBuffer[numOffscreenBuffers];
        for (int i = 0; i < bufferList.size(); i++) {
            rb = (RenderBuffer) bufferList.get(i);
            if (rb.getTexture() != null) {
                offscreenRenderList[index++] = rb;
            }
        }
    }
    
    /**
     * Check for canvas and buffer changes
     */
    void checkForRenderBuffers() {
        synchronized (bufferList) {
            synchronized (canvasList) {
                if (canvasChanged) {
                    updateCanvasList();
                    canvasChanged = false;
                }
            }
            if (buffersChanged) {
                updateOffscreenList();
                buffersChanged = false;
            }
        }
    }
    
    /**
     * Process all buffer and entity updates
     */
    void processEntityUpdates() {
        checkForRenderBuffers();
        synchronized (pickLock) {
            checkForEntityChanges();
        }     
    }
    
    /**
     * Give the offscreen buffers a chance to initialize/reset
     */
    void processBufferUpdates() {
        RenderBuffer rb = null;
        
        for (int i = 0; i < offscreenRenderList.length; i++) {
            rb = (RenderBuffer) offscreenRenderList[i];
            rb.update(displaySystem, currentSkybox, renderScenes);
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
        processBufferUpdates();

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
    
    /**
     * Render to all the offscreen buffers
     */
    boolean renderBuffers() {
        RenderBuffer rb = null;
        
        if (offscreenRenderList.length == 0) {
            return (false);
        }
        
        for (int i = 0; i < offscreenRenderList.length; i++) {
            rb = (RenderBuffer) offscreenRenderList[i];
            rb.render(this);
            if (rb.getRenderUpdater() != null) {
                rb.getRenderUpdater().update(rb);
            }
        }    
        
        return (true);
    }
    
    /**
     * This renders the scene to the current canvas
     */
    void renderScene(ArrayList excludeList) {
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
            if (excludeList == null || !excludeList.contains(sceneRoot)) {                
                jmeRenderer.draw(sceneRoot);
            }
        }

        for (int i = 0; i < renderPasses.size(); i++) {
            PassComponent pc = (PassComponent) renderPasses.get(i);
            pc.getPass().renderPass(jmeRenderer);
        }

        // This actually does the rendering
        jmeRenderer.renderQueue();     
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
     * Create the jmeCamera
     */
    Camera createJMECamera(int width, int height) {  
        return (jmeRenderer.createCamera(width, height));   
    }
    
    void swapAndWait(long time) {
        currentCanvas.swapBuffers();

        if (time != 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
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

            processEntityUpdates();

            /* 
             * This block of code handles calling entity processes which are
             * locked to the renderer - like the current camera.
             */
            runProcessorsTriggered();
                
            for (int i=0; i<renderCanvases.length; i++) {
                Canvas canvas = renderCanvases[i];
                if (setCurrentCanvas(canvas)) {
                    processJMEUpdates(totalTime / 1000000000.0f);
                    if (renderBuffers()) {
                        // If we rendered a buffer, need to reset the camera
                        setCurrentCanvas(canvas);
                    }
                    renderScene(null);
                    swapAndWait(0);
                }
                releaseCurrentCanvas();
            }
            
            /*
             * Now we track some times, and process the commit lists
             */
            
            // Snapshot the time it took to render
            renderTime = System.nanoTime() - frameStartTime;
            //Calculate the amount of time left to process commits
            processTime = desiredFrameTime - renderTime;
            
            // Process the commit list
                            
            for (int i = 0; i < renderCanvases.length; i++) {
                Canvas canvas = renderCanvases[i];
                setCurrentCanvas(canvas);
                synchronized (pickLock) {
                    processCommitList(processTime);
                    if (processTime < 0) {
                        //System.out.println("NEED TO ADAPT TO NEGATIVE PROCESS TIME");
                    }
                }
                releaseCurrentCanvas();
            }
                       
            // Let the processes know that we want to do a frame tick
            renderManager.triggerNewFrame();
          
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
     * Change the ortho flag for this render component
     */
    void changeOrthoFlag(RenderComponent rc) {
        synchronized (entityLock) {
            synchronized (orthos) {
                orthos.add(rc);
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
        RenderUpdater[] rus = null;
        Object[] objs = null;

        synchronized (renderUpdateList) {
            rus = new RenderUpdater[renderUpdateList.size()];
            objs = new Object[renderUpdateList.size()];
            for (int i = 0; i < renderUpdateList.size(); i++) {
                rus[i] = (RenderUpdater) renderUpdateList.get(i);
                objs[i] = (Object) renderUpdateArgs.get(i);
            }
            renderUpdateList.clear();
            renderUpdateArgs.clear();
        }

        for (int i = 0; i < rus.length; i++) {
            try {
                rus[i].update(objs[i]);
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
            worldManager.armProcessorComponent(pc.getArmingCondition());
            
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
                if (((RenderComponent) c).getAttachPoint() != null) {
                    processAttachPoint((RenderComponent) c, false);
                } else {
                    scenes.remove(c);
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
                processGraphRemove(scene.getSceneRoot());
                len--;
            }
        }
      
        // Now let's look for additions
        for (int i=0; i<scenes.size(); i++) {
            scene = (RenderComponent) scenes.get(i);
            if (!renderScenes.contains(scene)) {
                scene.updateLightState(globalLights);
                processGraphAddition(scene.getSceneRoot());
                renderScenes.add(scene);
                addToUpdateList(scene.getSceneRoot());
            }
        }
        
        // Just a sanity check
        if (scenes.size() != renderScenes.size()) {
            System.out.println("Error, Scene sizes differ: " + scenes.size() + ", " + renderScenes.size()); 
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
                RenderComponent rc = (RenderComponent) orthos.get(i);
                processSceneGraph(rc);
                addToUpdateList(rc.getSceneRoot());
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
            mtrenderer.addCanvas(canvas);
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
     * Do any pre-processing on the scene graph
     */
    void processSceneGraph(RenderComponent sc) {
        Node sg = sc.getSceneRoot();
        traverseGraph(sg, sc.getOrtho());
    }
    
    /**
     * Examine this node and travese it's children
     */
    void traverseGraph(Spatial sg, boolean ortho) {
   
        examineSpatial(sg, ortho);
        if (sg instanceof Node) {
            Node node = (Node)sg;
            for (int i=0; i<node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                traverseGraph(child, ortho);
            }
        }
    }
    
    /**
     * Examine the given node
     */
    void examineSpatial(Spatial s, boolean ortho) {
        setRenderQueue(s, ortho);
    }
    
    /**
     * This mehod checks for transpaency attributes
     */
    void setRenderQueue(Spatial s, boolean ortho) {
        BlendState blendState = (BlendState) s.getRenderState(RenderState.RS_BLEND);
        if (ortho) {
            s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_ORTHO);
        } else {
            if (blendState != null) {
                if (blendState.isBlendEnabled()) {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_TRANSPARENT);
                } else {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
                }
            } else {
                s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_INHERIT);
            }
        }
    }
}
