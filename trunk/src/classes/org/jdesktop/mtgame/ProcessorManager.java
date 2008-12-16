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

import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;
import java.util.ArrayList;

/**
 * This is the controller for all processor components.  It handles triggers and
 * schedules the processes on the various threads.  Processes can be scheduled
 * on the entity processors, or the render thread.
 * 
 * @author Doug Twilleager
 */
class ProcessorManager extends Thread {
    /**
     * The number of Processor Threads
     */
    private int numProcessorThreads = 0;
    
    /**
     * The number of processors on the client machine.
     */
    private int numProcessors = 1;
    
    /**
     * The array of ProcessorThreads
     */
    private ProcessorThread[] processorThreads = null;
    
    /**
     * The number of threads currently running
     */
    private int numProcessorsWorking = 0;
    
    /**
     * The list of entities that wish to be triggered on every render frame
     */
    private ArrayList newFrameArmed = new ArrayList();
    
        
    /**
     * The list of entities that wish to be triggered on awt events
     */
    private ArrayList awtEventsArmed = new ArrayList();
    
    /**
     * The list of entities that with to be triggered after an amount of 
     * time has elapsed.
     */
    private ArrayList timeElapseArmed = new ArrayList();
    
    /**
     * Two lists to keep track of post event processing
     */
    private ArrayList postEventArmed = new ArrayList();
    private ArrayList postEventListeners = new ArrayList();
    
    /**
     * The current list of triggered processors
     */    
    private ArrayList processorsTriggered = new ArrayList();
    
    /**
     * An instant snapshot of processors we are processing this frame.
     */
    private ArrayList currentProcessors = null;
    
    /**
     * The systems WorldManager
     */
    WorldManager worldManager = null;
    
    /**
     * A flag to indicate whether we should run
     */
    private boolean done = false;
    
    /**
     * A flag to say whether or not we are waiting for work
     */
    private boolean waiting = false;
    
    /**
     * The number of available processors.  
     */
    private int availableProcessors = 0;
    
    /**
     * The default constructor
     */
    ProcessorManager(WorldManager wm) {
        worldManager  = wm;
        numProcessors = Runtime.getRuntime().availableProcessors();
        
        // Just double it for now.
        numProcessorThreads = 2*numProcessors;
        
        // For initialization, all threads are running
        numProcessorsWorking = numProcessorThreads;
        
        processorThreads = new ProcessorThread[numProcessorThreads];
        for (int i=0; i<numProcessorThreads; i++) {
            processorThreads[i] = new ProcessorThread(this, i);
            processorThreads[i].initialize();
        }

    }
    
    /**
     * Initialize the process controller
     */
    synchronized void initialize() {
        this.start();
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    } 
    
    /**
     * Initialize the controller
     */
    synchronized void initController() {
        // For now, just notify the manager that we are ready
        notify();
    }
    
    /**
     * The main run loop
     */
    public void run() {
        ProcessorComponent[] runList = null;
        
        initController();
        while (!done) {
            // Gather the list of processor components to execute
            // This includes any chained processors
            runList = waitForProcessorsTriggered();

            // Hand off work until we are done with the compute phase
            for (int i=0; i<runList.length; i++) {
                // Assign the task.  This will wait for an available processor
                dispatchTask(runList[i]);

            }
            
            // Now, let the renderer complete the commit phase
            worldManager.runCommitList(runList);
            armProcessors(runList);
        } 
    }
    
    /**
     * This method hands runList work off to the worker threads unit it is done.
     */
    synchronized void dispatchTask(ProcessorComponent pc) {
        int i=0;
        
        // Wait if no one is available
        if (availableProcessors == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        
        // Find the first available thread.
        for (i = 0; i < processorThreads.length; i++) {
            // The processor will return true if it accepted the task
            //System.out.println("Trying to give task to " + i);
            //System.out.println("Processor: " + i + ", " + pc);
            if (processorThreads[i].isAvailable()) {
                processorThreads[i].setAvailable(false);
                processorThreads[i].runTask(pc);
                availableProcessors--;
                //System.out.println(entityProcessor[i] + " accepted task: " + pc);
                break;
            }
        }
    }
    
    /**
     * This simply tells us that the processor is ready for work.
     */
    synchronized void notifyDone(ProcessorThread ep) {
        ep.setAvailable(true);
        availableProcessors++;
        notify();
    }
    
    /**
     * Add a component to be potentially processed
     */
    void addComponent(EntityComponent c) {

        if (c instanceof ProcessorComponent) {
            ProcessorComponent pc = (ProcessorComponent) c;
            pc.setEntityProcessController(this);
            if (pc.getArmingCondition() != null) {
                armProcessorComponent(pc.getArmingCondition());
            }
            pc.initialize();
        }

        if (c instanceof ProcessorCollectionComponent) {
            ProcessorCollectionComponent pcc = (ProcessorCollectionComponent)c;
            ProcessorComponent[] procs = pcc.getProcessors();
            for (int i = 0; i < procs.length; i++) {
                procs[i].setEntityProcessController(this);
                if (procs[i].getArmingCondition() != null) {
                    armProcessorComponent(procs[i].getArmingCondition());
                }
                procs[i].initialize();
            }
        }
    }
    
    /**
     * Add an entity to be potentially processed
     */
    void removeComponent(EntityComponent c) {
        if (c instanceof ProcessorComponent) {
            ProcessorComponent pc = (ProcessorComponent) c;      
            if (pc.getArmingCondition() != null) {
                disarmProcessorComponent(pc.getArmingCondition());
            }
            pc.setEntityProcessController(null);
        }

        if (c instanceof ProcessorCollectionComponent) {
            ProcessorCollectionComponent pcc = (ProcessorCollectionComponent) c;
            ProcessorComponent[] procs = pcc.getProcessors();
            for (int i = 0; i < procs.length; i++) {        
                if (procs[i].getArmingCondition() != null) {
                    disarmProcessorComponent(procs[i].getArmingCondition());
                }
                procs[i].setEntityProcessController(null);
            }
        }
    }
    
    /**
     * Arm a new frame condition
     */
    void armNewFrame(NewFrameCondition condition) {
        ProcessorComponent pc = condition.getProcessorComponent();
        
        synchronized (newFrameArmed) {
            if (!newFrameArmed.contains(pc)) {
                newFrameArmed.add(pc);
            }
        }        
    }
    
    /**
     * Arm a timer expired condition
     */
    void armTimerExpired(TimerExpiredCondition condition) {
        ProcessorComponent pc = condition.getProcessorComponent();
        
        synchronized (timeElapseArmed) {
            if (!timeElapseArmed.contains(pc)) {
                timeElapseArmed.add(pc);
            }
        }      
    }
  
     
    /**
     * Arm an awt event condition
     */
    void armAwtEvent(AwtEventCondition condition) {
        boolean pendingTrigger = false;
        ProcessorComponent pc = condition.getProcessorComponent();

        synchronized (awtEventsArmed) {
            if (!awtEventsArmed.contains(pc)) {
                if (pc instanceof AWTEventProcessorComponent) {
                    AWTEventProcessorComponent apc = (AWTEventProcessorComponent) pc;
                    if (apc.eventsPending()) {
                        pendingTrigger = true;
                    }
                }
                awtEventsArmed.add(pc);
            }
        }
        if (pendingTrigger) {
            triggerAWTEvent();
        }
    }
    
        
    /**
     * Arm a post event condition
     */
    void armPostEvent(PostEventCondition condition) {
        boolean pendingTrigger = false;
        ProcessorComponent pc = condition.getProcessorComponent();
      
        synchronized (postEventArmed) {
            if (!postEventArmed.contains(pc)) {
                if (condition.eventsPending()) {
                    pendingTrigger = true;
                }
                postEventArmed.add(pc);
            }
            if (!postEventListeners.contains(condition)) {
                postEventListeners.add(condition);
            }
        }
        if (pendingTrigger) {
            //System.out.println("PENDING POST EVENT");
            triggerPostEvent();
        }
    }
        
    /**
     * Disarm a new frame condition
     */
    void disarmNewFrame(NewFrameCondition condition) {
        ProcessorComponent pc = condition.getProcessorComponent();
        
        synchronized (newFrameArmed) {
            newFrameArmed.remove(pc);
        }        
    }
    
    /**
     * Disarm a timer expired condition
     */
    void disarmTimerExpired(TimerExpiredCondition condition) {
        ProcessorComponent pc = condition.getProcessorComponent();
        
        synchronized (timeElapseArmed) {
            timeElapseArmed.remove(pc);
        }      
    }
  
     
    /**
     * Arm an awt event condition
     */
    void disarmAwtEvent(AwtEventCondition condition) {
        ProcessorComponent pc = condition.getProcessorComponent();

        synchronized (awtEventsArmed) {
            if (awtEventsArmed.contains(pc)) {
                if (pc instanceof AWTEventProcessorComponent) {
                    AWTEventProcessorComponent apc = (AWTEventProcessorComponent) pc;
                    if (apc.eventsPending()) {
                        // This clears out the events
                        apc.getEvents();
                    }
                }
                awtEventsArmed.remove(pc);
            }
        }
    }
    
        
    /**
     * Disarm a post event condition
     */
    void disarmPostEvent(PostEventCondition condition) {
        ProcessorComponent pc = condition.getProcessorComponent();
      
        synchronized (postEventArmed) {
            if (postEventArmed.contains(pc)) {
                postEventArmed.remove(pc);
            }
            if (postEventListeners.contains(condition)) {
                postEventListeners.remove(condition);
            }
            if (condition.eventsPending()) {
                condition.getTriggerEvents();
            }
        }
    }
    
    /**
     * Arm a condition
     */
    void armCondition(ProcessorArmingCondition armingCondition) {

        if (armingCondition instanceof NewFrameCondition) {
            armNewFrame((NewFrameCondition)armingCondition);
        }

        if (armingCondition instanceof TimerExpiredCondition) {
            armTimerExpired((TimerExpiredCondition) armingCondition);
        }

        if (armingCondition instanceof AwtEventCondition) {
            armAwtEvent((AwtEventCondition) armingCondition);
        }

        if (armingCondition instanceof PostEventCondition) {
            armPostEvent((PostEventCondition) armingCondition);
        }        
    }
    
    /**
     * Disarm a condition
     */
    void disarmCondition(ProcessorArmingCondition armingCondition) {

        if (armingCondition instanceof NewFrameCondition) {
            disarmNewFrame((NewFrameCondition)armingCondition);
        }

        if (armingCondition instanceof TimerExpiredCondition) {
            disarmTimerExpired((TimerExpiredCondition) armingCondition);
        }

        if (armingCondition instanceof AwtEventCondition) {
            disarmAwtEvent((AwtEventCondition) armingCondition);
        }

        if (armingCondition instanceof PostEventCondition) {
            disarmPostEvent((PostEventCondition) armingCondition);
        }        
    }
    
    /**
     * Add a processor component to the appropriate lists of possible arms
     */
    void armProcessorComponent(ProcessorArmingCondition armingCondition) {
        
        if (armingCondition instanceof ProcessorArmingCollection) {
            ProcessorArmingCollection pac = (ProcessorArmingCollection) armingCondition;
            for (int i=0; i<pac.size(); i++) {
                armProcessorComponent(pac.get(i));
            }
        } else {
            armCondition(armingCondition);
        }
    }
          
    /**
     * Add a processor component to the appropriate lists of possible arms
     */
    void disarmProcessorComponent(ProcessorArmingCondition armingCondition) {
        
        if (armingCondition instanceof ProcessorArmingCollection) {
            ProcessorArmingCollection pac = (ProcessorArmingCollection) armingCondition;
            for (int i=0; i<pac.size(); i++) {
                disarmProcessorComponent(pac.get(i));
            }
        } else {
            disarmCondition(armingCondition);
        }
    }
    
    /**
     * This method waits for processors to trigger
     */
    synchronized ProcessorComponent[] waitForProcessorsTriggered() {
        ProcessorComponent[] runList = new ProcessorComponent[0];

        if (processorsTriggered.size() == 0) {
            waiting = true;
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            waiting = false;
        }

        runList = (ProcessorComponent[]) processorsTriggered.toArray(runList);
        processorsTriggered.clear();
        
        return(runList);
    }
    
    /**
     * This re-arms processors once they are done commiting
     */
    void armProcessors(ProcessorComponent[] runList) {
        for (int i=0; i<runList.length; i++) {
            armProcessorComponent(runList[i].getArmingCondition());
        }
    }
    
    /**
     * Find the specified condition in the (possibly) collection of conditions
     */
    ProcessorArmingCondition findCondition(Class conditionClass, ProcessorArmingCondition condition) {
        ProcessorArmingCondition newCondition = null;
        
        if (condition instanceof ProcessorArmingCollection) {
            ProcessorArmingCollection pac = (ProcessorArmingCollection) condition;
            for (int i=0; i<pac.size(); i++) {
                newCondition = findCondition(conditionClass, pac.get(i));
                if (newCondition != null) {
                    return (newCondition);
                }
            }
        } else if (conditionClass.isInstance(condition)) {
            return (condition);
        }
        
        return (newCondition);
    }
    
    /**
     * This checks to see if this processor should be added to the triggered 
     * list.  If yes, then it returns true
     */
    boolean addToTriggered(ProcessorComponent pc) {
        if (pc.getRunInRenderer()) {
            worldManager.addTriggeredProcessor(pc);
        } else {
            if (!processorsTriggered.contains(pc)) {
                processorsTriggered.add(pc);
                return (true);
            }
        } 
        return (false);
    }
    
    /**
     * Trigger everyone waiting on a new frame
     */
    synchronized void triggerNewFrame() {
        int index = 0;
        ProcessorArmingCondition condition = null;
        ProcessorComponent pc = null;
        boolean anyTriggered = false;

        synchronized (newFrameArmed) {
            int length = newFrameArmed.size();
            for (int i = 0; i < length; i++) {
                pc = (ProcessorComponent) newFrameArmed.get(index);
                if (pc.isEnabled()) {
                    condition = findCondition(NewFrameCondition.class, pc.getArmingCondition());
                    pc.addTriggerCondition(condition);

                    if (addToTriggered(pc) && !anyTriggered) {
                        anyTriggered = true;
                    }
                    newFrameArmed.remove(index);
                } else {
                    // Skip over this processor   
                    index++;
                }
            }
            if (anyTriggered && waiting) {
                notify();
            }
        }
    }

    /**
     * Distribute a post event
     */
    void distributePostEvent(long event) {
        synchronized (postEventArmed) {
            // Pass out the event
            for (int i=0; i<postEventListeners.size(); i++) {
                PostEventCondition cond = (PostEventCondition) postEventListeners.get(i);
                if (cond.triggers(event)) {
                    cond.addTriggerEvent(event);
                }
            }
        }
    }
    
    /**
     * Trigger a post event
     */
    synchronized void triggerPostEvent() {
        int index = 0;
        PostEventCondition condition = null;
        ProcessorComponent pc = null;
        boolean anyTriggered = false;

        synchronized (postEventArmed) {
            int length = postEventArmed.size();
            for (int i = 0; i < length; i++) {
                pc = (ProcessorComponent) postEventArmed.get(index);               
                condition = (PostEventCondition)findCondition(PostEventCondition.class, pc.getArmingCondition());
                
                if (pc.isEnabled() && condition.eventsPending()) {
                    pc.addTriggerCondition(condition);

                    if (addToTriggered(pc) && !anyTriggered) {
                        anyTriggered = true;
                    }
                    postEventArmed.remove(index);
                } else {
                    // Just go to the next
                    index++;
                }
            }
            if (anyTriggered && waiting) {
                notify();
            }
        }
    }
    
    /**
     * Trigger everyone waiting on a new frame
     */
    synchronized void triggerAWTEvent() {
        int index = 0;
        ProcessorArmingCondition condition = null;
        AWTEventProcessorComponent apc = null;
        boolean anyTriggered = false;

        synchronized (awtEventsArmed) {
            int length = awtEventsArmed.size();
            for (int i = 0; i < length; i++) {
                apc = (AWTEventProcessorComponent) awtEventsArmed.get(index);
                if (apc.isEnabled() && apc.eventsPending()) {
                    condition = findCondition(NewFrameCondition.class, apc.getArmingCondition());
                    ((ProcessorComponent)apc).addTriggerCondition(condition);

                    if (addToTriggered(apc) && !anyTriggered) {
                        anyTriggered = true;
                    }
                    awtEventsArmed.remove(index);
                } else {
                    // Just go to the next
                    index++;
                }
            }
            if (anyTriggered && waiting) {
                notify();
            }
        }
    }
}
