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

package org.jdesktop.mtgame.test;

import org.jdesktop.mtgame.processor.LightCameraProcessor;
import org.jdesktop.mtgame.processor.MouseSelectionProcessor;
import org.jdesktop.mtgame.processor.SelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.LightNodeRotator;
import org.jdesktop.mtgame.processor.TransScaleProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.processor.FPSCameraProcessor;
import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import org.jdesktop.mtgame.shader.DiffuseMap;
import org.jdesktop.mtgame.util.Mirror;
import org.jdesktop.mtgame.util.RenderCapture;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.CameraNode;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.light.DirectionalLight;
import com.jme.light.Light;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.light.LightNode;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Sphere;
import com.jme.scene.Geometry;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.scene.Line;
import com.jme.math.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;
import javax.swing.JFileChooser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import com.jmex.model.collada.ColladaImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.ResourceLocator;

import java.util.Random;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.FloatBuffer;
import java.io.FileInputStream;
import com.jme.scene.TexCoords;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.scene.Skybox;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import com.jme.scene.Spatial.TextureCombineMode;

import com.jmex.model.converters.X3dToJme;
import java.util.HashMap;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class OrientationWorld implements WorldManager.ConfigLoadListener {
    /**
     * The WorldManager for this world
     */
    WorldManager wm = null;
    
    /**
     * The CameraNode
     */
    private CameraNode cameraNode = null;
    
    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 60;
    
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    private float aspect = 800.0f/600.0f;
    
    /**
     * Some options state variables
     */
    private boolean coordsOn = true;
    private boolean gridOn = true;
    
    /**
     * The width of the grid
     */
    private int gridWidth = 250;
    
    /**
     * The entity which represents the grid
     */
    private Entity grid = new Entity("Grid");
    
    /**
     * The Entity which represents the axis
     */
    private Entity axis = new Entity("Axis");
    
    /**
     * A list of the models we are looking at
     */
    private ArrayList models = new ArrayList();
    private LightNode lightNode = null;
        
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    private JMECollisionSystem collisionSystem = null;
    
    private SwingFrame frame = null;
    private String assetDir = "/Users/runner/Desktop/models/OrientationWorld/";
    private Skybox skybox = null;
    private RenderCapture renderCapture = null;
    private boolean doCapture = false;

    public OrientationWorld(String[] args) {
        wm = new WorldManager("TestWorld");
              
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        collisionSystem = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        
        LightNodeRotator rp = new LightNodeRotator("Light Rotator", wm,
                lightNode, null, new Vector3f(0, 0, 100), (float) (1.0f * Math.PI / 180.0f));
        Entity e = new Entity("Light Rotator");
        e.addComponent(RotationProcessor.class, rp);
        //wm.addEntity(e);
        
        createUI(wm);  
        createCameraEntity(wm);  
        //setGlobalLights();
        createLights();
        createSkybox(wm);

        try {
            FileInputStream fs = new FileInputStream(assetDir + "OrientationWorld.mtg");
            wm.setConfigBaseURL("file:/Users/runner/Desktop/models/");
            wm.loadConfiguration(fs, this);
        } catch (java.io.FileNotFoundException ex) {
            System.out.println(ex);
        }

        ConfigInstance ci[] = wm.getAllConfigInstances();
        for (int i=0; i<ci.length; i++) {
            wm.addEntity(ci[i].getEntity());
        }
        //createMirror();
    }

    public void configLoaded(ConfigInstance ci) {
        //System.out.println("------------- > Loaded: " + ci.getSceneGraph());
        //addModel(ci.getSceneGraph());
    }

    private void createLights() {
        Light[] lights = new Light[3];

        float radius = 75.0f;
        float lheight = 30.0f;
        float x = (float)(radius*Math.cos(Math.PI/6));
        float z = (float)(radius*Math.sin(Math.PI/6));
        lights[0] = createDirLight(x, lheight, z);
        x = (float)(radius*Math.cos(5*Math.PI/6));
        z = (float)(radius*Math.sin(5*Math.PI/6));
        lights[1] = createDirLight(x, lheight, z);
        x = (float)(radius*Math.cos(3*Math.PI/2));
        z = (float)(radius*Math.sin(3*Math.PI/2));
        lights[2] = createDirLight(x, lheight, z);

        LightProcessor lp = new LightProcessor(wm, lights, 20.0f, 0.6f, 0.0f, 0.7f);
        Entity e = new Entity("Light Processor");
        e.addComponent(LightProcessor.class, lp);
        //wm.addEntity(e);
    }

    private Light createLight(float x, float y, float z) {
        lightNode = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        light.setSpecular(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        //light.setLightMask(LightState.MASK_DIFFUSE);
        //light.setLocation(new Vector3f(10, 100, 10));
        light.setEnabled(true);
        lightNode.setLight(light);
        lightNode.setLocalTranslation(x, y, z);
        wm.getRenderManager().addLight(lightNode);

        Sphere sp = new Sphere("", 10, 10, 1.0f);
        Node n = new Node("");
        n.setLocalTranslation(x, y, z);
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        n.setRenderState(buf);
        n.attachChild(sp);
        RenderComponent lsp = wm.getRenderManager().createRenderComponent(n);
        lsp.setLightingEnabled(false);

        LightNodeRotator rp = new LightNodeRotator("Light Rotator", wm,
                lightNode, n, new Vector3f(0, 50, 50), (float) (1.0f * Math.PI / 180.0f));
        Entity e = new Entity("Light Rotator");
        //e.addComponent(RotationProcessor.class, rp);
        e.addComponent(RenderComponent.class, lsp);
        wm.addEntity(e);

        return (light);
    }

    private Light createDirLight(float x, float y, float z) {
        lightNode = new LightNode();
        DirectionalLight light = new DirectionalLight();
        light.setDiffuse(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        light.setSpecular(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        light.setEnabled(true);
        lightNode.setLight(light);
        lightNode.setLocalTranslation(x, y, z);
        light.setDirection(new Vector3f(-x, -y, -z));
        wm.getRenderManager().addLight(lightNode);
        return (light);
    }

    class LightProcessor extends ProcessorComponent {
        /**
         * The WorldManager - used for adding to update list
         */
        private WorldManager worldManager = null;

        /**
         * The Time it takes to do a full cycle
         */
        private float cycleTime = 0.0f;

        /**
         * The increment each frame
         */
        private float currentLight = 0.0f;
        private ColorRGBA diffuse = new ColorRGBA();
        private float increment = 0.01f;
        private float low = 0.0f;
        private float high = 0.0f;

        /**
         * The light targets
         */
        private Light[] targets = null;

        /**
         * The constructor
         */
        public LightProcessor(WorldManager worldManager, Light[] targets, float cycleTime, float currentLight,
                float low, float high) {
            this.worldManager = worldManager;
            this.targets = targets;
            this.cycleTime = cycleTime;
            this.currentLight = currentLight;
            this.low = low;
            this.high = high;
            increment = (high - low)/(60.0f * cycleTime);
            //System.out.println("Increment is : " + increment);
            setArmingCondition(new NewFrameCondition(this));
        }

        /**
         * The initialize method
         */
        public void initialize() {
            //setArmingCondition(new NewFrameCondition(this));
        }

        /**
         * The Calculate method
         */
        public void compute(ProcessorArmingCollection collection) {
            currentLight += increment;
            if (currentLight > high) {
                currentLight = high;
                increment = -increment;
            }

            if (currentLight < low) {
                currentLight = low;
                increment = -increment;
            }
        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            diffuse.set(currentLight, currentLight, currentLight, 1.0f);
            for (int i=0; i<targets.length; i++) {
                targets[i].setDiffuse(diffuse);
            }
        }
    }

    void createMirror() {
        Vector3f trans = new Vector3f(0.0f, 20.0f, 0.0f);
        Vector3f side = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f dir = new Vector3f(0.0f, -1.0f, 0.0f);

        Mirror mirror = new Mirror(wm, 25, 25, trans, dir, up, side, 1024, 1024);
    }

    void loadViaX3D(String filename, boolean normalMap, Vector3f trans,
            Vector3f scale, boolean enableLighting, boolean manipulate) {
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(filename);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

        HashMap texData = new HashMap();
        X3dToJme loader = null;
        try {
            loader = new X3dToJme();
        } catch (InstantiationException ie) {
            System.out.println(ie);
        }

        Spatial scene = null;
        try {
            scene = loader.loadScene(fileStream, texData, null);
        } catch (Exception e) {
            System.out.println(e);
        }
        wm.applyConfig(scene);

        frame.parseModel(0, scene, null);

        scene.setLocalTranslation(trans);
        scene.setLocalScale(scale);
        frame.addModel((Node)scene, enableLighting, manipulate);
    }

    public void setGlobalLights() {
        LightNode globalLight1 = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setSpecular(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setEnabled(true);
        globalLight1.setLight(light);
        globalLight1.setLocalTranslation(0.0f, 500.0f, 500.0f);

        LightNode globalLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setSpecular(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setEnabled(true);
        globalLight2.setLight(light);
        globalLight2.setLocalTranslation(0.0f, -500.0f, -500.0f);
        wm.getRenderManager().addLight(globalLight1);
        wm.getRenderManager().addLight(globalLight2);
    }
    
    private void createSkybox(WorldManager wm) {
        Texture north = null;
        Texture south = null;
        Texture east = null;
        Texture west = null;
        Texture up = null;
        Texture down = null;
        skybox = new Skybox("skybox", 500, 500, 500);
        String dir = "file:" + assetDir + "../jME/";
        
        try {
        URL url = new URL(dir + "1.jpg");
        north = TextureManager.loadTexture(url,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        url = new URL(dir + "3.jpg");
        south = TextureManager.loadTexture(url,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        url = new URL(dir + "2.jpg");
        east = TextureManager.loadTexture(url,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        url = new URL(dir + "4.jpg");
        west = TextureManager.loadTexture(url,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        url = new URL(dir + "6.jpg");
        up = TextureManager.loadTexture(url,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        url = new URL(dir + "5.jpg");
        down = TextureManager.loadTexture(url,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        skybox.setTexture(Skybox.Face.North, north);
        skybox.setTexture(Skybox.Face.West, west);
        skybox.setTexture(Skybox.Face.South, south);
        skybox.setTexture(Skybox.Face.East, east);
        skybox.setTexture(Skybox.Face.Up, up);
        skybox.setTexture(Skybox.Face.Down, down);

        CullState cullState = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cullState.setEnabled(true);
        skybox.setRenderState(cullState);

        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        //zState.setEnabled(false);
        skybox.setRenderState(zState);

        skybox.setLightCombineMode(Spatial.LightCombineMode.Off);
        skybox.setCullHint(Spatial.CullHint.Never);
        skybox.setTextureCombineMode(TextureCombineMode.Replace);
        skybox.updateRenderState();

        skybox.lockBounds();        
        
        Entity e = new Entity("Skybox");
        SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
        e.addComponent(SkyboxComponent.class, sbc);

        wm.addEntity(e);
    }

    void createAxis() {
        Entity e = new Entity("Axis");
        Node axis = new Node("Axis");

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        axis.setRenderState(buf);

        axis.attachChild(new AxisRods("Axis", true, 10.0f, 0.2f));
        RenderComponent sbc = wm.getRenderManager().createRenderComponent(axis);
        e.addComponent(RenderComponent.class, sbc);
        wm.addEntity(e);
    }

    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        rb.setCameraComponent(cc);
        camera.addComponent(CameraComponent.class, cc);

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        FPSCameraProcessor eventProcessor = new FPSCameraProcessor(cameraListener, cameraNode, wm, camera, true, false, renderCapture);
        //OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        //LightCameraProcessor eventProcessor = new LightCameraProcessor(cameraListener, cc.getCamera(), wm, camera);
        eventProcessor.setRunInRenderer(true);
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(eventProcessor);



        //AWTInputComponent selectionListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        //MouseSelectionProcessor selector = new MouseSelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
        //pcc.addProcessor(selector);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);
        
        wm.addEntity(camera);
    }
    
    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");        
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);        
        return (cameraSG);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OrientationWorld worldBuilder = new OrientationWorld(args);
        
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }
        }
    }
    
    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {             
        frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }
    
    class SwingFrame extends JFrame implements FrameRateListener, ActionListener, ResourceLocator, RenderCapture.RenderCaptureListener {

        JPanel contentPane;
        JPanel menuPanel = new JPanel();
        JPanel canvasPanel = new JPanel();
        JPanel capturePanel = new JPanel();
        MyCanvas captureCanvas = null;
        JPanel optionsPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        JLabel fpsLabel = new JLabel("FPS: ");
        
        JToggleButton coordButton = new JToggleButton("Coords", true);
        JToggleButton gridButton = new JToggleButton("Grid", true);
        JMenuItem loadItem = null;
        JMenuItem exitItem = null;
        JMenuItem createTeapotItem = null;
        String textureSubdir = "file:" + assetDir + "";
        String textureSubdirName = assetDir + "";
        int normalIndex = 0;

        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());

            // The Menu Bar
            menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JMenuBar menuBar = new JMenuBar();
            
            // File Menu
            JMenu fileMenu = new JMenu("File");
            exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(this);
            loadItem = new JMenuItem("Load");
            loadItem.addActionListener(this);
            fileMenu.add(loadItem);
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);
            
            menuPanel.add(menuBar);
            contentPane.add(menuPanel, BorderLayout.NORTH);
            
            // The Rendering Canvas
            rb = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, width, height);
            wm.getRenderManager().addRenderBuffer(rb);
            canvas = ((OnscreenRenderBuffer)rb).getCanvas();
            canvas.setVisible(true);
            canvas.setBounds(0, 0, width, height);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());           
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);

            if (doCapture) {
                renderCapture = new RenderCapture(wm, 400, 300, this);
                captureCanvas = new MyCanvas(renderCapture);
                captureCanvas.setSize(400, 300);
                capturePanel.setLayout(new GridBagLayout());
                capturePanel.add(captureCanvas);
                contentPane.add(capturePanel, BorderLayout.EAST);
            }
            
            // The options panel
            optionsPanel.setLayout(new GridBagLayout());
            
            coordButton.addActionListener(this);
            optionsPanel.add(coordButton);
          
            gridButton.addActionListener(this);
            optionsPanel.add(gridButton);
            
            contentPane.add(optionsPanel, BorderLayout.WEST);
            
            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
            
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, this);
        }

        public void update(BufferedImage image) {
            captureCanvas.repaint();
        }

        public class MyCanvas extends Canvas {
            private RenderCapture renderCapture = null;

            public MyCanvas(RenderCapture rc) {
                renderCapture = rc;
            }

            public void paint(Graphics g) {
                BufferedImage bi = renderCapture.getBufferedImage();
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 400, 300);
                if (bi != null) {
                    g.drawImage(bi, 0, 0, null);
                }
            }
        }
        
        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }
        
        public URL locateResource(String resourceName) {
            URL url = null;

            //System.out.println("Looking for: " + resourceName);
            try {
                if (resourceName.contains(textureSubdirName)) {
                    // We already resolved this one.
                    url = new URL("file:" + resourceName);
                } else {
                    url = new URL(textureSubdir + resourceName);
                }
                //System.out.println("TEXTURE: " + url);
            } catch (MalformedURLException e) {
                System.out.println(e);
            }

            return (url);
        }
        
        /**
         * Add a model to be visualized
         */
        private void addModel(Node model, boolean enableLighting, boolean manipulate) {
            Node modelRoot = new Node("Model");
                    
            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            modelRoot.setRenderState(buf);
            
            //System.out.println("Adding: " + model);
            modelRoot.attachChild(model);
            models.add(modelRoot);
            
            Entity e = new Entity("Model");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);
            sc.setLightingEnabled(enableLighting);
            JMECollisionComponent cc = collisionSystem.createCollisionComponent(modelRoot);
            e.addComponent(JMECollisionComponent.class, cc);
            e.addComponent(RenderComponent.class, sc);

            if (manipulate) {
                int eventMask = InputManager.KEY_EVENTS;
                AWTInputComponent l = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
                TransScaleProcessor p = new TransScaleProcessor(l, wm, modelRoot);
                e.addComponent(TransScaleProcessor.class, p);
            }
            wm.addEntity(e);              
        }
    
        /**
         * The method which gets the state change from the buttons
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == coordButton) {
                if (coordsOn) {
                    coordsOn = false;
                    System.out.println("Turning Coordinates Off");
                } else {
                    coordsOn = true;
                    System.out.println("Turning Coordinates On");
                }
                renderCapture.setEnable(coordsOn);
            }
            
            if (e.getSource() == gridButton) {
                if (gridOn) {
                    gridOn = false;
                    System.out.println("Turning Grid Off");
                } else {
                    gridOn = true;
                    System.out.println("Turning Grid On");
                }
            }
            
            if (e.getSource() == exitItem) {
                System.exit(1);
            }
        }
        
        void loadFile(String filename, boolean normalMap, Vector3f trans, Vector3f scale, boolean enableLighting,
                boolean manipulate) {
            FileInputStream fileStream = null;
            
            System.out.println("You chose to open this file: " + filename);
            try {
                fileStream = new FileInputStream(filename);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            }

            // Now load the model
            ColladaImporter.load(fileStream, "Model");
            Node model = ColladaImporter.getModel();
            wm.applyConfig(model);
            
            int normalCount = countNormals(model, 0);
            System.out.println("Number of NORMALS: " + normalCount);
            Vector3f[] lineData = new Vector3f[normalCount*2]; 
            normalIndex = 0;
            parseModel(0, model, lineData);
            Line normalGeometry = new Line("Normal Geometry", lineData, null, null, null);
            //FlatShader shader = new FlatShader(wm);
            //shader.applyToGeometry(normalGeometry);
            Node normalNode = new Node();
            normalNode.attachChild(normalGeometry);
            model.setLocalTranslation(trans);
            model.setLocalScale(scale);
            addModel(model, enableLighting, manipulate);
            //addModel(normalNode);
        }
        
        int countNormals(Spatial model, int currentCount) {
            if (model instanceof Node) {
                Node n = (Node) model;
                for (int i = 0; i < n.getQuantity(); i++) {
                    currentCount = countNormals(n.getChild(i), currentCount);
                }
            } else if (model instanceof Geometry) {
                Geometry geo = (Geometry)model;
                currentCount += geo.getVertexCount();
            }
            return (currentCount);
        }
        
        void parseModel(int level, Spatial model, Vector3f[] lineData) {
            if (model instanceof Node) {
                Node n = (Node)model;
                for (int i=0; i<n.getQuantity(); i++) {
                    parseModel(level+1, n.getChild(i), lineData);
                }
            } else if (model instanceof Geometry) {
                Geometry geo = (Geometry)model;
                System.out.println("FOUND GEOMETRY: " + geo.getName()); 

                if (lineData != null) {
                    FloatBuffer nBuffer = geo.getNormalBuffer();
                    FloatBuffer vBuffer = geo.getVertexBuffer();
                    vBuffer.rewind();
                    nBuffer.rewind();
                    float nScale = 2.0f;
                    for (int i = 0; i < geo.getVertexCount(); i++) {
                        lineData[normalIndex] = new Vector3f();
                        lineData[normalIndex].x = vBuffer.get();
                        lineData[normalIndex].y = vBuffer.get();
                        lineData[normalIndex].z = vBuffer.get();
                        lineData[normalIndex + 1] = new Vector3f();
                        lineData[normalIndex + 1].x = lineData[normalIndex].x + nScale * nBuffer.get();
                        lineData[normalIndex + 1].y = lineData[normalIndex].y + nScale * nBuffer.get();
                        lineData[normalIndex + 1].z = lineData[normalIndex].z + nScale * nBuffer.get();
                        normalIndex += 2;
                    }
                }
            }
        }
    }
}
