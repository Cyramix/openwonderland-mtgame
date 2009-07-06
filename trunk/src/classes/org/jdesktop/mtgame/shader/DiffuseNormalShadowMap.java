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
public class DiffuseNormalShadowMap extends Shader {
    /**
     * The vertex and fragment shader
     */
    protected static final String vShader =
        "varying vec3 viewDirection;" +
        "varying vec3 lightDirection[3];" +
        "varying vec4 shadowCoordinate;" +
        "uniform mat4 inverseView; " +

        "void main(void)" +
        "{" +
        "        vec3 n = normalize(gl_NormalMatrix * gl_Normal);" +
        "        vec3 t = normalize(gl_NormalMatrix * gl_SecondaryColor.xyz);" +
        "        vec3 b = cross(t, n);" +
        "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
        "        shadowCoordinate = gl_TextureMatrix[2] * inverseView * (gl_ModelViewMatrix * gl_Vertex);" +
        "        gl_Position = ftransform();" +
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        viewDirection.x = dot(-vVertex, t);" +
        "        viewDirection.y = dot(-vVertex, b);" +
        "        viewDirection.z = dot(-vVertex, n);" +

        "        vec3 tmpVec = normalize(gl_LightSource[0].position.xyz - vVertex);" +
        "        lightDirection[0].x = dot(tmpVec, t);" +
        "        lightDirection[0].y = dot(tmpVec, b);" +
        "        lightDirection[0].z = dot(tmpVec, n);" +

        "        tmpVec = normalize(gl_LightSource[1].position.xyz - vVertex);" +
        "        lightDirection[1].x = dot(tmpVec, t);" +
        "        lightDirection[1].y = dot(tmpVec, b);" +
        "        lightDirection[1].z = dot(tmpVec, n);" +

        "        tmpVec = normalize(gl_LightSource[2].position.xyz - vVertex);" +
        "        lightDirection[2].x = dot(tmpVec, t);" +
        "        lightDirection[2].y = dot(tmpVec, b);" +
        "        lightDirection[2].z = dot(tmpVec, n);" +
        "}";

    private static final String fShader =
        "varying vec3 viewDirection;" +
        "varying vec3 lightDirection[3];" +
        "varying vec4 shadowCoordinate;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D NormalMapIndex;" +
        "uniform sampler2D ShadowMapIndex;" +
        "uniform vec4 ambientMaterialColor; " +
        "uniform vec4 diffuseMaterialColor; " +
        "uniform vec4 specularMaterialColor; " +
        "vec3 normal;" +
        "vec3 finalColor;" +
        "vec3 diffuseColor;" +
        "vec3 specularColor;" +
        "float NdotL;" +
        "vec3 reflectDir;" +
        "vec2 shadowC; " +

        "float sampleShadowXY(vec2 shadowCoord, float fragDepth)"+
        "{"+
        "   float depth = 1.0;" +
        "   float shadowDepth = texture2D(ShadowMapIndex, shadowCoord).r;"+
        "   if (fragDepth > shadowDepth + 0.0001) " +
        "       depth = 0.0;" +
        "   return depth;" +
//        "   float depth = (fragDepth)/(shadowDepth);"+
//        "   return depth > shadowBias ? 0.0 : 1.0;"+
        "}"+

        "void main(void) { " +
                 // Do some setup
        "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
        "        normal = normalize(texture2D(NormalMapIndex, gl_TexCoord[0].st).xyz * 2.0 - 1.0);" +
        "        specularColor = vec3(1, 1, 1);" +

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

        "        shadowC = shadowCoordinate.xy/shadowCoordinate.w; " +
        "        float fragDepth = shadowCoordinate.z/shadowCoordinate.w;" +
        "		 float x, y, shadow;" +
		"        for (y = -1.5 ; y <=1.5 ; y+=1.0)" +
		"            for (x = -1.5 ; x <=1.5 ; x+=1.0)" +
		"                shadow += sampleShadowXY(vec2(x/2048.0+shadowC.x,y/2048.0+shadowC.y), fragDepth); " +
		"        shadow /= 16.0;" +
//        "        float shadow = sampleShadowXY(shadowC, fragDepth); " +
        "        shadow = min(1.0, shadow + 0.4); " +
        "        gl_FragColor.rgb = finalColor * shadow;" +
        "}";
    
    public DiffuseNormalShadowMap() {
        super(vShader, fShader);
        addRequiredUniform("DiffuseMapIndex");
        addRequiredUniform("NormalMapIndex");
        addRequiredUniform("ShadowMapIndex");
        addRequiredUniform("ambientMaterialColor");
        addRequiredUniform("diffuseMaterialColor");
        addRequiredUniform("specularMaterialColor");
        setShadowMapIndex(2);
    }

    /**
     * This applies this shader to the given geometry
     */
    public void applyToGeometry(Geometry geo) {
        applyUniforms(geo);
        geo.setRenderState(shaderState);
    }
}
