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
import com.jme.scene.Geometry;

/**
 *
 * @author Doug Twilleager
 */
public class DiffuseMapAlphaMap extends Shader {
    /**
     * The vertex and fragment shader
     */
    private static final String vShader =
        "vec3 EyeDir;" +
        "varying vec3 LightDir;" +
        "varying vec3 VNormal;" +
        "void main(void)" +
        "{" +
        "        VNormal = normalize(gl_NormalMatrix * gl_Normal);" +
        "        gl_Position = ftransform();" +
        "        EyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);" +   
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        LightDir = normalize(gl_LightSource[0].position.xyz - EyeDir);" +
        "}";
    
    private static final String fShader = 
        "varying vec3 LightDir;" +
        "varying vec3 VNormal;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D AlphaMapIndex;" +
        "vec3 FragLocalNormal;" +
        "vec4 finalColor;" +
        "vec3 alpha;" +
        "float NdotL;" +
        "void main(void) { " +
        "        finalColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st);" +
        "        alpha = texture2D(AlphaMapIndex, gl_TexCoord[0].st).rgb;" +
        "        NdotL = clamp(dot(VNormal, LightDir), 0.0, 1.0);" +
        "        finalColor.rgb = finalColor.rgb * NdotL;" +
        "        finalColor.a = alpha.r;" +
        "        gl_FragColor = finalColor;" +
        "" + 
//        "        vec3 reflectDir = reflect(LightDir, FragLocalNormal);" +
//        "        float spec = max(dot(EyeDir, reflectDir), 0.0);" +
//        "        spec = pow(spec, 6.0) * 0.5;" +
//        "        finalColor = min(finalColor + spec, vec3(1.0));" +        "        gl_FragColor = vec4(finalColor, 1.0);" +
        "}";
    
    public DiffuseMapAlphaMap() {
        super(vShader, fShader);
        addRequiredUniform("DiffuseMapIndex");
        addRequiredUniform("AlphaMapIndex");
    }

    /**
     * This applies this shader to the given geometry
     */
    public void applyToGeometry(Geometry geo) {
        applyUniforms(geo);
        geo.setRenderState(shaderState);
    }
}
