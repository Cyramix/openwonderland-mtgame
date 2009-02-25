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

package org.jdesktop.mtgame;

import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import org.jdesktop.mtgame.shader.DiffuseMap;
import org.jdesktop.mtgame.shader.DiffuseMapAO;
import org.jdesktop.mtgame.shader.DiffuseMapAlpha;
import org.jdesktop.mtgame.shader.DiffuseMapAlphaMap;
import org.jdesktop.mtgame.shader.DiffuseNormalSpecMap;
import org.jdesktop.mtgame.shader.DiffuseNormalSpecAOMap;
import org.jdesktop.mtgame.shader.DiffuseNormalSpecAOMapAlpha;
import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.TriMesh;
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

import java.util.HashMap;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.jmex.model.collada.ColladaImporter;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;


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
    
    /**
     * The Default Constructor
     */
    public ConfigManager(WorldManager wm) {
        worldManager = wm;
        collisionSystem = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, this);
    }

    /**
     * Set the texture directory
     */
    void setTextureDirectory(String dir) {
        textureDir = dir;
        System.out.println("Texture Dir: " + textureDir);
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
            } else if (token.startsWith("Shader")) {
                token = token.substring(6).trim();
                ga.setShaderName(token);
            }  else if (token.startsWith("DataDir")) {
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
        while (ret.equals("")) {
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
        //System.out.println("loadConfigFile Loading: " + name);
        int lastCurrentToken = currentToken;
        try {
            FileInputStream fs = new FileInputStream(dataDir + "/" + name);
            worldManager.loadConfiguration(fs);
        } catch (java.io.FileNotFoundException e) {
            System.out.println(e);
        }
        currentToken = lastCurrentToken;
    }

    /**
     * Load a collada file with the given name - it can be found in the
     * data directory
     */
    void loadColladaFile(String colladaString) {
        FileInputStream fileStream = null;

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
            fileStream = new FileInputStream(dataDir + "/" + colladaFile);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }

        // Now load the model
        ColladaImporter.load(fileStream, "Model");
        Node model = ColladaImporter.getModel();
        worldManager.applyConfig(model);

        model.setLocalTranslation(trans);
        model.setLocalRotation(rot);
        model.setLocalScale(scale);
        addModel(model);
    }

    void addModel(Node model) {
        Node modelRoot = new Node("Model");

        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        modelRoot.setRenderState(buf);

        //System.out.println("Adding: " + model);
        modelRoot.attachChild(model);

        Entity e = new Entity("Model");
        RenderComponent sc = worldManager.getRenderManager().createRenderComponent(modelRoot);
        JMECollisionComponent cc = collisionSystem.createCollisionComponent(modelRoot);
        e.addComponent(JMECollisionComponent.class, cc);
        e.addComponent(RenderComponent.class, sc);

        worldManager.addEntity(e);
    }


    /**
     * Apply the configuration map information to a jME graph
     */
    public void applyConfig(Spatial s) {
        parseModel(s);
    }
    
    void parseModel(Spatial model) {
        GeometryAttributes ga = (GeometryAttributes)configMap.get(model.getName());
        if (ga != null) {
            assignAttributes(model, ga);
        }
        if (model instanceof Node) {
            Node n = (Node) model;
            for (int i = 0; i < n.getQuantity(); i++) {
                parseModel(n.getChild(i));
            }
        }
    }
    
    /**
     * Assign the given GeometryAttributes to the given spatial
     * @param s
     * @param ga
     */
    void assignAttributes(Spatial s, GeometryAttributes ga) {
        if (!ga.getShaderName().equals("None")) {
            if (s instanceof TriMesh) {
                VBOInfo vboInfo = new VBOInfo();
                vboInfo.setVBOTangentEnabled(true);
                TangentBinormalGenerator.generate((TriMesh) s);
                ((Geometry)s).setVBOInfo(vboInfo);
            }
                    
            if (ga.getShaderName().equals("DiffuseNormalMap")) {
                DiffuseNormalMap diffuseNormalShader = new DiffuseNormalMap(worldManager);
                // Assume this is geometry for now
                diffuseNormalShader.applyToGeometry((Geometry)s);          
            } else if (ga.getShaderName().equals("DiffuseNormalSpecMap")) {
                DiffuseNormalSpecMap diffuseNormalSpecShader = new DiffuseNormalSpecMap(worldManager);
                // Assume this is geometry for now
                diffuseNormalSpecShader.applyToGeometry((Geometry)s);          
            } else if (ga.getShaderName().equals("DiffuseMapAO")) {
                DiffuseMapAO diffuseMapAOShader = new DiffuseMapAO(worldManager);
                // Assume this is geometry for now
                diffuseMapAOShader.applyToGeometry((Geometry)s);
            } else if (ga.getShaderName().equals("DiffuseNormalSpecAOMap")) {
                DiffuseNormalSpecAOMap shader = new DiffuseNormalSpecAOMap(worldManager);
                // Assume this is geometry for now
                shader.applyToGeometry((Geometry)s);
            } else if (ga.getShaderName().equals("DiffuseNormalSpecAOMapAlpha")) {
                DiffuseNormalSpecAOMapAlpha shader = new DiffuseNormalSpecAOMapAlpha(worldManager);
                // Assume this is geometry for now
                shader.applyToGeometry((Geometry)s);
                BlendState as = (BlendState) worldManager.getRenderManager().createRendererState(RenderState.RS_BLEND);
                as.setEnabled(true);
                as.setReference(0.5f);
                as.setTestFunction(BlendState.TestFunction.GreaterThan);
                as.setTestEnabled(true);
                s.setRenderState(as);
            } else if (ga.getShaderName().equals("DiffuseMap")) {
                DiffuseMap shader = new DiffuseMap(worldManager);
                // Assume this is geometry for now
                shader.applyToGeometry((Geometry)s);
            } else if (ga.getShaderName().equals("DiffuseMapAlpha")) {
                DiffuseMapAlpha shader = new DiffuseMapAlpha(worldManager);
                // Assume this is geometry for now
                shader.applyToGeometry((Geometry)s);
                BlendState as = (BlendState) worldManager.getRenderManager().createRendererState(RenderState.RS_BLEND);
                as.setEnabled(true);
                as.setReference(0.5f);
                as.setTestFunction(BlendState.TestFunction.GreaterThan);
                as.setTestEnabled(true);
                s.setRenderState(as);
            } else if (ga.getShaderName().equals("DiffuseMapAlphaMap")) {
                DiffuseMapAlphaMap shader = new DiffuseMapAlphaMap(worldManager);
                // Assume this is geometry for now
                shader.applyToGeometry((Geometry)s);
                BlendState as = (BlendState) worldManager.getRenderManager().createRendererState(RenderState.RS_BLEND);
                as.setEnabled(true);
                as.setReference(0.5f);
                as.setTestFunction(BlendState.TestFunction.GreaterThan);
                as.setTestEnabled(true);
                s.setRenderState(as);
            }

        }
        
        // Loop through the shader params
        String[] params = ga.getShaderParams();
        for (int i=0; i<params.length; i++) {
            String param = params[i];
            String textureName = null;
            
            if (param.startsWith("DiffuseMap")) {
                param = param.substring(10).trim();
                int index = param.indexOf(' ');
                textureName = param.substring(0, index);
                int tcIndex = Integer.valueOf(param.substring(index).trim()).intValue();
                loadTexture(s, textureName, tcIndex);
            } else if (param.startsWith("NormalMap")) { 
                param = param.substring(9).trim();
                int index = param.indexOf(' ');
                textureName = param.substring(0, index);
                int tcIndex = Integer.valueOf(param.substring(index).trim()).intValue();
                loadTexture(s, textureName, tcIndex);
            } else if (param.startsWith("SpecularMap")) { 
                param = param.substring(11).trim();
                int index = param.indexOf(' ');
                textureName = param.substring(0, index);
                int tcIndex = Integer.valueOf(param.substring(index).trim()).intValue();
                loadTexture(s, textureName, tcIndex);
            } else if (param.startsWith("AmbientOccMap")) {
                param = param.substring(13).trim();
                int index = param.indexOf(' ');
                textureName = param.substring(0, index);
                int tcIndex = Integer.valueOf(param.substring(index).trim()).intValue();
                loadTexture(s, textureName, tcIndex);
            } else if (param.startsWith("AlphaMap")) {
                param = param.substring(8).trim();
                int index = param.indexOf(' ');
                textureName = param.substring(0, index);
                int tcIndex = Integer.valueOf(param.substring(index).trim()).intValue();
                loadTexture(s, textureName, tcIndex);
            }
        }
    }
            
    /**
     * Load the specified texture
     */
    void loadTexture(Spatial s, String name, int index) {
        String textureName = "file:" + textureDir + "/" + name;
        TextureState ts = (TextureState) s.getRenderState(RenderState.RS_TEXTURE);
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

        if (ts == null) {
            ts = (TextureState) worldManager.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        }

        ts.setEnabled(true);
        ts.setTexture(texture, index);
        s.setRenderState(ts);
    }

    public URL locateResource(String resourceName) {
        URL url = null;

        //System.out.println("Looking for: " + resourceName);
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
