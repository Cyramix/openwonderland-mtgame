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
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState;
import com.jme.scene.Geometry;

/**
 *
 * @author Doug Twilleager
 */
public class DiffuseMap extends Shader {
    /**
     * The vertex and fragment shader
     */

    protected static final String vShader =
        "varying vec3 viewDirection;" +
        "varying vec3 lightDirection[3];" +
        "varying vec3 fNormal; " +
        "void main(void)" +
        "{" +
        "        fNormal = gl_NormalMatrix * gl_Normal;" +
        "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
        "        gl_Position = ftransform();" +
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +

        "        lightDirection[0] = gl_LightSource[0].position.xyz - vVertex;" +
        "        lightDirection[1] = gl_LightSource[1].position.xyz - vVertex;" +
        "        lightDirection[2] = gl_LightSource[2].position.xyz - vVertex;" +
        "        viewDirection = -vVertex;" +
        "}";

    private static final String fShader =
        "varying vec3 viewDirection;" +
        "varying vec3 lightDirection[3];" +
        "varying vec3 fNormal; " +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform vec4 ambientMaterialColor; " +
        "uniform vec4 diffuseMaterialColor; " +
        "uniform vec4 specularMaterialColor; " +
        "vec3 normal;" +
        "vec3 finalColor;" +
        "vec3 diffuseColor;" +
        "vec3 specularColor;" +
        "float NdotL;" +
        "vec3 reflectDir;" +

        "void main(void) { " +
                 // Do some setup
        "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
        "        normal = normalize(fNormal);" +
        "        specularColor = vec3(1.0, 1.0, 1.0);" +

        "        vec3 ambientMat = ambientMaterialColor.rgb; " +
        "        vec3 diffuseMat = diffuseMaterialColor.rgb; " +
        "        vec3 specularMat = specularMaterialColor.rgb; " +
        "        finalColor.rgb  = ambientMat * diffuseColor;" +

                 // Compute diffuse for light0
        "        vec3 ldir = normalize(lightDirection[0]); " +
        "        vec3 vdir = normalize(viewDirection); " +
        "        NdotL = max(0.0, dot(normal, ldir));" +
        "        finalColor.rgb += (diffuseMat * diffuseColor.rgb * NdotL * gl_LightSource[0].diffuse.rgb);" +

                 // Compte specular for light0
        "        reflectDir = normalize(reflect(normal, ldir));" +
        "        float fRDotV = max( 0.0, dot( reflectDir, vdir ) );" +
        "        finalColor.rgb += (specularMat * pow( fRDotV, 25.0 ) * specularColor * gl_LightSource[0].specular.rgb);" +

                 // Compute diffuse for light1
        "        ldir = normalize(lightDirection[1]); " +
        "        NdotL = max(0.0, dot(normal, ldir));" +
        "        finalColor.rgb += (diffuseMat * diffuseColor.rgb * NdotL * gl_LightSource[1].diffuse.rgb);" +

                 // Compte specular for light1
        "        reflectDir = normalize(reflect(normal, ldir));" +
        "        fRDotV = max( 0.0, dot( reflectDir, vdir ) );" +
        "        finalColor.rgb += (specularMat * pow( fRDotV, 25.0 ) * specularColor * gl_LightSource[1].specular.rgb);" +

                 // Compute diffuse for light2
        "        ldir = normalize(lightDirection[2]); " +
        "        NdotL = max(0.0, dot(normal, ldir));" +
        "        finalColor.rgb += (diffuseMat * diffuseColor.rgb * NdotL * gl_LightSource[2].diffuse.rgb);" +

                 // Compte specular for light2
        "        reflectDir = normalize(reflect(normal, ldir));" +
        "        fRDotV = max( 0.0, dot( reflectDir, vdir ) );" +
        "        finalColor.rgb += (specularMat * pow( fRDotV, 25.0 ) * specularColor * gl_LightSource[2].specular.rgb);" +

        "        gl_FragColor.rgb = finalColor;" +
        "}";
    
    public DiffuseMap() {
        super(vShader, fShader);
        addRequiredUniform("DiffuseMapIndex");
        addRequiredUniform("ambientMaterialColor");
        addRequiredUniform("diffuseMaterialColor");
        addRequiredUniform("specularMaterialColor");
    }
    
    /**
     * This applies this shader to the given geometry
     */
    public void applyToGeometry(Geometry geo) {
        applyUniforms(geo);
        geo.setRenderState(shaderState);
    }
}
