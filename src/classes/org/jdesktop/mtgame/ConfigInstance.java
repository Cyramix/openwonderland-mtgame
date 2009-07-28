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
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;

/**
 * This interface is implemented by anyone who wishes to create an instance
 * of an object in the config file
 * 
 * @author Doug Twilleager
 */
public interface ConfigInstance {
    /**
     * This method is called when the instance is initialized
     */
    public abstract void init(WorldManager wm, ConfigManager cm, String name, Vector3f location, Quaternion rotation, Vector3f scale, String[] args);

    /**
     * This method is called to get the scene graph for this instance
     */
    public Node getSceneGraph();

    /**
     * This method is called to get the entity for this instance
     */
    public Entity getEntity();
}
