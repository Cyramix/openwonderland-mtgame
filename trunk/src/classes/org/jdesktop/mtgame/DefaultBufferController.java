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
import javolution.util.FastList;
import com.jme.system.DisplaySystem;
import com.jme.scene.Spatial;


/**
 * This interface is implemented by anyone who wants to be updated in the render thread
 * 
 * @author Doug Twilleager
 */
public class DefaultBufferController implements BufferController {
    /**
     * A list of the buffers
     */
    private FastList<RenderBuffer> bufferList = new FastList<RenderBuffer>();
    protected FastList<RenderBuffer> renderBufferList = new FastList<RenderBuffer>();
    private boolean buffersChanged = false;
    private FastList<Spatial> renderList = null;
    private FastList<PassComponent> passList = null;

    /**
     * The current onscreen render buffer
     */
    private OnscreenRenderBuffer currentScreenBuffer = null;

    /**
     * This method is called when a frame is about to start
     */
    public void startFrame(Renderer jMERenderer) {
        updateBuffers();
    }
    
    /**
     * This method is called when a frame is done
     */
    public void endFrame(Renderer jMERenderer) {

    }

    /**
     * This method is called when a buffer is added
     */
    public void addBuffer(RenderBuffer rb) {
        synchronized (bufferList) {
            bufferList.add(rb);
            buffersChanged = true;
        }
    }

        /**
     * Check for new buffers and update lists accordingly
     */
    private void updateBuffers() {
        int startIndex = 0;

        synchronized (bufferList) {
            if (buffersChanged) {
                renderBufferList.clear();

                // Start with offscreen buffers
                for (int i = 0; i < bufferList.size(); i++) {
                    RenderBuffer rb = (RenderBuffer) bufferList.get(i);
                    if (rb.getTarget() != RenderBuffer.Target.ONSCREEN) {
                        insertRenderBuffer(startIndex, rb);
                    }
                }

                // Now the onscreen buffers, starting where we left off
                startIndex = renderBufferList.size();
                for (int i = 0; i < bufferList.size(); i++) {
                    RenderBuffer rb = (RenderBuffer) bufferList.get(i);
                    if (rb.getTarget() == RenderBuffer.Target.ONSCREEN) {
                        insertRenderBuffer(startIndex, rb);
                        currentScreenBuffer = (OnscreenRenderBuffer)rb;
                    }
                }
                buffersChanged = false;
            }
        }
    }

    /**
     * Insert the given render buffer into the list, start checking
     * at the given index
     */
    private void insertRenderBuffer(int startIndex, RenderBuffer rb) {
        if (renderBufferList.size() == startIndex) {
            renderBufferList.add(rb);
        } else {
            RenderBuffer currentRb = renderBufferList.get(startIndex);
            int index = 0;
            while (currentRb != null && rb.getOrder() < currentRb.getOrder()) {
                index++;
                currentRb = renderBufferList.get(index);
            }
            renderBufferList.add(index - 1, rb);
        }
    }

    /**
     * This method is called when a buffer is removed
     */
    public void removeBuffer(RenderBuffer rb) {
        synchronized (bufferList) {
            bufferList.add(rb);
            buffersChanged = true;
        }
    }

    /**
     * This returns whether or not there are any buffers
     */
    public boolean anyBuffers() {
        return (renderBufferList.size() != 0);
    }

    /**
     * Get the current onscreen renderbuffer
     */
    public OnscreenRenderBuffer getCurrentOnscreenBuffer() {
        return (currentScreenBuffer);
    }

    /**
     * This method is called to render the whole scene
     */
    public void renderScene(DisplaySystem ds, Renderer jmeRenderer, org.jdesktop.mtgame.Renderer mtRenderer) {
        for (int i=0; i<renderBufferList.size(); i++) {
            RenderBuffer rb = renderBufferList.get(i);
            renderBuffer(ds, jmeRenderer, mtRenderer, rb);
        }
    }
    
    public void renderBuffer(DisplaySystem displaySystem, Renderer jmeRenderer, org.jdesktop.mtgame.Renderer mtRenderer, RenderBuffer rb) {
        if (rb != currentScreenBuffer) {
            rb.makeCurrent(displaySystem, jmeRenderer);
        }

        rb.clear(jmeRenderer);

        renderList = mtRenderer.getRenderList(rb);
        passList = mtRenderer.getPassList(rb);

        for (int pass = 0; pass < rb.numRenderPasses(); pass++) {
            rb.preparePass(jmeRenderer, renderList, passList, pass);
            rb.renderOpaque(jmeRenderer);
            rb.renderPass(jmeRenderer);
            rb.renderTransparent(jmeRenderer);
            rb.renderOrtho(jmeRenderer);
            rb.completePass(jmeRenderer, pass);
        }

        mtRenderer.endFrame(rb);

        if (rb.getRenderUpdater() != null) {
            rb.getRenderUpdater().update(rb);
        }

        rb.swap();
        if (rb != currentScreenBuffer) {
            rb.release();
        }
    }
}
