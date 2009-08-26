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

import java.util.HashMap;
/**
 * This class handles physics management for the system.  A custom physics
 * manager can be created to handle the physics system
 * 
 * @author Doug Twilleager
 */
public class PhysicsManager {
    /**
     * A reference to the world manager
     */
    private WorldManager worldManager = null;
    
    /**
     * The collection of physics systems known in the system
     */
    private HashMap physicsSystems = new HashMap();
    
    /**
     * The Default constructor
     * 
     * @param worldManager
     */
    PhysicsManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
    
    /**
     * This method loads the specified collision system
     */
    public PhysicsSystem loadPhysicsSystem(Class system, CollisionSystem cs) {
        PhysicsSystem ps = (PhysicsSystem) physicsSystems.get(system);
        
        if (ps == null) {
            // Initialize the system
            try {
                try {
                    ps = (PhysicsSystem) system.newInstance();
                } catch (IllegalAccessException e1) {
                    System.out.println(system + " could not be created: " + e1);
                    return (null);
                }
            } catch (InstantiationException e2) {
                System.out.println(system + " could not be created: " + e2);
                return (null);
            }
            physicsSystems.put(system, ps);
            ps.setWorldmanager(worldManager);
            ps.setCollisionSystem(cs);
            ps.initialize();
        }
        
        return (ps);
    }

    void quit() {
        JBulletPhysicsSystem jbp = (JBulletPhysicsSystem)physicsSystems.get(JBulletPhysicsSystem.class);
        if (jbp != null) {
            jbp.quit();
        }
    }
}
