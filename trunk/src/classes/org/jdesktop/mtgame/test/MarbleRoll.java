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

import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.processor.JBSelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
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
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Dome;
import com.jme.scene.shape.Sphere;
import com.jme.scene.Geometry;
import com.jme.scene.TriMesh;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.jme.util.geom.BufferUtils;
import java.util.ArrayList;
import com.jmex.model.collada.ColladaImporter;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;

import java.util.Random;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class MarbleRoll {
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
    
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    private Node marble = null;

    JBulletPhysicsSystem physicsSystem = null;
    JBulletDynamicCollisionSystem collisionSystem = null;
    Entity marbleEntity = null;
    
    public MarbleRoll(String[] args) {
        System.setProperty("mtgame.runPhysicsInRenderer", "true");
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        createUI(wm);  
        createCameraEntity(wm);   
        createGrid(wm);
        //wm.addEntity(grid);
        createAxis();
        //wm.addEntity(axis);
        createGlobalLight();
        
        createRandomTeapots(wm); 
    }
    
    private void createGlobalLight() {
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setEnabled(true);
        LightNode ln = new LightNode();
        ln.setLight(light);
        ln.setLocalTranslation(new Vector3f(100, 100, 100));
        wm.getRenderManager().addLight(ln); 
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
        JBSelectionProcessor selector = new JBSelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
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
        MarbleRoll worldBuilder = new MarbleRoll(args);
        
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
        
        JToggleButton coordButton = new JToggleButton("Run", false);
        JToggleButton gridButton = new JToggleButton("Reset", false);
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
                    physicsSystem.setStarted(coordsOn);
                    System.out.println("Turning Sim Off");
                } else {
                    coordsOn = true;
                    physicsSystem.setStarted(coordsOn);
                    System.out.println("Turning Sim On");
                }
            }
            
            if (e.getSource() == gridButton) {
                removeMarble();
                createMarble();
                System.out.println("Resetting Marble");
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
    private void createRandomTeapots(WorldManager wm) {
        JBulletCollisionComponent cc = null;
        collisionSystem = (JBulletDynamicCollisionSystem) 
                wm.getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);   
        physicsSystem = (JBulletPhysicsSystem) 
                wm.getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, collisionSystem); 
        
        createMarble();

        //Cylinder trough = new Cylinder("Trough", 25, 25, 1.0f, 25.0f);
        //Dome trough = new Dome("Trough", 25, 25, 10.0f);
        Quad trough = new Quad("", 100, 100);

        Triangle[] tris = new Triangle[trough.getTriangleCount()];
        BoundingBox bbox = new BoundingBox();
        bbox.computeFromTris(trough.getMeshAsTriangles(tris), 0, tris.length);

        Node floor = new Node();
        floor.attachChild(trough);
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        floor.setRenderState(buf);
        floor.setModelBound(bbox);
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis((float)Math.toRadians(-80.0), new Vector3f(1.0f, 0.0f, 0.0f));
        trough.setLocalRotation(rot);
        trough.setLocalTranslation(0.0f, 0.0f, 7.0f);
        //trough.setLocalScale(1.5f);

        RenderComponent floorrc = wm.getRenderManager().createRenderComponent(floor);
        Entity floore = new Entity("Floor");

        cc = collisionSystem.createCollisionComponent(trough);
        floore.addComponent(CollisionComponent.class, cc);
        floore.addComponent(RenderComponent.class, floorrc);

        MoveProcessor rp = new MoveProcessor("Teapot Rotator", wm,
                floor, (float) (1.0f * Math.PI / 180.0f), 0.1f, 0.001f);
        floore.addComponent(RotationProcessor.class, rp);
        wm.addEntity(floore);
    }

    private void createMarble() {
        RenderComponent sc = null;
        JBulletCollisionComponent cc = null;
        JBulletPhysicsComponent pc = null;

        marble = createSphereModel(0, 50, 0);
        marbleEntity = new Entity("Marble ");
        sc = wm.getRenderManager().createRenderComponent(marble);
        cc = collisionSystem.createCollisionComponent(marble);
        pc = physicsSystem.createPhysicsComponent(cc);
        pc.setMass(1f);
        marbleEntity.addComponent(CollisionComponent.class, cc);
        marbleEntity.addComponent(PhysicsComponent.class, pc);
        marbleEntity.addComponent(RenderComponent.class, sc);
        wm.addEntity(marbleEntity);
    }

    private void removeMarble() {
        wm.removeEntity(marbleEntity);
    }

    private Node createSphereModel(float x, float y, float z) {
        Node node = new Node("Marble ");
        Sphere sphere = new Sphere("Marble", 10, 10, 0.75f);

        node.attachChild(sphere);

        Triangle[] tris = new Triangle[sphere.getTriangleCount()];

        BoundingBox bsphere = new BoundingBox();
        bsphere.computeFromTris(sphere.getMeshAsTriangles(tris), 0, tris.length);

        ColorRGBA color = new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f);

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        MaterialState matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(color);

        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setLocalTranslation(x, y, z);
        node.setModelBound(bsphere);

        return (node);
    }

    public class MoveProcessor extends ProcessorComponent {

        /**
         * The WorldManager - used for adding to update list
         */
        private WorldManager worldManager = null;

        /**
         * The current degrees of rotation
         */
        private float degrees = 0.0f;
        private float trans = 0.0f;
        private float scale = 1.0f;

        /**
         * The increment to rotate each frame
         */
        private float rinc = 0.0f;
        private float tinc = 0.0f;
        private float sinc = 0.0f;
        private float slow = 0.5f;
        private float shigh = 1.5f;
        private float tlow = -10.0f;
        private float thigh = 10.0f;

        /**
         * The rotation matrix to apply to the target
         */
        private Quaternion quaternion = new Quaternion();

        /**
         * The rotation target
         */
        private Node target = null;

        /**
         * A name
         */
        private String name = null;

        /**
         * The constructor
         */
        public MoveProcessor(String name, WorldManager worldManager, Node target, float rinc, float tinc, float sinc) {
            this.worldManager = worldManager;
            this.target = target;
            this.rinc = rinc;
            this.sinc = sinc;
            this.tinc = tinc;
            this.name = name;

            setArmingCondition(new NewFrameCondition(this));
        }

        public String toString() {
            return (name);
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
            degrees += rinc;
            quaternion.fromAngles(0.0f, degrees, 0.0f);

            trans += tinc;
            if (trans > thigh) {
                tinc = -tinc;
                trans = thigh;
            }
            if (trans < tlow) {
                tinc = -tinc;
                trans = tlow;
            }

            scale += sinc;
            if (scale > shigh) {
                sinc = -sinc;
                scale = shigh;
            }
            if (scale < slow) {
                sinc = -sinc;
                scale = slow;
            }
        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalRotation(quaternion);
            target.setLocalTranslation(0.0f, trans, 0.0f);
            //target.setLocalScale(scale);
            worldManager.addToUpdateList(target);
        }
    }

}
