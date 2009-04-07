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

import org.jdesktop.mtgame.processor.EyeSelectionProcessor;
import org.jdesktop.mtgame.processor.MouseSelectionProcessor;
import org.jdesktop.mtgame.processor.SelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.PostEventProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import org.jdesktop.mtgame.*;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.scene.TriMesh;
import com.jme.light.DirectionalLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.Spatial;
import com.jme.scene.Geometry;
import com.jme.image.Image;
import com.jme.util.TextureManager;
import com.jme.scene.TexCoords;
import com.jme.light.LightNode;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.RoundedBox;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
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

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Random;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class WorldShader {
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
    private ShadowMapRenderBuffer shadowMapBuffer = null;
    private DiffuseNormalMap shader = null;
    
    public WorldShader(String[] args) {
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        wm.getRenderManager().setMinSamples(4);
        
        createUI(wm);  
        createCameraEntity(wm);   
        createGrid(wm);
        //wm.addEntity(grid);
        createAxis();
        wm.addEntity(axis);
        shader = new DiffuseNormalMap(wm);
        createGlobalLight();
        createBoxes();
        //createFloor();
        //loadTree();
    }
                  
    private void loadTree() {
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream("/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/obj_lamp.dae");
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
                            
        ColladaImporter.load(fileStream, "Model");
        Node model = ColladaImporter.getModel();
        model.setLocalTranslation(20.0f, 0.0f, 20.0f);
        
        parseTree(model);
        
        Entity e = new Entity("Tree ");
        RenderComponent sc = wm.getRenderManager().createRenderComponent(model);
        e.addComponent(RenderComponent.class, sc);

        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        RotationProcessor rp = new RotationProcessor("Teapot Rotator", wm,
                model, (float) (1.0f * Math.PI / 180.0f));
        pcc.addProcessor(rp);
        e.addComponent(ProcessorCollectionComponent.class, pcc);
        wm.addEntity(e);
    }
    
    void parseTree(Spatial s) {
        if (s instanceof Geometry) {
            URL durl = null;
            URL nurl = null;
            Geometry g = (Geometry)s; 
            TangentBinormalGenerator.generate((TriMesh)g);

            Texture tex2d = null;
            Texture normMap = null;
        
            try {
                if (g.getName().contains("trunk")) {
                    System.out.println("Found Trunk: " + g);
                    durl = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/vegetation/veg_bark_D.jpg");
                    nurl = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/vegetation/veg_bark_N.jpg");
                } else if (g.getName().contains("branches")) {
                    durl = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/vegetation/veg_redtree.png");
                    nurl = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/vegetation/veg_redtree_N.tga");
                    System.out.println("Found Branches: " + g);
                }
            } catch (MalformedURLException ex) {
                System.out.println(ex);
            }
            
            tex2d = TextureManager.loadTexture(durl,
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            tex2d.setWrap(Texture.WrapMode.Clamp);
            normMap = TextureManager.loadTexture(nurl,
                    Texture.MinificationFilter.NearestNeighborNoMipMaps,
                    Texture.MagnificationFilter.NearestNeighbor);
            normMap.setWrap(Texture.WrapMode.Clamp);

            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            g.setRenderState(buf);

            TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
            ts.setEnabled(true);
            ts.setTexture(tex2d, 0);
            ts.setTexture(normMap, 1);
            //ts.setTexture(shadowMapBuffer.getTexture(), 2);
            g.setRenderState(ts);
            
        } else if (s instanceof Node) {
            Node n = (Node) s;
            for (int i=0; i<n.getQuantity(); i++) {
                parseTree(n.getChild(i));
            }
        }
    }
    
    private void createGlobalLight() {
        Vector3f direction = new Vector3f(-1.0f, -1.0f, -1.0f);
        Vector3f position = new Vector3f(100.0f, 100.0f, 100.0f);
        
        //PointLight light = new PointLight();
        DirectionalLight light = new DirectionalLight();
        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        //light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setEnabled(true);
        LightNode ln = new LightNode();
        ln.setLight(light);
        ln.setLocalTranslation(position);
        wm.getRenderManager().addLight(ln); 
        
        createShadowBuffer(direction, position);
        LightProcessor lp = new LightProcessor(wm, ln, shadowMapBuffer, (float)(1.0f * Math.PI / 180.0f));
        Entity e = new Entity("Light Rotator");
        e.addComponent(ProcessorComponent.class, lp);
        //wm.addEntity(e);
    }
    
    private void createShadowBuffer(Vector3f dir, Vector3f pos) {
        int shadowWidth = 2048;
        int shadowHeight = 2048;
     
        shadowMapBuffer = (ShadowMapRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.SHADOWMAP, shadowWidth, shadowHeight);
        shadowMapBuffer.setCameraLookAt(new Vector3f());
        shadowMapBuffer.setCameraUp(new Vector3f(-1.0f, 1.0f, -1.0f));
        shadowMapBuffer.setCameraPosition(pos);
        shadowMapBuffer.setManageRenderScenes(true);
        wm.getRenderManager().addRenderBuffer(shadowMapBuffer);   
    }
        
    class LightProcessor extends ProcessorComponent {     
        private WorldManager worldManager = null;
        private float degrees = 0.0f;
        private float increment = 0.0f;
        private Quaternion quaternion = new Quaternion();
        private LightNode target = null;
        private ShadowMapRenderBuffer smb = null;
        Vector3f position = new Vector3f(100.0f, 100.0f, 100.0f);
        Vector3f positionOut = new Vector3f(100.0f, 100.0f, 100.0f);
        Vector3f up = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f upOut = new Vector3f(-1.0f, 1.0f, -1.0f);

        public LightProcessor(WorldManager worldManager, LightNode ln, ShadowMapRenderBuffer sb, float increment) {
            this.worldManager = worldManager;
            this.target = ln;
            this.increment = increment;
            this.smb = sb;
            setArmingCondition(new NewFrameCondition(this));
        }

        public void initialize() {
        }

        public void compute(ProcessorArmingCollection collection) {
            degrees += increment;
            quaternion.fromAngles(0.0f, degrees, 0.0f);
            quaternion.mult(up, upOut);
            quaternion.mult(position, positionOut);
        }

        public void commit(ProcessorArmingCollection collection) {
            target.setLocalTranslation(positionOut);
            worldManager.addToUpdateList(target);
            smb.setCameraPosition(positionOut);
            smb.setCameraUp(upOut);
        }
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
        WorldShader worldBuilder = new WorldShader(args);
        
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
            //addToVisibleBounds(teapot);
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
     * Create various boxes with different materials/shaders
     */
    void createBoxes() {
        
        Texture diffuseT = null;
        Texture normalT = null;
        Texture specT = null;
        
        String loc = "file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/";

        // Start with the brick
        diffuseT = loadTexture(loc + "buildings/bldg_brick_002.png");
        normalT = loadTexture(loc + "buildings/bldg_brick_N_002.png");
        specT = loadTexture(loc + "buildings/bldg_brick_S_001.png");
        createBoxModel(10.0f, 0.0f, 10.0f, diffuseT, null, null);
        createBoxModel(20.0f, 0.0f, 10.0f, diffuseT, normalT, null);
        createBoxModel(30.0f, 00.0f, 10.0f, diffuseT, normalT, specT);

        // Now wood
        diffuseT = loadTexture(loc + "buildings/bldg_floor_wood_D.png");
        normalT = loadTexture(loc + "buildings/bldg_floor_wood_N.png");
        specT = loadTexture(loc + "buildings/bldg_floor_wood_S.png");
        createBoxModel(10.0f, 10.0f, 10.0f, diffuseT, null, null);
        createBoxModel(20.0f, 10.0f, 10.0f, diffuseT, normalT, null);
        createBoxModel(30.0f, 10.0f, 10.0f, diffuseT, normalT, specT);
        
        // Now some tile
        diffuseT = loadTexture(loc + "buildings/bldg_floor_tile.png");
        normalT = loadTexture(loc + "buildings/bldg_floor_tile_N.png");
        createBoxModel(10.0f, 20.0f, 10.0f, diffuseT, null, null);
        createBoxModel(20.0f, 20.0f, 10.0f, diffuseT, normalT, null);
        
        // Now some tile
        diffuseT = loadTexture(loc + "terrain/terr_pebbles_001.png");
        normalT = loadTexture(loc + "terrain/terr_pebbles_N_001.png");
        specT = loadTexture(loc + "terrain/terr_pebbles_S_001.png");
        createBoxModel(-10.0f, 20.0f, 10.0f, diffuseT, null, null);
        createBoxModel(-20.0f, 20.0f, 10.0f, diffuseT, normalT, null);
        createBoxModel(-30.0f, 20.0f, 10.0f, diffuseT, normalT, specT);
        
                
        // Now some tile
        diffuseT = loadTexture(loc + "terrain/terr_water_001.png");
        normalT = loadTexture(loc + "terrain/terr_water_N_001.png");
        specT = loadTexture(loc + "terrain/terr_water_S_001.png");
        createBoxModel(-10.0f, 10.0f, 10.0f, diffuseT, null, null);
        createBoxModel(-20.0f, 10.0f, 10.0f, diffuseT, normalT, null);
        createBoxModel(-30.0f, 10.0f, 10.0f, diffuseT, normalT, specT);
                        
        // Now some tile
        diffuseT = loadTexture(loc + "vegetation/veg_shrub_002.png");
        normalT = loadTexture(loc + "vegetation/veg_shrub_N_002.png");
        specT = loadTexture(loc + "vegetation/veg_shrub_S_002.png");
        createBoxModel(-10.0f, 0.0f, 10.0f, diffuseT, null, null);
        createBoxModel(-20.0f, 0.0f, 10.0f, diffuseT, normalT, null);
        createBoxModel(-30.0f, 0.0f, 10.0f, diffuseT, normalT, specT);
        
    }

    private Texture loadTexture(String path) {
        Texture texture = null;
        try {
            URL url = new URL(path);
            texture = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            texture.setWrap(Texture.WrapMode.Repeat);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        }
        return (texture);
    }
     
    private void createBoxModel(float x, float y, float z,
            Texture dMap, Texture nMap, Texture sMap) {
        Node node = new Node();
        RoundedBox box = new RoundedBox("BOX");
        node.attachChild(box);
        node.setLocalScale(new Vector3f(5.0f, 5.0f, 5.0f));
        
        TangentBinormalGenerator.generate(box);
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        TextureState ts = (TextureState)wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        if (dMap != null) {
           ts.setTexture(dMap, 0); 
        }
        if (nMap != null) {
           ts.setTexture(nMap, 1); 
        }
        if (sMap != null) {
           ts.setTexture(sMap, 2); 
        }
        shader.applyToGeometry(box);
        node.setRenderState(buf);
        node.setRenderState(ts);
        node.setLocalTranslation(x, y, z);

        Entity e = new Entity("Box ");
        RenderComponent rc = wm.getRenderManager().createRenderComponent(node);
        e.addComponent(RenderComponent.class, rc);
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        RotationProcessor rp = new RotationProcessor("Teapot Rotator", wm,
                node, (float) (1.0f * Math.PI / 180.0f));
        pcc.addProcessor(rp);
        //e.addComponent(ProcessorCollectionComponent.class, pcc);
        wm.addEntity(e);
    }
        
    /**
     * Create 50 randomly placed teapots, with roughly half of them transparent
     */
    private void createRandomTeapots(WorldManager wm) {
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;
        boolean transparent = false;
        int numTeapots = 25;
        Random r = new Random();
        RenderComponent sc = null;
        JMECollisionComponent cc = null;
        Entity e = null;
        JMECollisionSystem collisionSystem = (JMECollisionSystem) 
                wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        
        Texture tex2d = null;
        Texture normMap = null;
        try {
            URL url = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/buildings/bldg_brick_002.png");
            tex2d = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            tex2d.setWrap(Texture.WrapMode.Repeat);
            url = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/buildings/bldg_brick_N_002.png");
            normMap = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.NearestNeighborNoMipMaps,
                    Texture.MagnificationFilter.NearestNeighbor);
            normMap.setWrap(Texture.WrapMode.Repeat);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        }
        
        for (int i=0; i<numTeapots; i++) {
            x = (r.nextFloat()*50.0f) - 25.0f;
            y = (r.nextFloat()*50.0f) - 25.0f;
            z = (r.nextFloat()*50.0f) - 25.0f;
            transparent = r.nextBoolean();
            
            Node teapot = createTeapotModel(x, y, z, transparent, tex2d, normMap);
            teapot.setLocalScale(5.0f);
            
            //teapot.setCullHint(CullHint.Never);
            //shadowMapBuffer.addRenderScene(teapot);
            
            e = new Entity("Teapot " + i);
            sc = wm.getRenderManager().createRenderComponent(teapot);
            cc = collisionSystem.createCollisionComponent(teapot);
            e.addComponent(RenderComponent.class, sc);
            e.addComponent(CollisionComponent.class, cc);

            
            ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
            RotationProcessor rp = new RotationProcessor("Teapot Rotator", wm, 
                teapot, (float) (1.0f * Math.PI / 180.0f));       
            pcc.addProcessor(rp);
            //e.addComponent(ProcessorCollectionComponent.class, pcc);
            wm.addEntity(e);
                        
        }
    }
    
    private Node createTeapotModel(float x, float y, float z, boolean transparent, 
            Texture t, Texture nMap) {
        Node node = new Node();
        //Teapot teapot = new Teapot();
        //teapot.resetData();
        RoundedBox teapot = new RoundedBox("BOX");
        node.attachChild(teapot);

        //Triangle[] tris = new Triangle[teapot.getTriangleCount()];

        //BoundingBox bbox = new BoundingBox();
        //bbox.computeFromTris(teapot.getMeshAsTriangles(tris), 0, tris.length);
        
        TangentBinormalGenerator.generate(teapot);

        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        MaterialState matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(color);
        
        TextureState ts = (TextureState)wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setTexture(t, 0);
        if (transparent) {
            ts.setTexture(nMap, 1);
        }
        
        shader.applyToGeometry(teapot);
        
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setRenderState(ts);
        node.setLocalTranslation(x, y, z);
        //node.setModelBound(bbox); 
        
        return (node);
    }
    
    private void createFloor() {
                
        RenderComponent orthoRC = null;
        Quad quadGeo = null;
        Node orthoQuad = new Node();
        quadGeo = new Quad("Ortho", 100, 100);
        Entity e = new Entity("Ortho ");
        
        TangentBinormalGenerator.generate(quadGeo);
        
        TexCoords coords = quadGeo.getTextureCoords(0);
        coords.coords.rewind();
        coords.coords.put(0.0f).put(10.0f);
        coords.coords.put(0.0f).put(0.0f);
        coords.coords.put(10.0f).put(0.0f);
        coords.coords.put(10.0f).put(10.0f);
        
        Texture tex2d = null;
        Texture normMap = null;
        try {
            URL url = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/buildings/bldg_floor_tile.png");
            tex2d = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            tex2d.setWrap(Texture.WrapMode.Repeat);
            url = new URL("file:/Users/runner/NetBeansProjects/lg3d-wonderland-art-src/orientation/buildings/bldg_floor_tile_N.png");
            normMap = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.NearestNeighborNoMipMaps,
                    Texture.MagnificationFilter.NearestNeighbor);
            normMap.setWrap(Texture.WrapMode.Repeat);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        }
        
        shader.applyToGeometry(quadGeo);
        
        orthoQuad.attachChild(quadGeo);
        Quaternion q = new Quaternion();
        q.fromAngleAxis((float)(Math.PI/2.0), new Vector3f(1.0f, 0.0f, 0.0f));
        orthoQuad.setLocalRotation(q);

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        orthoQuad.setRenderState(buf);
                
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setEnabled(true);
        ts.setTexture(tex2d, 0);
        ts.setTexture(normMap, 1);
        quadGeo.setRenderState(ts);
 
        orthoRC = wm.getRenderManager().createRenderComponent(orthoQuad);
        e.addComponent(RenderComponent.class, orthoRC);
        wm.addEntity(e);
    }
}
