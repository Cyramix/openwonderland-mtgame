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
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.PathCameraProcessor;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.PassNode;
import com.jme.scene.Spatial;
import com.jme.image.Texture;
import com.jme.scene.Spatial.TextureCombineMode;
import com.jme.scene.state.TextureState;
import com.jme.scene.PassNodeState;
import com.jme.util.TextureManager;
import com.jme.scene.CameraNode;
import com.jme.renderer.Camera;
import com.jme.scene.Skybox;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Torus;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.RenderState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.Line;
import com.jme.math.*;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.RawHeightMap;
import com.jmex.effects.water.ProjectedGrid;
import com.jmex.effects.water.WaterRenderPass;
import com.jmex.effects.water.WaterHeightGenerator;


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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import com.jmex.model.collada.ColladaImporter;
import java.nio.FloatBuffer;

import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.bounding.BoundingBox;
import com.jme.light.PointLight;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;

import java.util.Random;

/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class ProjectedWater implements RenderUpdater {
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
    
    WaterRenderPass waterEffectRenderPass = null;
    Camera jmeCam = null;
    Skybox skybox = null;
    Spatial reflectionTerrain = null;
    PassNode splatTerrain = null;
    Quad waterQuad = null;
    private float farPlane = 10000.0f;
    //String urlpath = "file:/Documents and Settings/runner/My Documents/NetBeansProjects/trunk/src/";
    String urlpath = "file:/Users/runner/NetBeansProjects/jme-20/trunk/src/";
    FogState fogState = null;
    Node reflectedNode = new Node("Reflected");
    Node rootNode = new Node ("Root");
    Node movingObjects = null;
    ProjectedGrid projectedGrid = null;

    
    Vector3f[] positions = {
        new Vector3f(-237.04662f, 129.85626f, 77.02111f),
        new Vector3f(-428.3452f, 27.837791f, 79.38916f),
        new Vector3f(-428.3452f, 27.837791f, 79.38916f),
        new Vector3f(-47.800785f, 85.49566f, 234.94858f)
    };
    Quaternion[] rots = {
        new Quaternion(0.13472061f, 0.78748035f, -0.18542701f, 0.57213795f),
        new Quaternion(0.01673856f, 0.7685784f, -0.020125935f, 0.6392198f),
        new Quaternion(0.01673856f, 0.7685784f, -0.020125935f, 0.6392198f),
        new Quaternion(0.016104473f, 0.98203033f, -0.1599338f, 0.09888516f)
    };
    
    float[] times = {0.0f, 7.0f, 12.0f, 30.0f };
    
    /**
     * A list of the models we are looking at
     */
    private ArrayList models = new ArrayList();
        
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    
    public ProjectedWater(String[] args) {
        wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        createUI(wm);  
        createCameraEntity(wm);   
        setupFog();
        createSkybox(wm);

        reflectedNode.attachChild(skybox);
        movingObjects = createObjects();
        reflectedNode.attachChild(movingObjects);
        rootNode.attachChild(reflectedNode);
        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        //zState.setEnabled(false);
        rootNode.setRenderState(zState);
        
        wm.addRenderUpdater(this, wm);      
    }
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 10000.0f, true);
        rb.setCameraComponent(cc);
        jmeCam = cc.getCamera();
        camera.addComponent(CameraComponent.class, cc);

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        //FPSCameraProcessor eventProcessor = new FPSCameraProcessor(eventListener, cameraNode, wm, camera);
        
        //PathCameraProcessor eventProcessor = new PathCameraProcessor(cameraListener, cameraNode, wm, 
        //        camera, positions, rots, times);
        OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, 
                camera);
        eventProcessor.setRunInRenderer(true);
            
        WaterProcessor wp = new WaterProcessor();
        eventProcessor.addToChain(wp);
          
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
    
    
    private void setupFog() {
        fogState = (FogState) wm.getRenderManager().createRendererState(RenderState.StateType.Fog);
        fogState.setDensity(1.0f);
        fogState.setEnabled(true);
        fogState.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        fogState.setEnd(farPlane);
        fogState.setStart(farPlane / 10.0f);
        fogState.setDensityFunction(FogState.DensityFunction.Linear);
        fogState.setQuality(FogState.Quality.PerVertex);
        rootNode.setRenderState(fogState);
    }
    
    private void createSkybox(WorldManager wm) {
        Texture north = null;
        Texture south = null;
        Texture east = null;
        Texture west = null;
        Texture up = null;
        Texture down = null;
        skybox = new Skybox("skybox", 1000, 1000, 1000);
        setupEnvironment(skybox);

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

        CullState cullState = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cullState.setEnabled(true);
        skybox.setRenderState(cullState);

        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        //zState.setEnabled(false);
        skybox.setRenderState(zState);

        FogState fs = (FogState) wm.getRenderManager().createRendererState(RenderState.StateType.Fog);
        fs.setEnabled(false);
        skybox.setRenderState(fs);

        skybox.setLightCombineMode(Spatial.LightCombineMode.Off);
        skybox.setCullHint(Spatial.CullHint.Never);
        skybox.setTextureCombineMode(TextureCombineMode.Replace);
        skybox.updateRenderState();

        skybox.lockBounds();
        //skybox.lockMeshes();
        
        /*
        Entity e = new Entity("Skybox");
        SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
        e.addComponent(SkyboxComponent.class, sbc);
        wm.addEntity(e);
         * */
    }

    private void createWaterPass(WorldManager wm) {
        
        waterEffectRenderPass = new WaterRenderPass(jmeCam, 4, true, true);
        waterEffectRenderPass.setSpeedReflection(0.01f);
        waterEffectRenderPass.setSpeedRefraction(-0.005f);

        waterEffectRenderPass.setWaterPlane(new Plane(new Vector3f(0.0f, 1.0f,
                0.0f), 0.0f));

        waterEffectRenderPass.setClipBias(0.5f);
        waterEffectRenderPass.setWaterMaxAmplitude(5.0f);
        
        projectedGrid = new ProjectedGrid("ProjectedGrid", jmeCam, 100, 70, 0.01f,
                new WaterHeightGenerator());

        waterEffectRenderPass.setWaterEffectOnSpatial(projectedGrid);
        rootNode.attachChild(projectedGrid);


        waterEffectRenderPass.setReflectedScene(reflectedNode);
        waterEffectRenderPass.setSkybox(skybox);
        rootNode.setCullHint(Spatial.CullHint.Never);
        rootNode.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
        
        RotationProcessor rp = new RotationProcessor("Moving Objects", wm, 
                movingObjects, (float) (1.0f * Math.PI / 180.0f));
            
            
        Entity e = new Entity("Water");
        PassComponent pass = wm.getRenderManager().createPassComponent(waterEffectRenderPass);
        RenderComponent rc = wm.getRenderManager().createRenderComponent(rootNode);
        e.addComponent(ProcessorComponent.class, rp);
        e.addComponent(PassComponent.class, pass);
        e.addComponent(RenderComponent.class, rc);
        wm.addEntity(e);
    }
    
    class WaterProcessor extends ProcessorComponent {
        public void initialize() {
            
        }
        
        public void commit(ProcessorArmingCollection pcc) {
            if (waterEffectRenderPass != null) {
                /*
                Vector3f transVec = new Vector3f(jmeCam.getLocation().x,
                        waterEffectRenderPass.getWaterHeight(), jmeCam.getLocation().z);
                setTextureCoords(0, transVec.x, -transVec.z, textureScale);
                setVertexCoords(transVec.x, transVec.y, transVec.z);
                */
                skybox.getLocalTranslation().set(jmeCam.getLocation());
                
                //wm.addToPassUpdateList(waterEffectRenderPass);
                wm.addToUpdateList(skybox);
            }
        }
        
        public void compute(ProcessorArmingCollection pcc) {
            
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProjectedWater st = new ProjectedWater(args);

    }

    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i + 1]);
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

    public void update(Object obj) {
        if (obj instanceof WorldManager) {
            System.out.println("CREATING WATER");
            createWaterPass(wm);
        }
    }
    
    
    private void setVertexCoords(float x, float y, float z) {
        FloatBuffer vertBuf = waterQuad.getVertexBuffer();
        vertBuf.clear();

        vertBuf.put(x - farPlane).put(y).put(z - farPlane);
        vertBuf.put(x - farPlane).put(y).put(z + farPlane);
        vertBuf.put(x + farPlane).put(y).put(z + farPlane);
        vertBuf.put(x + farPlane).put(y).put(z - farPlane);
    }

    private void setTextureCoords(int buffer, float x, float y,
            float textureScale) {
        x *= textureScale * 0.5f;
        y *= textureScale * 0.5f;
        textureScale = farPlane * textureScale;
        FloatBuffer texBuf;
        texBuf = waterQuad.getTextureCoords(buffer).coords;
        texBuf.clear();
        texBuf.put(x).put(textureScale + y);
        texBuf.put(x).put(y);
        texBuf.put(textureScale + x).put(y);
        texBuf.put(textureScale + x).put(textureScale + y);
    }
    
    
    private Node createObjects() {
        Node objects = new Node("objects");
        Box box = null;
        URL url = null;
        
        try {
            Torus torus = new Torus("Torus", 50, 50, 10, 20);
            torus.setLocalTranslation(new Vector3f(50, -5, 20));
            TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
            url = new URL(urlpath + "jmetest/data/images/Monkey.jpg");
            Texture t0 = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            url = new URL(urlpath + "jmetest/data/texture/north.jpg");
            Texture t1 = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            t1.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.SphereMap);
            ts.setTexture(t0, 0);
            ts.setTexture(t1, 1);
            ts.setEnabled(true);
            torus.setRenderState(ts);
            objects.attachChild(torus);

            ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
            url = new URL(urlpath + "jmetest/data/texture/wall.jpg");
            t0 = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            t0.setWrap(Texture.WrapMode.Repeat);
            ts.setTexture(t0);


            box = new Box("box1", new Vector3f(-10, -10, -10), new Vector3f(10,
                    10, 10));
            box.setLocalTranslation(new Vector3f(0, -7, 0));
            box.setRenderState(ts);
            objects.attachChild(box);

            box = new Box("box2", new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5));
            box.setLocalTranslation(new Vector3f(15, 10, 0));
            box.setRenderState(ts);
            objects.attachChild(box);

            box = new Box("box3", new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5));
            box.setLocalTranslation(new Vector3f(0, -10, 15));
            box.setRenderState(ts);
            objects.attachChild(box);

            box = new Box("box4", new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5));
            box.setLocalTranslation(new Vector3f(20, 0, 0));
            box.setRenderState(ts);
            objects.attachChild(box);

            ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
            url = new URL(urlpath + "jmetest/data/images/Monkey.jpg");
            t0 = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            t0.setWrap(Texture.WrapMode.Repeat);
            ts.setTexture(t0);


            box = new Box("box5", new Vector3f(-50, -2, -50), new Vector3f(50, 2,
                    50));
            box.setLocalTranslation(new Vector3f(0, -15, 0));
            box.setRenderState(ts);
            box.setModelBound(new BoundingBox());
            box.updateModelBound();
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        objects.attachChild(box);

        return objects;
    }

    private void setupEnvironment(Node rootNode) {

        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        rootNode.setRenderState(cs);
        rootNode.setLightCombineMode(Spatial.LightCombineMode.Off);

        FogState fogState = (FogState) wm.getRenderManager().createRendererState(RenderState.StateType.Fog);
        fogState.setDensity(1.0f);
        fogState.setEnabled(true);
        fogState.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        fogState.setEnd(10000);
        fogState.setStart(10000 / 10.0f);
        fogState.setDensityFunction(FogState.DensityFunction.Linear);
        fogState.setQuality(FogState.Quality.PerVertex);
        rootNode.setRenderState(fogState);
    }
}
