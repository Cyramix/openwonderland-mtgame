/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    public DiffuseMapAlphaMap(WorldManager worldManager) {
        super(worldManager, vShader, fShader);
    }

    /**
     * This applies this shader to the given geometry
     */
    public void applyToGeometry(Geometry geo) {
        shaderState.setUniform("DiffuseMapIndex", 0);
        shaderState.setUniform("AlphaMapIndex", 1);
        geo.setRenderState(shaderState);
    }
}
