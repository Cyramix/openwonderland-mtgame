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
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import java.net.URL;
import java.net.MalformedURLException;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.scene.VBOInfo;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.renderer.ColorRGBA;

import java.util.HashMap;
import java.util.Collection;
import java.io.InputStream;
import com.jme.util.resource.ResourceLocator;
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
     * The base directory
     */
    private String baseDir = null;
    
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

    private ShadowMapRenderBuffer shadowMapBuffer = null;
    private int shadowMapWidth = 1024;
    private int shadowMapHeight = 1024;
    private ArrayList<Node> shadowSpatials = new ArrayList<Node>();
    private boolean showShadowMap = false;
    private Node shadowDebug = null;

    /**
     * A HashMap containing all ConfigInstances
     */
    private HashMap<String,ConfigInstance> configInstanceMap = new HashMap<String,ConfigInstance>();

    /**
     * The Default Constructor
     */
    public ConfigManager(WorldManager wm) {
        worldManager = wm;
        collisionSystem = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
    }

    /**
     * Get a named config instance
     */
    public ConfigInstance getConfigInstance(String name) {
        return (configInstanceMap.get(name));
    }

    /**
     * Get all of the ConfigInstances
     */
    public ConfigInstance[] getAllConfigInstances() {
        ConfigInstance[] ret = new ConfigInstance[configInstanceMap.size()];
        Collection<ConfigInstance> values = configInstanceMap.values();
        ret = values.toArray(ret);
        return (ret);
    }

    /**
     * Set the texture directory
     */
    void setTextureDirectory(String dir) {
        textureDir = baseDir + dir;
    }

    /**
     * Get the texture directory
     */
    public String getTextureDirectory() {
        return (textureDir);
    }

    /**
     * Set the texture directory
     */
    void setDataDirectory(String dir) {
        dataDir = baseDir + dir;
    }

    /**
     * Get the data directory
     */
    public String getDataDirectory() {
        return (dataDir);
    }

    /**
     * Set the texture directory
     */
    void setBaseURL(String dir) {
        baseDir = dir;
    }

    /**
     * Get the data directory
     */
    public String getBaseURL() {
        return (baseDir);
    }

    /**
     * Load the configuration data given by the InputStream
     */
    void loadConfiguration(InputStream stream, ConfigLoadListener listener) {
        loadListener = listener;
        loadConfiguration(stream);
    }

    /**
     * Load the configuration data given by the InputStream
     */
    void loadConfiguration(InputStream stream) {

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
            } else if (token.startsWith("BaseURL")) {
                token = token.substring(7).trim();
                setBaseURL(token);
            } else if (token.startsWith("ConfigFile")) {
                token = token.substring(10).trim();
                loadConfigFile(token);
            } else if (token.startsWith("ShadowMapEnable")) {
                token = token.substring(15).trim();
                processShadowMap(token);
            } else if (token.startsWith("ShadowMapWidth")) {
                token = token.substring(14).trim();
                setShadowMapWidth(token);
            } else if (token.startsWith("ShadowMapHeight")) {
                token = token.substring(15).trim();
                setShadowMapHeight(token);
            } else if (token.startsWith("Instance")) {
                token = token.substring(8).trim();
                parseInstance(token);
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
     * Parse an Instance entry
     */
    void parseInstance(String instance) {
        ConfigInstance ci = null;

        // First, split everything up
        String[] args = instance.split(" ");

        // The instance name is first
        String name = args[0];

        // Then the implementing class
        String implementor = args[1];

        // Now, translation, rotation, and scale

        Vector3f trans = new Vector3f();
        Quaternion rot = new Quaternion();
        Vector3f axis = new Vector3f();
        float angle = 0.0f;
        Vector3f scale = new Vector3f();

        trans.x = Float.parseFloat(args[2]);
        trans.y = Float.parseFloat(args[3]);
        trans.z = Float.parseFloat(args[4]);
        axis.x = Float.parseFloat(args[5]);
        axis.y = Float.parseFloat(args[6]);
        axis.z = Float.parseFloat(args[7]);
        angle = Float.parseFloat(args[8]);
        scale.x = Float.parseFloat(args[9]);
        scale.y = Float.parseFloat(args[10]);
        scale.z = Float.parseFloat(args[11]);
        rot.fromAngleAxis(angle, axis);

        try {
            ci = (ConfigInstance)Class.forName(implementor).newInstance();
        } catch (java.lang.InstantiationException ie) {
            System.out.println("Cannot create " + implementor + ": " + ie);
        } catch (java.lang.IllegalAccessException ie) {
            System.out.println("Cannot create " + implementor + ": " + ie);
        } catch (java.lang.ClassNotFoundException ie) {
            System.out.println("Cannot create " + implementor + ": " + ie);
        }

        if (ci != null) {
            String[] initArgs = new String[args.length - 12];
            for (int i=0; i<initArgs.length; i++) {
                initArgs[i] = args[i+12];
            }
            ci.init(worldManager, this, name, trans, rot, scale, initArgs);
            Node model = ci.getSceneGraph();
            if (model != null) {
                worldManager.applyConfig(model);
            }
            configInstanceMap.put(name, ci);
            if (loadListener != null) {
                loadListener.configLoaded(ci);
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
        String textureName = textureDir + "/" + name;

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
