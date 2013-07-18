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

package org.jdesktop.mtgame.util;

import org.jdesktop.mtgame.*;

import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.nio.Buffer;
import com.jme.util.geom.BufferUtils;
import javax.media.opengl.GL2;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class FrameBufferCapture implements RenderUpdater {
    /**
     * The world manager
     */
    WorldManager worldManager = null;

    private FrameBufferListener listener = null;
    private RenderBuffer renderBuffer = null;
    private ByteBuffer rawData = null;
    private int dataType = GL2.GL_BGR;

    public interface FrameBufferListener {
        public void update(Buffer b);
    }

    /**
     * The constructor
     */
    public FrameBufferCapture(WorldManager wm, RenderBuffer rb, FrameBufferListener l) {
        worldManager = wm;
        listener = l;
        renderBuffer = rb;
        createBuffer(wm);
        renderBuffer.setRenderUpdater(this);
    }

    /**
     * The constructor
     */
    public FrameBufferCapture(WorldManager wm, RenderBuffer rb, FrameBufferListener l, ByteBuffer buffer, int type) {
        worldManager = wm;
        listener = l;
        renderBuffer = rb;
        rawData = buffer;
        dataType = type;
        renderBuffer.setRenderUpdater(this);
    }

    void createBuffer(WorldManager wm) {
        int size = renderBuffer.getWidth() * renderBuffer.getHeight() * 3;
        rawData = BufferUtils.createByteBuffer(size);
    }

    /**
     * Set the buffer to be used for grabbing the frame bufer
     * @param buffer
     */
    public void setBuffer(ByteBuffer buffer) {
        rawData = buffer;
    }

    /**
     * Get the buffer to be used for grabbing the frame bufer
     * @return
     */
    public ByteBuffer getBuffer() {
        return (rawData);
    }

    public void update(Object obj) {
        GL gl = GLU.getCurrentGL();
        gl.glReadPixels(0, 0, renderBuffer.getWidth(), renderBuffer.getHeight(), dataType, GL.GL_BYTE, rawData);
        if (listener != null) {
            listener.update(rawData);
        }
    }

    BufferedImage createBufferedImage(ByteBuffer bb) {
        int width = renderBuffer.getWidth();
        int height = renderBuffer.getHeight();

        bb.rewind();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int index = (y*width + x)*3;
                int b = bb.get(index);
                int g = bb.get(index+1);
                int r = bb.get(index+2);

                int pixel = ((r&255)<<16) | ((g&255)<< 8) | ((b&255)) | 0xff000000;

                bi.setRGB(x, (height-y)-1, pixel);
            }
        }
        return (bi);
    }

    /**
     * This creates and returns a buffered image containing the frame buffer data
     * @return
     */
    public BufferedImage getBufferedImage() {
        return (createBufferedImage(rawData));
    }
}
