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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jdesktop.mtgame.WorldManager.WaitForLiveness;

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
   private final Map<Class, EntityComponent> componentMap =
           new HashMap<Class, EntityComponent>();

   /**
    * The Sub-entities for this Entity.  It is just a list of entities
    * with a link back to their parent.
    */
   private final List<Entity> subEntities = new ArrayList<Entity>();
   
   /**
    * If this is a subEntity, it has a link to it's parent.
    * The default is null - no parent.
    */
   private Entity parent = null;

   /**
    * The tree listeners for this entity
    */
   private final Set<EntityTreeListener> treeListeners =
           new CopyOnWriteArraySet<EntityTreeListener>();

   /**
    * The entity manager for this entity.  If it is null, the system
    * doesn't have this entity
    */
   private WorldManager worldManager = null;

   /**
    * An empty Entity constructor.
    */
   public Entity() {
       this (null);
   }

   /**
    * The default Entity constructor.
    */
   public Entity(String name) {
       this.name = name;

       // if we want to collect statistics for all entities, add the collection
       // component here, so it is guaranteed to be available
       if (Boolean.parseBoolean(System.getProperty("mtgame.entityStats"))) {
           addComponent(StatisticsComponent.class, new StatisticsComponent());
       }
   }
   
   /**
    * Add a component to the entity
    */
   public void addComponent(Class key, EntityComponent component) {
       synchronized (componentMap) {
           componentMap.put(key, component);
           component.setEntity(this);
           if (worldManager != null) {
               worldManager.addComponent(component);
           }
       }

       fireEntityComponentChange(this, component, true);
   }
   
   /**
    * Get a component from the entity.
    */
   public <T extends EntityComponent> T getComponent(Class<T> key) {
       EntityComponent ec = null;
       
       synchronized (componentMap) {
           ec = componentMap.get(key);
       }
       return (T)ec;
   }
   
   /**
    * Return whether or not an entity has a component
    */
   public boolean hasComponent(Class key) {
       return(getComponent(key) != null);
   }
   
   /**
    * Return all the components from this entity
    * 
    * @return
    */
   public Iterable<EntityComponent> getComponents() {
       Iterable<EntityComponent> comps = null;
       
       synchronized (componentMap) {
           comps = (Iterable<EntityComponent>)componentMap.values();
       }
       return (comps);
   }

   /**
    * Remove a component from the entity
    */
   public void removeComponent(Class key) {
       WaitForLiveness waiter = null;
       EntityComponent c;

       synchronized (componentMap) {
           c = (EntityComponent) componentMap.remove(key);
           if (c != null) {
               c.setEntity(null);
               if (worldManager != null) {
                   waiter = worldManager.removeComponent(c);
               }
           }
       }
       
       // OWL issue #163: if we removed the component from the world manager, 
       // wait for it to be marked as not live by the renderer. Note
       // that we are not holding any locks, since this method will potentially
       // block for a while
       if (waiter != null) {
           try {
               waiter.waitFor();
           } catch (InterruptedException ex) {
               // ignore
           }
       }

       if (c != null) {
            fireEntityComponentChange(this, c, true);
       }
    }

   /**
    * Set the parent of an Entity
    */
   void setParent(Entity entity) {
       parent = entity;

       // notify listeners of the change
       fireEntityParentChange(this, parent);
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
       synchronized (subEntities) {
           entity.setParent(this);
           entity.setWorldManager(worldManager);
           subEntities.add(entity);
       }
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

       synchronized (subEntities) {
           subEntities.remove(entity);
           entity.setParent(null);
           entity.setWorldManager(null);
       }
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
       synchronized (subEntities) {
           return (subEntities.size());
       }
   }
   
   /**
    * Get the sub entity at the given index
    */
   public Entity getEntity(int index) {
       synchronized (subEntities) {
           return ((Entity)subEntities.get(index));
       }
   }

   /**
    * Get the entities name
    * @return The name
    */
   public String getName() {
       return (name);
   }

   /**
    * Add a tree listener
    * @param listener the tree listener to add
    */
   public void addEntityTreeListener(EntityTreeListener listener) {
       treeListeners.add(listener);
   }

   /**
    * Remove a tree listener
    * @param listener the listener to remove
    */
   public void removeEntityTreeListener(EntityTreeListener listener) {
       treeListeners.remove(listener);
   }

   /**
    * Notify tree listeners of a change to this entity's parent
    * @param entity the entity that changed
    * @param parent the new entity parent
    */
   protected void fireEntityParentChange(Entity entity, Entity parent) {
       // notify listeners
       for (EntityTreeListener listener : treeListeners) {
           listener.parentChanged(entity, parent);
       }

       // notify children
       for (Entity child : subEntities) {
           child.fireEntityParentChange(entity, parent);
       }
   }

   /**
    * Notify tree listeners of a change to this entity's parent
    * @param entity the entity that changed
    * @param parent the new entity parent
    */
   protected void fireEntityComponentChange(Entity entity,
           EntityComponent component, boolean added)
   {
       // notify listeners
       for (EntityTreeListener listener : treeListeners) {
           listener.componentChanged(entity, component, added);
       }

       // notify children
       for (Entity child : subEntities) {
           child.fireEntityComponentChange(entity, component, added);
       }
   }

   public String toString() {
       return(name+" "+super.toString());
   }

   /**
    * Interface will be notified when parenting changes anywhere in the
    * entity tree
    */
   public interface EntityTreeListener {
       /**
        * Notification that the given entity's parent has changed
        * @param entity the entity whose parent changed
        * @param parent the new parent entity (may be null)
        */
       public void parentChanged(Entity entity, Entity parent);

       /**
        * Notification that a component has been added or removed
        * @param entity the entity where the component changed
        * @param component the component that was added or removed
        * @param added true if the component was added, or false if it was
        * removed
        */
       public void componentChanged(Entity entity, EntityComponent component,
                                    boolean added);
   }
}
