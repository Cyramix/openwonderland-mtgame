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
public class DiffuseMapAO implements RenderUpdater {
    /**
     * The vertex and fragment shader
     */
    private static final String vertexShader =
        "vec3 EyeDir;" +
        "varying vec3 LightDir;" +
        "varying vec3 VNormal;" +
        "void main(void)" +
        "{" +
        "        VNormal = normalize(gl_NormalMatrix * gl_Normal);" +
        "        gl_Position = ftransform();" +
        "        EyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);" +   
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        gl_TexCoord[1] = gl_MultiTexCoord1;" +
        "        LightDir = normalize(gl_LightSource[0].position.xyz - EyeDir);" +
        "}";
    
    private static final String fragmentShader = 
        "varying vec3 LightDir;" +
        "varying vec3 VNormal;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D AmbientOccIndex;" +
        "vec3 FragLocalNormal;" +
        "vec3 finalColor;" +
        "vec3 ambientOcc;" +
        "float NdotL;" +
        "void main(void) { " +
        "        finalColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
        "        ambientOcc = texture2D(AmbientOccIndex, gl_TexCoord[1].st).rgb;" +
        "        NdotL = clamp(dot(VNormal, LightDir), 0.0, 1.0);" +
        "        finalColor = finalColor * ambientOcc;" +
        "        gl_FragColor = vec4(finalColor, 1.0);" +
        "" + 
//        "        vec3 reflectDir = reflect(LightDir, FragLocalNormal);" +
//        "        float spec = max(dot(EyeDir, reflectDir), 0.0);" +
//        "        spec = pow(spec, 6.0) * 0.5;" +
//        "        finalColor = min(finalColor + spec, vec3(1.0));" +        "        gl_FragColor = vec4(finalColor, 1.0);" +
        "}";
    
    /**
     * The shader state object for this shader
     */
    private GLSLShaderObjectsState shaderState = null;
    
    public DiffuseMapAO(WorldManager worldManager) {
        shaderState = (GLSLShaderObjectsState) worldManager.getRenderManager().
                createRendererState(RenderState.RS_GLSL_SHADER_OBJECTS);
        worldManager.addRenderUpdater(this, this);        
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
    public void applyToGeometry(Geometry geo) {
        shaderState.setUniform("DiffuseMapIndex", 0);
        shaderState.setUniform("AmbientOccIndex", 1);
        geo.setRenderState(shaderState);
    }
    
    /**
     * This loads the shader
     */
    public void update(Object o) {
        shaderState.load(vertexShader, fragmentShader);
    }
}
