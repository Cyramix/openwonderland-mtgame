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
public class DiffuseShadowMapAO extends Shader {
    /**
     * The vertex and fragment shader
     */
    protected static final String vShader =
        "varying vec3 viewDirection;" +
        "varying vec3 lightDirection[3];" +
        "varying vec4 shadowCoordinate;" +
        "uniform mat4 inverseView;" +
        "varying vec3 normal;" +
        "void main(void)" +
        "{" +
        "        normal = gl_NormalMatrix * gl_Normal;" +
        "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        shadowCoordinate = gl_TextureMatrix[2] * inverseView * (gl_ModelViewMatrix * gl_Vertex);" +
        "" +
        "        lightDirection[0] = normalize(gl_LightSource[0].position.xyz);" +
        "        lightDirection[1] = normalize(gl_LightSource[1].position.xyz);" +
        "        lightDirection[2] = normalize(gl_LightSource[2].position.xyz);" +
        "        viewDirection = -vVertex;" +
        "        gl_Position = ftransform();" +
        "}";

    protected static final String fShader =
        "varying vec3 lightDirection[3];" +
        "varying vec3 normal;" +
        "varying vec3 viewDirection;" +

        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D AmbientOccMapIndex;" +
        "uniform sampler2D ShadowMapIndex;" +
        "varying vec4 shadowCoordinate;" +
        "uniform vec4 ambientMaterialColor; " +
        "uniform vec4 diffuseMaterialColor; " +
        "uniform vec4 specularMaterialColor; " +
        "vec4 finalColor;" +
        "vec3 ambientOcc;" +
        "vec4 diffuseColor;" +
        "vec3 specularColor;" +
        "float NdotL;" +
        "float spec;" +
        "float fRDotV;" +
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
        "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st);" +
        "        ambientOcc = texture2D(AmbientOccMapIndex, gl_TexCoord[0].st).rgb;" +
        "        specularColor = vec3(1.0, 1.0, 1.0);" +
        "        vec3 specMat = specularMaterialColor.rgb; " +
        "        finalColor.rgb  = ambientMaterialColor.rgb * diffuseColor.rgb;" +
        "        vec3 vdir = normalize(viewDirection);" +
                 // Compute diffuse for light0
        "        NdotL = max(0.0, dot(normal, lightDirection[0]));" +
        "        finalColor.rgb += diffuseMaterialColor.rgb * diffuseColor.rgb * NdotL * gl_LightSource[0].diffuse.rgb;" +

                 // Compte specular for light0
        "        reflectDir = normalize(reflect(lightDirection[0], normal));" +
        "        float fRDotV = max( 0.0, dot( reflectDir, vdir ) );" +
        "        finalColor.rgb += specMat * pow( fRDotV, 25.0 ) * specularColor * gl_LightSource[0].specular.rgb;" +

                 // Compute diffuse for light1
        "        NdotL = max(0.0, dot(normal, lightDirection[1]));" +
        "        finalColor.rgb += diffuseMaterialColor.rgb * diffuseColor.rgb * NdotL * gl_LightSource[0].diffuse.rgb;" +

                 // Compte specular for light1
        "        reflectDir = normalize(reflect(lightDirection[1], normal));" +
        "        fRDotV = max( 0.0, dot( reflectDir, vdir ) );" +
        "        finalColor.rgb += specMat * pow( fRDotV, 25.0 ) * specularColor * gl_LightSource[0].specular.rgb;" +

                         // Compute diffuse for light2
        "        NdotL = max(0.0, dot(normal, lightDirection[2]));" +
        "        finalColor.rgb += diffuseMaterialColor.rgb * diffuseColor.rgb * NdotL * gl_LightSource[0].diffuse.rgb;" +

                 // Compte specular for light2
        "        reflectDir = normalize(reflect(lightDirection[2], normal));" +
        "        fRDotV = max( 0.0, dot( reflectDir, vdir ) );" +
        "        finalColor.rgb += specMat * pow( fRDotV, 25.0 ) * specularColor * gl_LightSource[0].specular.rgb;" +

        "        shadowC = shadowCoordinate.xy/shadowCoordinate.w; " +
        "        float fragDepth = shadowCoordinate.z/shadowCoordinate.w;" +
        "		 float x, y, shadow;" +
		"        for (y = -1.5 ; y <=1.5 ; y+=1.0)" +
		"            for (x = -1.5 ; x <=1.5 ; x+=1.0)" +
		"                shadow += sampleShadowXY(vec2(x/2048.0+shadowC.x,y/2048.0+shadowC.y), fragDepth); " +
		"        shadow /= 16.0;" +
//        "        float shadow = sampleShadowXY(shadowC, fragDepth); " +
        "        shadow = min(1.0, shadow + 0.4); " +

        "        gl_FragColor.rgb = finalColor.rgb * ambientOcc * shadow;" +
        "}";
    
    public DiffuseShadowMapAO() {
        super(vShader, fShader);
        addRequiredUniform("DiffuseMapIndex");
        addRequiredUniform("AmbientOccMapIndex");
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
