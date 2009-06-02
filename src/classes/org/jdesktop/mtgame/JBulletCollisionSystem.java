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

import java.util.ArrayList;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionWorld;

import javax.vecmath.Vector3f;
import com.jme.math.Ray;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.GeometricUpdateListener;

/**
 * This collision system uses jme methods to implement collision queries
 * @author Doug Twilleager
 */
public class JBulletCollisionSystem extends CollisionSystem implements GeometricUpdateListener {
    /**
     * Some JBullet Collision Objects
     */
    private BroadphaseInterface overlappingPairCache = null;
    private CollisionDispatcher dispatcher = null;
    private CollisionWorld collisionWorld = null;
    private DefaultCollisionConfiguration collisionConfiguration = null;
    
    /**
     * The list of collision components to be used for collision queries
     */
    private ArrayList collisionComponents = new ArrayList();
    
    /**
     * The default constructor
     */
    public JBulletCollisionSystem() {
        collisionConfiguration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(collisionConfiguration);
        Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
        Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
        overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, 1000);
        collisionWorld = new CollisionWorld(dispatcher, overlappingPairCache, collisionConfiguration);
    }
    
    /**
     * This creates a default collision component object.  The default collision
     * component object exposes jME picking/collision API's
     */
    public JBulletCollisionComponent createCollisionComponent(Node node) {
        JBulletCollisionComponent cc = new JBulletCollisionComponent(this, node);
        // Need to wait for node to initialize, so just return.
        return (cc);
    }
    
    /**
     * Remove the JME collision component from collision consideration
     * @param cc
     */
    public void removeCollisionComponent(CollisionComponent cc) {
        JBulletCollisionComponent jcc = (JBulletCollisionComponent) cc;
        synchronized (collisionComponents) {
            collisionComponents.remove(jcc);
            jcc.getNode().removeGeometricUpdateListener(this);
            collisionWorld.removeCollisionObject(jcc.getCollisionObject());
        }
    }
    
    public void addCollisionComponent(CollisionComponent cc) {
        JBulletCollisionComponent jcc = (JBulletCollisionComponent) cc;
        jcc.initialize();
        synchronized (collisionComponents) {
            jcc.getNode().addGeometricUpdateListener(this);
            collisionWorld.addCollisionObject(jcc.getCollisionObject());
            collisionComponents.add(cc);
        }
    }
    /**
     * A pick routine, which will pick against every scene rendered
     */
    public void rayTest(Vector3f from, Vector3f to, CollisionWorld.RayResultCallback result) {
        synchronized (collisionComponents) {
            collisionWorld.rayTest(from, to, result);
        }
    }
    
    public void geometricDataChanged(Spatial s) {
        synchronized (collisionComponents) {
            for (int i=0; i<collisionComponents.size(); i++) {
                JBulletCollisionComponent jcc = (JBulletCollisionComponent) collisionComponents.get(i);
                if (jcc.getNode() == s) {
                    jcc.nodeChanged();
                    break;
                }
            }
        }
    }
    
    /**
     * TODO: Need to implement this
     */
    public PickInfo pickAllEyeRay(Ray eyeRay, CameraComponent cc, boolean geometryPick, boolean interpolataData) {
        return (null);
    }

    /**
     * TODO: Need to implement this
     */
    public PickInfo pickAllWorldRay(Ray worldRay, boolean geometryPick, boolean interpolataData) {
        return (null);    
    }


    /**
     * TODO: Need to implement this
     */
    public PickInfo pickAllEyeRay(Ray eyeRay, CameraComponent cc, boolean geometryPick, boolean interpolataData, boolean includeOrtho) {
        return (null);
    }

    /**
     * TODO: Need to implement this
     */
    public PickInfo pickAllWorldRay(Ray worldRay, boolean geometryPick, boolean interpolataData, boolean includeOrtho, CameraComponent cc) {
        return (null);
    }

}
