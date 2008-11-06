/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import java.util.ArrayList;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class OnscreenRenderBuffer extends RenderBuffer {
    /**
     * The constructor
     */
    public OnscreenRenderBuffer(Target target, int width, int height) {
        super(target, width, height);
    }
    
    void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents) {
    }
    
    /**
     * Render the current RenderList into this buffer
     */
    void render(Renderer r) {
    }
}
