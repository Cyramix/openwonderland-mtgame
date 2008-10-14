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

import com.jme.math.Ray;

/**
 * This interface is implemented by anyone who wishes to implement a 
 * collision system.
 *
 * @author Doug Twilleager
 */
public abstract class CollisionSystem {
    /**
     * The WorldManager for this system
     */
    protected WorldManager worldManager = null;
    
    /**
     * Set the world manager
     */
    void setWorldManager(WorldManager wm) {
        worldManager = wm;
    }
         
    /**
     * Set the world manager
     */
    WorldManager getWorldManager() {
        return(worldManager);
    }
    
    /**
     * Systems need to define this for themselves
     */
    public abstract void addCollisionComponent(CollisionComponent cc);

    /**
     * Collision Systems need to implment this query
     */
    public abstract PickInfo pickAllEyeRay(Ray eyeRay, CameraComponent cc, boolean geometryPick, boolean interpolataData);

    /**
     * Collision Systems need to implment this query
     */
    public abstract PickInfo pickAllWorldRay(Ray worldRay, boolean geometryPick, boolean interpolataData);

}
