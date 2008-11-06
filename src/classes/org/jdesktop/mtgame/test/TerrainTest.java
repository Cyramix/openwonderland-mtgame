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

import org.jdesktop.mtgame.processor.SelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
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
import com.jme.scene.Skybox;
import com.jme.scene.shape.AxisRods;
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
import com.jmex.terrain.util.RawHeightMap;

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

/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class TerrainTest implements RenderUpdater {
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
    
    private float globalSplatScale = 90.0f;
    /**
     * A list of the models we are looking at
     */
    private ArrayList models = new ArrayList();
        
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    
    public TerrainTest(String[] args) {
        wm = new WorldManager("TestWorld");
        
        try {
            vert = new URL("file:/Users/runner/Desktop/runner/Work/mtgame/src/org/jdesktop/mtgame/SampleVertShader");
            frag = new URL("file:/Users/runner/Desktop/runner/Work/mtgame/src/org/jdesktop/mtgame/SampleFragShader");
            //vert = new URL("file:/Documents and Settings/runner/Desktop/Work/mtgame/src/org/jdesktop/mtgame/SampleVertShader");
            //frag = new URL("file:/Documents and Settings/runner/Desktop/Work/mtgame/src/org/jdesktop/mtgame/SampleFragShader");
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
        
        createSkybox(wm);
        
        createTerrain(wm);
        
        //createRoom();
        createTeapots();
       
    }
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 10000.0f, true);
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
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
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
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
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
    
    private void createSkybox(WorldManager wm) {
        Skybox skybox = new Skybox("skybox", 1000, 1000, 1000);

        String dir = "jmetest/data/skybox1/";
        Texture north = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(dir + "1.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        Texture south = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(dir + "3.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        Texture east = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(dir + "2.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        Texture west = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(dir + "4.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        Texture up = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(dir + "6.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        Texture down = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(dir + "5.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);

        skybox.setTexture(Skybox.Face.North, north);
        skybox.setTexture(Skybox.Face.West, west);
        skybox.setTexture(Skybox.Face.South, south);
        skybox.setTexture(Skybox.Face.East, east);
        skybox.setTexture(Skybox.Face.Up, up);
        skybox.setTexture(Skybox.Face.Down, down);
        //skybox.preloadTextures();

        CullState cullState = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);
        cullState.setEnabled(true);
        skybox.setRenderState(cullState);

        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        //zState.setEnabled(false);
        skybox.setRenderState(zState);

        FogState fs = (FogState) wm.getRenderManager().createRendererState(RenderState.RS_FOG);
        fs.setEnabled(false);
        skybox.setRenderState(fs);

        skybox.setLightCombineMode(Spatial.LightCombineMode.Off);
        skybox.setCullHint(Spatial.CullHint.Never);
        skybox.setTextureCombineMode(TextureCombineMode.Replace);
        skybox.updateRenderState();

        skybox.lockBounds();
        //skybox.lockMeshes();
        
        Entity e = new Entity("Skybox");
        SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
        e.addComponent(SkyboxComponent.class, sbc);
        wm.addEntity(e);
    }
    
    private void createTerrain(WorldManager wm) {
        RawHeightMap heightMap = new RawHeightMap(Texture.class.getClassLoader().getResource(
                "jmetest/data/texture/terrain/heights.raw"),
                129, RawHeightMap.FORMAT_16BITLE, false);

        Vector3f terrainScale = new Vector3f(5, 0.003f, 6);
        heightMap.setHeightScale(0.001f);
        TerrainPage page = new TerrainPage("Terrain", 33, heightMap.getSize(),
                terrainScale, heightMap.getHeightMap());
        page.getLocalTranslation().set(0, -9.5f, 0);
        page.setDetailTexture(1, 1);

        // create some interesting texturestates for splatting
        TextureState ts1 = createSplatTextureState(
                "jmetest/data/texture/terrain/baserock.jpg", null);

        TextureState ts2 = createSplatTextureState(
                "jmetest/data/texture/terrain/darkrock.jpg",
                "jmetest/data/texture/terrain/darkrockalpha.png");

        TextureState ts3 = createSplatTextureState(
                "jmetest/data/texture/terrain/deadgrass.jpg",
                "jmetest/data/texture/terrain/deadalpha.png");

        TextureState ts4 = createSplatTextureState(
                "jmetest/data/texture/terrain/nicegrass.jpg",
                "jmetest/data/texture/terrain/grassalpha.png");

        TextureState ts5 = createSplatTextureState(
                "jmetest/data/texture/terrain/road.jpg",
                "jmetest/data/texture/terrain/roadalpha.png");

        TextureState ts6 = createLightmapTextureState("jmetest/data/texture/terrain/lightmap.jpg");

        // alpha used for blending the passnodestates together
        BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        as.setTestEnabled(true);
        as.setTestFunction(BlendState.TestFunction.GreaterThan);
        as.setEnabled(true);

        // alpha used for blending the lightmap
        BlendState as2 = (BlendState) wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        as2.setBlendEnabled(true);
        as2.setSourceFunction(BlendState.SourceFunction.DestinationColor);
        as2.setDestinationFunction(BlendState.DestinationFunction.SourceColor);
        as2.setTestEnabled(true);
        as2.setTestFunction(BlendState.TestFunction.GreaterThan);
        as2.setEnabled(true);

        // //////////////////// PASS STUFF START
        // try out a passnode to use for splatting
        PassNode splattingPassNode = new PassNode("SplatPassNode");
        splattingPassNode.attachChild(page);

        PassNodeState passNodeState = new PassNodeState();
        passNodeState.setPassState(ts1);
        splattingPassNode.addPass(passNodeState);

        passNodeState = new PassNodeState();
        passNodeState.setPassState(ts2);
        passNodeState.setPassState(as);
        splattingPassNode.addPass(passNodeState);

        passNodeState = new PassNodeState();
        passNodeState.setPassState(ts3);
        passNodeState.setPassState(as);
        splattingPassNode.addPass(passNodeState);

        passNodeState = new PassNodeState();
        passNodeState.setPassState(ts4);
        passNodeState.setPassState(as);
        splattingPassNode.addPass(passNodeState);

        passNodeState = new PassNodeState();
        passNodeState.setPassState(ts5);
        passNodeState.setPassState(as);
        splattingPassNode.addPass(passNodeState);

        passNodeState = new PassNodeState();
        passNodeState.setPassState(ts6);
        passNodeState.setPassState(as2);
        splattingPassNode.addPass(passNodeState);
        // //////////////////// PASS STUFF END

        // lock some things to increase the performance
        splattingPassNode.lockBounds();
        splattingPassNode.lockTransforms();
        splattingPassNode.lockShadows();

        splattingPassNode.setCullHint(Spatial.CullHint.Dynamic);
        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zState.setEnabled(true);
        zState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        splattingPassNode.setRenderState(zState);
        
        Entity e = new Entity("Terrain");
        RenderComponent rc = wm.getRenderManager().createRenderComponent(splattingPassNode);
        e.addComponent(RenderComponent.class, rc);
        wm.addEntity(e);
    }
    
    
    private void addAlphaSplat(TextureState ts, String alpha) {
        Texture t1 = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(alpha),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t1.setWrap(Texture.WrapMode.Repeat);
        t1.setApply(Texture.ApplyMode.Combine);
        t1.setCombineFuncRGB(Texture.CombinerFunctionRGB.Replace);
        t1.setCombineSrc0RGB(Texture.CombinerSource.Previous);
        t1.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
        t1.setCombineFuncAlpha(Texture.CombinerFunctionAlpha.Replace);
        ts.setTexture(t1, ts.getNumberOfSetTextures());
    }
    
    private TextureState createSplatTextureState(String texture, String alpha) {
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);

        Texture t0 = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(texture),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        t0.setApply(Texture.ApplyMode.Modulate);
        t0.setScale(new Vector3f(globalSplatScale, globalSplatScale, 1.0f));
        ts.setTexture(t0, 0);

        if (alpha != null) {
            addAlphaSplat(ts, alpha);
        }

        return ts;
    }

    private TextureState createLightmapTextureState(String texture) {
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);

        Texture t0 = TextureManager.loadTexture(Texture.class
                .getClassLoader().getResource(texture),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t0, 0);

        return ts;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TerrainTest st = new TerrainTest(args);

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
        createTeapotEntity(  0.0f, 0.0f,   0.0f, new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
        createTeapotEntity(-15.0f, 0.0f, -15.0f, new ColorRGBA(1.0f, 1.0f, 0.0f, 1.0f));
        createTeapotEntity(-15.0f, 0.0f,  15.0f, new ColorRGBA(0.0f, 1.0f, 1.0f, 1.0f));
        createTeapotEntity( 15.0f, 0.0f,  15.0f, new ColorRGBA(1.0f, 0.0f, 1.0f, 1.0f));
        createTeapotEntity( 15.0f, 0.0f, -15.0f, new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f));
    }
    
    private void createTeapotEntity(float x, float y, float z, ColorRGBA color) {
        RenderComponent sc = null;
        Entity e = null;

        Node teapot = createTeapotModel(x, y, z, color);
        e = new Entity("Teapot");
        sc = wm.getRenderManager().createRenderComponent(teapot);
        e.addComponent(RenderComponent.class, sc);

        RotationProcessor rp = new RotationProcessor("Teapot Rotator", wm,
                teapot, (float) (6.0f * Math.PI / 180.0f));
        e.addComponent(ProcessorComponent.class, rp);
        wm.addEntity(e);
    }
    
    private Node createTeapotModel(float x, float y, float z, ColorRGBA color) {
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.resetData();
        node.attachChild(teapot);

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        GLSLShaderObjectsState shader = (GLSLShaderObjectsState) wm.getRenderManager().createRendererState(RenderState.RS_GLSL_SHADER_OBJECTS);
        shader.setUniform("color", color);
        // shader.load(vert, frag);
        // Defer loading until we are in the renderer - this is actually a jme bug we are working around.
        wm.addRenderUpdater(this, shader);
        
        node.setRenderState(buf);
        node.setRenderState(shader);
        node.setLocalTranslation(x, y, z);
        
        return (node);
    }

    public void update(Object obj) {
        GLSLShaderObjectsState shader = (GLSLShaderObjectsState)obj;
        shader.load(vert, frag); 
    }

}
