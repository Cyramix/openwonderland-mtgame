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

package org.jdesktop.mtgame.processor;

import org.jdesktop.mtgame.*;
import com.jme.scene.Geometry;
import com.jme.renderer.ColorRGBA;

/**
 * This is a simple test processor that increments/decrements alpha
 * 
 * @author Doug Twilleager
 */
public class AlphaProcessor extends ProcessorComponent {     
    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
    /**
     * The current alpha
     */
    private float alpha = 0.0f;

    /**
     * The increment apply each frame
     */
    private float increment = 0.0f;
    
    /**
     * The geometry target
     */
    private Geometry target = null;
    
    /**
     * A name
     */
    private String name = null;
    
    /**
     * The constructor
     */
    public AlphaProcessor(String name, WorldManager worldManager, Geometry target, float increment) {
        this.worldManager = worldManager;
        this.target = target;
        this.increment = increment;
        this.name = name;
        alpha = target.getDefaultColor().a;
        setArmingCondition(new NewFrameCondition(this));
    }
    
    public String toString() {
        return (name);
    }
    
    /**
     * The initialize method
     */
    public void initialize() {
        //setArmingCondition(new NewFrameCondition(this));
    }
    
    /**
     * The Calculate method
     */
    public void compute(ProcessorArmingCollection collection) {
        alpha += increment;
        if (alpha > 1.0f) {
            increment = -increment;
            alpha = 1.0f;
        } else if (alpha < 0.0f) {
            increment = -increment;
            alpha = 0.0f;
        }
    }

    /**
     * The commit method
     */
    public void commit(ProcessorArmingCollection collection) {
        ColorRGBA color = target.getDefaultColor();
        color.a = alpha;
        target.setDefaultColor(color);
        worldManager.addToUpdateList(target);
    }
}
