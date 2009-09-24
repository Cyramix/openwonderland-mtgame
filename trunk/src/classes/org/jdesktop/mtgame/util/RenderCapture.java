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

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class RenderCapture implements BufferUpdater {
    /**
     * The world manager
     */
    WorldManager worldManager = null;

    /**
     * The width and height of this buffer
     */
    private int textureWidth = 0;
    private int textureHeight = 0;
    private float aspect = 0.0f;

    /**
     * Vectors for the camera
     */
    private Vector3f pos = new Vector3f();
    private Quaternion quat = new Quaternion();
    
    /**
     * The Camera Component used to render into this buffer
     */
    private CameraComponent cameraComponent = null;
    
    /**
     * The background color
     */
    private ColorRGBA backgroundColor = new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f);

    /**
     * The RenderBuffer
     */
    RenderBuffer rb = null;

    /**
     * The camera SG
     */
    Node cameraSG = null;

    private BufferedImage bi = null;
    private RenderCaptureListener listener = null;

    public interface RenderCaptureListener {
        public void update(BufferedImage bi);
    }

    /**
     * The constructor
     */
    public RenderCapture(WorldManager wm, int texWidth, int texHeight,
            RenderCaptureListener l) {
        textureWidth = texWidth;
        textureHeight = texHeight;
        aspect = textureWidth/textureHeight;
        worldManager = wm;
        listener = l;
        createRenderBuffer(wm);
    }

    public RenderCapture(WorldManager wm, Vector3f position, Quaternion rotation,
            int texWidth, int texHeight) {
        textureWidth = texWidth;
        textureHeight = texHeight;
        pos.set(position);
        quat.set(rotation);
        aspect = textureWidth/textureHeight;
        worldManager = wm;

        createRenderBuffer(wm);
    }

    public void setEnable(boolean flag) {
        rb.setEnable(flag);
    }

    public boolean getEnable() {
        return (rb.getEnable());
    }

    public void init(RenderBuffer rb) {
        Entity e = new Entity("Mirror ");
        createCamera(worldManager, e);
        createCaptureProcessor(worldManager, e);
        worldManager.addEntity(e);
    }

    void createCamera(WorldManager wm, Entity e) {
        CameraNode cn = new CameraNode("MyCamera", null);
        cameraSG = new Node();
        cameraSG.attachChild(cn);
        cameraSG.setLocalTranslation(pos);
        cameraSG.setLocalRotation(quat);

        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn,
                textureWidth, textureHeight, 45.0f, aspect, 1.0f, 1000.0f, true);
        rb.setCameraComponent(cc);
        rb.setBackgroundColor(new ColorRGBA(1.0f, 0.0f, 0.0f, 0.0f));
        e.addComponent(CameraComponent.class, cc);
    }

    void createRenderBuffer(WorldManager wm) {
        rb = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, textureWidth, textureHeight);
        wm.getRenderManager().addRenderBuffer(rb);
        rb.setBufferUpdater(this);
    }

    void createCaptureProcessor(WorldManager wm, Entity e) {
        CaptureProcessor cp = new CaptureProcessor((TextureRenderBuffer)rb);
        e.addComponent(ProcessorComponent.class, cp);
    }
    
    public void setCameraData(Vector3f position, Quaternion rotation) {
        pos.set(position);
        quat.set(rotation);

        if (cameraSG != null) {
            cameraSG.setLocalTranslation(pos);
            cameraSG.setLocalRotation(quat);
            worldManager.addToUpdateList(cameraSG);
        }
    }

    BufferedImage createBufferedImage(ByteBuffer bb) {
        bb.rewind();
        BufferedImage bi = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_4BYTE_ABGR);
        for (int y=0; y<textureHeight; y++) {
            for (int x=0; x<textureWidth; x++) {
                int index = (y*textureWidth + x)*4;
                int r = bb.get(index);
                int g = bb.get(index+1);
                int b = bb.get(index+2);

                int pixel = ((r&255)<<16) | ((g&255)<< 8) | ((b&255)) | 0xff000000;

                bi.setRGB(x, (textureHeight-y)-1, pixel);
            }
        }
        return (bi);
    }

    public BufferedImage getBufferedImage() {
        return (bi);
    }

    public class CaptureProcessor extends ProcessorComponent {
        TextureRenderBuffer textureRenderBuffer = null;
        NewFrameCondition condition = null;

        public CaptureProcessor(TextureRenderBuffer trb) {
            textureRenderBuffer = trb;
        }

        public void initialize() {
            condition = new NewFrameCondition(this);
            setArmingCondition(condition);
        }

        public void compute(ProcessorArmingCollection collection) {

        }

        public void commit(ProcessorArmingCollection collection) {
            // Just grab the bits.
            ByteBuffer data = textureRenderBuffer.getTextureData();
            data.rewind();
            bi = createBufferedImage(data);
            if (listener != null) {
                listener.update(bi);
            }
            //System.out.println("GOT DATA: " + bi);
        }
    }
}
