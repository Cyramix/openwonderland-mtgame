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

import javax.vecmath.Vector3f;

/**
* This is an entity component that implements the physics interface of 
* an entity.
* 
* @author Doug Twilleager
*/
public class JBulletPhysicsComponent extends PhysicsComponent {  
    /**
     * The mass for this component
     */
    private float mass = 0.0f;
    
    /**
     * The inertia for this component
     */
    private Vector3f inertia = new Vector3f(0.0f, 0.0f, 0.0f);
       
    /**
     * The inertia for this component
     */
    private Vector3f linearVelocity = new Vector3f(0.0f, 0.0f, 0.0f);
    
    /**
     * The default constructor
     */
    JBulletPhysicsComponent(PhysicsSystem ps, JBulletCollisionComponent cc) {
        super(ps, cc);
        cc.setPhysicsComponent(this);
    }
    
    /**
     * Get the mass
     */
    public void setMass(float m) {
        mass = m;
    }
    
    /**
     * Get the inertia
     */
    public void setInertia(float x, float y, float z) {
        inertia.x = x;
        inertia.y = y;
        inertia.z = z;
    }
    
    /**
     * Set the linear velocity
     */
    public void setLinearVelocity(float x, float y, float z) {
        linearVelocity.x = x;
        linearVelocity.y = y;
        linearVelocity.z = z;
    }
    
    /**
     * Get the mass
     */
    public float getMass() {
        return (mass);
    }
    
    /**
     * Get the inertia
     */
    public Vector3f getInertia() {
        return (inertia);
    }
        
    /**
     * Get the inertia
     */
    public Vector3f getLinearVelocity() {
        return (linearVelocity);
    }
}