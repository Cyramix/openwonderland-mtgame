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
 * This class handles collision management for the system.  A custom collision
 * manager can be created to handle the collision system
 * 
 * @author Doug Twilleager
 */
public class CollisionManager {
    /**
     * A reference to the world manager
     */
    private WorldManager worldManager = null;
    
    /**
     * The collection of collision systems known in the system
     */
    private HashMap collisionSystems = new HashMap();
    
    /**
     * The Default constructor
     * 
     * @param worldManager
     */
    CollisionManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
    
    /**
     * This method loads the specified collision system
     */
    public CollisionSystem loadCollisionSystem(Class system) {
        CollisionSystem cs = (CollisionSystem) collisionSystems.get(system);
        
        if (cs == null) {
            // Initialize the system
            try {
                try {
                    cs = (CollisionSystem) system.newInstance();
                } catch (IllegalAccessException e1) {
                    System.out.println(system + " could not be created: " + e1);
                    return (null);
                }
            } catch (InstantiationException e2) {
                System.out.println(system + " could not be created: " + e2);
                return (null);
            }
            collisionSystems.put(system, cs);
            cs.setWorldManager(worldManager);
        }
        
        return (cs);
    }
}
