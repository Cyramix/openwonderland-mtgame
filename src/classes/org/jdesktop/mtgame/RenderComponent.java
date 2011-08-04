/*
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation. All rights reserved.
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
 *  . Neither the name of Open Wonderland Foundation, nor the names of its
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

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.light.LightNode;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import com.jme.util.export.Savable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * This is an entity component that implements the visual representation of
 * an entity.
 *
 * @author Doug Twilleager
 */
public class RenderComponent extends EntityComponent {
    /**
     * The key in UserData for the Entity associated with a spatial
     */
    static final String ENTITY_KEY = "org.jdesktop.mtgame.RenderComponent.Entity";

    /**
     * The base node for the JME Scene Graph
     */
    private Node sceneRoot = null;

    /**
     * A sometimes non-null place for this RenderComponent to attach to.
     */
    private Node attachPoint = null;

    /**
     * A non-collidable dummy node to insert between this node and its
     * parent attach point.
     */
    private Node attachRoot = null;

    /**
     * A flag to indicate whether or not this render component should
     * use an orthographic projection
     */
    private boolean ortho = false;

    /**
     * This flag controls whether or not lighting is applied to
     * this RenderComponent
     */
    private boolean lightingEnabled = true;

    /**
     * The lights that only apply to this RenderCompoenent
     */
    private ArrayList lights = new ArrayList();

    /**
     * The LightState that is used to apply lighting to just this
     * RenderComponent.
     */
    private LightState lightState = null;

    /**
     * An object used for render techniques
     */
    private Object renderTechniqueObject = null;

    /**
     * The render technique for this render component
     */
    private RenderTechnique renderTechnique = null;

    /**
     * An object used for render techniques
     */
    private String renderTechniqueName = "org.jdesktop.mtgame.DefaultRenderTechnique";

    /**
     * The current level of detail level
     */
    private int currentLODLevel = 0;

    /**
     * This boolean indicates that we are waiting for an update
     */
    boolean pendingUpdate = false;

    /**
     * The default constructor
     */
    RenderComponent(Node node) {
        sceneRoot = node;
    }

    /**
     * The constructor with attach point
     */
    RenderComponent(Node node, Node attachPoint) {
        sceneRoot = node;
        this.attachPoint = attachPoint;
    }

    /**
     * The constructor with RenderTechnique
     */
    RenderComponent(String rt, Node node, Object rtObject) {
        renderTechniqueName = rt;
        sceneRoot = node;
        renderTechniqueObject = rtObject;
    }

    /**
     * Set the render technique
     */
    void setRenderTechnique(RenderTechnique rt) {
        renderTechnique = rt;
    }

    /**
     * Get the render technique
     */
    RenderTechnique getRenderTechnique() {
        return (renderTechnique);
    }

    public void setEntity(Entity entity) {
        super.setEntity(entity);

        getSceneRoot().setUserData(ENTITY_KEY, new EntityRef(entity));
    }

    /**
     * Get the name of the render technique
     */
    public String getRenderTechniqueName() {
        return (renderTechniqueName);
    }

    /**
     * Get the scene root
     */
    public Node getSceneRoot() {
        return (sceneRoot);
    }

    /**
     * This waits for the update to finish
     */
    void waitForUpdate() {
        while (pendingUpdate) {
            try {
                Thread.currentThread().sleep(0, 10);
            } catch (java.lang.InterruptedException ie) {
                // Just wrap around
            }
        }
    }

    /**
     * Set the scene root
     */
    public void setSceneRoot(Node node) {
        pendingUpdate = true;
        WorldManager.getDefaultWorldManager().getRenderManager().updateSceneRoot(this, node);
        waitForUpdate();
    }

    /**
     * Update any scene graph state for this RenderComponent
     */
    void updateSceneRoot(WorldManager wm, Node newRoot) {
        // Start with attach points
        if (attachPoint != null) {
            Node oap = attachPoint;
            updateAttachPoint(wm, null, false);
            attachPoint = oap;
        }

        sceneRoot.setLive(false);
        sceneRoot = newRoot;
        newRoot.setLive(true);
        newRoot.setUserData(ENTITY_KEY, new EntityRef(getEntity()));
        updateAttachPoint(wm, attachPoint, false);
        updateOrtho(wm, ortho, false);
        updateLightState(wm, false);
        wm.addToUpdateList(sceneRoot);
        pendingUpdate = false;
    }

    /**
     * Set the attach point for this RenderComponent
     * This can only be called from a commit method if it is attaching/detaching
     * from a live entity.
     *
     * @parameter ap - the parent node to which the sceneRoot of this component will be attached
     */
    public void setAttachPoint(Node ap) {
        pendingUpdate = true;
        WorldManager.getDefaultWorldManager().getRenderManager().updateAttachPoint(this, ap);
        waitForUpdate();
    }

    /**
     * Do the processing of a changed attach point
     */
    void updateAttachPoint(WorldManager wm, Node newAttachPoint, boolean clearUpdate) {
        // first detach the current attach point
        detachAttachPoint(wm);

        // Now, see if we need to notify new attachment
        if (newAttachPoint != null) {
            // OWL issue #103: add in a dummy node here that will act as a
            // break for things like picking down the tree. If a child is
            // pickable, it will have a separate collision component, which
            // will be queried separately during collision and pick checks.
            attachRoot = new AttachPointNode("Attach point");
            attachRoot.attachChild(sceneRoot);

            newAttachPoint.attachChild(attachRoot);
            wm.addToUpdateList(newAttachPoint);
        }

        attachPoint = newAttachPoint;
        if (clearUpdate) {
            pendingUpdate = false;
        }
    }

    /**
     * Detach the current attach point without changing the value of
     * the attach Point variable or clearing the update flag.
     */
    void detachAttachPoint(WorldManager wm) {
        if (attachPoint != null) {
            // Detach and put the highest parent on the update list
            Node current = attachPoint;
            Node parent = current.getParent();
            while (parent != null) {
                current = parent;
                parent = parent.getParent();
            }

            if (attachRoot != null) {
                attachPoint.detachChild(attachRoot);
                attachRoot.detachChild(sceneRoot);
                attachRoot = null;
            }

            wm.addToUpdateList(current);
        }
    }

    /**
     * Set the attach point for this RenderComponent
     */
    public Node getAttachPoint() {
        return (attachPoint);
    }

    /**
     * Set the othographic projection flag
     */
    public void setOrtho(boolean flag) {
        pendingUpdate = true;
        WorldManager.getDefaultWorldManager().getRenderManager().updateOrtho(this, flag);
        waitForUpdate();
    }

    /**
     * Get the value of the orthographic projection flag
     */
    public boolean getOrtho() {
        return (ortho);
    }

    /**
     * Update ortho settings on the scene graph
     */
    void updateOrtho(WorldManager wm, boolean flag, boolean clearUpdate) {
        ortho = flag;
        BlendState bs = (BlendState) sceneRoot.getRenderState(RenderState.StateType.Blend);
        traverseGraph(sceneRoot, flag, bs);
        wm.addToUpdateList(sceneRoot);

        if (clearUpdate) {
            pendingUpdate = false;
        }
    }

    /**
     * Examine this node and travese it's children
     */
    void traverseGraph(Spatial sg, boolean ortho, BlendState bs) {

        examineSpatial(sg, ortho, bs);
        if (sg instanceof Node) {
            Node node = (Node) sg;
            for (int i = 0; i < node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                BlendState cbs = (BlendState) child.getRenderState(RenderState.StateType.Blend);
                if (cbs == null) {
                    traverseGraph(child, ortho, bs);
                } else {
                    traverseGraph(child, ortho, cbs);
                }
            }
        }
    }

    /**
     * Examine the given node
     */
    void examineSpatial(Spatial s, boolean ortho, BlendState bs) {
        setRenderQueue(s, ortho, bs);
    }

    void clearUpdateFlag() {
        pendingUpdate = false;
    }

    /**
     * This mehod checks for transpaency attributes
     */
    void setRenderQueue(Spatial s, boolean ortho, BlendState bs) {
        if (ortho) {
            s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_ORTHO);
        } else {
            if (bs != null) {
                if (bs.isBlendEnabled()) {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_TRANSPARENT);
                } else {
                    s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
                }
            } else {
                s.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
            }
        }
    }

    /**
     * Set the current LOD level
     */
    void setCurrentLOD(int level) {
        currentLODLevel = level;
    }

    /**
     * Get the current LOD level
     */
    public int getCurrentLOD() {
        return (currentLODLevel);
    }

    /**
     * Set the lighting enable flag
     */
    public void setLightingEnabled(boolean flag) {
        if (lightingEnabled != flag) {
            lightingEnabled = flag;
            pendingUpdate = true;
            WorldManager.getDefaultWorldManager().getRenderManager().updateLighting(this);
            waitForUpdate();
        }
    }

    /**
     * Get the lighting enabled flag
     */
    public boolean getLightingEnabled() {
        return (lightingEnabled);
    }

    /**
     * Add a global light to the scene
     */
    public void addLight(LightNode light) {
        synchronized (lights) {
            lights.add(light);
            pendingUpdate = true;
            WorldManager.getDefaultWorldManager().getRenderManager().updateLighting(this);
            waitForUpdate();
        }
    }

    /**
     * Remove a global light from the scene
     */
    public void removeLight(LightNode light) {
        synchronized (lights) {
            lights.remove(light);
            pendingUpdate = true;
            WorldManager.getDefaultWorldManager().getRenderManager().updateLighting(this);
            waitForUpdate();
        }
    }

    /**
     * Create a LightState with the current set of lights
     */
    void updateLightState(WorldManager wm, boolean clearUpdate) {
        ArrayList globalLights = wm.getRenderManager().getGlobalLights();
        lightState = (LightState) wm.getRenderManager().createRendererState(RenderState.StateType.Light);
        for (int i = 0; i < globalLights.size(); i++) {
            LightNode ln = (LightNode) globalLights.get(i);
            wm.addToUpdateList(ln);
            lightState.attach(ln.getLight());
        }

        for (int i = 0; i < lights.size(); i++) {
            LightNode ln = (LightNode) lights.get(i);
            wm.addToUpdateList(ln);
            lightState.attach(ln.getLight());
        }
        lightState.setEnabled(lightingEnabled);
        sceneRoot.setRenderState(lightState);
        sceneRoot.setLightCombineMode(Spatial.LightCombineMode.Replace);

        if (clearUpdate) {
            pendingUpdate = false;
        }
    }

    /**
     * Get the LightState for this RenderComponent
     */
    LightState getLightState() {
        return (lightState);
    }

    /**
     * Return the number of global Lights
     */
    public int numLights() {
        int num = 0;
        synchronized (lights) {
            num = lights.size();
        }
        return (num);
    }

    /**
     * Get a light at the index specified
     */
    public LightNode getLight(int i) {
        LightNode light = null;

        synchronized (lights) {
            light = (LightNode) lights.get(i);
        }
        return (light);
    }

    /**
     * A piece of user data in a spatial that associates that spatial with
     * its owning entity. This reference is added to the root spatial of
     * a render component, so that statistics about that entity can be tracked
     * by the renderer.
     * 
     * This class holds a weak reference to the entity, so it does not
     * prevent garbage collection.
     */
    static class EntityRef implements Savable {
        private final Reference<Entity> entityRef;

        EntityRef(Entity entity) {
            this.entityRef = new WeakReference<Entity>(entity);
        }

        Entity getEntity() {
            return entityRef.get();
        }

        public Class getClassTag() {
            return EntityRef.class;
        }

        public void write(JMEExporter jmee) throws IOException {
            // do nothing - this data is transient
        }

        public void read(JMEImporter jmei) throws IOException {
            // do nothing - this data is transient
        }
    }

    /**
     * A class that represents an attachment point. This can be used to
     * determine when a node is part of a different entity when walking the
     * tree.
     */
    public static class AttachPointNode extends Node {
        public AttachPointNode(String name) {
            super (name);

            setIsCollidable(false);
            setLightCombineMode(Spatial.LightCombineMode.Off);
        }
    }
}
