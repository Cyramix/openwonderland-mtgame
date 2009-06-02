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
public class DiffuseMapAO extends Shader {
    /**
     * The vertex and fragment shader
     */
    protected static final String vShader =
        "varying vec3 EyeDir;" +
        "varying vec3 LightDir[2];" +
        "varying vec3 VNormal;" +
        "void main(void)" +
        "{" +
        "        VNormal = normalize(gl_NormalMatrix * gl_Normal);" +
        "        gl_Position = ftransform();" +
        "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        gl_TexCoord[1] = gl_MultiTexCoord1;" +
        "" +
        "        LightDir[0] = normalize(gl_LightSource[0].position.xyz - vVertex);" +
        "        LightDir[1] = normalize(gl_LightSource[1].position.xyz - vVertex);" +
        "        EyeDir = normalize(vVertex);" +
        "}";

    protected static final String fShader =
        "varying vec3 LightDir[2];" +
        "varying vec3 VNormal;" +
        "varying vec3 EyeDir;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D AmbientOccIndex;" +
        "vec3 FragLocalNormal;" +
        "vec4 finalColor;" +
        "vec3 ambientOcc;" +
        "vec4 diffuseColor;" +
        "vec3 specularColor;" +
        "float NdotL;" +
        "float spec;" +
        "vec3 reflectDir;" +
        "void main(void) { " +
        "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st);" +
        "        ambientOcc = texture2D(AmbientOccIndex, gl_TexCoord[0].st).rgb;" +
        "        specularColor = vec3(1.0, 1.0, 1.0);" +
        "        finalColor.rgb  = gl_FrontMaterial.ambient.rgb * gl_LightSource[0].ambient.rgb;" +
                 // Compute diffuse for light0
        "        NdotL = clamp(dot(VNormal, LightDir[0]), 0.0, 1.0);" +
        "        finalColor.rgb += diffuseColor.rgb * NdotL * gl_LightSource[0].diffuse.rgb;" +

                 // Compte specular for light0
        "        reflectDir = reflect(LightDir[0], VNormal);" +
        "        spec = max(dot(EyeDir, reflectDir), 0.0);" +
        "        spec = pow(spec, 32.0);" +
        "        finalColor.rgb += spec * specularColor * gl_LightSource[0].specular.rgb;" +

                 // Compute diffuse for light1
        "        finalColor.rgb += gl_FrontMaterial.ambient.rgb * gl_LightSource[1].ambient.rgb;" +
        "        NdotL = clamp(dot(VNormal, LightDir[1]), 0.0, 1.0);" +
        "        finalColor.rgb += diffuseColor.rgb * NdotL * gl_LightSource[1].diffuse.rgb;" +

                 // Compte specular for light1
        "        reflectDir = reflect(LightDir[1], VNormal);" +
        "        spec = max(dot(EyeDir, reflectDir), 0.0);" +
        "        spec = pow(spec, 32.0);" +
        "        finalColor.rgb = min(finalColor.rgb + (spec * specularColor * gl_LightSource[1].specular.rgb), vec3(1.0));" +
        "        finalColor.rgb = finalColor.rgb * ambientOcc;" +
                 // Final assignment
        "        gl_FragColor = finalColor;" +
        "}";
    
    public DiffuseMapAO(WorldManager worldManager) {
        super(worldManager, vShader, fShader);
    }
    
    /**
     * This applies this shader to the given geometry
     */
    public void applyToGeometry(Geometry geo) {
        shaderState.setUniform("DiffuseMapIndex", 0);
        shaderState.setUniform("AmbientOccIndex", 1);
        geo.setRenderState(shaderState);
    }
}
