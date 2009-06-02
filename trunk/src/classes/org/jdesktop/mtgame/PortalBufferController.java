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

import com.jme.renderer.Renderer;
import javolution.util.FastList;
import com.jme.system.DisplaySystem;
import com.jme.renderer.Camera;
import com.jme.math.Vector3f;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import javax.media.opengl.GLCanvas;


/**
 * This interface is implemented by anyone who wants to be updated in the render thread
 * 
 * @author Doug Twilleager
 */
public class PortalBufferController extends DefaultBufferController {
    /**
     * This list of known sectors
     */
    private FastList<Sector> sectorList = new FastList<Sector>();

    /**
     * The recursive threshold
     */
    private int recurseThreshold = 1;

    /**
     * The Camera used for portal rendering
     */
    private Camera portalCamera = null;
    private Camera lastPortalCamera = null;

    /**
     * The current onscreen buffer
     */
    OnscreenRenderBuffer screenBuffer = null;


    /**
     * This method is called to render the whole scene
     */
    public void renderScene(DisplaySystem ds, Renderer jmeRenderer, org.jdesktop.mtgame.Renderer mtRenderer) {
        jmeRenderer.clearBuffers();
        jmeRenderer.clearStencilBuffer();

        renderAllBuffers(ds, jmeRenderer, mtRenderer);

        screenBuffer = getCurrentOnscreenBuffer();
        Camera jMECamera = jmeRenderer.getCamera();
        Sector s = findSector(jMECamera.getLocation());

        if (s != null) {
            if (portalCamera == null) {
                portalCamera = jmeRenderer.createCamera(screenBuffer.getWidth(), screenBuffer.getHeight());
                lastPortalCamera = jmeRenderer.createCamera(screenBuffer.getWidth(), screenBuffer.getHeight());
            }

            Vector3f location = new Vector3f(jMECamera.getLocation());
            Vector3f direction = new Vector3f(jMECamera.getDirection());
            Vector3f up = new Vector3f(jMECamera.getUp());
            Vector3f left = new Vector3f(jMECamera.getLeft());
            //jmeRenderer.setCamera(portalCamera);

            renderPortalsInSector(s, 0, ds, jmeRenderer, mtRenderer, location, direction, up, left);

            //jmeRenderer.setCamera(saveCamera);
            jMECamera.update();
            jMECamera.apply();
        }
        ((GLCanvas) (screenBuffer.getCanvas())).swapBuffers();
    }

    /**
     * This method is called to render the whole scene
     */
    void renderPortalsInSector(Sector s, int level, DisplaySystem ds, Renderer jmeRenderer, org.jdesktop.mtgame.Renderer mtRenderer,
                               Vector3f cLoc, Vector3f cDir, Vector3f cUp, Vector3f cLeft) {
        if (level > recurseThreshold) {
            return;
        }
        
        // Calculate frustum
        Portal p = null;
        FastList<Portal> portalList = s.getPortalList();
        for (int i=0; i<portalList.size(); i++) {
            //System.out.println(" ========================== Portal: " + i);
            p = portalList.get(i);
            jmeRenderer.clearStencilBuffer();
            // Do intersect test
            // Calculate new Camera
            Vector3f loc = new Vector3f();
            Vector3f dir = new Vector3f();
            Vector3f up = new Vector3f();
            Vector3f left = new Vector3f();
            updateCamera(jmeRenderer.getCamera(), p, cLoc, cDir, cUp, cLeft, loc, dir, up, left);

            screenBuffer.setPortal(p, cLoc, cDir, cUp, cLeft);
            renderBuffer(ds, jmeRenderer, mtRenderer, screenBuffer);
            screenBuffer.setPortal(null, null, null, null, null);

            //renderPortalsInSector(p.getNextSector(), level+1, ds, jmeRenderer, mtRenderer, cLoc, cDir, cUp, cLeft);
        }
        
    }  
    
    /**
     * Update the camera position.  This is done my applying the
     * portal transform (from entry to exit) to the existing camera.
     */
    private void updateCamera(Camera c, Portal p, Vector3f cLoc, Vector3f cDir, Vector3f cUp, Vector3f cLeft,
                              Vector3f nLoc, Vector3f nDir, Vector3f nUp, Vector3f nLeft) {
        //Quaternion rot = p.getEnterExitRotation();
        Matrix3f rot = p.getEnterExitRotation();
        Matrix3f rotI = rot.invert().transpose();
        //Vector3f camDir = new Vector3f();

        //System.out.println("updateCamera for Portal: " + p);
        //Vector3f camToEnter = new Vector3f();
        //camToEnter.x = cLoc.x - p.getEnterLocation().x;
        //camToEnter.y = cLoc.y - p.getEnterLocation().y;
        //camToEnter.z = cLoc.z - p.getEnterLocation().z;
        //System.out.println("Camera To Enter: " + camToEnter);
        //float d = camToEnter.length();
        //camToEnter.normalizeLocal();
        //rotI.mult(camToEnter, camDir);
        //camDir.normalizeLocal();
        //camDir.multLocal(d);

        nLoc.set(p.getExitLocation().x, p.getExitLocation().y, p.getExitLocation().z);
        //nLoc.addLocal(camDir);

        rotI.mult(cDir, nDir);
        rotI.mult(cUp, nUp);
        rotI.mult(cLeft, nLeft);
//        System.out.println("Current Camera is at: " + cLoc);
//        System.out.println("Current Camera dirct: " + cDir);
//        System.out.println("New Camera is at: " + nLoc);
//        System.out.println("New Camera dirct: " + nDir);
        c.setLocation(nLoc);
        c.setDirection(nDir);
        c.setUp(nUp);
        c.setLeft(nLeft);
        c.update();
        c.apply();
    }

    /**
     * This method is called to render the whole scene
     */
    void renderAllBuffers(DisplaySystem ds, Renderer jmeRenderer, org.jdesktop.mtgame.Renderer mtRenderer) {
        for (int i=0; i<renderBufferList.size(); i++) {
            RenderBuffer rb = renderBufferList.get(i);
            renderBuffer(ds, jmeRenderer, mtRenderer, rb);
        }
    }

    /**
     * Find the sector that includes this point.
     * @param pos
     * @return
     */
    Sector findSector(Vector3f pos) {
        Sector s = null;

        synchronized (sectorList) {
            for (int i = 0; i < sectorList.size(); i++) {
                s = sectorList.get(i);
                if (s.getBoundingVolume().contains(pos)) {
                    return (s);
                }
            }
        }
        return (null);
    }

    /**
     * This method is called when a frame is about to start
     */
    public void startFrame(Renderer jMERenderer) {
        super.startFrame(jMERenderer);
    }

    /**
     * Add a sector
     */
    public void addSector(Sector s) {
        synchronized (sectorList) {
            sectorList.add(s);
        }
    }

    /**
     * Remove a sector
     */
    public void removeSector(Sector s) {
        synchronized (sectorList) {
            sectorList.remove(s);
        }
    }
}
