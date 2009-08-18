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

import org.jdesktop.mtgame.processor.EyeSelectionProcessor;
import org.jdesktop.mtgame.processor.MouseSelectionProcessor;
import org.jdesktop.mtgame.processor.SelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.PostEventProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.shader.Shader;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.TextureState;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.light.LightNode;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jmex.effects.particles.ParticleFactory;
import com.jme.util.TextureManager;
import com.jme.image.Texture;
import com.jme.scene.Geometry;
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
import java.util.ArrayList;
import com.jmex.model.collada.ColladaImporter;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.ParticleController;

import java.util.Random;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class NoiseTest implements RenderUpdater {
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
    private int desiredFrameRate = 500;
    
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
    
    public NoiseTest(String[] args) {
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        lightNode = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        lightNode.setLight(light);
        lightNode.setLocalTranslation(0.0f, 0.0f, 50.0f);
        wm.getRenderManager().addLight(lightNode);
        
        createUI(wm);  
        createCameraEntity(wm);   
        createGrid(wm);
        wm.addEntity(grid);
        createAxis();
        wm.addEntity(axis);
         
        createTeapot(wm);
        createParticles(wm);
        
    }
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        //CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
        //        width, height, 1.0f, 1000.0f, -100, 100, 100, -100, true);
        rb.setCameraComponent(cc);
        camera.addComponent(CameraComponent.class, cc);

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        //FPSCameraProcessor eventProcessor = new FPSCameraProcessor(eventListener, cameraNode, wm, camera);
        OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);
        
        AWTInputComponent selectionListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);        
        MouseSelectionProcessor selector = new MouseSelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
        //EyeSelectionProcessor selector = new EyeSelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
        //SelectionProcessor selector = new SelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);

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
        NoiseTest worldBuilder = new NoiseTest(args);
        
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
            createTeapotItem = new JMenuItem("Teapot1");
            createTeapotItem.addActionListener(this);
            createMenu.add(createTeapotItem);
            menuBar.add(createMenu);
            
            JMenu test1Menu = new JMenu("Test1");
            createTeapotItem = new JMenuItem("Teapot2");
            createTeapotItem.addActionListener(this);
            test1Menu.add(createTeapotItem);
            menuBar.add(test1Menu);
            
            JMenu test2Menu = new JMenu("Create");
            createTeapotItem = new JMenuItem("Teapot3");
            createTeapotItem.addActionListener(this);
            test2Menu.add(createTeapotItem);
            menuBar.add(test2Menu);
            
            JMenu test3Menu = new JMenu("Create");
            createTeapotItem = new JMenuItem("Teapot4");
            createTeapotItem.addActionListener(this);
            test3Menu.add(createTeapotItem);
            createTeapotItem = new JMenuItem("Teapot5");
            createTeapotItem.addActionListener(this);
            test3Menu.add(createTeapotItem);
            createTeapotItem = new JMenuItem("Teapot6");
            createTeapotItem.addActionListener(this);
            test3Menu.add(createTeapotItem);
            test3Menu.getPopupMenu().setLightWeightPopupEnabled(false);
            menuBar.add(test3Menu);
            
            
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
            
            
            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            modelRoot.setRenderState(buf);
            
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
            
            if (e.getSource() == createTeapotItem) {
                createTeapot();
            }
        }
    }
    
    /**
     * Create 50 randomly placed teapots, with roughly half of them transparent
     */
    private void createTeapot(WorldManager wm) {
        RenderComponent sc = null;
        JMECollisionComponent cc = null;
        Entity e = null;
        JMECollisionSystem collisionSystem = (JMECollisionSystem) wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);


        Node teapot = createTeapotModel(0.0f, 0.0f, 0.0f);

        e = new Entity("Teapot ");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        cc = collisionSystem.createCollisionComponent(teapot);
        e.addComponent(RenderComponent.class, sc);
        e.addComponent(CollisionComponent.class, cc);


        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        RotationProcessor rp = new RotationProcessor("Teapot Rotator", wm,
                teapot, (float) (6.0f * Math.PI / 180.0f));
        pcc.addProcessor(rp);
        //e.addComponent(ProcessorCollectionComponent.class, pcc);
        wm.addEntity(e);

    }

    private void createParticles(WorldManager wm) {
        RenderComponent sc = null;
        Entity e = null;
        Node rootNode = new Node();

        BlendState as1 = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
        as1.setBlendEnabled(true);
        as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as1.setDestinationFunction(BlendState.DestinationFunction.One);
        as1.setTestEnabled(true);
        as1.setTestFunction(BlendState.TestFunction.GreaterThan);
        as1.setEnabled(true);
        as1.setEnabled(true);

        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setTexture(
                TextureManager.loadTexture(
                NoiseTest.class.getClassLoader().getResource(
                "jmetest/data/texture/flaresmall.jpg"),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear));
        ts.setEnabled(true);

        ParticleMesh pMesh = ParticleFactory.buildParticles("particles", 300);
        pMesh.setEmissionDirection(new Vector3f(0, 1, 0));
        pMesh.setInitialVelocity(.006f);
        pMesh.setStartSize(2.5f);
        pMesh.setEndSize(.5f);
        pMesh.setMinimumLifeTime(1200f);
        pMesh.setMaximumLifeTime(1400f);
        pMesh.setStartColor(new ColorRGBA(1, 0, 0, 1));
        pMesh.setEndColor(new ColorRGBA(0, 1, 0, 0));
        pMesh.setMaximumAngle(360f * FastMath.DEG_TO_RAD);
        pMesh.getParticleController().setControlFlow(false);
        pMesh.setParticlesInWorldCoords(true);
        pMesh.warmUp(60);

        rootNode.setRenderState(ts);
        rootNode.setRenderState(as1);
        ZBufferState zstate = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zstate.setEnabled(false);
        pMesh.setRenderState(zstate);
        pMesh.setModelBound(new BoundingSphere());
        pMesh.updateModelBound();

        rootNode.attachChild(pMesh);
        //pMesh.setOriginOffset(new Vector3f(1.0f, 0.0f, 0.0f));
        pMesh.setLocalTranslation(10.0f, 0.0f, 0.0f);

        e = new Entity("Particles ");
        sc = wm.getRenderManager().createRenderComponent(rootNode);
        sc.setLightingEnabled(false);

        ParticleProcessor rp = new ParticleProcessor(wm, pMesh, (float) (6.0f * Math.PI / 180.0f), new Vector3f(10.0f, 0.0f, 0.0f));
        e.addComponent(RotationProcessor.class, rp);

        e.addComponent(RenderComponent.class, sc);
        wm.addEntity(e);

        wm.addRenderUpdater(this, rootNode);
    }

    public void update(Object p) {
        wm.addToUpdateList((Node)p);
        wm.addRenderUpdater(this, p);
    }
    
    private Node createTeapotModel(float x, float y, float z) {
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

        MaterialState matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(color);
        
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setLocalTranslation(x, y, z);
        node.setLocalScale(2.0f);
        node.setModelBound(bbox);

        MarbleMap shader = new MarbleMap(wm);
        shader.applyToGeometry(teapot);
        
        return (node);
    }


    public class MarbleMap implements RenderUpdater {
        GLSLShaderObjectsState shaderState = null;
        WorldManager worldManager = null;

        /**
         * The vertex and fragment shader
         */
        protected static final String vShader =
                "uniform float Scale;" +
                "varying vec3  MCposition;" +
                "void main(void)" +
                "{" +
                    "MCposition      = vec3(gl_Vertex) * Scale;" +
                    "gl_Position     = ftransform();" +
                "}";

        private static final String fShader =
                "varying vec3  MCposition;" +
                "uniform vec3 Color1;" +
                "uniform vec3 Color2;" +
                "void main(void) { " +
                    "vec4 noisevec   = noise4(MCposition);" +

                    "float intensity = abs(noisevec[0] - 0.25) +" +
                                      "abs(noisevec[1] - 0.125) +" +
                                      "abs(noisevec[2] - 0.0625) +" +
                                      "abs(noisevec[3] - 0.03125);" +
                    "intensity    = clamp(intensity, 0.0, 1.0);" +
                    "vec3 color    = mix(Color1, Color2, intensity);" +
                    "gl_FragColor  = vec4(color, 1.0);" +
                "}";

        public MarbleMap(WorldManager wm) {
            worldManager = wm;
        }

        /**
         * This applies this shader to the given geometry
         */
        public void applyToGeometry(Geometry geo) {
            shaderState = (GLSLShaderObjectsState) worldManager.getRenderManager().
                createRendererState(RenderState.StateType.GLSLShaderObjects);
            shaderState.setUniform("Scale", 1.2f);
            shaderState.setUniform("Color1", 0.8f, 0.7f, 0.0f);
            shaderState.setUniform("Color2", 0.6f, 0.1f, 0.0f);
            geo.setRenderState(shaderState);
            worldManager.addRenderUpdater(this, this);
        }
        /**
         * This loads the shader
         */
        public void update(Object o) {
            shaderState.load(vShader, fShader);
        }
    }

    public class ParticleProcessor extends ProcessorComponent {

        /**
         * The WorldManager - used for adding to update list
         */
        private WorldManager worldManager = null;
        /**
         * The current degrees of rotation
         */
        private float degrees = 0.0f;
        /**
         * The increment to rotate each frame
         */
        private float increment = 0.0f;
        /**
         * The rotation matrix to apply to the target
         */
        private Quaternion quaternion = new Quaternion();
        /**
         * The rotation target
         */
        private Node target = null;
        private Vector3f position = new Vector3f();
        private Vector3f transPos = new Vector3f();

        /**
         * The constructor
         */
        public ParticleProcessor(WorldManager worldManager, Node target, float increment, Vector3f pos) {
            this.worldManager = worldManager;
            this.target = target;
            this.increment = increment;
            this.position.set(pos.x, pos.y, pos.z);
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
            degrees += increment;
            quaternion.fromAngles(0.0f, degrees, 0.0f);
            quaternion.mult(position, transPos);
        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalTranslation(transPos.x, transPos.y, transPos.z);
            worldManager.addToUpdateList(target);
        }
    }
}
