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

import com.jme.scene.Node;

/**
 * This is an entity component that implements the collision interface of 
 * an entity.
 * 
 * @author Doug Twilleager
 */
public class CollisionComponent extends EntityComponent {

    /**
     * The collision system for this component
     */
    CollisionSystem collisionSystem = null;

    /**
     * The jme node for this collision component
     */
    private Node node = null;
    
    /**
     * A boolean to indicate whether or not this component is currently
     * enabled for picking
     */
    private boolean pickable = true;
    
    /**
     * The default constructor
     */
    CollisionComponent(CollisionSystem cs, Node node) {
        collisionSystem = cs;
        this.node = node;
    }

    /**
     * Get the collision system for this component
     */
    public CollisionSystem getCollisionSystem() {
        return (collisionSystem);
    } 
    
    /**
     * get the Collision node
     */
    public Node getNode() {
        return (node);
    }
    
    /**
     * Set's whether or not this collision component should be considered 
     * for pick queries
     */
    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }
    
    /**
     * Retuns whether or not this collision component is considered for pick
     * queries
     */
    public boolean isPickable() {
        return (pickable);
    }
}
