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

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TriMesh;

/**
 * This is the base class for detailed information returned by collision
 * system pick queries.  The interpolated data values are only valid if the
 * pick query requested them.
 * 
 * @author Doug Twilleager
 */
public class PickDetails {
    /**
     * The CollisionSystem that generated these details
     */
    private CollisionSystem collisionSystem = null;
    /**
     * The CollisionComponent that holds the pick hit.
     */
    private CollisionComponent collisionComponent = null;
    
    /**
     * The Entity which encompases this graph
     */
    private Entity entity = null;
    
    /**
     * The PickInfo object that contains this set of PickDetails
     */
    private PickInfo pickInfo = null;
    
    /**
     * The distance from the ray orgin
     */
    float distance = Float.MAX_VALUE;
    
    /**
     * The exact position, texture coordinates, normal, and color
     * These are only valid if this is an interpolated geometry based pick.
     */
    private Vector3f position = null;
    private Vector3f normal = null;
    private Vector3f texCoord = null;
    private ColorRGBA color = null;
    private TriMesh triMesh = null;
    private int triIndex = -1;
    
    /**
     * The default constructor
     */
    PickDetails(CollisionSystem cs, Entity e, CollisionComponent cc, 
            PickInfo pi, float distance) {
        collisionSystem = cs;
        entity = e;
        collisionComponent = cc;
        pickInfo = pi;
        this.distance = distance;
    }
    
    /**
     * get the collision system
     */
    public CollisionSystem getCollisionSystem() {
        return (collisionSystem);
    }
    
    /**
     * Get the entity
     */
    public Entity getEntity() {
        return (entity);
    }
        
    /**
     * Get the CollisionComponent
     */
    public CollisionComponent getCollisionComponent() {
        return (collisionComponent);
    }
    
    /**
     * Get the PickInfo
     */
    public PickInfo getPickInfo() {
        return (pickInfo);
    }
    
    /**
     * Get the distance from the intersection
     */
    public float getDistance() {
        return (distance);
    }
    
    /**
     * Get the position
     */
    public Vector3f getPosition() {
        return (position);
    }
    
    /**
     * set the position
     */
    void setPosition(Vector3f pos) {
        position = pos;
    }
    
    /**
     * Get the normal
     */
    public Vector3f getNormal() {
        return (normal);
    }
    
    /**
     * set the normal
     */
    void setNormal(Vector3f n) {
        normal = n;
    }
        
    /**
     * Get the texture coord
     */
    public Vector3f getTexCoord() {
        return (texCoord);
    }
    
    /**
     * set the texture coordinate
     */
    void setTexCoord(Vector3f t) {
        texCoord = t;
    }
        
    /**
     * Get the Color
     */
    public ColorRGBA getColor() {
        return (color);
    }
    
    /**
     * set the color
     */
    void setColor(ColorRGBA c) {
        color = c;
    }

    /**
     * Set the mesh
     */
    void setTriMesh(TriMesh mesh) {
        triMesh = mesh;
    }

    /**
     * Get the tri mesh
     */
    public TriMesh getTriMesh() {
        return (triMesh);
    }

    /**
     * Set the tri index
     */
    void setTriIndex(int index) {
        triIndex = index;
    }

    /**
     * Get the tri index
     */
    public int getTriIndex() {
        return (triIndex);
    }

}
