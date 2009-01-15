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
public class DiffuseNormalMap implements RenderUpdater {
    /**
     * The vertex and fragment shader
     */
    private static final String vertexShader =
        "attribute vec3 tangent;" +
        "attribute vec3 binormal;" +
        "varying vec3 EyeDir;" +
        "varying vec3 LightDir;" +
        "void main(void)" +
        "{" +
        "        vec3 n = normalize(gl_NormalMatrix * gl_Normal);" +
        "        vec3 t = normalize(gl_NormalMatrix * tangent);" +
        "        vec3 b = normalize(gl_NormalMatrix * binormal);" +
        "        gl_Position = ftransform();" +
        "        EyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);" +   
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        gl_TexCoord[1] = gl_MultiTexCoord1;" +
        "" +
        "        vec3 v;" +
        "        v.x = dot(gl_LightSource[0].position.xyz, t);" +
        "        v.y = dot(gl_LightSource[0].position.xyz, b);" +
        "        v.z = dot(gl_LightSource[0].position.xyz, n);" +
        "        LightDir = normalize(v);" +
        "" +
        "        v.x = dot(EyeDir, t);" +
        "        v.y = dot(EyeDir, b);" +
        "        v.z = dot(EyeDir, n);" +
        "        EyeDir = normalize(v);" +
        "}";
    
    private static final String fragmentShader = 
        "varying vec3 EyeDir;" +
        "varying vec3 LightDir;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D NormalMapIndex;" +
        "vec3 FragLocalNormal;" +
        "vec3 finalColor;" +
        "float NdotL;" +
        "void main(void) { " +
        "        finalColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
        "        FragLocalNormal = normalize(texture2D(NormalMapIndex, gl_TexCoord[0].st).xyz * 2.0 - 1.0);" +
        "        NdotL = clamp(dot(FragLocalNormal, LightDir), 0.0, 1.0);" +
        "        finalColor = finalColor * NdotL;" +
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
    
    public DiffuseNormalMap(WorldManager worldManager) {
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
        shaderState.setAttributePointer("binormal", 3, false, 0, geo.getBinormalBuffer());
        shaderState.setAttributePointer("tangent", 3, false, 0, geo.getTangentBuffer()); 
        shaderState.setUniform("DiffuseMapIndex", 0);
        shaderState.setUniform("NormalMapIndex", 1);
        geo.setRenderState(shaderState);
    }
    
    /**
     * This loads the shader
     */
    public void update(Object o) {
        shaderState.load(vertexShader, fragmentShader);
    }
}
