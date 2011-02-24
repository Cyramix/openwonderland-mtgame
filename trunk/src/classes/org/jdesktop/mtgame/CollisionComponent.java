/*
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation. All rights reserved.
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
 *  . Neither the name of Open Wonderland Foundation, nor the names of its
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

import com.jme.scene.Node;

/**
 * This is an entity component that implements the collision interface of 
 * an entity.
 * 
 * @author Doug Twilleager
 */
public class CollisionComponent extends EntityComponent
    implements Entity.EntityTreeListener
{
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
     * Indicate whether or not picking behavior should be inherited from
     * parent entities. If no parent entities have collision components
     * (or there are no parents) the value of pickable will be used.
     */
    private boolean inheritPickable = false;

    /**
     * A boolean to indicate whether or not this component is currently
     * enabled for collision
     */
    private boolean collidable = true;

     /**
     * Indicate whether or not collision behavior should be inherited from
     * parent entities. If no parent entities have collision components
     * (or there are no parents) the value of collidable will be used.
     */
    private boolean inheritCollidable = false;

    /**
     * The parent collision component (if any), used when one of the
     * inherit booleans is true
     */
    private CollisionComponent parentCollisionComponent;

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
        if (inheritPickable && parentCollisionComponent != null) {
            return parentCollisionComponent.isPickable();
        }

        return (pickable);
    }

    /**
     * Set whether or not we should inherit the value of pickable
     */
    public void setInheritPickable(boolean inherit) {
        this.inheritPickable = inherit;
    }

    /**
     * Get whether we are currently inheriting the value of pickable
     */
    public boolean isInheritPickable() {
        return inheritPickable;
    }

    /**
     * Set's whether or not this collision component should be considered
     * for collision queries
     */
    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }

    /**
     * Retuns whether or not this collision component is considered for collision
     * queries
     */
    public boolean isCollidable() {
        if (inheritCollidable && parentCollisionComponent != null) {
            return parentCollisionComponent.isCollidable();
        }

        return (collidable);
    }

    /**
     * Set whether or not we should inherit the value of collidable
     */
    public void setInheritCollidable(boolean inherit) {
        this.inheritCollidable = inherit;
    }

    /**
     * Get whether we are currently inheriting the value of collidable
     */
    public boolean isInheritCollidable() {
        return inheritCollidable;
    }

    /**
     * Update entity listener when the entity changes
     */
    @Override
    public void setEntity(Entity entity) {
        // remove listener from old entity (if any)
        if (getEntity() != null) {
            getEntity().removeEntityTreeListener(this);
        }

        // update to the new entity
        super.setEntity(entity);

        // add a listener to the new entity (if any)
        if (entity != null) {
            entity.addEntityTreeListener(this);
        }
    }

    /**
     * @{inheritDoc}
     */
    public void parentChanged(Entity entity, Entity parent) {
        findParentCollisionComponent();
    }

    /**
     * @{inheritDoc}
     */
    public void componentChanged(Entity entity, EntityComponent component, boolean added) {
        findParentCollisionComponent();
    }

    /**
     * Find the parent collision component by walking up the tree until we
     * find the first parent with a collision component
     */
    protected void findParentCollisionComponent() {
       Entity parent = getEntity().getParent();

       while (parent != null) {
           parentCollisionComponent = parent.getComponent(CollisionComponent.class);
           if (parentCollisionComponent != null) {
               return;
           }

           parent = parent.getParent();
       }

       // no parent collision component found
       parentCollisionComponent = null;
    }
}
