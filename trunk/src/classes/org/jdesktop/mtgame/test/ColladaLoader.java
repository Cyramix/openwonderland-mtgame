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
import org.jdesktop.mtgame.processor.LightNodeRotator;
import org.jdesktop.mtgame.processor.PostEventProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.processor.FPSCameraProcessor;
import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import org.jdesktop.mtgame.shader.DiffuseMap;
import org.jdesktop.mtgame.util.GraphOptimizer;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.CameraNode;
import com.jme.scene.TriMesh;
import com.jme.scene.SharedMesh;
import com.jme.image.Texture;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.light.DirectionalLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.light.LightNode;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.Geometry;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.scene.Line;
import com.jme.math.*;
import com.jme.animation.BoneAnimation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import com.jme.animation.AnimationController;
import com.jme.scene.Controller;

import java.util.Random;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.FloatBuffer;
import com.jme.scene.TexCoords;
import com.jme.util.geom.TangentBinormalGenerator;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class ColladaLoader {
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
    
    private SwingFrame frame = null;
    private String assetDir = "/Users/runner/Desktop/models/servers/";
    private String loadfile = assetDir + "SUNPOD_complete.dae";
    private boolean showBounds = false;
    private boolean useAlphaTest = false;
    private boolean doRotate = false;
    private boolean doScale = false;
    private boolean lighting = false;
    private boolean doCull = false;
    private boolean optimize = false;
    
    public ColladaLoader(String[] args) {
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        createUI(wm);
        createLights();
        createCameraEntity(wm);   
        createGrid(wm);
        wm.addEntity(grid);
        createAxis();
        wm.addEntity(axis);
        //frame.loadFile(loadfile, true, new Vector3f());
        //createRandomTeapots(wm);
        
    }

    private void createLights() {
        float radius = 20.0f;
        float y = 20.0f;
        float x = (float)(radius*Math.cos(Math.PI/6));
        float z = (float)(radius*Math.sin(Math.PI/6));
        createDirLight(x, y, z);
        x = (float)(radius*Math.cos(5*Math.PI/6));
        z = (float)(radius*Math.sin(5*Math.PI/6));
        createDirLight(x, y, z);
        x = (float)(radius*Math.cos(3*Math.PI/2));
        z = (float)(radius*Math.sin(3*Math.PI/2));
        createDirLight(x, y, z);
    }

    private void createLight(float x, float y, float z) {
        lightNode = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
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
    }

    private void createDirLight(float x, float y, float z) {
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

        Entity e = new Entity("Light Rotator");
        //e.addComponent(RotationProcessor.class, rp);
        e.addComponent(RenderComponent.class, lsp);
        //wm.addEntity(e);
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
        //FPSCameraProcessor eventProcessor = new FPSCameraProcessor(cameraListener, cameraNode, wm, camera, false, false, null);

        OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);
        
        AWTInputComponent selectionListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);        
        //MouseSelectionProcessor selector = new MouseSelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
        //EyeSelectionProcessor selector = new EyeSelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);
        //SelectionProcessor selector = new SelectionProcessor(selectionListener, wm, camera, camera, width, height, eventProcessor);

        //selector.setRunInRenderer(true);
        
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
        ColladaLoader worldBuilder = new ColladaLoader(args);
        
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
    
    class SwingFrame extends JFrame implements FrameRateListener, ActionListener, ResourceLocator {

        JPanel contentPane;
        JPanel menuPanel = new JPanel();
        JPanel canvasPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        JLabel fpsLabel = new JLabel("FPS: ");
        
        JRadioButton coordButton = new JRadioButton("Coords", true);
        JRadioButton gridButton = new JRadioButton("Grid", true);
        JRadioButton alphaButton = new JRadioButton("AlphaTest", false);
        JRadioButton rotateButton = new JRadioButton("SU Rotate", false);
        JRadioButton scaleButton = new JRadioButton("SU Scale", false);
        JRadioButton lightButton = new JRadioButton("Lighting", false);
        JRadioButton cullButton = new JRadioButton("Cull", false);
        JRadioButton optButton = new JRadioButton("Optimize", true);
        JMenuItem loadItem = null;
        JMenuItem exitItem = null;
        JMenuItem createTeapotItem = null;
        String textureSubdir = "file:" + assetDir + "./";
        String textureSubdirName = assetDir + "./";
        Entity currentEntity = null;


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
            //rb.setBackgroundColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
            wm.getRenderManager().addRenderBuffer(rb);
            canvas = ((OnscreenRenderBuffer)rb).getCanvas();
            canvas.setVisible(true);
            canvas.setBounds(0, 0, width, height);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());           
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);
            
            // The options panel
            optionsPanel.setLayout(new GridLayout(20, 1));
            
            coordButton.addActionListener(this);
            optionsPanel.add(coordButton);
          
            gridButton.addActionListener(this);
            optionsPanel.add(gridButton);

            alphaButton.addActionListener(this);
            optionsPanel.add(alphaButton);

            rotateButton.addActionListener(this);
            optionsPanel.add(rotateButton);

            scaleButton.addActionListener(this);
            optionsPanel.add(scaleButton);

            lightButton.addActionListener(this);
            optionsPanel.add(lightButton);

            cullButton.addActionListener(this);
            optionsPanel.add(cullButton);

            optButton.addActionListener(this);
            optionsPanel.add(optButton);
            
            contentPane.add(optionsPanel, BorderLayout.WEST);
            
            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
            
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, this);
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
        private void addModel(Node model, Vector3f trans) {
            Node modelRoot = new Node("Model");

            GraphOptimizer go = new GraphOptimizer();
            if (optimize) {
                go.removeSharedMeshes(model);
            }
                    
            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            modelRoot.setRenderState(buf);
            //modelRoot.setLocalScale(1.0f);

            CullState culls = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
            if (doCull) {
                culls.setCullFace(CullState.Face.Back);
            } else {
                culls.setCullFace(CullState.Face.None);
            }
            modelRoot.setRenderState(culls);

            Quaternion rot = new Quaternion();
            Vector3f axis = new Vector3f(1.0f, 0.0f, 0.0f);
            float angle = -1.57079632679f;
            rot.fromAngleAxis(angle, axis);
            if (doRotate) {
                modelRoot.setLocalRotation(rot);
            }

            if (doScale) {
                modelRoot.setLocalScale(0.1f);
            }
            modelRoot.setLocalTranslation(trans.x, trans.y, trans.z);
            
            //System.out.println("Adding: " + model);
            modelRoot.attachChild(model);
            models.add(modelRoot);
            
            Entity e = new Entity("Model");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);

            if (!lighting) {
                sc.setLightingEnabled(false);
            }
            JMECollisionSystem cs = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
            JMECollisionComponent cc = cs.createCollisionComponent(model);
            e.addComponent(RenderComponent.class, sc);
            e.addComponent(JMECollisionComponent.class, cc);
            currentEntity = e;
            wm.addEntity(e);              
        }
    
        /**
         * The method which gets the state change from the buttons
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == coordButton) {
                boolean val = coordButton.isSelected();
                if (val != coordsOn) {
                    if (coordsOn) {
                        coordsOn = false;
                        wm.removeEntity(axis);
                    } else {
                        coordsOn = true;
                        wm.addEntity(axis);
                    }
                }
            }
            
            if (e.getSource() == gridButton) {
                boolean val = gridButton.isSelected();
                if (val != gridOn) {
                    if (gridOn) {
                        gridOn = false;
                        wm.removeEntity(grid);
                    } else {
                        gridOn = true;
                        wm.addEntity(grid);
                    }
                }
            }

            if (e.getSource() == alphaButton) {
                useAlphaTest = alphaButton.isSelected();
            }

            if (e.getSource() == rotateButton) {
                doRotate = rotateButton.isSelected();
            }

            if (e.getSource() == scaleButton) {
                doScale = scaleButton.isSelected();
            }

            if (e.getSource() == lightButton) {
                lighting = lightButton.isSelected();
            }

            if (e.getSource() == cullButton) {
                doCull = cullButton.isSelected();
            }

            if (e.getSource() == optButton) {
                optimize = optButton.isSelected();
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

                    if (currentEntity != null) {
                        wm.removeEntity(currentEntity);
                    }

                    assetDir = chooser.getSelectedFile().getParent() + "/";
                    textureSubdir = "file:" + assetDir + "./";
                    textureSubdirName = assetDir + "./";
                    // Now load the model
                    if (useAlphaTest) {
                        System.setProperty("Collada.useAlphaTest", "true");
                    } else {
                        System.setProperty("Collada.useAlphaTest", "false");
                    }
                    ColladaImporter.load(fileStream, "Model");
                    Node model = ColladaImporter.getModel();
                    //parseModel(0, model);
                    addModel(model, new Vector3f());
                }
            }
            
            if (e.getSource() == exitItem) {
                System.exit(1);
            }
        }
        
        void loadFile(String filename, boolean normalMap, Vector3f trans) {
            FileInputStream fileStream = null;
            ArrayList transpList = new ArrayList();
            ArrayList<Box> boundsList = new ArrayList<Box>();
            
            System.out.println("You chose to open this file: " + filename);
            try {
                fileStream = new FileInputStream(filename);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            }

            // Now load the model
            ColladaImporter.load(fileStream, "Model");
            Node model = ColladaImporter.getModel();

//            ArrayList names = ColladaImporter.getControllerNames();
//            AnimationController ac = null;
//            System.out.println("Skel?: " + ColladaImporter.getSkeletonNames());
//            //Bone skel = ColladaImporter.getSkeleton(ColladaImporter.getSkeletonNames().get(0));
//            if (names.size() > 0) {
//                ac = new AnimationController();
//                ac.setRepeatType(Controller.RT_WRAP);
//                ac.setActive(true);
//                System.out.println("Found " + names.size() + " controllers");
//                for (int i = 0; i < names.size(); i++) {
//                    BoneAnimation ba = ColladaImporter.getAnimationController((String) names.get(i));
//                    ba.setInterpolate(true);
//                    ba.setInterpolationRate(1.0f/100.0f);
//                    ac.addAnimation(ba);
//                    ac.setActiveAnimation(ba);
//                    System.out.println("Loading Controller " + names.get(i) + ": " + ba);
//                }
//                model.addController(ac);
//                wm.getRenderManager().addToAnimationList(model);
//            }


            //model.setLocalTranslation(-10.0f, 0.0f, 0.0f);
            //model.setLocalScale(5.0f);
//            transpList.clear();
//            boundsList.clear();
//            parseModel(0, model, normalMap, transpList, boundsList);
//            System.out.println("BoundsList SIZE: " + boundsList.size());
//            for (int i=0; i<boundsList.size(); i++) {
//                model.attachChild(boundsList.get(i));
//            }
//            System.out.println("TLIST SIZE: " + transpList.size());
//            for (int i=0; i<transpList.size(); i++) {
//                TriMesh tm = (TriMesh) transpList.get(i);
//                Node n = org.jdesktop.mtgame.util.Geometry.explodeIntoSpatials(wm, tm);
//                Node p = tm.getParent();
//                tm.removeFromParent();;
//                p.attachChild(n);
//            }
            //addNormals(model, null);
            //parseModel(0, model);
            addModel(model, trans);
            
        }

        void addNormals(Spatial s, Node normals) {
            if (s instanceof Node) {
                Node n = (Node)s;
                Node nn = new Node();
                for (int i=0; i<n.getQuantity(); i++) {
                    Spatial child = n.getChild(i);
                    addNormals(child, nn);
                }
                n.attachChild(nn);
            } else if (s instanceof Geometry) {
                Geometry geo = (Geometry)s;

                CullState cs = (CullState)geo.getRenderState(RenderState.StateType.Cull);
                if (cs != null) {
                    System.out.println("Cull State for " + geo + " is: " + cs.getCullFace());
                } else {
                    System.out.println("No Cull State for " + geo);
                }
                Vector3f[] lineData = new Vector3f[geo.getVertexCount()*2];
                int normalIndex = 0;
                FloatBuffer nBuffer = geo.getNormalBuffer();
                FloatBuffer vBuffer = geo.getVertexBuffer();
                vBuffer.rewind();
                nBuffer.rewind();
                float nScale = 50.0f;
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
                Line normalGeometry = new Line("Normal Geometry", lineData, null, null, null);
                normalGeometry.setDefaultColor(ColorRGBA.red);
                normals.attachChild(normalGeometry);
            }
        }
        
        void parseModel(int level, Spatial model, boolean normalMap, ArrayList tList, ArrayList bList) {
            for (int i=0; i<level; i++) {
                System.out.print("\t");
            }

            if (model instanceof Node) {
                Node n = (Node)model;
                System.out.println("Node " + n + " with children: " + n.getQuantity());
                for (int i=0; i<n.getQuantity(); i++) {
                    parseModel(level+1, n.getChild(i), normalMap, tList, bList);
                }
            } else if (model instanceof Geometry) {
                Geometry geo = (Geometry)model;
                System.out.println("Geometry: " + geo);
                System.out.println("Bounds: " + geo.getModelBound());
                if (showBounds && bList != null) {
                    BoundingVolume b = geo.getModelBound();
                    if (b instanceof BoundingBox) {
                        BoundingBox bbox = (BoundingBox)b;
                        Box box = new Box("Bounds: " + geo, bbox.getCenter(), bbox.xExtent, bbox.yExtent, bbox.zExtent);
                        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
                        buf.setEnabled(true);
                        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
                        box.setRenderState(buf);
                        bList.add(box);
                    }
                }
                
                String str = "";
                if (geo instanceof TriMesh && str != null) {
                    //System.out.println("Generating Tangents: " + geo);
                    //TangentBinormalGenerator.generate((TriMesh)geo);
//                    System.out.println("Vertex Buffer: " + geo.getVertexBuffer());
//                    System.out.println("Normal Buffer: " + geo.getNormalBuffer());
//                    System.out.println("Color Buffer: " + geo.getColorBuffer());
//                    System.out.println("TC 0 Buffer: " + geo.getTextureCoords(0));
//                    System.out.println("TC 1 Buffer: " + geo.getTextureCoords(1));
//                    System.out.println("Tangent Buffer: " + geo.getTangentBuffer());
//                    System.out.println("Binormal Buffer: " + geo.getBinormalBuffer());
                }

                BlendState bs = (BlendState)geo.getRenderState(RenderState.StateType.Blend);
                if (geo instanceof TriMesh && bs != null && bs.isEnabled() && bs.isBlendEnabled()) {
                    tList.add(geo);
                }
            } else {
                System.out.println("Unkown: " + model);
            }

        }

        void parseModel(int level, Spatial model) {
//            for (int i = 0; i < level; i++) {
//                System.out.print("\t");
//            }

            if (model instanceof Node) {
                Node n = (Node) model;
                //System.out.println("Node " + n + " with children: " + n.getQuantity());
                for (int i = 0; i < n.getQuantity(); i++) {
                    parseModel(level + 1, n.getChild(i));
                }
            } else if (model instanceof Geometry) {
                Geometry geo = (Geometry)model;
                BlendState bs = (BlendState)geo.getRenderState(RenderState.StateType.Blend);
                if (bs != null) {
                    bs.setEnabled(true);
                    bs.setBlendEnabled(false);
                    bs.setReference(0.5f);
                    bs.setTestFunction(BlendState.TestFunction.GreaterThan);
                    bs.setTestEnabled(true);
                    //s.setRenderState(as);
                }
                //System.out.println("Geometry " + model);
//                TextureState ts = (TextureState)geo.getRenderState(RenderState.StateType.Texture);
//                for (int i=0; i<ts.getNumberOfFixedUnits(); i++) {
//                    Texture t = ts.getTexture(i);
//                    if (t != null) {
//                        System.out.println("Texture Unit " + i + ": " + t);
//                    }
//                }
            }
        }
    }
}
