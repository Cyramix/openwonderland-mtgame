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

import org.jdesktop.mtgame.processor.SelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.RenderState;
import com.jme.scene.shape.Teapot;
import com.jme.bounding.BoundingBox;
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
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.event.KeyEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import com.jmex.model.collada.ColladaImporter;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class RenderUpdaterTest implements RenderUpdater {
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
     * URL's for the shaders
     */
    private URL vert = null;
    private URL frag = null;
    
    /**
     * A list of the models we are looking at
     */
    private ArrayList models = new ArrayList();
    
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    
    public RenderUpdaterTest(String[] args) {
        wm = new WorldManager("TestWorld");
        
        try {
            vert = new URL("file:/Users/runner/NetBeansProjects/mtgame/trunk/src/classes/org/jdesktop/mtgame/SampleVertShader");
            frag = new URL("file:/Users/runner/NetBeansProjects/mtgame/trunk/src/classes/org/jdesktop/mtgame/SampleFragShader");
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        createUI(wm);  
        createCameraEntity(wm);   
        createGrid(wm);
        wm.addEntity(grid);
        createAxis();
        wm.addEntity(axis);
        
        //createRoom();
        createTeapots();
       
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
        //FPSCameraProcessor eventProcessor = new FPSCameraProcessor(eventListener, cameraNode, wm, camera);
        OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);
        
        AWTInputComponent selectionListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);        
        SelectionProcessor selector = new SelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
        selector.setRunInRenderer(true);
        
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(eventProcessor);
        pcc.addProcessor(selector);
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
        RenderUpdaterTest st = new RenderUpdaterTest(args);
        
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
    
    class SwingFrame extends JFrame implements FrameRateListener, ActionListener {

        JPanel contentPane;
        JPanel menuPanel = new JPanel();
        JPanel canvasPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        JLabel fpsLabel = new JLabel("FPS: ");
        
        JToggleButton coordButton = new JToggleButton("Coords", true);
        JToggleButton gridButton = new JToggleButton("Grid", true);
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
            menuBar.add(createMenu);
            
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
            RenderComponent rc = wm.getRenderManager().createRenderComponent(modelRoot);
            e.addComponent(RenderComponent.class, rc);
            wm.addEntity(e);              
        }
    
        /**
         * The method which gets the state change from the buttons
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == coordButton) {
                if (coordsOn) {
                    coordsOn = false;
                    wm.removeEntity(axis);
                    System.out.println("Turning Coordinates Off");
                } else {
                    coordsOn = true;
                    wm.addEntity(axis);
                    System.out.println("Turning Coordinates On");
                }
            }
            
            if (e.getSource() == gridButton) {
                if (gridOn) {
                    gridOn = false;
                    wm.removeEntity(grid);
                    System.out.println("Turning Grid Off");
                } else {
                    gridOn = true;
                    wm.addEntity(grid);
                    System.out.println("Turning Grid On");
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
        }
    }
    
    /**
     * Create 50 randomly placed teapots, with roughly half of them transparent
     */
    private void createTeapots() {
        RenderComponent rc = null;
        RenderComponent greenRc = null;
        
        JMECollisionSystem collisionSystem = 
                (JMECollisionSystem) wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class); 
        
        rc = createTeapotEntity(  0.0f, 0.0f,   0.0f, new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f), null, collisionSystem, false, false);
        createTeapotEntity(-15.0f, 0.0f, -15.0f, new ColorRGBA(1.0f, 1.0f, 0.0f, 1.0f), rc.getSceneRoot(), collisionSystem, true, true);
        createTeapotEntity(-15.0f, 0.0f,  15.0f, new ColorRGBA(0.0f, 1.0f, 1.0f, 1.0f), rc.getSceneRoot(), collisionSystem, true, false);
        createTeapotEntity( 15.0f, 0.0f,  15.0f, new ColorRGBA(1.0f, 0.0f, 1.0f, 1.0f), rc.getSceneRoot(), collisionSystem, false, true);
        greenRc = createTeapotEntity( 15.0f, 0.0f, -15.0f, new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f), rc.getSceneRoot(), collisionSystem, false, false);
        Entity e = new Entity("INPUT");
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent listener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        KeyboardProcessor kp = new KeyboardProcessor(listener, rc.getSceneRoot(), greenRc);
        e.addComponent(ProcessorComponent.class, kp);
        wm.addEntity(e);
        
    }
    
    private RenderComponent createTeapotEntity(float x, float y, float z, ColorRGBA color, Node attachPoint, JMECollisionSystem cs, boolean updateInRenderer, boolean doWait) {
        RenderComponent sc = null;
        Entity e = null;
        JMECollisionComponent cc = null;

        Node teapot = createTeapotModel(x, y, z, color);

        BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        teapot.setRenderState(as);

        e = new Entity("Teapot");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        e.addComponent(RenderComponent.class, sc);
        if (attachPoint != null) {
            sc.setAttachPoint(attachPoint);

            // Add a render updater to changes the alpha value
            RenderUpdaterProcessor rup = new RenderUpdaterProcessor(wm, (Geometry)teapot.getChild(0), doWait);
            if (updateInRenderer) {
                rup.setRunInRenderer(true);
            }
            e.addComponent(RenderUpdaterProcessor.class, rup);
        }
        cc = (JMECollisionComponent)cs.createCollisionComponent(teapot);
        e.addComponent(CollisionComponent.class, cc);
        
        RotationProcessor rp = new RotationProcessor("Teapot Rotator", wm,
                teapot, (float) (6.0f * Math.PI / 180.0f));
        e.addComponent(ProcessorComponent.class, rp);
        wm.addEntity(e);
        
        return (sc);
    }
    
    private Node createTeapotModel(float x, float y, float z, ColorRGBA color) {
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.updateGeometryData();
        node.attachChild(teapot);

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        //GLSLShaderObjectsState shader = (GLSLShaderObjectsState) wm.getRenderManager().createRendererState(RenderState.StateType.GLSLShaderObjects);
        //shader.setUniform("color", color);
        // Defer loading until we are in the renderer
        //wm.addRenderUpdater(this, shader);
        
        node.setRenderState(buf);
        //node.setRenderState(shader);
        teapot.setDefaultColor(color);
        node.setLocalTranslation(x, y, z);
        
        
        Triangle[] tris = new Triangle[teapot.getTriangleCount()];
        BoundingBox bbox = new BoundingBox();
        bbox.computeFromTris(teapot.getMeshAsTriangles(tris), 0, tris.length);
        node.setModelBound(bbox);
        
        return (node);
    }

    public void update(Object obj) {
        GLSLShaderObjectsState shader = (GLSLShaderObjectsState)obj;
        shader.load(vert, frag); 
    }

    public class RenderUpdaterProcessor extends ProcessorComponent implements RenderUpdater {
        private NewFrameCondition condition = new NewFrameCondition(this);
        private WorldManager worldManager = null;
        private Geometry target = null;
        private boolean doWait = false;
        private float alpha = 1.0f;
        private float alphaInc = 0.01f;

        public RenderUpdaterProcessor(WorldManager wm, Geometry target, boolean doWait) {
            this.target = target;
            this.doWait = doWait;
            worldManager = wm;
        }

        public void initialize() {
            setArmingCondition(condition);
        }

        public void compute(ProcessorArmingCollection collection) {
            worldManager.addRenderUpdater(this, null, doWait);
        }

        public void commit(ProcessorArmingCollection pc) {
        }

        public void update(Object object) {
            ColorRGBA color = target.getDefaultColor();
            color.a = alpha;
            target.setDefaultColor(color);
            worldManager.addToUpdateList(target);
            alpha += alphaInc;
            if (alpha > 1.0f) {
                alphaInc = -alphaInc;
                alpha = 1.0f;
            }

            if (alpha < 0.0f) {
                alphaInc = -alphaInc;
                alpha = 0.0f;
            }
        }
    }

    public class KeyboardProcessor extends AWTEventProcessorComponent {

        Node ap = null;
        RenderComponent rc = null;
        boolean attached = true;    
        private ProcessorArmingCollection collection = null;
        

        public KeyboardProcessor(AWTInputComponent listener, Node ap, RenderComponent rc) {
            super(listener);
            collection = new ProcessorArmingCollection(this);
            collection.addCondition(new AwtEventCondition(this));
            this.ap = ap;
            this.rc = rc;
        }
    
        public void initialize() {
            setArmingCondition(collection);
        }
        
        public void compute(ProcessorArmingCollection collection) {
        }
        
        public void commit(ProcessorArmingCollection pc) {
            Object[] events = getEvents();
            for (int i = 0; i < events.length; i++) {
                if (events[i] instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) events[i];
                    if (ke.getID() == KeyEvent.KEY_TYPED && ke.getKeyChar() == ' ') {
                        if (attached) {
                            rc.setAttachPoint(null);
                            attached = false;
                            System.out.println("DETACHING");
                        } else {
                            rc.setAttachPoint(ap);
                            attached = true;
                            System.out.println("ATTCHING");
                        }
                    }
                }
            }
        }
    }

}
