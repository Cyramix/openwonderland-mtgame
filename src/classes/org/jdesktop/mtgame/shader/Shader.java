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
public abstract class Shader implements RenderUpdater {
    /**
     * The vertex and fragment shader
     */
    private String vertexShader = null;
    
    private String fragmentShader = null;
    
    /**
     * The shader state object for this shader
     */
    protected GLSLShaderObjectsState shaderState = null;
    
    public Shader(WorldManager worldManager, String vShader, String fShader) {
        vertexShader = vShader;
        fragmentShader = fShader;
        shaderState = (GLSLShaderObjectsState) worldManager.getRenderManager().
                createRendererState(RenderState.StateType.GLSLShaderObjects);
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
    public abstract void applyToGeometry(Geometry geo);
    
    /**
     * This loads the shader
     */
    public void update(Object o) {
        shaderState.load(vertexShader, fragmentShader);
    }
}
