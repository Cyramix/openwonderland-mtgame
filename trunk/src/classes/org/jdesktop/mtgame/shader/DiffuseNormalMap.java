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
//        "attribute vec3 binormal;" +
        "varying vec3 EyeDir;" +
        "varying vec3 LightDir[2];" +
        "void main(void)" +
        "{" +
        "        vec3 n = normalize(gl_NormalMatrix * gl_Normal);" +
        "        vec3 t = normalize(gl_NormalMatrix * tangent);" +
        "        vec3 b = cross(n, t);" +
        "        gl_Position = ftransform();" +
        "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +   
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        gl_TexCoord[1] = gl_MultiTexCoord1;" +
        "" +
        "        vec3 v;" +
        "        vec3 tmpVec = normalize(gl_LightSource[0].position.xyz - vVertex);" + 
        "        v.x = dot(tmpVec, t);" +
        "        v.y = dot(tmpVec, b);" +
        "        v.z = dot(tmpVec, n);" +
        "        LightDir[0] = normalize(v);" +
        "        tmpVec = normalize(gl_LightSource[1].position.xyz - vVertex);" +
        "        v.x = dot(tmpVec, t);" +
        "        v.y = dot(tmpVec, b);" +
        "        v.z = dot(tmpVec, n);" +
        "        LightDir[1] = normalize(v);" +
        "" +
        "        tmpVec = vVertex;" +
        "        v.x = dot(tmpVec, t);" +
        "        v.y = dot(tmpVec, b);" +
        "        v.z = dot(tmpVec, n);" +
        "        EyeDir = normalize(v);" +
        "}";
    
    private static final String fragmentShader = 
        "varying vec3 EyeDir;" +
        "varying vec3 LightDir[2];" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform sampler2D NormalMapIndex;" +
        "vec3 FragLocalNormal;" +
        "vec3 finalColor;" +
        "vec3 diffuseColor;" +    
        "vec3 specularColor;" +
        "float NdotL;" +
        "float spec;" +
        "vec3 reflectDir;" +
        "void main(void) { " +
                 // Do some setup
        "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
        "        FragLocalNormal = normalize(texture2D(NormalMapIndex, gl_TexCoord[0].st).xyz * 2.0 - 1.0);" +
        "        specularColor = vec3(1.0, 1.0, 1.0);" +
        "        finalColor = gl_FrontMaterial.ambient.rgb * gl_LightSource[0].ambient.rgb;" +
        
                 // Compute diffuse for light0
        "        NdotL = clamp(dot(FragLocalNormal, LightDir[0]), 0.0, 1.0);" +
        "        finalColor += diffuseColor * NdotL * gl_LightSource[0].diffuse.rgb;" +
   
                 // Compte specular for light0       
        "        reflectDir = reflect(LightDir[0], FragLocalNormal);" +
        "        spec = max(dot(EyeDir, reflectDir), 0.0);" +
        "        spec = pow(spec, 32.0);" +
        "        finalColor += spec * specularColor * gl_LightSource[0].specular.rgb;" + 
        
                 // Compute diffuse for light1
        "        finalColor += gl_FrontMaterial.ambient.rgb * gl_LightSource[1].ambient.rgb;" +
        "        NdotL = clamp(dot(FragLocalNormal, LightDir[1]), 0.0, 1.0);" +
        "        finalColor += diffuseColor * NdotL * gl_LightSource[1].diffuse.rgb;" +
        
                 // Compte specular for light1       
        "        reflectDir = reflect(LightDir[1], FragLocalNormal);" +
        "        spec = max(dot(EyeDir, reflectDir), 0.0);" +
        "        spec = pow(spec, 32.0);" +
        "        finalColor = min(finalColor + (spec * specularColor * gl_LightSource[1].specular.rgb), vec3(1.0));" +   
                 
                 // Final assignment
        "        gl_FragColor = vec4(finalColor, 1.0);" +
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
//        shaderState.setAttributePointer("binormal", 3, false, 0, geo.getBinormalBuffer());
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
