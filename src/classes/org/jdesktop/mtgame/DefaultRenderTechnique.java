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

import javax.media.opengl.GL;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Geometry;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.renderer.Camera;
import javolution.util.FastList;
import com.jme.math.Vector3f;


/**
 * This interface is implemented by anyone who wants to be updated in the render thread
 * 
 * @author Doug Twilleager
 */
public class DefaultRenderTechnique implements RenderTechnique {
    public enum ListType {
        Opaque,
        Transparent,
        Ortho
    }

    /**
     * The list of Nodes to render
     */
    private FastList<RenderComponent> componentList = new FastList<RenderComponent>();
    private FastList<Spatial> opaqueList = new FastList<Spatial>();
    private FastList<Spatial> transparentList = new FastList<Spatial>();
    private FastList<Spatial> orthoList = new FastList<Spatial>();
    private FastList<Spatial> spatialList = new FastList<Spatial>();;

    /**
     * This is called when the technique is first loaded
     */
    public void initialize() {

    }

    /**
     * This gets called at the start of a rendered frame
     */
    public void startFrame(RenderBuffer rb) {

    }

    private void cullAndSort(Camera camera, Spatial s, ListType type, FastList<Spatial> tmpList) {
        Camera.FrustumIntersect intersect = camera.contains(s.getWorldBound());

        if (intersect == Camera.FrustumIntersect.Outside) {
            return;
        }

        if (s instanceof Node) {
            Node node = (Node)s;
            for (int i=0; i<node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                cullAndSort(camera, child, type, tmpList);
            }
        } else if (s instanceof Geometry) {
            insertSpatial(camera, s, type, tmpList);
        }
    }

    private void insertSpatial(Camera camera, Spatial s1, ListType type, FastList<Spatial> tmpList) {
        int i=0;

        if (tmpList.size() == 0) {
            tmpList.add(s1);
            return;
        }

        if (type == ListType.Ortho) {
            for (i=0; i<tmpList.size(); i++) {
                Spatial s2 = tmpList.get(i);
                if (s1.getZOrder() > s2.getZOrder()) {
                    continue;
                }
            }
            tmpList.add(i-1, s1);
            return;
        }

        s1.queueDistance = distanceToCamera(camera, s1);

        for (i=0; i<tmpList.size(); i++) {
            Spatial s2 = tmpList.get(i);
            if (type == ListType.Opaque) {
                if (s1.queueDistance < s2.queueDistance) {
                    continue;
                }
            } else if (type == ListType.Transparent) {
                if (s1.queueDistance > s2.queueDistance) {
                    continue;
                }
            }
        }
        tmpList.add(i-1, s1);

    }

    private float distanceToCamera(Camera camera, Spatial s) {
        Vector3f p1 = camera.getLocation();
        Vector3f p2 = null;

        if (s.getWorldBound() != null) {
            p2 = s.getWorldBound().getCenter();
        } else if (s.getWorldTranslation() != null) {
            p2 = s.getWorldTranslation();
        } else {
            System.out.println("MODEL IS NOWHERE!!!!!!!!!!!!!!");
        }

        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float dz = p2.z - p1.z;
        return (dx*dx + dy*dy + dz*dz);
    }

    /**
     * This gets called to get all of the opaque spatial objects
     * They are asumed to be sorted front to back
     */
    public FastList<Spatial> getSpatials(RenderBuffer rb) {
        return (spatialList);
    }

    /**
     * This gets called at the end of a rendered frame
     */
    public void endFrame(RenderBuffer rb) {

    }

    /**
     * This method is called when a RenderComponent is added
     */
    public void addRenderComponent(RenderComponent rc) {
        rc.setRenderTechnique(this);
        //BlendState blendState = (BlendState) rc.getSceneRoot().getRenderState(RenderState.StateType.Blend);
        //traverseGraph(rc.getSceneRoot(), rc.getOrtho(), blendState, true);
        componentList.add(rc);
        spatialList.add(rc.getSceneRoot());

    }

    /**
     * This method is called when RenderComponent is removed
     */
    public void removeRenderComponent(RenderComponent rc) {
        //BlendState blendState = (BlendState) rc.getSceneRoot().getRenderState(RenderState.StateType.Blend);
        rc.setRenderTechnique(null);
        //traverseGraph(rc.getSceneRoot(), rc.getOrtho(), blendState, false);
        spatialList.remove(rc.getSceneRoot());
        componentList.remove(rc);
    }

    /**
     * This returns the
     */
    void traverseGraph(Spatial sg, boolean ortho, BlendState bs, boolean add) {

        if (sg instanceof Node) {
            Node node = (Node)sg;
            for (int i=0; i<node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                BlendState blendState = (BlendState) child.getRenderState(RenderState.StateType.Blend);
                if (blendState == null) {
                    blendState = bs;
                }
                traverseGraph(child, ortho, blendState, add);
            }
        } else if (sg instanceof Geometry) {
            updateLists(sg, ortho, bs, add);
        }
    }

    /**
     * This mehod checks for transpaency attributes
     */
    void updateLists(Spatial s, boolean ortho, BlendState bs, boolean add) {
        FastList<Spatial> list = null;

        if (ortho) {
            list = orthoList;
        } else {
            if (bs != null) {
                if (bs.isBlendEnabled()) {
                    list = transparentList;
                } else {
                    list = opaqueList;
                }
            } else {
                list = opaqueList;
            }
        }

        if (add) {
            list.add(s);
        } else {
            list.remove(s);
        }
    }


    /**
     * The name of the technique
     */
    public String getName() {
        return ("org.jdesktop.mtgame.DefaultRenderTechnique");
    }

}
