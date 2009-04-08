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

package org.jdesktop.mtgame.test;

import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.light.LightNode;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Sphere;
import com.jme.scene.shape.Box;
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

import java.util.Random;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class LightTest {
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
    private LightNode globalLight1 = null;
    private LightNode globalLight2 = null;
    private LightNode localLight1 = null;
    private LightNode localLight2 = null;
    private RenderComponent localLightRC = null;
    private Node lls2 = null;
    private Node gls2 = null;
        
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    
    public LightTest(String[] args) {
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        
        createUI(wm);  
        createCameraEntity(wm);   
        createGrid(wm);
        wm.addEntity(grid);
        createAxis();
        wm.addEntity(axis);
        createLights();
        createTeapots();
       
    }
    
    private void createLights() {    
        MyLightProcessor lp = null;
        Entity e = null;
        Node lightSphere = null;
        RenderComponent src = null;
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
  
        globalLight1 = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.0f, 0.0f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        globalLight1.setLight(light);
        globalLight1.setLocalTranslation(0.0f, 0.0f, 50.0f);
        lightSphere = createLightSphere(new ColorRGBA(0.75f, 0.0f, 0.0f, 1.0f),
                new Vector3f(0.0f, 0.0f, 50.0f));
        src = wm.getRenderManager().createRenderComponent(lightSphere);
        src.setLightingEnabled(false);
        lp = new MyLightProcessor(wm, globalLight1, lightSphere, (float) (1.0f * Math.PI / 180.0f),
                0.0f, 0.0f, 50.0f);
        e = new Entity("Light1");
        e.addComponent(MyLightProcessor.class, lp);
        e.addComponent(RenderComponent.class, src);
        wm.addEntity(e);

        globalLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.0f, 0.75f, 0.0f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        globalLight2.setLight(light);
        globalLight2.setLocalTranslation(0.0f, 0.0f, -50.0f);
        gls2 = createLightSphere(new ColorRGBA(0.0f, 0.75f, 0.0f, 1.0f),
                new Vector3f(0.0f, 0.0f, -50.0f));
        src = wm.getRenderManager().createRenderComponent(gls2);
        src.setLightingEnabled(false);
        lp = new MyLightProcessor(wm, globalLight2, gls2, (float) (1.0f * Math.PI / 180.0f),
                0.0f, 0.0f, -50.0f);
        e = new Entity("Light1");
        e.addComponent(MyLightProcessor.class, lp);
        e.addComponent(RenderComponent.class, src);
        wm.addEntity(e);

        localLight1 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.0f, 0.0f, 0.75f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        localLight1.setLight(light);
        localLight1.setLocalTranslation(50.0f, 0.0f, 0.0f);
        lightSphere = createLightSphere(new ColorRGBA(0.0f, 0.0f, 0.75f, 1.0f),
                new Vector3f(50.0f, 0.0f, 0.0f));
        src = wm.getRenderManager().createRenderComponent(lightSphere);
        src.setLightingEnabled(false);
        lp = new MyLightProcessor(wm, localLight1, lightSphere, (float) (1.0f * Math.PI / 180.0f),
                50.0f, 0.0f, 0.0f);
        e = new Entity("Light1");
        e.addComponent(MyLightProcessor.class, lp);
        e.addComponent(RenderComponent.class, src);
        wm.addEntity(e);

        localLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.0f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        localLight2.setLight(light);
        localLight2.setLocalTranslation(-50.0f, 0.0f, 0.0f);
        lls2 = createLightSphere(new ColorRGBA(0.75f, 0.75f, 0.0f, 1.0f),
                new Vector3f(-50.0f, 0.0f, 0.0f));
        src = wm.getRenderManager().createRenderComponent(lls2);
        src.setLightingEnabled(false);
        lp = new MyLightProcessor(wm, localLight2, lls2, (float) (1.0f * Math.PI / 180.0f),
                -50.0f, 0.0f, 0.0f);
        e = new Entity("Light1");
        e.addComponent(MyLightProcessor.class, lp);
        e.addComponent(RenderComponent.class, src);
        wm.addEntity(e);
        
        wm.getRenderManager().addLight(globalLight1);
        wm.getRenderManager().addLight(globalLight2);
    }
    
    private Node createLightSphere(ColorRGBA color, Vector3f pos) {
        Node node = new Node();
        Sphere sphere = new Sphere("Light", 10, 10, 1.0f);
        node.attachChild(sphere);
        sphere.setSolidColor(color);
        return (node);
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
        OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);
        
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(eventProcessor);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);
        
        wm.addEntity(camera);
    }
    
    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");        
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);
        
        return (cameraSG);
    }
    
    private void createGrid(WorldManager wm) {
        float startx = 0.0f, startz = 0.0f;
        float endx = 0.0f, endz = 0.0f;

        int numLines = (gridWidth/5)*2 + 2;       
        Vector3f[] points = new Vector3f[numLines*2];       
        int numSegs = numLines/2;
        
        // Start with the Z lines
        startx = -gridWidth/2.0f;
        startz = -gridWidth/2.0f;
        endx = -gridWidth/2.0f;
        endz = gridWidth/2.0f;
        int pointNum = 0;
        for (int i=0; i<numSegs; i++) {
            points[pointNum++] = new Vector3f(startx, 0.0f, startz);
            points[pointNum++] = new Vector3f(endx, 0.0f, endz);
            startx += 5.0f;
            endx += 5.0f;
        }
        
        // Now the Z lines
        startx = -gridWidth/2.0f;
        startz = -gridWidth/2.0f;
        endx = gridWidth/2.0f;
        endz = -gridWidth/2.0f;
        for (int i=0; i<numSegs; i++) {
            points[pointNum++] = new Vector3f(startx, 0.0f, startz);
            points[pointNum++] = new Vector3f(endx, 0.0f, endz);
            startz += 5.0f;
            endz += 5.0f;
        }
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
         
        Node gridSG = new Node("Grid");
        Line gridG = new Line("Grid", points, null, null, null);
        gridSG.attachChild(gridG);
        gridSG.setRenderState(buf);
        
        RenderComponent rc = wm.getRenderManager().createRenderComponent(gridSG);
        rc.setLightingEnabled(false);
        grid.addComponent(RenderComponent.class, rc);
    }
    
    private void createAxis() { 
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            
        Node axisSG = new Node("Axis");
        AxisRods axisG = new AxisRods("Axis", true, 10.0f, 0.2f);
        axisSG.attachChild(axisG);
        axisSG.setRenderState(buf);
        
        RenderComponent rc = wm.getRenderManager().createRenderComponent(axisSG);
        rc.setLightingEnabled(false);
        axis.addComponent(RenderComponent.class, rc);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LightTest worldBuilder = new LightTest(args);
        
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
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }
    
    class MyLightProcessor extends ProcessorComponent {
        private WorldManager worldManager = null;
        private float degrees = 0.0f;
        private float increment = 0.0f;
        private Quaternion quaternion = new Quaternion();
        private Node target = null;
        private Node target2 = null;
        private Vector3f startPosition = new Vector3f();
        private Vector3f currentPosition = new Vector3f();

        public MyLightProcessor(WorldManager worldManager, Node target, Node target2,
                float increment, float x, float y, float z) {
            this.worldManager = worldManager;
            this.target = target;
            this.target2 = target2;
            this.increment = increment;
            startPosition.x = x; startPosition.y = y; startPosition.z = z; 
            setArmingCondition(new NewFrameCondition(this));
        }

        public void initialize() {
            //setArmingCondition(new NewFrameCondition(this));
        }

        public void compute(ProcessorArmingCollection collection) {
            degrees += increment;
            quaternion.fromAngles(0.0f, degrees, 0.0f);
            quaternion.mult(startPosition, currentPosition);
            //System.out.println(this + " is " + currentPosition);
        }

        public void commit(ProcessorArmingCollection collection) {
            target.setLocalTranslation(currentPosition);
            worldManager.addToUpdateList(target);
            target2.setLocalTranslation(currentPosition);
            worldManager.addToUpdateList(target2);
        }
    }
    
    class SwingFrame extends JFrame implements FrameRateListener, ActionListener {

        JPanel contentPane;
        JPanel menuPanel = new JPanel();
        JPanel canvasPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        JLabel fpsLabel = new JLabel("FPS: ");
        
        JToggleButton coordButton = new JToggleButton("Global Light", true);
        JToggleButton gridButton = new JToggleButton("Local Light", true);
        JMenuItem loadItem = null;
        JMenuItem exitItem = null;
        JMenuItem createTeapotItem = null;


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
            
            // Create Menu
            JMenu createMenu = new JMenu("Create");
            createTeapotItem = new JMenuItem("Teapot");
            createTeapotItem.addActionListener(this);
            createMenu.add(createTeapotItem);
            menuBar.add(createMenu);
            
            menuPanel.add(menuBar);
            contentPane.add(menuPanel, BorderLayout.NORTH);
            
            // The Rendering Canvas
            rb = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, width, height);
            wm.getRenderManager().addRenderBuffer(rb);
            canvas = rb.getCanvas();
            canvas.setVisible(true);
            canvas.setBounds(0, 0, width, height);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());           
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);
            
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
        }
        
        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }
        
        /**
         * Add a model to be visualized
         */
        private void addModel(Node model) {
            Node modelRoot = new Node("Model");
            System.out.println("Adding: " + model);
            modelRoot.attachChild(model);
            models.add(modelRoot);
            
            Entity e = new Entity("Model");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);
            e.addComponent(RenderComponent.class, sc);
            wm.addEntity(e);              
        }
        
        private void createTeapot() {
            Node node = new Node();
            Teapot teapot = new Teapot();
            teapot.updateGeometryData();
            node.attachChild(teapot);
            
            Triangle[] tris = new Triangle[teapot.getTriangleCount()];
            
            BoundingBox bbox = new BoundingBox();
            bbox.computeFromTris(teapot.getMeshAsTriangles(tris), 0, tris.length);
            System.out.println(bbox);
        
            ColorRGBA color = new ColorRGBA();

            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
            MaterialState matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
            matState.setDiffuse(color);
            
            BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
            as.setEnabled(true);
            as.setBlendEnabled(true);
            as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            node.setRenderState(as);

            CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
            cs.setEnabled(true);
            cs.setCullFace(CullState.Face.Back);
            node.setRenderState(cs);
            
            node.setRenderState(matState);
            node.setRenderState(buf);
            node.setLocalTranslation(0.0f, 0.0f, 0.0f);
            teapot.setModelBound(bbox);
            addModel(node);
            addToVisibleBounds(teapot);
        }
            
        private void addToVisibleBounds(Geometry g) {
            BoundingVolume bv = g.getModelBound();
            Entity e = null;
            Node node = null;
            ColorRGBA color = new ColorRGBA(1.0f, 0.0f, 0.0f, 0.4f);
            Box box = null;

            System.out.println("BOUNDS: " + bv);
            if (bv instanceof BoundingBox) {
                BoundingBox bbox = (BoundingBox) bv;
                Vector3f center = bbox.getCenter();

                Vector3f extent = bbox.getExtent(null);
                box = new Box("Bounds", center, extent.x, extent.y, extent.z);
                box.setDefaultColor(color);

                e = new Entity("Bounds");
                node = new Node();
                node.attachChild(box);
                RenderComponent sc = wm.getRenderManager().createRenderComponent(node);
                sc.setLightingEnabled(false);
                e.addComponent(RenderComponent.class, sc);
            }

            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            node.setRenderState(buf);

            BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
            as.setEnabled(true);
            as.setBlendEnabled(true);
            as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            node.setRenderState(as);

            CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
            cs.setEnabled(true);
            cs.setCullFace(CullState.Face.Back);
            node.setRenderState(cs);

            wm.addEntity(e);
        }
    
        /**
         * The method which gets the state change from the buttons
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == coordButton) {
                if (coordsOn) {
                    coordsOn = false;
                    wm.getRenderManager().removeLight(globalLight2);
                    System.out.println("Turning Global Light Off");
                } else {
                    coordsOn = true;
                    wm.getRenderManager().addLight(globalLight2);
                    System.out.println("Turning Global Light On");
                }
            }
            
            if (e.getSource() == gridButton) {
                if (gridOn) {
                    gridOn = false;
                    localLightRC.removeLight(localLight2);
                    System.out.println("Turning Local Light Off");
                } else {
                    gridOn = true;
                    localLightRC.addLight(localLight2);
                    System.out.println("Turning Local Light On");
                }
            }
            
            if (e.getSource() == loadItem) {
                FileInputStream fileStream = null;
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " +
                            chooser.getSelectedFile().getName());
                    try {
                        fileStream = new FileInputStream(chooser.getSelectedFile());
                    } catch (FileNotFoundException ex) {
                        System.out.println(ex);
                    }
                    
                    // Now load the model
                    ColladaImporter.load(fileStream, "Model");
                    Node model = ColladaImporter.getModel();
                    addModel(model);                 
                }
            }
            
            if (e.getSource() == exitItem) {
                System.exit(1);
            }
            
            if (e.getSource() == createTeapotItem) {
                createTeapot();
            }
        }
    }
    
    /**
     * Create 50 randomly placed teapots, with roughly half of them transparent
     */
    private void createTeapots() {
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;
        boolean transparent = false;
        int numTeapots = 500;
        Random r = new Random();
        RenderComponent sc = null;
        JMECollisionComponent cc = null;
        Entity e = null;
        Node teapot = null;
        JMECollisionSystem collisionSystem = (JMECollisionSystem) 
                wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        
        teapot = createTeapotModel(x, y, z, transparent);
        e = new Entity("Teapot 1");
        localLightRC = wm.getRenderManager().createRenderComponent(teapot);
        localLightRC.addLight(localLight1);
        localLightRC.addLight(localLight2);
        cc = collisionSystem.createCollisionComponent(teapot);
        e.addComponent(RenderComponent.class, localLightRC);
        e.addComponent(CollisionComponent.class, cc);
        wm.addEntity(e);

        x = 25.0f; z = 25.0f;
        teapot = createTeapotModel(x, y, z, transparent);
        e = new Entity("Teapot 2");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        cc = collisionSystem.createCollisionComponent(teapot);
        e.addComponent(RenderComponent.class, sc);
        e.addComponent(CollisionComponent.class, cc);
        wm.addEntity(e);
        
        
        x = 25.0f; z = -25.0f;
        teapot = createTeapotModel(x, y, z, transparent);
        e = new Entity("Teapot 3");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        cc = collisionSystem.createCollisionComponent(teapot);
        e.addComponent(RenderComponent.class, sc);
        e.addComponent(CollisionComponent.class, cc);
        wm.addEntity(e);
        
        
        x = -25.0f; z = -25.0f;
        teapot = createTeapotModel(x, y, z, transparent);
        e = new Entity("Teapot 4");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        cc = collisionSystem.createCollisionComponent(teapot);
        e.addComponent(RenderComponent.class, sc);
        e.addComponent(CollisionComponent.class, cc);
        wm.addEntity(e);
        
        
        x = -25.0f; z = 25.0f;
        teapot = createTeapotModel(x, y, z, transparent);
        e = new Entity("Teapot 5");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        cc = collisionSystem.createCollisionComponent(teapot);
        e.addComponent(RenderComponent.class, sc);
        e.addComponent(CollisionComponent.class, cc);
        wm.addEntity(e);
    }
    
    private Node createTeapotModel(float x, float y, float z, boolean transparent) {
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.updateGeometryData();
        node.attachChild(teapot);

        Triangle[] tris = new Triangle[teapot.getTriangleCount()];

        BoundingBox bbox = new BoundingBox();
        bbox.computeFromTris(teapot.getMeshAsTriangles(tris), 0, tris.length);

        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        if (transparent) {
            BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
            as.setEnabled(true);
            as.setBlendEnabled(true);
            as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            node.setRenderState(as);
            
            CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
            cs.setEnabled(true);
            cs.setCullFace(CullState.Face.Back);
            node.setRenderState(cs);
            
            color.set(0.0f, 1.0f, 1.0f, 0.75f);
        }

        MaterialState matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(color);
        
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setLocalTranslation(x, y, z);
        node.setModelBound(bbox); 
        
        return (node);
    }


}