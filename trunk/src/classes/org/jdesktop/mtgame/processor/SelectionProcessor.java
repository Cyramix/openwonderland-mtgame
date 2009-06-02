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
import com.jme.math.Vector2f;
import com.jme.math.Ray;
import com.jme.scene.Line;
import com.jme.intersection.PickResults;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.TrianglePickResults;
import com.jme.intersection.PickData;
import com.jme.scene.Node;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.scene.state.*;
import com.jme.scene.Geometry;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.util.geom.BufferUtils;

import java.awt.event.MouseEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 *
 * @author runner
 */
public class SelectionProcessor extends AWTEventProcessorComponent {    
    /**
     * Some objects used for selection
     */
    private Ray ray = new Ray();
    private Vector3f origin = new Vector3f();
    private Vector3f thru = new Vector3f();
    private Vector3f start = new Vector3f();
    private Vector3f end = new Vector3f();
    private Vector3f direction = new Vector3f();
    private BoundingPickResults boundingPickResults = new BoundingPickResults();
    private TrianglePickResults trianglePickResults = new TrianglePickResults();
    private Vector3f cameraPosition = new Vector3f();
    private Vector3f upDirection = new Vector3f();
    private Vector3f sideDirection = new Vector3f();
    private Vector3f lookDirection = new Vector3f();
    private float deltaX = 0.0f;
    private float deltaY = 0.0f;
    private float dist = 0.0f;
    private int halfWidth = 0;
    private int halfHeight = 0;
    private Camera jmeCamera = null;
    
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
     * The current camera processor - this should be upleved into the CameraProcessor
     */
    private OrbitCameraProcessor camera = null;
    
    /**
     * The Node to modify
     */
    private Node selection = null;
    
    /**
     * The Geometry to update
     */
    private FloatBuffer lineBuffer = null;
    private Node lineSG = null;
    
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
    public SelectionProcessor(AWTInputComponent listener, WorldManager wm, 
            Entity myEntity, Entity cameraEntity, int windowWidth, int windowHeight,
            OrbitCameraProcessor ocp) {
        super(listener);
        worldManager = wm;
        collisionSystem = (JMECollisionSystem) 
                worldManager.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        
        setEntity(myEntity);
        this.cameraEntity = cameraEntity;
        boundingPickResults.setCheckDistance(true);
        trianglePickResults.setCheckDistance(true);
        
        cc = (CameraComponent) cameraEntity.getComponent(CameraComponent.class);
        float fov = cc.getFieldOfView();
        float aspect = cc.getAspectRatio();
        dist = cc.getNearClipDistance();
        
        
        // Precalculate some deltas for when we create the picking ray
        float lenX = (float)Math.tan(Math.toRadians(fov/2.0f))*dist*2.0f;
        float lenY = (float)Math.tan(Math.toRadians((1.0f/aspect)*(fov/2.0f)))*dist*2.0f;
        
        // Predivide the window dimentions
        deltaX = lenX/windowWidth;
        deltaY = lenY/windowHeight;
        halfWidth = windowWidth/2;
        halfHeight = windowHeight/2;
        
        camera = ocp;
        
        lineSG = new Node("Selection Line");
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        //ByteBuffer bbuf = ByteBuffer.allocateDirect(6*4);
        //lineBuffer = bbuf.asFloatBuffer();
        
        Vector3f[] points = new Vector3f[2];
        points[0] = new Vector3f();
        points[1] = new Vector3f();
        lineBuffer = BufferUtils.createFloatBuffer(points);
        
        Line line = new Line("Selection Line", lineBuffer, null, null, null);
        //Line line = new Line("Selection Line", points, null, null, null);
        line.setVertexCount(2);
        line.generateIndices();
        //IntBuffer indexBuffer = line.getIndexBuffer();
        
        //indexBuffer.rewind();
        //System.out.println("INDEX BUFFER SIZE: " + indexBuffer);
        //System.out.println(indexBuffer.get());
        //System.out.println(indexBuffer.get());
        
        lineSG.attachChild(line);
        lineSG.setRenderState(buf);
        
        Entity entity = new Entity("Selection Line");
        RenderComponent rc = wm.getRenderManager().createRenderComponent(lineSG);
        rc.setLightingEnabled(false);
        entity.addComponent(RenderComponent.class, rc);
        
        //wm.addEntity(entity);
    }
    
    public void initialize() {
        setArmingCondition(new AwtEventCondition(this));
    }
    
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = getEvents();
        int dx = 0;
        int dy = 0;
        //PickResults pr = boundingPickResults;
        PickResults pr = trianglePickResults;
        
        for (int i=0; i<events.length; i++) {
            if (events[i] instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_RELEASED &&
                    me.getButton() == MouseEvent.BUTTON1) {
                    int x = me.getX();
                    int y = me.getY();
                    
                    camera.getPosition(origin);
                    jmeCamera = cc.getCamera();
                    Vector2f sc = new Vector2f(x, (halfHeight*2) - y);
                    jmeCamera.getWorldCoordinates(sc, 0.0f, thru);
                    
                    direction.x = thru.x - origin.x;
                    direction.y = thru.y - origin.y;
                    direction.z = thru.z - origin.z;
                    direction.normalize();
                    
                    //System.out.println("Ray starts at: " + origin.x + ", " + origin.y + ", " + origin.z);
                    //System.out.println("And has direction: " + direction.x + ", " + direction.y + ", " + direction.z);
                    ray.setOrigin(origin);
                    ray.setDirection(direction);
                    pr.clear();
                    clearVisibleBounds();
                    collisionSystem.pickAll(ray, pr, false, cc);
                    //System.out.println(pr.getNumber() + " Geometries were picked");
                    for (int j = 0; j < pr.getNumber(); j++) {
                        PickData pd = pr.getPickData(j);
                        Geometry geo = pd.getTargetMesh();
                        addToVisibleBounds(geo);
                        //System.out.println("\tGeometry " + j + ": " + pd.getDistance());
                    }
                    worldManager.postEvent(100);
                    worldManager.postEvent(1955);
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
