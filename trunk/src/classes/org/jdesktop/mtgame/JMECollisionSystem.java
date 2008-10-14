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

import java.util.ArrayList;

import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.renderer.AbstractCamera;

import com.jme.intersection.CollisionResults;
import com.jme.math.Ray;
import com.jme.intersection.PickResults;
import com.jme.intersection.PickData;
import com.jme.intersection.TrianglePickData;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.TrianglePickResults;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;

/**
 * This collision system uses jme methods to implement collision queries
 * @author Doug Twilleager
 */
public class JMECollisionSystem extends CollisionSystem {
    /**
     * The list of collision components to be used for collision queries
     */
    private ArrayList collisionComponents = new ArrayList();
    
    /**
     * Cached camera information
     */
    Matrix4f camInverse = null;
    Matrix4f camMatrix = new Matrix4f();
                
    /**
     * This creates a default collision component object.  The default collision
     * component object exposes jME picking/collision API's
     */
    public JMECollisionComponent createCollisionComponent(Node node) {
        JMECollisionComponent cc = new JMECollisionComponent(this, node);
        return (cc);
    }
    
    public void addCollisionComponent(CollisionComponent cc) {
        synchronized (collisionComponents) {
            collisionComponents.add(cc);
        }
    }
        
    /**
     * Remove the JME collision component from collision consideration
     * @param cc
     */
    public void removeCollisionComponent(JMECollisionComponent cc) {
        synchronized (collisionComponents) {
            collisionComponents.remove(cc);
        }
    }
    
    
    /**
     * A pick routine, which will pick against every scene rendered
     */
    public void pickAll(Ray ray, PickResults result) {
        synchronized (worldManager.getRenderManager().getCollisionLock()) {
            synchronized (collisionComponents) {
                for (int i = 0; i < collisionComponents.size(); i++) {
                    JMECollisionComponent cc = (JMECollisionComponent) collisionComponents.get(i);
                    if (cc.isPickable()) {
                        Node node = cc.getNode();
                        node.findPick(ray, result);
                    }
                }
            }
        }
    }
    
    /**
     * A pick routine that picks against all collision components - returning them
     * all in the PickInfo.  The boolean geometryPick signals whether to use geometry 
     * data or just bounds.  interpolateData signals whether or not interpolated data
     * is calculated.
     */
    public PickInfo pickAllEyeRay(Ray eyeRay, CameraComponent cc, boolean geometryPick, boolean interpolataData) {
        Ray eRay = new Ray();
        Ray wRay = new Ray();
        
        eRay.direction.set(eyeRay.direction);
        eRay.origin.set(eyeRay.origin);

        AbstractCamera ac = (AbstractCamera)cc.getCamera();
        Matrix4f mvMatrix = ac.getModelViewMatrix();
        
        if (!mvMatrix.equals(camMatrix)) {
            camMatrix.set(mvMatrix);
            camInverse = camMatrix.invert();
        }

        //System.out.println("Matrix: " + camMatrix);
        camInverse.multAcross(eRay.origin, wRay.origin);
        camMatrix.mult(eRay.direction, wRay.direction);
        wRay.direction.normalizeLocal();
               
        //System.out.println("Eye Ray: " + eRay.origin + ", " + eRay.direction);
        //System.out.println("World Ray: " + wRay.origin + ", " + wRay.direction);
        // convert the eye ray to world ray, then call the pick routine
        JMEPickInfo pickInfo = (JMEPickInfo)pickAllWorldRay(wRay, geometryPick, interpolataData);
        pickInfo.setEyeRay(eRay);
        
        return (pickInfo);
    }
      
    /**
     * A pick routine that picks against all collision components - returning them
     * all in the PickInfo.  The boolean geometryPick signals whether to use geometry 
     * data or just bounds.  interpolateData signals whether or not interpolated data
     * is calculated.
     */
    public PickInfo pickAllWorldRay(Ray worldRay, boolean geometryPick, boolean interpolataData) {
        PickInfo pickInfo = null;
        PickResults pickResults = null;
        
        // create the correct pick results
        if (geometryPick) {
            pickResults = new TrianglePickResults();
        } else {
            pickResults = new BoundingPickResults();
        }
        pickResults.setCheckDistance(true);
        
        // Do the actual query
        pickAll(worldRay, pickResults);
        
        //System.out.println("PickResults: " + pickResults.getNumber());
        
        // Create out pick info
        pickInfo = new JMEPickInfo(geometryPick, interpolataData, worldRay);
        
        // Now add data from the pickResults
        for (int i=0; i<pickResults.getNumber(); i++) {
            PickData pickData = pickResults.getPickData(i); 
            // Prune out non-hits in geometry case
            if (geometryPick && pickData.getDistance() == Float.POSITIVE_INFINITY) {
                continue;
            }
            JMEPickDetails pickDetails = getPickDetails(pickData.getTargetMesh(), pickInfo, pickData);
            pickInfo.addPickDetail(pickDetails);
            
            // Add more details in geometry pick case
            if (geometryPick) {
                TrianglePickData tpd = (TrianglePickData)pickData;
                Vector3f intersectionPoint = new Vector3f();
                tpd.getIntersectionPoint(intersectionPoint);         
                pickDetails.setPosition(intersectionPoint);
                
                if (interpolataData) {
                    // TODO calculate this
                }
            }
        }
        
        return (pickInfo);
    }

    /**
     * Find the entity which contains this geometry
     */
    private JMEPickDetails getPickDetails(Geometry g, PickInfo pickInfo, PickData pickData) {
        CollisionComponent cc = null;
        Entity entity = null;
        JMEPickDetails pickDetails = null;
        
        // First, find the topmost parent
        Node parent = g.getParent();
        Node node = parent;
        while (parent != null) {
            node = parent;
            parent = parent.getParent();
        }
        
        // Now look for node through our collision components
        // We should probably turn this into a hastable
        synchronized (collisionComponents) {
            for (int i=0; i<collisionComponents.size(); i++) {
                cc = (CollisionComponent)collisionComponents.get(i);
                if (cc.getNode() == node) {
                    // This is the one we want
                    entity = cc.getEntity();
                    break;
                }
            } 
        }
        pickDetails = new JMEPickDetails(this, entity, cc, pickInfo, 
                pickData, pickData.getDistance());
        return (pickDetails);
    }
        
    /**
     * Find the collisions for the scene
     * @param sp
     * @param cr
     */
    public void findCollisions(Spatial sp, CollisionResults cr) {
        synchronized (collisionComponents) {
            synchronized (worldManager.getRenderManager().getCollisionLock()) {
                for (int i = 0; i < collisionComponents.size(); i++) {
                    JMECollisionComponent cc = (JMECollisionComponent) collisionComponents.get(i);
                    if (cc.isPickable()) {
                        Node node = cc.getNode();
                        node.findCollisions(sp, cr);
                    }
                }
            }
        }
    }
}
