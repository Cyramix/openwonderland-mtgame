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
//import org.jdesktop.mtgame.shader.FlatShader;
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
import java.io.FileInputStream;
import com.jme.scene.TexCoords;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.scene.Skybox;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import com.jme.scene.Spatial.TextureCombineMode;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class OrientationWorld {
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
    private String loadfile = "/Users/runner/Desktop/Orientation/terrain_test.dae";
    private String configFile = "/Users/runner/Desktop/Orientation/terrain_test.mtg";
    private String textureDir = "/Users/runner/Desktop/Orientation/textures";
    private Skybox skybox = null;

    
    public OrientationWorld(String[] args) {
        wm = new WorldManager("TestWorld");
        
        try {
            FileInputStream fs = new FileInputStream(configFile);
            wm.loadConfiguration(fs);
        } catch (java.io.FileNotFoundException e) {
            System.out.println(e);
        }
        wm.setTextureDirectory(textureDir);
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        collisionSystem = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        
        LightNodeRotator rp = new LightNodeRotator("Light Rotator", wm,
                lightNode, new Vector3f(0, 0, 100), (float) (1.0f * Math.PI / 180.0f));
        Entity e = new Entity("Light Rotator");
        e.addComponent(RotationProcessor.class, rp);
        //wm.addEntity(e);
        
        createUI(wm);  
        createCameraEntity(wm);  
        setGlobalLights();
        createSkybox(wm);
        frame.loadFile(loadfile, true);
        //frame.loadFile(loadfile, false);
        //createRandomTeapots(wm);
        
    }
    
    public void setGlobalLights() {
        LightNode globalLight1 = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.95f, 0.95f, 0.95f, 1.0f));
        light.setSpecular(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setEnabled(true);
        globalLight1.setLight(light);
        globalLight1.setLocalTranslation(0.0f, 500.0f, 500.0f);

        LightNode globalLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setAmbient(new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f));
        light.setSpecular(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
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
        String urlpath = "file:/Users/runner/NetBeansProjects/jme-20/trunk/src/";
        String dir = urlpath + "jmetest/data/skybox1/";
        
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

        CullState cullState = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);
        cullState.setEnabled(true);
        skybox.setRenderState(cullState);

        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
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
        FPSCameraProcessor eventProcessor = new FPSCameraProcessor(cameraListener, cameraNode, wm, camera, true, false);
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
        String textureSubdir = "file:/Users/runner/Desktop/Orientation/textures/";
        String textureSubdirName = "/Users/runner/Desktop/Orientation/textures/";
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
                    
            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            modelRoot.setRenderState(buf);
            
            //System.out.println("Adding: " + model);
            modelRoot.attachChild(model);
            models.add(modelRoot);
            
            Entity e = new Entity("Model");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);
            JMECollisionComponent cc = collisionSystem.createCollisionComponent(modelRoot);
            e.addComponent(JMECollisionComponent.class, cc);
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
                    System.out.println("Turning Coordinates Off");
                } else {
                    coordsOn = true;
                    System.out.println("Turning Coordinates On");
                }
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
        
        void loadFile(String filename, boolean normalMap) {       
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
            addModel(model);
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
                
                FloatBuffer nBuffer = geo.getNormalBuffer();
                FloatBuffer vBuffer = geo.getVertexBuffer();
                vBuffer.rewind();
                nBuffer.rewind();
                float nScale = 2.0f;
                for (int i=0; i<geo.getVertexCount(); i++) {
                    lineData[normalIndex] = new Vector3f();
                    lineData[normalIndex].x = vBuffer.get();
                    lineData[normalIndex].y = vBuffer.get();
                    lineData[normalIndex].z = vBuffer.get();
                    lineData[normalIndex+1] = new Vector3f();
                    lineData[normalIndex+1].x = lineData[normalIndex].x + nScale * nBuffer.get();
                    lineData[normalIndex+1].y = lineData[normalIndex].y + nScale * nBuffer.get();
                    lineData[normalIndex+1].z = lineData[normalIndex].z + nScale * nBuffer.get();
                    normalIndex += 2;
                }
            }
        }
        
        void assignShader(Geometry geo, String shaderFlag, boolean normalMap) {
            if (shaderFlag.equals("MTGAMEDiffuseNormalMap")) {
                if (normalMap) {
                    DiffuseNormalMap shader = new DiffuseNormalMap(wm);
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
