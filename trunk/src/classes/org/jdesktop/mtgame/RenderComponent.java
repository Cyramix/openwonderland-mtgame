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
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.light.LightNode;
import java.util.ArrayList;

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
    * A flag to indicate whether or not this render component should
    * use an orthographic projection
    */
   private boolean ortho = false;
   
   /**
    * This flag controls whether or not lighting is applied to 
    * this RenderComponent
    */
   private boolean lightingEnabled = true;
   
   /**
    * The lights that only apply to this RenderCompoenent
    */
   private ArrayList lights = new ArrayList();
   
   /**
    * The LightState that is used to apply lighting to just this
    * RenderComponent.
    */
   private LightState lightState = null;

   /**
    * An object used for render techniques
    */
   private Object renderTechniqueObject = null;

   /**
    * The render technique for this render component
    */
   private RenderTechnique renderTechnique = null;

   /**
    * An object used for render techniques
    */
   private String renderTechniqueName = "org.jdesktop.mtgame.DefaultRenderTechnique";

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
    * The constructor with RenderTechnique
    */
   RenderComponent(String rt, Node node, Object rtObject) {
       renderTechniqueName = rt;
       sceneRoot = node;
       renderTechniqueObject = rtObject;
   }

   /**
    * Set the render technique
    */
   void setRenderTechnique(RenderTechnique rt) {
       renderTechnique = rt;
   }

   /**
    * Get the render technique
    */
   RenderTechnique getRenderTechnique() {
       return (renderTechnique);
   }

    @Override
   public void setEntity(Entity entity) {
       super.setEntity(entity);
       if (entity!=null)
           sceneRoot.setLive(true);
       else
           sceneRoot.setLive(false);
   }

   /**
    * Get the name of the render technique
    */
   public String getRenderTechniqueName() {
       return (renderTechniqueName);
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
    *
    * @parameter ap - the parent node to which the sceneRoot of this component will be attached
    */
   public void setAttachPoint(Node ap) {
       Entity e = getEntity();

       // Nothing to do if we don't have an entity
       if (e != null && e.getWorldManager() != null) {
           synchronized (e.getWorldManager().getRenderManager().getJMESGLock()) {
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
       }
       attachPoint = ap;
   }
   
   /**
    * Set the attach point for this RenderComponent
    */
   public Node getAttachPoint() {
       return(attachPoint);
   }
   
   /**
    * Set the othographic projection flag
    */
   public void setOrtho(boolean flag) {
       Entity e = getEntity();

       if (ortho != flag) {
           ortho = flag;
           if (e != null && e.getWorldManager() != null) {
               e.getWorldManager().getRenderManager().changeOrthoFlag(this);
           }
       }
   }
   
   /**
    * Get the value of the orthographic projection flag
    */
   public boolean getOrtho() {
       return (ortho);
   }
   
   /**
    * Set the lighting enable flag
    */
   public void setLightingEnabled(boolean flag) {
       Entity e = getEntity();
       
       if (lightingEnabled != flag) {
           lightingEnabled = flag;
           if (e != null && e.getWorldManager() != null) {
               e.getWorldManager().getRenderManager().changeLighting(this);
           }
       }
   }
   
   /**
    * Get the lighting enabled flag
    */
   public boolean getLightingEnabled() {
       return (lightingEnabled);
   }
   
    /**
     * Add a global light to the scene
     */
    public void addLight(LightNode light) {
        Entity e = getEntity();

        synchronized (lights) {
            lights.add(light);
            if (e != null && e.getWorldManager() != null) {
                e.getWorldManager().getRenderManager().changeLighting(this);
            }
        }
    }

    /**
     * Remove a global light from the scene
     */
    public void removeLight(LightNode light) {
        Entity e = getEntity();

        synchronized (lights) {
            lights.remove(light);
            if (e != null && e.getWorldManager() != null) {
                e.getWorldManager().getRenderManager().changeLighting(this);
            }
        }
    }
    
    /**
     * Create a LightState with the current set of lights
     */
    void updateLightState(ArrayList globalLights) {
        Entity e = getEntity();

        lightState = (LightState) e.getWorldManager().
                getRenderManager().createRendererState(RenderState.StateType.Light);
        for (int i = 0; i < globalLights.size(); i++) {
            LightNode ln = (LightNode) globalLights.get(i);
            e.getWorldManager().addToUpdateList(ln);
            lightState.attach(ln.getLight());
        }

        for (int i = 0; i < lights.size(); i++) {
            LightNode ln = (LightNode) lights.get(i);
            e.getWorldManager().addToUpdateList(ln);
            lightState.attach(ln.getLight());
        }
        lightState.setEnabled(lightingEnabled);
        sceneRoot.setRenderState(lightState);
    }
    
    /**
     * Get the LightState for this RenderComponent
     */
    LightState getLightState() {
        return (lightState);
    }

    /**
     * Return the number of global Lights
     */
    public int numLights() {
        int num = 0;
        synchronized (lights) {
            num = lights.size();
        }
        return (num);
    }

    /**
     * Get a light at the index specified
     */
    public LightNode getLight(int i) {
        LightNode light = null;

        synchronized (lights) {
            light = (LightNode) lights.get(i);
        }
        return (light);
    }
}
