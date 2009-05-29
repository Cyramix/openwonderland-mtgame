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
import org.jdesktop.mtgame.processor.LightNodeRotator;
import org.jdesktop.mtgame.processor.PostEventProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.processor.FPSCameraProcessor;
import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import org.jdesktop.mtgame.shader.DiffuseMap;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.CameraNode;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.light.LightNode;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.shape.Teapot;
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
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.ResourceLocator;

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
    private String assetDir = "/Users/runner/Desktop/models/nicole/";
    private String loadfile = assetDir + "models/CapeHouse3-exp.dae";
    
    public ColladaLoader(String[] args) {
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        lightNode = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        //light.setLocation(new Vector3f(10, 10, 10));
        light.setEnabled(true);
        lightNode.setLight(light);
        lightNode.setLocalTranslation(100.0f, 100.0f, 100.0f);
        wm.getRenderManager().addLight(lightNode);
        
        LightNodeRotator rp = new LightNodeRotator("Light Rotator", wm,
                lightNode, new Vector3f(0, 0, 100), (float) (1.0f * Math.PI / 180.0f));
        Entity e = new Entity("Light Rotator");
        e.addComponent(RotationProcessor.class, rp);
        //wm.addEntity(e);
        
        createUI(wm);  
        createCameraEntity(wm);   
        createGrid(wm);
        //wm.addEntity(grid);
        createAxis();
        //wm.addEntity(axis);
        //frame.loadFile(loadfile, true);
        frame.loadFile(loadfile, false);
        //createRandomTeapots(wm);
        
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
        //FPSCameraProcessor eventProcessor = new FPSCameraProcessor(cameraListener, cameraNode, wm, camera);
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
        
        JToggleButton coordButton = new JToggleButton("Coords", true);
        JToggleButton gridButton = new JToggleButton("Grid", true);
        JMenuItem loadItem = null;
        JMenuItem exitItem = null;
        JMenuItem createTeapotItem = null;
        String textureSubdir = "file:" + assetDir + "images/";
        String textureSubdirName = assetDir + "images/";
        //String textureSubdir = "file:/Users/runner/Desktop/";
        //String textureSubdirName = "/Users/runner/Desktop/";


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
        private void addModel(Node model) {
            Node modelRoot = new Node("Model");
                    
            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            modelRoot.setRenderState(buf);
            modelRoot.setLocalScale(0.04f);

            Quaternion rot = new Quaternion();
            Vector3f axis = new Vector3f(1.0f, 0.0f, 0.0f);
            float angle = -1.57079632679f;
            rot.fromAngleAxis(angle, axis);
            modelRoot.setLocalRotation(rot);

            
            //System.out.println("Adding: " + model);
            modelRoot.attachChild(model);
            models.add(modelRoot);
            
            Entity e = new Entity("Model");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);
            e.addComponent(RenderComponent.class, sc);
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
                    parseModel(0, model, false, null);
                    addModel(model);                 
                }
            }
            
            if (e.getSource() == exitItem) {
                System.exit(1);
            }
        }
        
        void loadFile(String filename, boolean normalMap) {       
            FileInputStream fileStream = null;
            ArrayList transpList = new ArrayList();
            
            System.out.println("You chose to open this file: " + filename);
            try {
                fileStream = new FileInputStream(filename);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            }

            // Now load the model
            ColladaImporter.load(fileStream, "Model");
            Node model = ColladaImporter.getModel();
            if (normalMap) {
                model.setLocalTranslation(10.0f, 0.0f, 0.0f);
            } else {
                model.setLocalTranslation(-10.0f, 0.0f, 0.0f);
            }
            model.setLocalScale(5.0f);
            transpList.clear();
            //parseModel(0, model, normalMap, transpList);
            //for (int i=0; i<transpList.size(); i++) {
            //    TriMesh tm = (TriMesh) transpList.get(i);
            //    Node n = org.jdesktop.mtgame.util.Geometry.explodeIntoSpatials(wm, tm);
            //    Node p = tm.getParent();
            //    tm.removeFromParent();;
            //    p.attachChild(n);
            //}
            addModel(model);
        }
        
        void parseModel(int level, Spatial model, boolean normalMap, ArrayList tList) {
            if (model instanceof Node) {
                Node n = (Node)model;
                for (int i=0; i<n.getQuantity(); i++) {
                    parseModel(level+1, n.getChild(i), normalMap, tList);
                }
            } else if (model instanceof Geometry) {
                Geometry geo = (Geometry)model;
                
                String str = "";
                //if (geo instanceof TriMesh && str != null) {
                    //System.out.println("Generating Tangents: " + geo);
                    //TangentBinormalGenerator.generate((TriMesh)geo);
                    //System.out.println("Vertex Buffer: " + geo.getVertexBuffer());
                    //System.out.println("Normal Buffer: " + geo.getNormalBuffer());
                    //System.out.println("Color Buffer: " + geo.getColorBuffer());
                    //System.out.println("TC 0 Buffer: " + geo.getTextureCoords(0));
                    //System.out.println("TC 1 Buffer: " + geo.getTextureCoords(1));
                    //System.out.println("Tangent Buffer: " + geo.getTangentBuffer());
                    //System.out.println("Binormal Buffer: " + geo.getBinormalBuffer());
                    //assignShader(geo, str, normalMap);
                //}

                BlendState bs = (BlendState)geo.getRenderState(RenderState.StateType.Blend);
                if (geo instanceof TriMesh && bs != null && bs.isEnabled() && bs.isBlendEnabled()) {
                    tList.add(geo);
                }
            }
        }
        
        void assignShader(Geometry geo, String shaderFlag, boolean normalMap) {
            if (shaderFlag.equals("MTGAMEDiffuseNormalMap")) {
                if (normalMap) {
                    DiffuseNormalMap shader = new DiffuseNormalMap(wm);
                    System.out.println("Assigning to " + geo);
                    shader.applyToGeometry(geo);
                } else {
                    DiffuseMap shader = new DiffuseMap(wm);
                    shader.applyToGeometry(geo);
                }
                //System.out.println("Assigning Shader: " + shaderFlag);
            }
        }
    }
}
