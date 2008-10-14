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
* This is an entity component that implements the visual representation of 
* an entity.
* 
* @author Doug Twilleager
*/
public class RenderComponent extends EntityComponent {
   /**
    * The base node for the JME Scene Graph
    */
   private Node sceneRoot = null;
   
   /**
    * A sometimes non-null place for this RenderComponent to attach to.
    */
   private Node attachPoint = null;

   /**
    * The default constructor
    */
   RenderComponent(Node node) {
       sceneRoot = node;
   }
    
   /**
    * The constructor with attach point
    */
   RenderComponent(Node node, Node attachPoint) {
       sceneRoot = node;
       this.attachPoint = attachPoint;
   }
   
   /**
    * Get the scene root
    */
   public Node getSceneRoot() {
       return (sceneRoot);
   }
   
   /**
    * Set the attach point for this RenderComponent
    * This can only be called from a commit method if it is attaching/detaching
    * from a live entity.
    */
   public void setAttachPoint(Node ap) {
       Entity e = getEntity();
       
       // Nothing to do if we don't have an entity
       if (e != null) {
           // First, see if we need to detach from our current location
           if (attachPoint != null) {
               // Detach and put the highest parent on the update list
               Node current = attachPoint;
               Node parent = current.getParent();
               while (parent != null) {
                   current = parent;
                   parent = parent.getParent();
               }
               attachPoint.detachChild(sceneRoot);
               e.getWorldManager().addToUpdateList(current);
           }

           // Now, see if we need to notify new attachment
           if (ap != null) {
               ap.attachChild(sceneRoot);
               e.getWorldManager().addToUpdateList(ap);
           }
       }
       attachPoint = ap;
   }
   
   /**
    * Set the attach point for this RenderComponent
    */
   public Node getAttachPoint() {
       return(attachPoint);
   }
   
}
