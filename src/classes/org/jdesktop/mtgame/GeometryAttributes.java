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

import java.util.ArrayList;

/**
 * This class contains all attributes for a geometry - as specified by 
 * the config files
 * 
 * @author Doug Twilleager
 */
public class GeometryAttributes {
    /**
     * The name of the geometry
     */
    private String name = null;
    
    /**
     * The Shader name for this geometry
     */
    private String shaderName = null;

    /**
     * The Shader name for this geometry
     */
    private String lowShaderName = null;

    /**
     * The list of shader params
     */
    private ArrayList shaderParams = new ArrayList();

    /**
     * The list of shader uniforms
     */
    private ArrayList shaderUniforms = new ArrayList();

    /**
     * The list of shader params
     */
    private ArrayList lowShaderParams = new ArrayList();

    /**
     * The distance cutoff for lod
     */
    private float distance = 10.0f;

    /**
     * A boolean indicating whether this geometry blocks light
     */
    private boolean shadowOccluder = false;

    /**
     * A boolean indicating whether this geometry receives shadows
     */
    private boolean shadowReceiver = false;

    /**
     * The default constructor
     */
    GeometryAttributes(String geometryName) {
        name = geometryName;
    }
    
    /**
     * get the geometry name
     */
    public String getGeometryName() {
        return (name);
    }

    /**
     * Get the shader name
     */
    public String getShaderName() {
        return (shaderName);
    }

    /**
     * Set the shader name
     */
    public void setShaderName(String name) {
        shaderName = name;
    }

    /**
     * Get the occluder flag
     */
    public boolean getShadowOccluder() {
        return (shadowOccluder);
    }

    /**
     * Set the shader occluder flag
     */
    public void setShadowOccluder(boolean enable) {
        shadowOccluder = enable;
    }

    /**
     * Get the occluder flag
     */
    public boolean getShadowReceiver() {
        return (shadowReceiver);
    }

    /**
     * Set the shader occluder flag
     */
    public void setShadowReceiver(boolean enable) {
        shadowReceiver = enable;
    }

    /**
     * Add a shader parameter
     */
    public void addShaderParam(String param) {
        shaderParams.add(param);
    }

    /**
     * Add a shader uniform
     */
    public void addShaderUniform(String param) {
        shaderUniforms.add(param);
    }

    /**
     * Get the shader params
     */
    public String[] getShaderParams() {
        String[] params = new String[shaderParams.size()];
        for (int i=0; i<params.length; i++) {
            params[i] = (String) shaderParams.get(i);
        }
        return (params);
    }

    /**
     * Get the shader uniforms
     */
    public String[] getShaderUniforms() {
        String[] uniforms = new String[shaderUniforms.size()];
        for (int i=0; i<uniforms.length; i++) {
            uniforms[i] = (String) shaderUniforms.get(i);
        }
        return (uniforms);
    }
    
    /**
     * Get the shader name
     */
    public String getLowShaderName() {
        return (lowShaderName);
    }
    
    /**
     * Set the shader name
     */
    public void setLowShaderName(String name) {
        lowShaderName = name;
    }
        
    /**
     * Get the CollisionComponent
     */
    public void addLowShaderParam(String param) {
        lowShaderParams.add(param);
    }
    
    /**
     * Get the PickInfo
     */
    public String[] getLowShaderParams() {
        String[] params = new String[lowShaderParams.size()];
        for (int i=0; i<params.length; i++) {
            params[i] = (String) lowShaderParams.get(i);
        }
        return (params);
    }

    /**
     * Get the distance cutoff for LOD
     */
    public float getDistance() {
        return (distance);
    }

    /**
     * Set the distance cutoff for LOD
     */
    public void setDistance(float d) {
        distance = d;
    }
}
