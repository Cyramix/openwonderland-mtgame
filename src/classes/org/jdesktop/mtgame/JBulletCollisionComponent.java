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

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.SphereShape;

import com.jme.scene.Node;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.scene.TriMesh;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.vecmath.Vector3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Matrix4f;
import org.jdesktop.mtgame.JBulletPhysicsSystem.TimeStepEvent;
import org.jdesktop.mtgame.JBulletPhysicsSystem.TimeStepListener;

/**
* This is a collision component that implements the jme collision interface
* 
* @author Doug Twilleager
*/
public class JBulletCollisionComponent extends CollisionComponent implements MotionState, RenderUpdater {
    /**
     * The jme node for this collision component
     */
    private CollisionObject collisionObject = null;
    
    /**
     * This allows for an invisible collision shape
     */
    private CollisionShape collisionShape = null;
    
    /**
     * The physics component - for dynamic objects
     */
    private JBulletPhysicsComponent physicsComponent = null;
    
    /**
     * The world transform - as seen by the physics system
     */
    private Transform worldTransform = new Transform();

    /**
     * The TriMesh when we are doing mesh collision
     */
    private TriMesh triMesh = null;
    
    /**
     * Data for the jme transform
     */
    private com.jme.math.Matrix3f rotation = new com.jme.math.Matrix3f();
    private com.jme.math.Vector3f translation = new com.jme.math.Vector3f();

    /**
     * The set of listeners for initializ events
     */
    private Set<InitializedListener> listenerSet = new HashSet();

    /**
     * The default constructor
     */
    public JBulletCollisionComponent(CollisionSystem cs, Node node) {
        super(cs, node);      
    }
    
    /**
     * The default constructor
     */
    public JBulletCollisionComponent(CollisionSystem cs, CollisionShape colShape) {
        super(cs, null);  
        collisionShape = colShape;
    }     
    
    /**
     * The default constructor
     */
    public JBulletCollisionComponent(CollisionSystem cs, TriMesh tm) {
        super(cs, null);
        triMesh = tm;
        java.nio.IntBuffer indexBuffer = tm.getIndexBuffer();
        java.nio.ByteBuffer indexbbuf = java.nio.ByteBuffer.allocate(indexBuffer.capacity() * 4);
        indexBuffer.rewind();
        for (int k = 0; k < indexBuffer.capacity(); k++) {
            indexbbuf.putInt(indexBuffer.get());
        }
        
        java.nio.FloatBuffer vertBuffer = tm.getVertexBuffer();
        java.nio.ByteBuffer vertbbuf = java.nio.ByteBuffer.allocate(vertBuffer.capacity() * 4);
        vertBuffer.rewind();
        for (int k = 0; k < vertBuffer.capacity(); k++) {
            vertbbuf.putFloat(vertBuffer.get());
        }

        TriangleIndexVertexArray tva = new TriangleIndexVertexArray(tm.getTriangleCount(),
                indexbbuf, 12, tm.getVertexCount(), vertbbuf, 12);
        //TriangleIndexVertexArray tva = new TriangleIndexVertexArray(2,
        //        indexbbuf, 12, 4, vertbbuf, 12);
        collisionShape = new BvhTriangleMeshShape(tva, false);
        worldTransform.origin.set(0.0f, 0.0f, 0.0f);
        worldTransform.basis.setIdentity();
    }
    
    /**
     * Initialize the component - this happens after the node is updated
     */
    void initialize() {  
        Node node = getNode();
        Transform transform = new Transform();
        transform.setIdentity();

        if (node != null && collisionShape == null) {          
            BoundingVolume bv = node.getWorldBound();

            if (bv instanceof BoundingBox) {
                BoundingBox bbox = (BoundingBox) bv;
                Vector3f extent = new Vector3f(bbox.xExtent, bbox.yExtent, bbox.zExtent);

                BoxShape bs = new BoxShape(extent);
                com.jme.math.Vector3f center = bbox.getCenter();
                transform.origin.x = center.x;
                transform.origin.y = center.y;
                transform.origin.z = center.z;
                collisionShape = bs;
                worldTransform.set(transform);
                //System.out.println("Center: " + center);
                //System.out.println("Extent: " + bbox.xExtent + ", " +
                //        bbox.yExtent + ", " + bbox.zExtent);
            } else if (bv instanceof BoundingSphere) {
                BoundingSphere bsphere = (BoundingSphere)bv;
                SphereShape bs = new SphereShape(bsphere.getRadius());
                com.jme.math.Vector3f center = bsphere.getCenter();
                transform.origin.x = center.x;
                transform.origin.y = center.y;
                transform.origin.z = center.z;
                collisionShape = bs;
                worldTransform.set(transform);
            } else {
                Logger.getLogger(JBulletCollisionComponent.class.getName()).warning("JBullet CollisionComponent BOUNDS NOT SUPPORTED !" + bv +"  node "+node);
            }
        } else {
            com.jme.math.Vector3f trans = triMesh.getWorldTranslation();
            com.jme.math.Quaternion rot = triMesh.getWorldRotation();
            //com.jme.math.Vector3f scale = triMesh.getWorldScale();
            Vector3f t = new Vector3f(trans.x, trans.y, trans.z);
            Matrix4f m = new Matrix4f();
            m.set(new Quat4f(rot.x, rot.y, rot.z, rot.w), t, 1.0f);
            worldTransform.set(m);
        }

        //worldTransform.set(transform);
        transform.origin.set(worldTransform.origin.x, worldTransform.origin.y, worldTransform.origin.z);
        transform.basis.set(worldTransform.basis);
        
        if (physicsComponent == null) {
            collisionObject = new CollisionObject();
            collisionObject.setCollisionShape(collisionShape);
        } else {
            float mass = physicsComponent.getMass();
            Vector3f inertia = physicsComponent.getInertia();
            Vector3f linVel = physicsComponent.getLinearVelocity();
            if (mass != 0.0f) {
                collisionShape.calculateLocalInertia(mass, inertia);
            }
            //System.out.println("CS: " + collisionShape);
            RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, this, collisionShape, inertia);
            RigidBody rb = new RigidBody(rbInfo);
            //rb.setLinearVelocity(linVel);
            collisionObject = rb;
        }
        collisionObject.setWorldTransform(transform);   
        collisionObject.setUserPointer(this);

        // Inform all of the listeners that this component has been initialized
        fireInitializedEvent();
    }
    
    void nodeChanged() {
        JBulletDynamicCollisionSystem cs = (JBulletDynamicCollisionSystem)getCollisionSystem();
        collisionObject.setWorldTransform(computeTransform(getNode(), triMesh));
        cs.getDynamicsWorld().updateSingleAabb(collisionObject);
    }

    private Transform computeTransform(Node n, TriMesh tm) {
        Transform transform = new Transform();
        transform.setIdentity();

        if (n != null) {
            BoundingVolume bv = n.getWorldBound();
            if (bv instanceof BoundingBox) {
                BoundingBox bbox = (BoundingBox) bv;
                //Vector3f extent = new Vector3f(bbox.xExtent, bbox.yExtent, bbox.zExtent);

                //BoxShape bs = new BoxShape(extent);
                com.jme.math.Vector3f center = bbox.getCenter();
                transform.origin.x = center.x;
                transform.origin.y = center.y;
                transform.origin.z = center.z;
                //collisionShape = bs;
            } else if (bv instanceof BoundingSphere) {
                BoundingSphere bsphere = (BoundingSphere) bv;
                com.jme.math.Vector3f center = bsphere.getCenter();
                transform.origin.x = center.x;
                transform.origin.y = center.y;
                transform.origin.z = center.z;
            } else {
                System.out.println("BOUNDS NOT SUPPORTED!!!!!!!!!!!!!!!!!" + bv + "  node " + n);
            }
        }

        if (tm != null) {
            com.jme.math.Vector3f trans = tm.getWorldTranslation();
            com.jme.math.Quaternion rot = tm.getWorldRotation();
            Vector3f t = new Vector3f(trans.x, trans.y, trans.z);
            Matrix4f m = new Matrix4f();
            m.set(new Quat4f(rot.x, rot.y, rot.z, rot.w), t, 1.0f);
            transform.set(m);
        }
        return (transform);
    }

    /**
     * Get the TriMesh
     */
    public TriMesh getTriMesh() {
        return (triMesh);
    }

    /**
     * get the Collision shape
     */
    public CollisionShape getCollisionShape() {
        return (collisionShape);
    }
    
    /**
     * get the Collision object
     */
    public CollisionObject getCollisionObject() {
        return (collisionObject);
    }
    
    /**
     * Set the physics object
     */
    void setPhysicsComponent(JBulletPhysicsComponent pc) {
        physicsComponent = pc;
    }
    
    /**
     * Get the physics component
     */
    JBulletPhysicsComponent getPhysicsComponent() {
        return (physicsComponent);
    }
    
    /**
     * Catch the setTransform method
     */
    public void setWorldTransform(Transform t) {
        synchronized (rotation) {
            worldTransform.set(t);
            translation.x = worldTransform.origin.x;
            translation.y = worldTransform.origin.y;
            translation.z = worldTransform.origin.z;
            rotation.m00 = worldTransform.basis.m00;
            rotation.m01 = worldTransform.basis.m01;
            rotation.m02 = worldTransform.basis.m02;
            rotation.m10 = worldTransform.basis.m10;
            rotation.m11 = worldTransform.basis.m11;
            rotation.m12 = worldTransform.basis.m12;
            rotation.m20 = worldTransform.basis.m20;
            rotation.m21 = worldTransform.basis.m21;
            rotation.m22 = worldTransform.basis.m22;
        }
        collisionSystem.getWorldManager().getRenderManager().addRenderUpdater(this, this, false);
    }
    
    /**
     * Get the world transform
     */
    public Transform getWorldTransform(Transform t) {
        synchronized (rotation) {
            t.set(worldTransform);
        }
        return (t);
    }
    
    public void update(Object obj) {
        JBulletCollisionComponent jcc = (JBulletCollisionComponent)obj;
        synchronized (rotation) {
            jcc.getNode().setLocalRotation(rotation);
            jcc.getNode().setLocalTranslation(translation);
            collisionSystem.getWorldManager().addToUpdateList(jcc.getNode());
        }
    }

    /**
     * Adds an initialize listener. If the listener already exists, this method
     * does nothing.
     *
     * @param listener The listener to add
     */
    public void addInitializedListener(InitializedListener listener) {
        synchronized (listenerSet) {
            listenerSet.add(listener);
        }
    }

    /**
     * Removes an initialize listener. If this listener does not exist, this
     * method does nothing.
     * @param listener The listener to remove
     */
    public void removeInitializedListener(InitializedListener listener) {
        synchronized (listenerSet) {
            listenerSet.remove(listener);
        }
    }

    /**
     * Notifies all of the listeners of the initialized event
     */
    private void fireInitializedEvent() {
        synchronized (listenerSet) {
            for (InitializedListener l : listenerSet) {
                l.componentInitialized();
            }
        }
    }

    /**
     * Listener interface for callbacks for collision component initialization.
     */
    public interface InitializedListener {
        /**
         * Invoked after the component has been initialized.
         */
        public void componentInitialized();
    }
}
