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
import com.jme.scene.Spatial;
import com.jme.renderer.pass.Pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.Canvas;


/**
 * This is the class which manages everything in the system.
 * It creates and intializes all the other system.
 * 
 * @author Doug Twilleager
 */
public class WorldManager {
    /**
     * The name of this world
     */
    private String name = null;
    
    /**
     * The RenderManager
     */
    private RenderManager renderManager = null;
    
    /**
     * The InputManager
     */
    private InputManager inputManager = null;
    
    /**
     * The ProcessorManager
     */
    private ProcessorManager processorManager = null;
    
    /**
     * The Collsion manager
     */
    private CollisionManager collisionManager = null;
    
    /**
     * The Collsion manager
     */
    private PhysicsManager physicsManager = null;
    
    /**
     * The list of known entities
     */
    private ArrayList entities = new ArrayList();
    
    /**
     * the event allocator
     */
    private SmallIntegerAllocator eventAllocator = new SmallIntegerAllocator();  
    
    /**
     * The user data map contains all of the user data.  The map is 
     * indexed by a class.
     */
    private HashMap userData = new HashMap();
    
    /**
     * The Default Constructor
     */
    public WorldManager(String name) {
        this.name = name;
        renderManager = new RenderManager(this);
        processorManager = new ProcessorManager(this);
        processorManager.initialize();
        inputManager = new AWTInputManager();
        inputManager.initialize(this); 
        collisionManager = new CollisionManager(this);
        physicsManager = new PhysicsManager(this);
        //System.out.println("Done Initializing!");
    }
    
    /**
     * Get the RenderManager.
     */
    public RenderManager getRenderManager() {
        return(renderManager);
    }
  
    /**
     * Get the InputManager.
     */
    public InputManager getInputManager() {
        return(inputManager);
    }

    /**
     * Get the CollisionManager.
     */
    public CollisionManager getCollisionManager() {
        return(collisionManager);
    }

    /**
     * Get the PhysicsManager.
     */
    public PhysicsManager getPhysicsManager() {
        return(physicsManager);
    }
    
    /**
     * Get the ProcessorManager.
     */
    ProcessorManager getProcessorManager() {
        return(processorManager);
    }
    
    /**
     * This adds an already created entity to the system
     */
    public void addEntity(Entity e) {
        EntityComponent c = null;
        e.setWorldManager(this);
        
        Iterator comps = e.getComponents().iterator(); 
        while (comps.hasNext()) {
            c = (EntityComponent) comps.next();
            addComponent(c);
        }
        entities.add(e);
        
        // Now add the sub-entities
        for (int i=0; i<e.numEntities(); i++) {
            addEntity((Entity)e.getEntity(i));
        }
    }   
    
    /**
     * This adds an already created entity to the system
     */
    public void removeEntity(Entity e) {
        EntityComponent c = null;
        
        Iterator comps = e.getComponents().iterator();
        while (comps.hasNext()) {
            c = (EntityComponent) comps.next();
            removeComponent(c);
        }

        // remove the sub-entities
        for (int i=0; i<e.numEntities(); i++) {
            removeEntity((Entity)e.getEntity(i));
        }
        
        entities.remove(e);
        e.setWorldManager(null);
    } 
    
    /**
     * Get the number of entities
     */
    public int numEntities() {
        return (entities.size());
    }
    
    /**
     * Get the entity at the given index
     */
    public Entity getEntity(int index) {
        return ((Entity)entities.get(index));
    }
    
    /**
     * This adds a component, which is being added to an entity which is
     * already being processed
     */
    void addComponent(EntityComponent c) {
        if (RenderComponent.class.isInstance(c) ||
            CollisionComponent.class.isInstance(c) ||
            JMECollisionComponent.class.isInstance(c) ||
            JBulletCollisionComponent.class.isInstance(c) ||
            SkyboxComponent.class.isInstance(c) ||
            PassComponent.class.isInstance(c) ||
            CameraComponent.class.isInstance(c)) {
            renderManager.addComponent(c);
        }

        if (ProcessorComponent.class.isInstance(c) ||
            ProcessorCollectionComponent.class.isInstance(c)) {
            processorManager.addComponent(c);
        }  
    }
    
    /**
     * This adds a component, which is being added to an entity which is
     * already being processed
     */
    void removeComponent(EntityComponent c) {
        if (RenderComponent.class.isInstance(c) ||
            CollisionComponent.class.isInstance(c) ||
            JMECollisionComponent.class.isInstance(c) ||
            JBulletCollisionComponent.class.isInstance(c) ||
            SkyboxComponent.class.isInstance(c) ||
            PassComponent.class.isInstance(c) ||
            CameraComponent.class.isInstance(c)) {
            renderManager.removeComponent(c);
        }

        if (ProcessorComponent.class.isInstance(c) ||
            ProcessorCollectionComponent.class.isInstance(c)) {
            processorManager.removeComponent(c);
        }    
    }
    
    /**
     * Post an event to the system
     */
    public void postEvent(long event) {
        processorManager.distributePostEvent(event);
        processorManager.triggerPostEvent();
    }
    
    /**
     * Allocate an event id to be used by postEvent.
     */
    public long allocateEvent() {
        return (eventAllocator.allocate());
    }
    
    /**
     * Free an event id
     */
    public void freeEvent(long event) {
        eventAllocator.free((int)event);
    }
    
    /**
     * Add a listener to the list of listening for scene changes
     */
    public void addNodeChangedListener(NodeChangedListener l) {
        renderManager.addNodeChangedListener(l);
    }
            
    /**
     * Add a updater to be called in the render thread
     */
    public void addRenderUpdater(RenderUpdater ru, Object obj) {
        renderManager.addRenderUpdater(ru, obj);
    }
    
    /**
     * Remove a listener from the list of listening for scene changes
     */
    public void removeNodeChangedListener(NodeChangedListener l) {
        renderManager.removeNodeChangedListener(l);
    }  

    /**
     * Add a node to the update lists
     */
    public void addToUpdateList(Spatial spatial) {
        // What about multiple renderers
        renderManager.addToUpdateList(spatial);
    }
    
    /**
     * Add a Pass to the update lists
     */
    public void addToPassUpdateList(Pass pass) {
        // What about multiple renderers
        renderManager.addToPassUpdateList(pass);
    }
    
       
   /**
    * Add user data
    */
   public void addUserData(Class key, Object data) {
       userData.put(key, data);
   }
   
   /**
    * Get user data
    */
   public Object getUserData(Class key) {
       return(userData.get(key));
   }
   
    /**
     * Pass along an awt event trigger to the process controller
     */
    void triggerAWTEvent() {
        processorManager.triggerAWTEvent();
    }
    
    /**
     * Trigger everyone waiting on a new frame
     */
    void triggerNewFrame() {
        processorManager.triggerNewFrame();
    }
    
    /**
     * Add a processor component to the appropriate lists of possible arms
     */
    void armProcessorComponent(ProcessorArmingCondition armingCondition) {
        processorManager.armProcessorComponent(armingCondition);
    }
    
    /**
     * Run the processes component commit list
     * For now, we'll just run them on screen 0
     */
    void runCommitList(ProcessorComponent[] runList) {
        // This method will block until the renderer processes 
        // the whole list
        renderManager.runCommitList(runList);
    }
    
    
    /**
     * Add a processor which has triggerd to the Renderer Processor List
     */
    void addTriggeredProcessor(ProcessorComponent pc) {
        renderManager.addTriggeredProcessor(pc);
    }
    
    /**
     * Start tracking key input.
     */
    void trackKeyInput(Canvas c, Object listener) {
        renderManager.trackKeyInput(c, listener);
    }

    /**
     * Stop tracking key input
     */
    void untrackKeyInput(Canvas c, Object listener) {
        renderManager.untrackKeyInput(c, listener);
    }

    /**
     * Set the MouseInput to track.  Null means stop tracking
     */
    void trackMouseInput(Canvas c, Object listener) {
        renderManager.trackMouseInput(c, listener);
    }

    /**
     * Stop tracking key input
     */
    void untrackMouseInput(Canvas c, Object listener) {
        renderManager.untrackMouseInput(c, listener);
    }
    
}
