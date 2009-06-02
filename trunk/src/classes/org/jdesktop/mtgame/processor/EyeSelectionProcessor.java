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

package org.jdesktop.mtgame.processor;


import org.jdesktop.mtgame.*;
import com.jme.math.Vector3f;
import com.jme.math.Ray;
import com.jme.scene.Node;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.scene.state.*;
import com.jme.scene.Geometry;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;


import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 *
 * @author runner
 */
public class EyeSelectionProcessor extends AWTEventProcessorComponent {    
    /**
     * Some objects used for selection
     */
    private Ray eyeRay = new Ray();
    private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f direction = new Vector3f(0.0f, 0.0f, -1.0f);

    
    /**
     * An array list of entities for the currently visible bounds
     */
    private ArrayList visibleBounds = new ArrayList();
       
    /**
     * The current camera entity
     */
    private Entity cameraEntity = null;
    private CameraComponent cc = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The collision component for this processor
     */
    private JMECollisionSystem collisionSystem = null;
    
    /**
     * The default constructor
     */
    public EyeSelectionProcessor(AWTInputComponent listener, WorldManager wm, 
            Entity myEntity, Entity cameraEntity, int windowWidth, int windowHeight,
            OrbitCameraProcessor ocp) {
        super(listener);
        worldManager = wm;
        collisionSystem = (JMECollisionSystem) 
                worldManager.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        
        setEntity(myEntity);
        this.cameraEntity = cameraEntity;     
        cc = (CameraComponent) cameraEntity.getComponent(CameraComponent.class);
        eyeRay.origin.set(origin);
        eyeRay.direction.set(direction);
    }
    
    public void initialize() {
        setArmingCondition(new AwtEventCondition(this));
    }
    
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = getEvents();
        
        for (int i=0; i<events.length; i++) {
            if (events[i] instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_RELEASED &&
                    me.getButton() == MouseEvent.BUTTON1) {
                    
                    //System.out.println("Ray starts at: " + origin.x + ", " + origin.y + ", " + origin.z);
                    //System.out.println("And has direction: " + direction.x + ", " + direction.y + ", " + direction.z);
                    clearVisibleBounds();
                    JMEPickInfo pickInfo = (JMEPickInfo) collisionSystem.pickAllEyeRay(eyeRay, cc, true, false);
                    //System.out.println(pickInfo.size() + " Geometries were picked");
                    for (int j = 0; j < pickInfo.size(); j++) {
                        JMEPickDetails pd = (JMEPickDetails)pickInfo.get(j);
                        //System.out.println("================ Geometry " + j + " ================");
                        //System.out.println("\tDistance: " + pd.getDistance());
                        //System.out.println("\tEntity: " + pd.getEntity());
                        //System.out.println("\tCollision Component: " + pd.getCollisionComponent());
                        //System.out.println("\tGeometry: " + pd.getPickData().getTargetMesh());
                        addToVisibleBounds(pd.getPickData().getTargetMesh());
                    }
                }
            }
        }

    }
    
    private void addToVisibleBounds(Geometry g) {
        BoundingVolume bv = g.getWorldBound();
        Entity e = null;
        Node node = null;
        ColorRGBA color = new ColorRGBA(1.0f, 0.0f, 0.0f, 0.4f);
        Box box = null;
        
        if (bv instanceof BoundingBox) {
            BoundingBox bbox = (BoundingBox) bv;
            Vector3f center = bbox.getCenter();
            
            Vector3f extent = bbox.getExtent(null);
            box = new Box("Bounds", center, extent.x, extent.y, extent.z);
            box.setDefaultColor(color);
           
            e = new Entity("Bounds");
            node = new Node();
            node.attachChild(box);
            RenderComponent rc = worldManager.getRenderManager().createRenderComponent(node);
            rc.setLightingEnabled(false);
            e.addComponent(RenderComponent.class, rc);
        }
        
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        node.setRenderState(buf);

        BlendState as = (BlendState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Blend);
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        node.setRenderState(as);

        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setEnabled(true);
        cs.setCullFace(CullState.Face.Back);
        node.setRenderState(cs);

        worldManager.addEntity(e);
        visibleBounds.add(e);
    }
    
    private void clearVisibleBounds() {
        for (int i=0; i<visibleBounds.size(); i++) {
            Entity e = (Entity) visibleBounds.get(i);
            worldManager.removeEntity(e);
        }
        visibleBounds.clear();
    }
    
    /**
     * The commit methods
     */
    public void commit(ProcessorArmingCollection collection) {
        
    }
}
