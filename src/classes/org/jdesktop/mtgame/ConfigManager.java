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

package org.jdesktop.mtgame;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.shader.Shader;
import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.TriMesh;
import com.jme.scene.Line;
import com.jme.scene.shape.Quad;
import com.jme.scene.SharedMesh;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.MaterialState;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import java.net.URL;
import java.net.MalformedURLException;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.scene.VBOInfo;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.renderer.ColorRGBA;
import java.nio.FloatBuffer;

import java.util.HashMap;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.jmex.model.collada.ColladaImporter;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import java.io.IOException;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager.ConfigLoadListener;


/**
 * This is the class which manages the configuration system
 * 
 * @author Doug Twilleager
 */
public class ConfigManager implements ResourceLocator {
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * A counter used for parsing config streams
     */
    private int currentToken = 0;
    
    /**
     * The hashmap of configuation data.  The key is the name of the Spatial
     */
    private HashMap configMap = new HashMap();
    
    /**
     * The base directory for textures
     */
    private String textureDir = null;


    /**
     * The base directory for data
     */
    private String dataDir = null;

    /**
     * The jME Collision System
     */
    JMECollisionSystem collisionSystem = null;

    /**
     * A HashMap to share textures
     */
    HashMap textureMap = new HashMap();

    private ConfigLoadListener loadListener = null;

    private String baseURL = null;
    
    private boolean localResourceLocatorInstalled = false;

    int normalIndex = 0;
    boolean showNormals = false;
    boolean showTangents = false;

    private ShadowMapRenderBuffer shadowMapBuffer = null;
    private int shadowMapWidth = 1024;
    private int shadowMapHeight = 1024;
    private ArrayList<Node> shadowSpatials = new ArrayList<Node>();
    private boolean showShadowMap = false;
    private Node shadowDebug = null;

    /**
     * The Default Constructor
     */
    public ConfigManager(WorldManager wm) {
        worldManager = wm;
        collisionSystem = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
    }

    /**
     * Set the texture directory
     */
    void setTextureDirectory(String dir) {
        textureDir = dir;
    }

    /**
     * Set the texture directory
     */
    void setDataDirectory(String dir) {
        dataDir = dir;
    }
    
    /**
     * Load the configuration data given by the InputStream
     */
    void loadConfiguration(InputStream stream) {
        if (!localResourceLocatorInstalled) {
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, this);
            localResourceLocatorInstalled = true;
        }

        try {
            int numBytes = stream.available();
            byte[] data = new byte[numBytes];
            stream.read(data);
            String configString = new String(data);
            parseConfigString(configString);
        } catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    void loadConfiguration(URL url, ConfigLoadListener listener) {
        try {
            loadListener = listener;
            baseURL = url.toExternalForm();
            baseURL = baseURL.substring(0, baseURL.lastIndexOf('/'));

            setDataDirectory(baseURL);
            loadConfiguration(url.openStream());

        } catch (MalformedURLException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Parse the config string
     */
    void parseConfigString(String configString) {
        GeometryAttributes ga = null;
        // First split the string
        String[] tokens = configString.split("\\n", -1);
        currentToken = 0;
      
        String token = nextToken(tokens);
        while (token != null) {
            token = token.trim();
            
            if (token.startsWith("Geometry")) {
                token = token.substring(8).trim();
                ga = new GeometryAttributes(token);
                configMap.put(token, ga);
            } else if (token.startsWith("ShaderParam")) {
                token = token.substring(11).trim();
                ga.addShaderParam(token);
            } else if (token.startsWith("ShaderUniform")) {
                token = token.substring(13).trim();
                ga.addShaderUniform(token);
            } else if (token.startsWith("Shader")) {
                token = token.substring(6).trim();
                ga.setShaderName(token);
            } else if (token.startsWith("LowShaderParam")) {
                token = token.substring(14).trim();
                ga.addLowShaderParam(token);
            }  else if (token.startsWith("LowShaderDist")) {
                token = token.substring(13).trim();
                ga.setDistance(Float.parseFloat(token));
            } else if (token.startsWith("LowShader")) {
                token = token.substring(9).trim();
                ga.setLowShaderName(token);
            } else if (token.startsWith("ShadowOccluder")) {
                token = token.substring(14).trim();
                ga.setShadowOccluder(Boolean.parseBoolean(token));
            } else if (token.startsWith("ShadowReceiver")) {
                token = token.substring(14).trim();
                ga.setShadowReceiver(Boolean.parseBoolean(token));
            } else if (token.startsWith("DataDir")) {
                token = token.substring(7).trim();
                setDataDirectory(token);
            } else if (token.startsWith("TextureDir")) {
                token = token.substring(10).trim();
                setTextureDirectory(token);
            } else if (token.startsWith("ConfigFile")) {
                token = token.substring(10).trim();
                loadConfigFile(token);
            } else if (token.startsWith("Collada")) {
                token = token.substring(7).trim();
                loadColladaFile(token);
            } else if (token.startsWith("ShadowMapEnable")) {
                token = token.substring(15).trim();
                processShadowMap(token);
            } else if (token.startsWith("ShadowMapWidth")) {
                token = token.substring(14).trim();
                setShadowMapWidth(token);
            } else if (token.startsWith("ShadowMapHeight")) {
                token = token.substring(15).trim();
                setShadowMapHeight(token);
            }

            token = nextToken(tokens);
        }
    }
    
    String nextToken(String[] tokens) {
        String ret = null;
        
        if (currentToken == tokens.length) {
            return (null);
        }
        
        ret = tokens[currentToken++];
        while (ret.equals("") || ret.charAt(0) == '#') {
            if (currentToken == tokens.length) {
                return (null);
            }
            ret = tokens[currentToken++];
        }
        
        return (ret);
    }

    /**
     * Load a config file with the given name - it can be found in the
     * data directory
     */
    void loadConfigFile(String name) {
        int lastCurrentToken = currentToken;
        try {
            URL url = new URL(dataDir + "/" + name);
            InputStream fs = url.openStream();
            worldManager.loadConfiguration(fs);
        } catch (java.io.FileNotFoundException e) {
            System.out.println(e);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentToken = lastCurrentToken;
    }

    /**
     * Process a shadow map enable
     */
    private void processShadowMap(String token) {
        if (token.equals("true")) {
            if (shadowMapBuffer == null) {
                Vector3f up = new Vector3f(-1.0f, 1.0f, -1.0f);
                Vector3f lookAt = new Vector3f();
                Vector3f position = new Vector3f(125.0f, 100.0f, 125.0f);
                createShadowBuffer(lookAt, position, up);
            }
         } else {
            if (shadowMapBuffer != null) {
                // TODO: Implement this
                //worldManager.getRenderManager().removeRenderBuffer(shadowMapBuffer);
            }
        }
    }

    /**
     * Create a shadowmap buffer with the given position and direction
     * @param dir
     * @param pos
     */
    private void createShadowBuffer(Vector3f lookAt, Vector3f pos, Vector3f up) {
        shadowMapBuffer = (ShadowMapRenderBuffer) worldManager.getRenderManager().createRenderBuffer(RenderBuffer.Target.SHADOWMAP, shadowMapWidth, shadowMapHeight);
        shadowMapBuffer.setCameraLookAt(lookAt);
        shadowMapBuffer.setCameraUp(up);
        shadowMapBuffer.setCameraPosition(pos);
        shadowMapBuffer.setManageRenderScenes(true);
        shadowMapBuffer.setBackgroundColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));

        worldManager.getRenderManager().addRenderBuffer(shadowMapBuffer);
    }

    private void createDebugShadowMap() {
        shadowDebug = new Node("Shadow Debug");
        Quad shadowImage = new Quad("Shadow Quad", shadowMapWidth, shadowMapHeight);
        Entity e = new Entity("Shadow Debug ");

        shadowDebug.attachChild(shadowImage);
        shadowDebug.setLocalTranslation(new Vector3f(0.0f, 0.0f, 100.0f));

        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        shadowDebug.setRenderState(buf);

        TextureState ts = (TextureState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setEnabled(true);
        ts.setTexture(shadowMapBuffer.getTexture(), 0);
        shadowDebug.setRenderState(ts);

        RenderComponent shadowDebugRC = worldManager.getRenderManager().createRenderComponent(shadowDebug);
        shadowDebugRC.setLightingEnabled(false);
        e.addComponent(RenderComponent.class, shadowDebugRC);
        worldManager.addEntity(e);
    }

    /**
     * Set the shadow map width
     */
    private void setShadowMapWidth(String width) {
        shadowMapWidth = Integer.parseInt(width);
    }

    /**
     * Set the shadow map height
     */
    private void setShadowMapHeight(String height) {
        shadowMapHeight = Integer.parseInt(height);
    }

    /**
     * Load a collada file with the given name - it can be found in the
     * data directory
     */
    void loadColladaFile(String colladaString) {
        InputStream fileStream = null;

        String[] tokens = colladaString.split(" ", -1);

        Vector3f trans = new Vector3f();
        Quaternion rot = new Quaternion();
        Vector3f axis = new Vector3f();
        float angle = 0.0f;
        Vector3f scale = new Vector3f();

        String colladaFile = tokens[0];
        trans.x = Float.parseFloat(tokens[1]);
        trans.y = Float.parseFloat(tokens[2]);
        trans.z = Float.parseFloat(tokens[3]);
        axis.x = Float.parseFloat(tokens[4]);
        axis.y = Float.parseFloat(tokens[5]);
        axis.z = Float.parseFloat(tokens[6]);
        angle = Float.parseFloat(tokens[7]);
        scale.x = Float.parseFloat(tokens[8]);
        scale.y = Float.parseFloat(tokens[9]);
        scale.z = Float.parseFloat(tokens[10]);
        rot.fromAngleAxis(angle, axis);

        try {
            URL url;
            url = new URL(dataDir + "/" + colladaFile);
            fileStream = url.openStream();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Now load the model
        ColladaImporter.load(fileStream, "Model");
        Node model = ColladaImporter.getModel();
        if (model != null) {
            worldManager.applyConfig(model);

            model.setLocalTranslation(trans);
            model.setLocalRotation(rot);
            model.setLocalScale(scale);
            addModel(model);
        }
    }

    void addModel(Node model) {
        Node modelRoot = new Node("Model");

        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        modelRoot.setRenderState(buf);

        //System.out.println("Adding: " + model);
        modelRoot.attachChild(model);

        if (showNormals) {
            int normalCount = countNormals(model, 0, false);
            //System.out.println("Number of NORMALS: " + normalCount);
            Vector3f[] lineData = new Vector3f[normalCount*2];
            normalIndex = 0;
            parseModel(0, model, lineData, false);
            Line normalGeometry = new Line("Normal Geometry", lineData, null, null, null);
            MaterialState ms = (MaterialState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Material);
            ms.setDiffuse(new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
            normalGeometry.setRenderState(ms);
            //FlatShader shader = new FlatShader(wm);
            //shader.applyToGeometry(normalGeometry);
            Node normalNode = new Node();
            normalNode.attachChild(normalGeometry);
            model.attachChild(normalNode);
        }

        if (showTangents) {
            int normalCount = countNormals(model, 0, true);
            //System.out.println("Number of Tangents: " + normalCount);
            Vector3f[] lineData = new Vector3f[normalCount*2];
            normalIndex = 0;
            parseModel(0, model, lineData, true);
            Line normalGeometry = new Line("Tangent Geometry", lineData, null, null, null);
            MaterialState ms = (MaterialState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Material);
            ms.setDiffuse(new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f));
            normalGeometry.setRenderState(ms);
            //FlatShader shader = new FlatShader(wm);
            //shader.applyToGeometry(normalGeometry);
            Node normalNode = new Node();
            normalNode.attachChild(normalGeometry);
            model.attachChild(normalNode);
        }

        Entity e = new Entity("Model");
        RenderComponent sc = worldManager.getRenderManager().createRenderComponent(modelRoot);
        JMECollisionComponent cc = collisionSystem.createCollisionComponent(modelRoot);
        e.addComponent(JMECollisionComponent.class, cc);
        e.addComponent(RenderComponent.class, sc);

        if (loadListener==null)
            worldManager.addEntity(e);
        else
            loadListener.entityLoaded(e);
    }

    int countNormals(Spatial model, int currentCount, boolean tangents) {
        if (model instanceof Node) {
            Node n = (Node) model;
            for (int i = 0; i < n.getQuantity(); i++) {
                currentCount = countNormals(n.getChild(i), currentCount, tangents);
            }
        } else if (model instanceof Geometry) {
            Geometry geo = (Geometry) model;
            //System.out.println("Buffer: " + geo.getColorBuffer());
            //System.out.println("Buffer: " + geo.getBinormalBuffer());
            //System.out.println("Buffer: " + geo.getTangentBuffer());
            //System.out.println("Buffer: " + geo.getNormalBuffer());
            if (tangents) {
                if (geo.getTangentBuffer() != null) {
                    currentCount += geo.getVertexCount();
                }
            } else {
                currentCount += geo.getVertexCount();
            }
        }
        return (currentCount);
    }

    void parseModel(int level, Spatial model, Vector3f[] lineData, boolean tangents) {
        FloatBuffer lBuffer = null;
        if (model instanceof Node) {
            Node n = (Node) model;
            for (int i = 0; i < n.getQuantity(); i++) {
                parseModel(level + 1, n.getChild(i), lineData, tangents);
            }
        } else if (model instanceof Geometry) {
            Geometry geo = (Geometry) model;
            //System.out.println("FOUND GEOMETRY: " + geo.getName());

            if (lineData != null) {
                FloatBuffer vBuffer = geo.getVertexBuffer();
                if (tangents) {
                    lBuffer = geo.getTangentBuffer();
                } else {
                    lBuffer = geo.getNormalBuffer();
                }
                if (lBuffer != null) {
                    vBuffer.rewind();
                    lBuffer.rewind();
                    float nScale = 1.0f;
                    for (int i = 0; i < geo.getVertexCount(); i++) {
                        lineData[normalIndex] = new Vector3f();
                        lineData[normalIndex].x = vBuffer.get();
                        lineData[normalIndex].y = vBuffer.get();
                        lineData[normalIndex].z = vBuffer.get();
                        lineData[normalIndex + 1] = new Vector3f();
                        lineData[normalIndex + 1].x = lineData[normalIndex].x + nScale * lBuffer.get();
                        lineData[normalIndex + 1].y = lineData[normalIndex].y + nScale * lBuffer.get();
                        lineData[normalIndex + 1].z = lineData[normalIndex].z + nScale * lBuffer.get();
                        normalIndex += 2;
                    }
                }
            }
        }
    }

    /**
     * Apply the configuration map information to a jME graph
     */
    public void applyConfig(Spatial s) {
        clearShadowSpatials();
        parseModel(s, 0);
        addShadowSpatials();
    }

    private void clearShadowSpatials() {
        shadowSpatials.clear();
    }

    private void addShadowSpatials() {
        for (int i=0; i<shadowSpatials.size(); i++) {
            RenderComponent rc = worldManager.getRenderManager().createRenderComponent(shadowSpatials.get(i));
            shadowMapBuffer.addRenderScene(rc);
        }

        if (showShadowMap && (shadowDebug == null)) {
            createDebugShadowMap();
        }

    }

    void parseModel(Spatial model, int level) {
        GeometryAttributes ga = (GeometryAttributes)configMap.get(model.getName());
        if (ga != null) {
            assignAttributes(model, ga);
        }
        if (model instanceof Node) {
            Node n = (Node) model;
            for (int i = 0; i < n.getQuantity(); i++) {
                parseModel(n.getChild(i), level+1);
            }
        }
    }
    
    /**
     * Assign the given GeometryAttributes to the given spatial
     * @param s
     * @param ga
     */
    void assignAttributes(Spatial s, GeometryAttributes ga) {
        Shader highShader = null;
        Shader lowShader = null;
        TextureState highTS = null;
        TextureState lowTS = null;

        if (!ga.getShaderName().equals("None")) {
            if (s instanceof TriMesh) {
                VBOInfo vboInfo = new VBOInfo();
                vboInfo.setVBOTangentEnabled(true);
                vboInfo.setVBONormalEnabled(true);
                vboInfo.setVBOVertexEnabled(true);
                TangentBinormalGenerator.generate((TriMesh) s);
                //((SharedMesh)s).getTarget().setVBOInfo(vboInfo);
            }

            boolean usesShadowMap = ga.getShadowReceiver();
            highShader = createShader(ga.getShaderName(), s);
            highTS = createTextureState(ga.getShaderUniforms(), s, highShader);
            highShader.setShaderUniforms(ga.getShaderUniforms());
            if (usesShadowMap) {
                highShader.setShadowMap(shadowMapBuffer.getTexture());
                worldManager.getRenderManager().addShadowMapShader(highShader);
            }
            if (ga.getLowShaderName() != null) {
                lowShader = createShader(ga.getLowShaderName(), s);
                lowTS = createTextureState(ga.getShaderUniforms(), s, lowShader);
                lowShader.setShaderUniforms(ga.getShaderUniforms());
                if (usesShadowMap) {
                    lowShader.setShadowMap(shadowMapBuffer.getTexture());
                    worldManager.getRenderManager().addShadowMapShader(lowShader);
                }
                GeometryLOD geoLOD = new GeometryLOD((Geometry)s, lowShader, highShader,
                        lowTS, highTS, ga.getDistance());
                worldManager.getRenderManager().addGeometryLOD(geoLOD);
            } else {
                // Just apply and move on...
                s.setRenderState(highTS);
                highShader.applyToGeometry((Geometry)s);
            }           
        }

        if (ga.getShadowOccluder()) {
            shadowSpatials.add(s.getParent());
        }
    }


    /**
     * Create a shader, with the given name
     */
    private Shader createShader(String name, Spatial s) {
        Shader shader = null;

        try {
            shader = (Shader)Class.forName("org.jdesktop.mtgame.shader." + name).newInstance();
        } catch (java.lang.ClassNotFoundException ce) {
            System.out.println("Shader Not Found: " + ce);
        } catch (java.lang.InstantiationException ie) {
            System.out.println("Shader Not Found: " + ie);
        } catch (java.lang.IllegalAccessException iae) {
            System.out.println("Shader Not Found: " + iae);
        }
        shader.init(worldManager);

        if (name.contains("Alpha")) {
            BlendState as = (BlendState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Blend);
            as.setEnabled(true);
            as.setReference(0.5f);
            as.setTestFunction(BlendState.TestFunction.GreaterThan);
            as.setTestEnabled(true);
            s.setRenderState(as);
        }

        return (shader);
    }


    private TextureState createTextureState(String[] uniforms, Spatial s, Shader shader) {
        TextureState ts = (TextureState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Texture);

         // Loop through the shader params
        for (int i=0; i<uniforms.length; i++) {
            String[] tokens = uniforms[i].split("\\ ");
            String name = tokens[0];
            if (name.contains("Map")) {
                String textureName = tokens[1];
                int tcIndex = Integer.valueOf(tokens[2]);
                loadTexture(s, textureName, tcIndex, ts);
                shader.addUniform(name + "Index", new Integer(tcIndex));
            }
        }
        return (ts);
    }

    /**
     * Load the specified texture
     */
    private void loadTexture(Spatial s, String name, int index, TextureState ts) {
        String textureName;
        
        if (baseURL==null)
            textureName = textureDir + "/" + name;
        else
            textureName = baseURL + "/" + textureDir + "/" +name;

        Texture texture = (Texture)textureMap.get(textureName);

        if (texture == null) {
            URL url = null;
            try {
                url = new URL(textureName);
            } catch (MalformedURLException ex) {
                System.out.println(ex);
            }

            texture = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            texture.setWrap(Texture.WrapMode.Repeat);
            textureMap.put(textureName, texture);
        }
      
        ts.setEnabled(true);
        ts.setTexture(texture, index);
    }

    public URL locateResource(String resourceName) {
        URL url = null;

        if (textureDir==null)
            return url;

        try {
            if (resourceName.contains(textureDir)) {
                // We already resolved this one.
                url = new URL("file:" + resourceName);
            } else {
                url = new URL(textureDir + resourceName);
            }
        //System.out.println("TEXTURE: " + url);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        return (url);
    }
}
