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

import com.jme.intersection.CollisionResults;

/**
 * This is the base class for detailed information returned by collision
 * system collision queries.
 * 
 * @author Doug Twilleager
 */
public class CollisionDetails {
    /**
     * The CollisionSystem that generated these details
     */
    private CollisionSystem collisionSystem = null;
    /**
     * The CollisionComponent that holds the collision hit.
     */
    private CollisionComponent collisionComponent = null;
    
    /**
     * The Entity which encompases this graph
     */
    private Entity entity = null;
    
    /**
     * The CollisionInfo object that contains this set of CollisionDetails
     */
    private CollisionInfo collisionInfo = null;

    /**
     * The jME CollsionResults object for this collision
     */
    private CollisionResults collisionResults = null;
    
    /**
     * The default constructor
     */
    CollisionDetails(CollisionSystem cs, Entity e, CollisionComponent cc, CollisionInfo ci) {
        collisionSystem = cs;
        entity = e;
        collisionComponent = cc;
        collisionInfo = ci;
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
     * Get the CollisionInfo
     */
    public CollisionInfo getCollisionInfo() {
        return (collisionInfo);
    }
    
    /**
     * Get the distance from the intersection
     */
    public CollisionResults getCollisionResults() {
        return (collisionResults);
    }
}
