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

package org.jdesktop.mtgame.processor;


import org.jdesktop.mtgame.*;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.math.Ray;
import com.jme.scene.Line;
import com.jme.intersection.PickResults;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.TrianglePickResults;
import com.jme.scene.Node;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Box;
import com.jme.scene.state.*;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.util.geom.BufferUtils;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;

/**
 *
 * @author runner
 */
public class JBSelectionProcessor extends AWTEventProcessorComponent {    
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
    private JBulletDynamicCollisionSystem collisionSystem = null;
    
    /**
     * The pysics system for this processor
     */
    private JBulletPhysicsSystem physicsSystem = null;
    
    /**
     * A boolean for toggling physics
     */
    private boolean physicsProcessing = false;
    
    /**
     * The default constructor
     */
    public JBSelectionProcessor(AWTInputComponent listener, WorldManager wm, 
            Entity myEntity, Entity cameraEntity, int windowWidth, int windowHeight,
            OrbitCameraProcessor ocp) {
        super(listener);
        worldManager = wm;
        collisionSystem = (JBulletDynamicCollisionSystem) 
                worldManager.getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);
        physicsSystem = (JBulletPhysicsSystem) 
                worldManager.getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, collisionSystem);
        
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
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
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
                    
                    ray.setOrigin(origin);
                    ray.setDirection(direction);
                    pr.clear();
                    clearVisibleBounds();
                    
                    javax.vecmath.Vector3f to = new javax.vecmath.Vector3f(thru.x, thru.y, thru.z);
                    javax.vecmath.Vector3f from = new javax.vecmath.Vector3f(origin.x, origin.y, origin.z);
                    
                    to.x = from.x + direction.x *1000.0f;
                    to.y = from.y + direction.y *1000.0f;
                    to.z = from.z + direction.z *1000.0f;
                    
                    //System.out.println("Ray starts at: " + from.x + ", " + from.y + ", " + from.z);
                    //System.out.println("And ends at: " + to.x + ", " + to.y + ", " + to.z);
                    ClosestRayResultCallback result = new ClosestRayResultCallback(from, to);
                    
                    collisionSystem.rayTest(from, to, result);
                    if (result.hasHit()) {
                        //System.out.println("Ray Test: " + result.collisionObject);
                        JBulletCollisionComponent jcc = (JBulletCollisionComponent)result.collisionObject.getUserPointer();
                        if (jcc.getNode() != null) {
                            addToVisibleBounds(jcc.getNode());
                        }
                    } else {
                        //System.out.println("NO HIT");
                    }
                }
            } else if (events[i] instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }

    }
        
    private void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_P) {
                physicsProcessing = !physicsProcessing;
                physicsSystem.setStarted(physicsProcessing);
            }
        }
    }
        
    private void addToVisibleBounds(Node g) {
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
            e.addComponent(RenderComponent.class, rc);
        }
        
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        node.setRenderState(buf);

        BlendState as = (BlendState) worldManager.getRenderManager().createRendererState(RenderState.RS_BLEND);
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        node.setRenderState(as);

        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(RenderState.RS_CULL);
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
