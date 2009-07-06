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

package org.jdesktop.mtgame.shader;

import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.RenderUpdater;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.Geometry;
import com.jme.renderer.ColorRGBA;
import com.jme.image.Texture;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Integer;

/**
 *
 * @author Doug Twilleager
 */
public abstract class Shader implements RenderUpdater {
    /**
     * The vertex and fragment shader
     */
    private String vertexShader = null;
    
    private String fragmentShader = null;

    /**
     * The hashmap of uniforms
     */
    private HashMap uniforms = new HashMap();

    /**
     * The hashmap of uniforms
     */
    private ArrayList<String> requiredUniforms = new ArrayList<String>();

    /**
     * A boolean indicating that the uniforms have been loaded
     */
    private boolean uniformsLoaded = false;

    /**
     * The shadow map for this shader
     */
    private Texture shadowMap = null;

    /**
     * The index for the shadow map
     */
    private int shadowMapIndex = -1;

    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The shader state object for this shader
     */
    protected GLSLShaderObjectsState shaderState = null;
    
    public Shader(String vShader, String fShader) {
        vertexShader = vShader;
        fragmentShader = fShader;     
    }

    public void init(WorldManager worldManager) {
        this.worldManager = worldManager;
        shaderState = (GLSLShaderObjectsState) worldManager.getRenderManager().
                createRendererState(RenderState.StateType.GLSLShaderObjects);
        worldManager.addRenderUpdater(this, this);
    }

    /**
     * Set the uniforms for this shader
     */
    public void setShaderUniforms(String[] u) {
        for (int i=0; i<u.length; i++) {
            parseUniform(u[i]);
        }
    }

    /**
     * Set the shadow map for this shader
     */
    public void setShadowMap(Texture t) {
        shadowMap = t;
    }

    /**
     * Get the shadow map for this shader
     */
    public Texture getShadowMap() {
        return (shadowMap);
    }

    /**
     * Set the shadow map index for this shader
     */
    public void setShadowMapIndex(int index) {
        shadowMapIndex = index;
    }

    /**
     * Get the shadow map index for this shader
     */
    public int getShadowMapIndex() {
        return (shadowMapIndex);
    }

    private void parseUniform(String uniform) {
        String[] tokens = uniform.split("\\ ");
        String name = tokens[0];
        if (name.contains("Color")) {
            // Value is 4 floats
            ColorRGBA val = new ColorRGBA();
            val.r = Float.parseFloat(tokens[1]);
            val.g = Float.parseFloat(tokens[2]);
            val.b = Float.parseFloat(tokens[3]);
            val.a = Float.parseFloat(tokens[4]);
            uniforms.put(name, val);
        } else if (name.contains("Map")) {
            // Value is a texture file and an index
        }
    }

    /**
     * Add a uniform to the list of known uniforms
     */
    public void addUniform(String key, Object value) {
        uniforms.put(key, value);
    }


    /**
     * Add a uniform to the list of uniforms needed for this shader
     */
    public void addRequiredUniform(String key) {
        requiredUniforms.add(key);
    }

    /**
     * Checks if the given uniform is required for this shader
     */
    public boolean isRequiredUniform(String key) {
        for (int i=0; i<requiredUniforms.size(); i++) {
            if (requiredUniforms.get(i).equals(key)) {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Apply the uniforms to this shader
     */
    public void applyUniforms(Geometry g) {

        if (uniformsLoaded) {
            return;
        }

        Iterator keys = uniforms.keySet().iterator();

        while (keys.hasNext()) {
            String key = (String)keys.next();
            if (isRequiredUniform(key)) {
                if (key.contains("Color")) {
                    ColorRGBA color = (ColorRGBA) uniforms.get(key);
                    shaderState.setUniform(key, color.r, color.g, color.b, color.a);
                } else if (key.contains("Map")) {
                    Integer iVal = (Integer) uniforms.get(key);
                    shaderState.setUniform(key, iVal.intValue());
                }
            }
        }

        if (shadowMap != null) {
            shaderState.setUniform("ShadowMapIndex", shadowMapIndex);
            applyShadowMap(g, shadowMapIndex);
        }
        uniformsLoaded = true;
    }

    /**
     * Set up the shadow map for this shader
     */
    private void applyShadowMap(Geometry g, int index) {
        TextureState ts = (TextureState)g.getRenderState(RenderState.StateType.Texture);

        if (ts == null) {
            ts = (TextureState)worldManager.getRenderManager().createRendererState(RenderState.StateType.Texture);
            g.setRenderState(ts);
        }

        ts.setTexture(shadowMap, index);
    }

    /**
     * Get the GLSLShaderObjectsState for this object
     */
    public GLSLShaderObjectsState getShaderState() {
        return (shaderState);
    }
    
    /**
     * This applies this shader to the given geometry
     */
    public abstract void applyToGeometry(Geometry geo);
    
    /**
     * This loads the shader
     */
    public void update(Object o) {
        shaderState.load(vertexShader, fragmentShader);
    }
}
