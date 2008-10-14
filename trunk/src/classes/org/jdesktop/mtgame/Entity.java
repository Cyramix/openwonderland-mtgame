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

import java.util.HashMap;
import java.util.ArrayList;

/**
 * The Entity object is the base object for all things in the game.  It uses
 * a component based architecture which allows an Entity to acquire and adapt
 * to new features dynamically.
 * 
 * @author Doug Twilleager
 */
public class Entity {
   /**
    * The name of the entity
    */
   private String name = null;
   
   /**
    * The component map contains all features of the entity.  The map is 
    * indexed by a string.  The default strings are declared above.
    */
   private HashMap componentMap = new HashMap();

   /**
    * The partition within the current space which this entity resides
    * TODO: Should this be an object?
    */
   private int partition = 0;

   /**
    * The Sub-entities for this Entity.  It is just a list of entities
    * with a link back to their parent.
    */
   private ArrayList subEntities = new ArrayList();
   
   /**
    * The list of spaces that I belong in
    */
   private ArrayList spaces = new ArrayList();

   /**
    * If this is a subEntity, it has a link to it's parent.
    * The default is null - no parent.
    */
   private Entity parent = null;
   
   /**
    * The entity manager for this entity.  If it is null, the system
    * doesn't have this entity
    */
   private WorldManager worldManager = null;

   /**
    * The default Entity constructor.
    */
   public Entity(String name) {
       this.name = name;
   }
   
   /**
    * Add a component to the entity
    */
   public void addComponent(Class key, EntityComponent component) {
       componentMap.put(key, component);
       component.setEntity(this);
       if (worldManager != null) {
           worldManager.addComponent(component);
       }
   }
   
   /**
    * Get a component from the entity.
    */
   public EntityComponent getComponent(Class key) {
       return((EntityComponent)componentMap.get(key));
   }
   
   /**
    * Remove a component from the entity
    */
   public void removeComponent(Class key) {
       EntityComponent c = (EntityComponent) componentMap.remove(key);
       if (c != null) {
           c.setEntity(null);
           if (worldManager != null) {
               worldManager.removeComponent(c);
           }
       }
   }

   /**
    * Set the parent of an Entity
    */
   void setParent(Entity entity) {
       parent = entity;
   }

   
   /**
    * Get the parent of an Entity
    */
   public Entity getParent() {
       return (parent);
   }
   
   /**
    * Add a SubEntity to this entity.  It is managed by the parent Entity.
    */
   public void addEntity(Entity entity) {
       entity.setParent(this);
       entity.setWorldManager(worldManager);
       subEntities.add(entity);
       
       // If we have a world manager, we are live and therefore need
       // to add via the world manager
       if (worldManager != null) {
           worldManager.addEntity(entity);
       }
   }

   /**
    * Remove a SubEntity from the sub entity list
    */
   public void removeEntity(Entity entity) {             
       // If we have a world manager, we are live and therefore need
       // to remove via the world manager
       if (worldManager != null) {
           worldManager.removeEntity(entity);
       }
       
       subEntities.remove(entity);
       entity.setParent(null);
       entity.setWorldManager(null);
   }
   
   /**
    * Set the world manager.  Null means that the system does not have
    * this entity.
    */
   void setWorldManager(WorldManager wm) {
       worldManager = wm;
       for (int i=0; i<subEntities.size(); i++) {
           Entity e = (Entity) subEntities.get(i);
           e.setWorldManager(wm);
       }
   }
   
   /**
    * Get the world manager for this entity
    */
   WorldManager getWorldManager() {
       return (worldManager);
   }
   
   /**
    * Get the number of sub entities
    */
   public int numEntities() {
       return (subEntities.size());
   }
   
   /**
    * Get the sub entity at the given index
    */
   public Entity getEntity(int index) {
       return ((Entity)subEntities.get(index));
   }
   
   /**
    * Get the entities name
    * @return The name
    */
   public String getName() {
       return (name);
   }
   
   public String toString() {
       return(name);
   }
}