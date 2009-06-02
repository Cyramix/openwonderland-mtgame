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
