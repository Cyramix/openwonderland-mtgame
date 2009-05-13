/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import com.jme.scene.Spatial;
import javolution.util.FastList;

/**
 * This interface abstracts a RenderTechnique.
 * 
 * @author Doug Twilleager
 */
public interface RenderTechnique {
    /**
     * This is called when the technique is first loaded
     */
    public abstract void initialize();

    /**
     * This gets called at the start of a rendered frame
     */
    public abstract void startFrame(RenderBuffer rb);

    /**
     * This gets called to get all of the opaque spatial objects
     * They are asumed to be sorted front to back
     */
    public abstract FastList<Spatial> getSpatials(RenderBuffer rb);

    /**
     * This gets called at the end of a rendered frame
     */
    public abstract void endFrame(RenderBuffer rb);

    /**
     * This method is called when a RenderComponent is added
     */
    public abstract void addRenderComponent(RenderComponent rc);

    /**
     * This method is called when RenderComponent is removed
     */
    public abstract void removeRenderComponent(RenderComponent rc);

    /**
     * The name of the technique
     */
    public abstract String getName();
}
