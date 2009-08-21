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
import com.jme.light.LightNode;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.shape.Quad;
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
import com.jmex.effects.water.WaterRenderPass;

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
public class PassTest implements RenderUpdater {
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
    WaterRenderPass waterEffectRenderPass = null;
    Camera jmeCam = null;
    Skybox skybox = null;
    Spatial reflectionTerrain = null;
    PassNode splatTerrain = null;
    Quad waterQuad = null;
    private float farPlane = 10000.0f;
    private float textureScale = 0.07f;
    //String urlpath = "file:/Documents and Settings/runner/My Documents/NetBeansProjects/trunk/src/";
    String urlpath = "file:/Users/runner/NetBeansProjects/jme-20/trunk/src/";
    JBulletDynamicCollisionSystem collisionSystem = null;
    JBulletPhysicsSystem physicsSystem = null;
    
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
    
    public PassTest(String[] args) {
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
        createReflectionTerrain();
        
        wm.addRenderUpdater(this, wm);
        //createWaterPass(wm);
        
        //createRoom();
        createRandomTeapots(wm);
       
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
         //       camera, positions, rots, times);
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
        /*
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
         */
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        skybox.setTexture(Skybox.Face.North, north);
        skybox.setTexture(Skybox.Face.West, west);
        skybox.setTexture(Skybox.Face.South, south);
        skybox.setTexture(Skybox.Face.East, east);
        skybox.setTexture(Skybox.Face.Up, up);
        skybox.setTexture(Skybox.Face.Down, down);
        //skybox.preloadTextures();

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
        skybox.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_SKIP);
        //skybox.lockMeshes();
        
        
        Entity e = new Entity("Skybox");
        SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
        e.addComponent(SkyboxComponent.class, sbc);
        wm.addEntity(e);
    }
    
    private void createTerrain(WorldManager wm) {
        URL url = null;
        try {
            url = new URL(urlpath + "jmetest/data/texture/terrain/heights.raw");
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        RawHeightMap heightMap = new RawHeightMap(url,
                129, RawHeightMap.FORMAT_16BITLE, false);

        Vector3f terrainScale = new Vector3f(5, 0.003f, 6);
        heightMap.setHeightScale(0.001f);
        TerrainPage page = new TerrainPage("Terrain", 33, heightMap.getSize(),
                terrainScale, heightMap.getHeightMap());
        page.getLocalTranslation().set(0, -9.5f, 0);
        page.setDetailTexture(1, 1);
        
        collisionSystem = (JBulletDynamicCollisionSystem) wm.getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);
        physicsSystem = (JBulletPhysicsSystem) wm.getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, collisionSystem);
        physicsSystem.setStarted(false);

        // create some interesting texturestates for splatting
        TextureState ts1 = createSplatTextureState(
                urlpath + "jmetest/data/texture/terrain/baserock.jpg", null);

        TextureState ts2 = createSplatTextureState(
                urlpath + "jmetest/data/texture/terrain/darkrock.jpg",
                urlpath + "jmetest/data/texture/terrain/darkrockalpha.png");

        TextureState ts3 = createSplatTextureState(
                urlpath + "jmetest/data/texture/terrain/deadgrass.jpg",
                urlpath + "jmetest/data/texture/terrain/deadalpha.png");

        TextureState ts4 = createSplatTextureState(
                urlpath + "jmetest/data/texture/terrain/nicegrass.jpg",
                urlpath + "jmetest/data/texture/terrain/grassalpha.png");

        TextureState ts5 = createSplatTextureState(
                urlpath + "jmetest/data/texture/terrain/road.jpg",
                urlpath + "jmetest/data/texture/terrain/roadalpha.png");

        TextureState ts6 = createLightmapTextureState(urlpath + "jmetest/data/texture/terrain/lightmap.jpg");

        // alpha used for blending the passnodestates together
        BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        as.setTestEnabled(true);
        as.setTestFunction(BlendState.TestFunction.GreaterThan);
        as.setEnabled(true);

        // alpha used for blending the lightmap
        BlendState as2 = (BlendState) wm.getRenderManager().createRendererState(RenderState.StateType.Blend);
        as2.setBlendEnabled(true);
        as2.setSourceFunction(BlendState.SourceFunction.DestinationColor);
        as2.setDestinationFunction(BlendState.DestinationFunction.SourceColor);
        as2.setTestEnabled(true);
        as2.setTestFunction(BlendState.TestFunction.GreaterThan);
        as2.setEnabled(true);

        // //////////////////// PASS STUFF START
        // try out a passnode to use for splatting
        PassNode splattingPassNode = new PassNode("SplatPassNode");
        setupEnvironment(splattingPassNode);
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
        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zState.setEnabled(true);
        zState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        splattingPassNode.setRenderState(zState);
        
        splatTerrain = splattingPassNode;
       
        parseTerrain(0, page);
        splattingPassNode.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_SKIP);
        Entity e = new Entity("Terrain");
        RenderComponent rc = wm.getRenderManager().createRenderComponent(splattingPassNode);
        e.addComponent(RenderComponent.class, rc);
        wm.addEntity(e);
    }
    
    private void parseTerrain(int level, Node node) {
        for (int j = 0; j < level; j++) {
            System.out.print("\t");
        }
        //System.out.println("Level: " + level);

        for (int i = 0; i < node.getQuantity(); i++) {
            Spatial s = (Spatial) node.getChild(i);
            if (s instanceof TerrainBlock) {
                TerrainBlock tb = (TerrainBlock) s;
                for (int j = 0; j < level; j++) {
                    System.out.print("\t");
                }
                
                JBulletCollisionComponent cc = collisionSystem.createCollisionComponent(tb);
                Entity e = new Entity("Terrain");
                e.addComponent(CollisionComponent.class, cc);
                wm.addEntity(e);
            } else if (s instanceof TerrainPage) {
                parseTerrain(level + 1, (TerrainPage) s);
            }
        }
    }
    
    private void addAlphaSplat(TextureState ts, String alpha) {
        URL url = null;
        try {
            url = new URL(alpha);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        
        Texture t1 = TextureManager.loadTexture(url,
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
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        URL url = null;
        try {
            url = new URL(texture);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        Texture t0 = TextureManager.loadTexture(url,
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
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        URL url = null;
        try {
            url = new URL(texture);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        Texture t0 = TextureManager.loadTexture(url,
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t0, 0);

        return ts;
    }

    private void createWaterPass(WorldManager wm) {
        
        waterEffectRenderPass = new WaterRenderPass(jmeCam, 4, true, true);
        waterEffectRenderPass.setWaterPlane(new Plane(new Vector3f(0.0f, 1.0f,
                0.0f), 0.0f));
        waterEffectRenderPass.setClipBias(0.0f);
        waterEffectRenderPass.setReflectionThrottle(0.0f);
        waterEffectRenderPass.setRefractionThrottle(0.0f);

        waterQuad = new Quad("waterQuad", 1, 1);
        FloatBuffer normBuf = waterQuad.getNormalBuffer();
        normBuf.clear();
        normBuf.put(0).put(1).put(0);
        normBuf.put(0).put(1).put(0);
        normBuf.put(0).put(1).put(0);
        normBuf.put(0).put(1).put(0);

        waterEffectRenderPass.setWaterEffectOnSpatial(waterQuad);
        Node rootNode = new Node("Water");
        setupEnvironment(rootNode);
        rootNode.attachChild(waterQuad);

        waterEffectRenderPass.setReflectedScene(skybox);
        waterEffectRenderPass.addReflectedScene(reflectionTerrain);
        waterEffectRenderPass.setSkybox(skybox);
        
        ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zState.setEnabled(true);
        zState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        waterQuad.setRenderState(zState);
        
        Entity e = new Entity("Water");
        PassComponent pass = wm.getRenderManager().createPassComponent(waterEffectRenderPass);
        RenderComponent rc = wm.getRenderManager().createRenderComponent(rootNode);
        e.addComponent(PassComponent.class, pass);
        e.addComponent(RenderComponent.class, rc);
        wm.addEntity(e);
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
        
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setEnabled(true);
        LightNode ln = new LightNode();
        ln.setLight(light);
        ln.setLocalTranslation(new Vector3f(100, 100, 100));
        wm.getRenderManager().addLight(ln);
    }
    
    private void createReflectionTerrain() {
        URL url = null;
        try {
            url = new URL(urlpath + "jmetest/data/texture/terrain/heights.raw");
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
                
        RawHeightMap heightMap = new RawHeightMap(url,
                129, RawHeightMap.FORMAT_16BITLE, false);

        Vector3f terrainScale = new Vector3f(5, 0.003f, 6);
        heightMap.setHeightScale(0.001f);
        TerrainPage page = new TerrainPage("Terrain", 33, heightMap.getSize(),
                terrainScale, heightMap.getHeightMap());
        page.getLocalTranslation().set(0, -9.5f, 0);
        page.setDetailTexture(1, 1);

        try {
            url = new URL(urlpath + "jmetest/data/texture/terrain/terrainlod.jpg");
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        // create some interesting texturestates for splatting
        TextureState ts1 = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        Texture t0 = TextureManager.loadTexture(url,
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        t0.setApply(Texture.ApplyMode.Modulate);
        t0.setScale(new Vector3f(1.0f, 1.0f, 1.0f));
        ts1.setTexture(t0, 0);

        // //////////////////// PASS STUFF START
        // try out a passnode to use for splatting
        PassNode splattingPassNode = new PassNode("SplatPassNode");
        splattingPassNode.attachChild(page);

        PassNodeState passNodeState = new PassNodeState();
        passNodeState.setPassState(ts1);
        splattingPassNode.addPass(passNodeState);
        // //////////////////// PASS STUFF END

        // lock some things to increase the performance
        splattingPassNode.lockBounds();
        splattingPassNode.lockTransforms();
        splattingPassNode.lockShadows();

        //reflectionTerrain = splattingPassNode;
        page.setRenderState(ts1);
        reflectionTerrain = page;

        initSpatial(reflectionTerrain);
    }
     
    
    private void initSpatial(Spatial spatial) {
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        spatial.setRenderState(buf);

        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        spatial.setRenderState(cs);

        spatial.setCullHint(Spatial.CullHint.Never);

        spatial.updateGeometricState(0.0f, true);
        spatial.updateRenderState();
    }
    
    class WaterProcessor extends ProcessorComponent {
        public void initialize() {
            
        }
        
        public void commit(ProcessorArmingCollection pcc) {
            if (waterEffectRenderPass != null) {
                Vector3f transVec = new Vector3f(jmeCam.getLocation().x,
                        waterEffectRenderPass.getWaterHeight(), jmeCam.getLocation().z);
                setTextureCoords(0, transVec.x, -transVec.z, textureScale);
                setVertexCoords(transVec.x, transVec.y, transVec.z);

                skybox.getLocalTranslation().set(jmeCam.getLocation());
                
                wm.addToPassUpdateList(waterEffectRenderPass);
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
        PassTest st = new PassTest(args);

    }

    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i + 1]);
                //System.out.println("DesiredFrameRate: " + desiredFrameRate);
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
            //System.out.println("Adding: " + model);
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
                    physicsSystem.setStarted(true);
                    wm.removeEntity(axis);
                    //System.out.println("Turning Coordinates Off");
                } else {
                    coordsOn = true;
                    physicsSystem.setStarted(false);
                    wm.addEntity(axis);
                    //System.out.println("Turning Coordinates On");
                }
            }
            
            if (e.getSource() == gridButton) {
                if (gridOn) {
                    gridOn = false;
                    wm.removeEntity(grid);/*
                    //System.out.println("new Vector3f(" + jmeCam.getLocation().x + "f, " + 
                    //       jmeCam.getLocation().y + "f, " + jmeCam.getLocation().z + "f)");
                    //System.out.println("new Vector3f(" + jmeCam.getDirection().x + "f, " +
                    //        jmeCam.getDirection().y + "f, " + jmeCam.getDirection().z + "f)");
                    //System.out.println("new Vector3f(" + jmeCam.getUp().x + "f, " +
                    //        jmeCam.getUp().y + "f, " + jmeCam.getUp().z + "f)");
                    //System.out.println("new Vector3f(" + jmeCam.getLeft().x + "f, " +
                    //        jmeCam.getLeft().y + "f, " + jmeCam.getLeft().z + "f)");
                                           * */
                    //System.out.println("new Quaternion(" + 
                    //        cameraNode.getWorldRotation().x + "f, " +
                    //        cameraNode.getWorldRotation().y + "f, " + 
                    //        cameraNode.getWorldRotation().z + "f, " +
                    //        cameraNode.getWorldRotation().w + "f);");
                    //System.out.println("new Vector3f(" + 
                    //        cameraNode.getWorldTranslation().x + "f, " +
                    //        cameraNode.getWorldTranslation().y + "f, " + 
                    //        cameraNode.getWorldTranslation().z + "f)");
                    
                    //System.out.println("Turning Grid Off");
                } else {
                    gridOn = true;
                    wm.addEntity(grid);
                    //System.out.println("Turning Grid On");
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
    private void createRandomTeapots(WorldManager wm) {
        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;
        boolean transparent = false;
        int numTeapots = 300;
        Random r = new Random();
        RenderComponent sc = null;
        JBulletCollisionComponent cc = null;
        JBulletPhysicsComponent pc = null;
        Entity e = null;       
        
        // Create the ground plane
        CollisionShape groundShape = new StaticPlaneShape(new javax.vecmath.Vector3f(0, 1, 0), 0);
        cc = collisionSystem.createCollisionComponent(groundShape);
        e = new Entity("Ground Plane");
        e.addComponent(CollisionComponent.class, cc);
        //wm.addEntity(e);
        
        for (int i=0; i<numTeapots; i++) {
            x = (r.nextFloat()*100.0f) - 50.0f;
            y = (r.nextFloat()*200.0f) + 200.0f;
            z = (r.nextFloat()*100.0f) - 50.0f;
            transparent = r.nextBoolean();
            Node teapot = createTeapotModel(i, x, y, z, transparent);
            //Node teapot = createBoxModel(i, x, y, z);
            
            e = new Entity("Teapot " + i);
            sc = wm.getRenderManager().createRenderComponent(teapot);
            cc = collisionSystem.createCollisionComponent(teapot);
            pc = physicsSystem.createPhysicsComponent(cc);
            pc.setMass(.01f);
            //pc.setLinearVelocity(0.0f, 1.0f, 0.0f);
            
            e.addComponent(CollisionComponent.class, cc);
            e.addComponent(PhysicsComponent.class, pc);
            e.addComponent(RenderComponent.class, sc);
            e.addComponent(CollisionComponent.class, cc);
            
            wm.addEntity(e);                        
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
        
    private Node createTeapotModel(int number, float x, float y, float z, boolean transparent) {
        Node node = new Node("Teapot " + number);
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
        
    private Node createTeapotModel(float x, float y, float z, ColorRGBA color) {
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.updateGeometryData();
        node.attachChild(teapot);

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        GLSLShaderObjectsState shader = (GLSLShaderObjectsState) wm.getRenderManager().createRendererState(RenderState.StateType.GLSLShaderObjects);
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
        if (obj instanceof WorldManager) {
            //System.out.println("CREATING WATER");
            createWaterPass(wm);
        } else {
            GLSLShaderObjectsState shader = (GLSLShaderObjectsState)obj;
            shader.load(vert, frag);
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

}
