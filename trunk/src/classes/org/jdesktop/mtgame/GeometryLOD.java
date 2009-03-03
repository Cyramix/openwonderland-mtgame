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

import com.jme.scene.Geometry;
import org.jdesktop.mtgame.shader.Shader;
import com.jme.math.Vector3f;
import com.jme.scene.state.TextureState;

/**
 * This class contains all attributes for a geometry - as specified by 
 * the config files
 * 
 * @author Doug Twilleager
 */
public class GeometryLOD {
    /**
     * The geometry
     */
    private Geometry geometry = null;
    
    /**
     * The distance cutoff from high to low
     */
    private float distance = 10.0f;

    /**
     * The low Shader for this geometry
     */
    private Shader lowShader = null;

    /**
     * The high Shader for this geometry
     */
    private Shader highShader = null;

    /**
     * The low texture state for this geometry
     */
    private TextureState lowTS = null;

    /**
     * The high texture state for this geometry
     */
    private TextureState highTS = null;

    /**
     * The current shader being used by the renderer
     */
    private Shader currentShader = null;

    /**
     * The current shader being used by the renderer
     */
    private TextureState currentTS = null;

    /**
     * The default constructor
     */
    GeometryLOD(Geometry geo, Shader lowS, Shader highS,
            TextureState lowTex, TextureState highTex, float d) {
        geometry = geo;
        lowShader = lowS;
        highShader = highS;
        highTS = highTex;
        lowTS = lowTex;
        distance = d;
    }
    
    /**
     * get the geometry name
     */
    public Geometry getGeometry() {
        return (geometry);
    }

    /**
     * Get the appropriate shader, given the position - null means it is the
     * same as it was.
     */
    public void applyShader(Vector3f position) {
        Shader ret = null;

        float d = geometry.getWorldBound().distanceTo(position);
        if (d < distance) {
            if (currentShader != highShader) {
                currentShader = highShader;
                currentTS = highTS;
                ret = currentShader;
            }
        } else {
            if (currentShader != lowShader) {
                currentShader = lowShader;
                currentTS = lowTS;
                ret = currentShader;
            }
        }
        
        // If ret != null, need to reapply
        if (ret != null) {
            //System.out.println("Applying shader: " + currentShader);
            geometry.setRenderState(currentTS);
            currentShader.applyToGeometry(geometry);
            geometry.updateRenderState();
        }
    }
}
