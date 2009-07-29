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
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jmex.effects.water.ProjectedGrid;
import com.jmex.effects.water.WaterRenderPass;
import com.jmex.effects.water.WaterHeightGenerator;
import com.jme.scene.Skybox;
import com.jme.math.Plane;
import com.jme.scene.Spatial;
import com.jme.renderer.Camera;

/**
 * This is the Config Instance that implements the loading of a Collada file
 * 
 * @author Doug Twilleager
 */
public class JMEWaterInstance implements ConfigInstance, RenderUpdater {
    /**
     * The WorldManager
     */
    private WorldManager wm = null;

    /**
     * The Model for this collada file
     */
    private Node model = null;

    /**
     * The Entity for this collada file
     */
    private Entity entity = null;

    /**
     * The location and size for this water
     */
    private Vector3f location = new Vector3f();
    private int sizeX = 100;
    private int sizeY = 100;

    WaterRenderPass waterEffectRenderPass = null;
    WaterHeightGenerator waterHeightGenerator = null;
    ProjectedGrid projectedGrid = null;
    Camera jmeCam = null;
    Skybox skybox = null;
    Node reflectedModel = new Node();

    /**
     * This method is called when the instance is initialized
     */
    public void init(WorldManager wm, ConfigManager cm, String name, Vector3f location, Quaternion rotation, Vector3f scale, String[] args) {
        this.wm = wm;
        this.location.set(location);

        sizeX = Integer.parseInt(args[0]);
        sizeY = Integer.parseInt(args[1]);
        model = new Node("JME Water: " + name);
        model.setLocalTranslation(location);
        model.setLocalScale(scale);
        entity = new Entity("JME Water: " + name);
        jmeCam = wm.getRenderManager().getCurrentScreenCamera();
        skybox = wm.getRenderManager().getCurrentSkybox();

        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                ConfigInstance ci = wm.getConfigInstance(args[i]);
                addToReflected(ci.getSceneGraph());
            }
        } else {
            ConfigInstance ci[] = wm.getAllConfigInstances();
            for (int i = 0; i < ci.length; i++) {
                addToReflected(ci[i].getSceneGraph());
            }
        }
        addToReflected(skybox);

        wm.addRenderUpdater(this, null);
    }
    
    public void update(Object obj) {
        createWaterPass();
    }

    /**
     * Add a model to the reflected scene
     */
    public void addToReflected(Node n) {
        reflectedModel.attachChild(n);
    }

    private void createWaterPass() {
        JMECollisionSystem cs = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);

        waterEffectRenderPass = new WaterRenderPass(jmeCam, 4, true, true);
        waterEffectRenderPass.setSpeedReflection(0.00005f);
        waterEffectRenderPass.setSpeedRefraction(-0.0001f);

        waterEffectRenderPass.setWaterPlane(new Plane(new Vector3f(0.0f, 1.0f,
                0.0f), 0.0f));

        waterEffectRenderPass.setClipBias(0.0f);
        waterEffectRenderPass.setWaterMaxAmplitude(0.1f);

        waterHeightGenerator = new WaterHeightGenerator();
        waterHeightGenerator.setHeightsmall(0.0f);
        waterHeightGenerator.setHeightbig(0.01f);

        projectedGrid = new ProjectedGrid("ProjectedGrid", jmeCam, sizeX, sizeY, 0.01f,
                waterHeightGenerator);

        waterEffectRenderPass.setWaterEffectOnSpatial(projectedGrid);
        model.attachChild(projectedGrid);
        
        waterEffectRenderPass.setReflectedScene(reflectedModel);
        waterEffectRenderPass.setSkybox(skybox);
        model.setCullHint(Spatial.CullHint.Never);
        model.setRenderQueueMode(com.jme.renderer.Renderer.QUEUE_OPAQUE);
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        model.setRenderState(buf);

        PassComponent pass = wm.getRenderManager().createPassComponent(waterEffectRenderPass);
        RenderComponent rc = wm.getRenderManager().createRenderComponent(model);
        JMECollisionComponent cc = cs.createCollisionComponent(model);
        entity.addComponent(JMECollisionComponent.class, cc);
        entity.addComponent(PassComponent.class, pass);
        entity.addComponent(RenderComponent.class, rc);
    }

    /**
     * This method is called to get the scene graph for this instance
     */
    public Node getSceneGraph() {
        return (model);
    }

    /**
     * This method is called to get the scene graph for this instance
     */
    public Entity getEntity() {
        return (entity);
    }
}
