/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.mtgame;

import com.jme.renderer.Renderer;
import com.jme.system.DisplaySystem;


/**
 * This interface is implemented by anyone who wants to be updated in the render thread
 * 
 * @author Doug Twilleager
 */
public interface BufferController {
    /**
     * This method is called when a frame is about to start
     */
    public void startFrame(Renderer jMERenderer);

    /**
     * This method is called when a frame is done
     */
    public void endFrame(Renderer jMERenderer);

    /**
     * This method is called to render the whole scene
     */
    public abstract void renderScene(DisplaySystem ds, Renderer jmeRenderer, org.jdesktop.mtgame.Renderer mtRenderer);

    /**
     * This method is called when a buffer is added
     */
    public abstract void addBuffer(RenderBuffer rb);

    /**
     * This method is called when a buffer is removed
     */
    public abstract void removeBuffer(RenderBuffer rb);

    /**
     * This returns whether or not there are any buffers
     */
    public abstract boolean anyBuffers();

    /**
     * Get the current onscreen renderbuffer
     */
    public OnscreenRenderBuffer getCurrentOnscreenBuffer();

}
