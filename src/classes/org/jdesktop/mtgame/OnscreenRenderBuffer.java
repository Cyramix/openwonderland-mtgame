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
import com.jme.system.DisplaySystem;
import java.awt.Canvas;
import javolution.util.FastList;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.GL;

import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class OnscreenRenderBuffer extends RenderBuffer {
    /**
     * The Canvas used for ONSCREEN rendering
     */
    private GLCanvas canvas = null;

    /**
     * The current GLContext
     */
    private GLContext glContext = null;

    /**
     * The current GL object
     */
    private GL gl = null;

    /**
     * A boolean that indicates that we should do a clear
     */
    private boolean doClear = true;

    /**
     * A boolean that indicates  that we should do a swap
     */
    private boolean doSwap = true;

    /**
     * The constructor
     */
    public OnscreenRenderBuffer(Target target, int width, int height, int order) {
        super(target, width, height, order);
    }

    /**
     * Set the canvas in onscreen mode
     */
    void setCanvas(GLCanvas c) {
        canvas = c;
    }

    /**
     * Get the onscreen canvas.
     */
    public Canvas getCanvas() {
        return (canvas);
    }

    /**
     * Set the swap flag
     */
    public void setSwapEnable(boolean flag) {
        doSwap = flag;
    }

    /**
     * Get the swap flag
     */
    public boolean getSwapEnable() {
        return (doSwap);
    }


    /**
     * Set the clear flag
     */
    public void setClearEnable(boolean flag) {
        doClear = flag;
    }

    /**
     * Get the clear flag
     */
    public boolean getClearEnable() {
        return (doClear);
    }

    /**
     * This gets called to clear the buffer
     */
    public void clear(Renderer renderer) {
        if (doClear) {
            renderer.clearBuffers();
            renderer.clearStencilBuffer();
        }
    }

    /**
     * This gets called to make this render buffer current for rendering
     */
    public boolean makeCurrent(DisplaySystem display, Renderer jMERenderer) {
        boolean doRender = true;

        GLCanvas currentCanvas = (GLCanvas) canvas;
        glContext = currentCanvas.getContext();
        try {
            glContext.makeCurrent();
        } catch (javax.media.opengl.GLException e) {
            System.out.println(e);
        }
        gl = glContext.getGL();


        CameraComponent cc = getCameraComponent();
        if (cc != null) {
            Camera camera = cc.getCamera();
            if (getWidth() != canvas.getWidth() ||
                getHeight() != canvas.getHeight()) {
                setWidth(canvas.getWidth());
                setHeight(canvas.getHeight());
                camera.resize(canvas.getWidth(), canvas.getHeight());
            }
            camera.update();
            jMERenderer.setCamera(camera);
            camera.apply();
            jMERenderer.setBackgroundColor(backgroundColor);
        } else {
            doRender = false;
        }
        return (doRender);
    }

    /**
     * These are used to render the given opaque, transparent, and ortho objects
     */
    public void preparePass(Renderer renderer, FastList<Spatial> renderList, FastList<PassComponent> passList, int pass) {
        renderer.clearQueue();

        Portal p = getPortal();
        if (p != null) {
            renderer.getQueue().setPortalGeometry(p.getGeometry(), lastLoc, lastDir, lastUp, lastLeft);
        }

        if (getManageRenderScenes()) {
            synchronized (renderComponentList) {
                renderList(renderer, managedRenderList);
                renderPassList(renderer, managedPassList);
            }
        } else {
            renderList(renderer, renderList);
            renderPassList(renderer, passList);
        }
    }

    public void completePass(Renderer renderer, int pass) {
        // Nothing to do
        renderer.renderQueue();
        renderer.getQueue().setPortalGeometry(null, null, null, null, null);
    }

    public void renderOpaque(Renderer renderer) {
    }

    public void renderPass(Renderer renderer) {
    }

    public void renderTransparent(Renderer renderer) {
    }

    public void renderOrtho(Renderer renderer) {
    }

    private void renderPassList(Renderer renderer, FastList<PassComponent> list) {
        for (int i=0; i<list.size(); i++) {
            PassComponent pc = (PassComponent) list.get(i);
            pc.getPass().renderPass(renderer);
        }
    }

    private void renderList(Renderer renderer, FastList<Spatial> list) {
        for (int i=0; i<list.size(); i++) {
            renderer.draw(list.get(i));
        }
    }

    /**
     * This is called when a frame has completed
     */
    public void release() {
        glContext.release();
    }

    /**
     * This is called when the buffer needs to be swaped
     */
    public void swap() {
        if (doSwap) {
            canvas.swapBuffers();
        }
    }
}
