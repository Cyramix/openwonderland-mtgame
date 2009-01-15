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

/**
* 
* The processor runs in two phases - compute and commit.  Due to the lack of
* multithread safeness in the current JME API, local calculations should be
* done in the compute phase, where no live JME scene elements can be modified.
* In the commit phase, all live JME scene elements may be modified.
* 
* Processors may staticallhain in execution.  This allows
* a processor to make calculations based upon a previous processors calculations.
* 
* @author Doug Twilleager
*/
public abstract class ProcessorComponent extends EntityComponent {
   /**
    * A reference to the entity process controller
    */
   private ProcessorManager entityProcessController = null;
   
   /**
    * The current conditions that will trigger execution of this process.
    */
   private ProcessorArmingCondition armingCondition = null;

   /**
    * The actual condition that have triggered this execution
    */
   private ProcessorArmingCollection triggerCollection = null;

   /**
    * The next process in the chain of executing processes.
    */
   private ProcessorComponent nextInChain = null;
   
   /**
    * A flag to indicate that this processor wishes to run in the Render thread
    */
   private boolean runInRenderer = false;
   
   /**
    * A flag to indicate that this processor is enabled
    */
   private boolean enabled = true;
   
   /**
    * A flag indicating that this methods commit method should be executed 
    * in a manner that it is safe to make swing methods calls
    */
   private boolean swingSafe = false;

   /**
    * The compute callback to be defined by the subclass.
    * 
    * @param condition The XOR of all conditions which triggered this process.
    */
   public abstract void compute(ProcessorArmingCollection collection);

   /**
    * The commit callback to be defined by the subclass.
    * 
    * @param condition The XOR of all conditions which triggered this process.
    */
   public abstract void commit(ProcessorArmingCollection collection);
   
   /**
    * The initialize callback allows the process to set itself up and set its
    * initial trigger condition
    */
   public abstract void initialize();

   /**
    * The constructor
    */
   public ProcessorComponent() {
       triggerCollection = new ProcessorArmingCollection(this);
   }
   
   /**
    * Add a processor to the chain of execution.
    */
   public void addToChain(ProcessorComponent proc) {
       ProcessorComponent currentPC = this;
       ProcessorComponent nextPC = nextInChain;
       ProcessorComponent tmpPC = null;

       while (nextPC != null) {
           tmpPC = currentPC;
           currentPC = nextPC;
           nextPC = tmpPC.nextInChain;
       }
       currentPC.nextInChain = proc;
   }

   /**
    * Remove a processor from the chain
    */
   public void removeFromChain(ProcessorComponent proc) {
       ProcessorComponent currentPC = this;
       ProcessorComponent prevPC = null;

       while (currentPC != proc) {
           prevPC = currentPC;
           currentPC = prevPC.nextInChain;
       }

       prevPC.nextInChain = currentPC.nextInChain;
       proc.nextInChain = null;
   }
   
   /**
    * Set the SwingSafe flag on this ProcessorComponent.  If true, it is safe
    * for this ProcessComponent to make Swing method calls in the processor
    * commit method.  The default value of this flag is false, so it is not safe
    * to make Swing method calls from a ProcessorComponent commit method.
    */
   public void setSwingSafe(boolean flag) {
       swingSafe = flag;
   }
   
   /**
    * Get the current value of the SwingSafe flag
    */
   public boolean getSwingSafe() {
       return (swingSafe);
   }

   /**
    * Return the next processor component in the chain
    */
   ProcessorComponent getNextInChain() {
       return (nextInChain);
   }
   
   /**
    * Set the entity process controller
    */
   void setEntityProcessController(ProcessorManager epc) {
       entityProcessController = epc;
   }
  
   /**
    * Set the trigger conditions for this Process
    */
   public void setArmingCondition(ProcessorArmingCondition condition) {
       if (armingCondition != null && entityProcessController != null) {
           entityProcessController.disarmProcessorComponent(armingCondition);
       }
       armingCondition = condition;
       if (entityProcessController != null) {
           entityProcessController.armProcessorComponent(armingCondition);
       }
   }
   
   /**
    * gets the current arming conditions
    */
   public ProcessorArmingCondition getArmingCondition() {
       return(armingCondition);
   }
   
   /**
    * Add a condition to the list of conditions that have triggerd this process
    */
   void addTriggerCondition(ProcessorArmingCondition condition) {
       triggerCollection.addCondition(condition);
   }
   
   /**
    * Clear the current trigger condition
    */
   void clearTriggerCollection() {
       triggerCollection.clear();
   }
   
   /**
    * Get the current triggered conditions
    */
   public ProcessorArmingCollection getCurrentTriggerCollection() {
       return(triggerCollection);
   }
   
   /**
    * Set the Run in Renderer flag
    */
   public void setRunInRenderer(boolean flag) {
       runInRenderer = flag;
   }
   
   /**
    * Get the Run in Renderer flag
    */
   public boolean getRunInRenderer() {
       return (runInRenderer);
   }
   
   /**
    * Set the enable flag for this processor - the default is true.
    */
   public void setEnabled(boolean enable) {
       enabled = enable;
   }
   
   /**
    * Get whether or not this processor is enabled
    */
   public boolean isEnabled() {
       return (enabled);
   }
   
}
