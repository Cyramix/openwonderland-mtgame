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
import org.jdesktop.mtgame.shader.DiffuseNormalSpecMap;
import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.TriMesh;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jme.util.TextureManager;
import java.net.URL;
import java.net.MalformedURLException;
import com.jme.util.geom.TangentBinormalGenerator;
import com.jme.scene.VBOInfo;

import java.util.HashMap;
import java.io.InputStream;


/**
 * This is the class which manages the configuration system
 * 
 * @author Doug Twilleager
 */
public class ConfigManager {
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
     * The Default Constructor
     */
    public ConfigManager(WorldManager wm) {
        worldManager = wm;
    }

    /**
     * Set the texture directory
     */
    void setTextureDirectory(String dir) {
        textureDir = dir;
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
            }
        }
    }
            
    /**
     * Load the specified texture
     */
    void loadTexture(Spatial s, String name, int index) {
        TextureState ts = (TextureState) s.getRenderState(RenderState.RS_TEXTURE);
        URL url = null;
        try {
            url = new URL("file:" + textureDir + "/" + name);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        }

        Texture texture = TextureManager.loadTexture(url,
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        texture.setWrap(Texture.WrapMode.Repeat);

        if (ts == null) {
            ts = (TextureState) worldManager.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        }

        ts.setEnabled(true);
        ts.setTexture(texture, index);
        s.setRenderState(ts);
    }
}
