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
import java.util.HashMap;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import com.jme.scene.Node;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
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
import com.jme.math.Quaternion;

/**
 * This collision system uses jme methods to implement collision queries
 * @author Doug Twilleager
 */
public class JMECollisionSystem extends CollisionSystem {
    /**
     * The list of collision components to be used for collision queries
     */
    private ArrayList collisionComponents = new ArrayList();
    private HashMap spatialMap = new HashMap();
    
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
            spatialMap.put(cc.getNode(), cc);
        }
    }
        
    /**
     * Remove the JME collision component from collision consideration
     * @param cc
     */
    public void removeCollisionComponent(JMECollisionComponent cc) {
        synchronized (collisionComponents) {
            collisionComponents.remove(cc);
            spatialMap.remove(cc.getNode());
        }
    }
    
    
    /**
     * A pick routine, which will pick against every scene rendered
     */
    public void pickAll(Ray ray, PickResults result, CameraComponent camera) {
        //System.out.println("==================== pickAll =====================");
        synchronized (worldManager.getRenderManager().getCollisionLock()) {
            synchronized (collisionComponents) {
                for (int i = 0; i < collisionComponents.size(); i++) {
                    JMECollisionComponent cc = (JMECollisionComponent) collisionComponents.get(i);
                    if (cc.isPickable()) {
                        Node node = cc.getNode();
                        if (camera != null && node.getRenderQueueMode() == com.jme.renderer.Renderer.QUEUE_ORTHO) {
                            processOrthoPick(node, ray, result, camera);
                        }
                        node.findPick(ray, result);
                    }
                }
            }
        }
    }

    /**
     * Pick against a graph which is in ortho mode
     */
    private void processOrthoPick(Node node, Ray ray, PickResults result, CameraComponent cc) {
        Vector3f P = new Vector3f();
        Vector3f screenPt = new Vector3f();
        AbstractCamera ac = (AbstractCamera)cc.getCamera();
        ray.getOrigin().add(ray.getDirection(), P);
        ac.getScreenCoordinates(P, screenPt);

        checkForIntersection(node, result, cc, screenPt, ray);
    }

    /**
     * Check for a intersection with the given point
     */
    void checkForIntersection(Spatial s, PickResults result, CameraComponent cc, 
            Vector3f screenPt, Ray ray) {
        if (s instanceof TriMesh) {
            if (checkMeshForIntersection((TriMesh)s, result, cc, screenPt)) {
                result.addPickData(new PickData(ray, (TriMesh)s, false));
            }
        } else if (s instanceof Node) {
            Node n = (Node)s;

            for (int i=0; i<n.getQuantity(); i++) {
                checkForIntersection(n.getChild(i), result, cc, screenPt, ray);
            }
        }
    }

    /**
     * Check the given mesh for an intersection
     */
    boolean checkMeshForIntersection(TriMesh mesh, PickResults result, CameraComponent cc, Vector3f screenPt) {
        IntBuffer ibuf = mesh.getIndexBuffer();
        FloatBuffer vbuf = mesh.getVertexBuffer();
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();

        int numTris = ibuf.capacity()/3;
        ibuf.rewind();
        vbuf.rewind();
        for (int i=0; i<numTris; i++) {
            int index = ibuf.get()*3;
            v0.x = vbuf.get(index);
            v0.y = vbuf.get(index+1);
            v0.z = vbuf.get(index+2);
            index = ibuf.get()*3;
            v1.x = vbuf.get(index);
            v1.y = vbuf.get(index+1);
            v1.z = vbuf.get(index+2);
            index = ibuf.get()*3;
            v2.x = vbuf.get(index);
            v2.y = vbuf.get(index+1);
            v2.z = vbuf.get(index+2);
            //System.out.println("Vertex 0: " + v0);
            //System.out.println("Vertex 1: " + v1);
            //System.out.println("Vertex 2: " + v2);
            if (checkTriForIntersection(v0, v1, v2, mesh, result, cc, screenPt)) {
                return (true);
            }

        }
        return(false);
    }

    /**
     * Check the triangle for intersection
     */
    boolean checkTriForIntersection(Vector3f v0, Vector3f v1, Vector3f v2,
            TriMesh mesh, PickResults result, CameraComponent cc, Vector3f screenPt) {
        Vector3f trans = mesh.getWorldTranslation();
        Quaternion rot = mesh.getWorldRotation();
        Vector3f scale = mesh.getWorldScale();
        Matrix4f mat = new Matrix4f();

        mat.setTranslation(trans);
        mat.setRotationQuaternion(rot);
        mat.scale(scale);

        Vector3f wv0 = new Vector3f();
        Vector3f wv1 = new Vector3f();
        Vector3f wv2 = new Vector3f();

        // First transform to world space
        mat.mult(v0, wv0);
        mat.mult(v1, wv1);
        mat.mult(v2, wv2);
        //System.out.println("Vertex 0: " + v0);
        //System.out.println("Vertex 1: " + v1);
        //System.out.println("Vertex 2: " + v2);
        //System.out.println("World Vertex 0: " + wv0);
        //System.out.println("World Vertex 1: " + wv1);
        //System.out.println("World Vertex 2: " + wv2);

        AbstractCamera ac = (AbstractCamera)cc.getCamera();
        float viewportWidth = ac.getWidth() * (ac.getViewPortRight() - ac.getViewPortLeft());
        float viewportHeight = ac.getHeight() * (ac.getViewPortTop() - ac.getViewPortBottom());

        Matrix4f scrMat = getGluOrtho(0, viewportWidth, 0, viewportHeight, -1, 1);
        Vector3f sv0 = new Vector3f();
        Vector3f sv1 = new Vector3f();
        Vector3f sv2 = new Vector3f();
        scrMat.mult(wv0, sv0);
        scrMat.mult(wv1, sv1);
        scrMat.mult(wv2, sv2);

        sv0.x = sv0.x * viewportWidth/2.0f;
        sv0.y = sv0.y * viewportHeight/2.0f;
        sv1.x = sv1.x * viewportWidth/2.0f;
        sv1.y = sv1.y * viewportHeight/2.0f;
        sv2.x = sv2.x * viewportWidth/2.0f;
        sv2.y = sv2.y * viewportHeight/2.0f;
        //System.out.println("Screen Vertex 0: " + sv0);
        //System.out.println("Screen Vertex 1: " + sv1);
        //System.out.println("Screen Vertex 2: " + sv2);
        //System.out.println("Screen Point: " + screenPt);

        return (intersects(sv0, sv1, sv2, screenPt));
    }

    /**
     * Test if the point is in the triangle
     */
    boolean intersects(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f p) {
        float r1 = (p.y - v0.y) * (v1.x - v0.x) - (p.x - v0.x) * (v1.y - v0.y);
        float r2 = (p.y - v1.y) * (v2.x - v1.x) - (p.x - v1.x) * (v2.y - v1.y);
        float r3 = (p.y - v2.y) * (v0.x - v2.x) - (p.x - v2.x) * (v0.y - v2.y);
        //System.out.println("RS: " + r1 + ", " + r2 + ", " + r3);
        if ((r1 > 0 && r2 > 0 && r3 > 0) ||
             r1 < 0 && r2 < 0 && r3 < 0) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Construct an Ortho matrix
     */
    Matrix4f getGluOrtho(float left, float right, float bottom, float top, float near, float far) {
        Matrix4f mat = new Matrix4f();
        float rpl = right + left;
        float rml = right - left;
        float tpb = top + bottom;
        float tmb = top - bottom;
        float fpn = far + near;
        float fmn = far - near;
        float Tx = rpl/rml;
        float Ty = tpb/tmb;
        float Tz = fpn/fmn;

        mat.m00 = 2.0f/rml;
        mat.m11 = 2.0f/tmb;
        mat.m22 = 2.0f/fmn;
        mat.m33 = 1.0f;

        mat.m30 = Tx;
        mat.m31 = Ty;
        mat.m32 = Tz;
        return (mat);
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
    public PickInfo pickAllWithOrthoEyeRay(Ray eyeRay, CameraComponent cc, boolean geometryPick, boolean interpolataData) {
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
        JMEPickInfo pickInfo = (JMEPickInfo)pickAllWithOrthoWorldRay(wRay, geometryPick, interpolataData, cc);
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
        int j = 0;

        // create the correct pick results
        if (geometryPick) {
            pickResults = new TrianglePickResults();
        } else {
            pickResults = new BoundingPickResults();
        }
        pickResults.setCheckDistance(true);

        // Do the actual query
        pickAll(worldRay, pickResults, null);

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
                TrianglePickData tpd = (TrianglePickData) pickData;
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
     * A pick routine that picks against all collision components - returning them
     * all in the PickInfo.  The boolean geometryPick signals whether to use geometry 
     * data or just bounds.  interpolateData signals whether or not interpolated data
     * is calculated.
     */
    public PickInfo pickAllWithOrthoWorldRay(Ray worldRay, boolean geometryPick, boolean interpolataData, CameraComponent cc) {
        PickInfo pickInfo = null;
        PickResults pickResults = null;
        int j = 0;
        
        // create the correct pick results
        if (geometryPick) {
            pickResults = new TrianglePickResults();
        } else {
            pickResults = new BoundingPickResults();
        }
        pickResults.setCheckDistance(true);
        
        // Do the actual query
        pickAll(worldRay, pickResults, cc);
        
        //System.out.println("PickResults: " + pickResults.getNumber());
        
        // Create out pick info
        pickInfo = new JMEPickInfo(geometryPick, interpolataData, worldRay);

        // Run through the list picking out orthos
        for (int i=0; i<pickResults.getNumber(); i++) {
            PickData pickData = pickResults.getPickData(i);
            // Handle the Ortho case
            if (pickData.getTargetMesh().getRenderQueueMode() == com.jme.renderer.Renderer.QUEUE_ORTHO) {
                JMEPickDetails pickDetails = getPickDetails(pickData.getTargetMesh(), pickInfo, pickData);
                // Look through current pickInfo list to see where to put this one
                // based upon it's z order
                int len = pickInfo.size();
                for (j=0; j<len; j++) {
                    JMEPickDetails pd = (JMEPickDetails)pickInfo.get(j);
                    if (pickData.getTargetMesh().getZOrder() <=
                            pd.getPickData().getTargetMesh().getZOrder()) {
                        pickInfo.addPickDetail(j, pickDetails);
                        break;
                    }
                }
                if (j==len) {
                   pickInfo.addPickDetail(pickDetails);
                }
            }
        }

        // Now add data from the pickResults
        for (int i=0; i<pickResults.getNumber(); i++) {
            PickData pickData = pickResults.getPickData(i);
            // Skip over orthos
            if (pickData.getTargetMesh().getRenderQueueMode() != com.jme.renderer.Renderer.QUEUE_ORTHO) {
                // Prune out non-hits in geometry case
                if (geometryPick && pickData.getDistance() == Float.POSITIVE_INFINITY) {
                    continue;
                }
                JMEPickDetails pickDetails = getPickDetails(pickData.getTargetMesh(), pickInfo, pickData);
                pickInfo.addPickDetail(pickDetails);

                // Add more details in geometry pick case
                if (geometryPick) {
                    TrianglePickData tpd = (TrianglePickData) pickData;
                    Vector3f intersectionPoint = new Vector3f();
                    tpd.getIntersectionPoint(intersectionPoint);
                    pickDetails.setPosition(intersectionPoint);

                    if (interpolataData) {
                        // TODO calculate this
                    }
                }
            }
        }
        
        return (pickInfo);
    }

    /**
     * Find the entity which contains this geometry
     */
    private JMEPickDetails getPickDetails(Geometry g, PickInfo pickInfo, PickData pickData) {
        JMECollisionComponent cc = null;
        Entity entity = null;
        JMEPickDetails pickDetails = null;
        
        // First, find the topmost parent
        Node parent = g.getParent();
        Node node = parent;
        cc = (JMECollisionComponent) spatialMap.get(node);
        while (cc == null && parent != null) {
            node = parent;
            parent = parent.getParent();
            cc = (JMECollisionComponent) spatialMap.get(node);
        }
        entity = cc.getEntity();        
        
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
