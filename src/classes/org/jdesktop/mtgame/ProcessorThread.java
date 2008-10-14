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

import java.util.LinkedList;

/**
 * This class represents a thread for processing entities.  The ProcessorManager
 * places entities on this processors queue to be scheduled.
 * 
 * @author Doug Twilleager
 */
class ProcessorThread extends Thread {
    /**
     * The processor number
     */
    private int processorNumber = -1;
    
    /**
     * A boolean telling when us when to quit
     */
    private boolean done = false;
    
    /**
     * This flag indicates whether or not we are waiting for a task
     */
    private boolean waiting = true;
    
    /**
     * The Queue of Processor Components to be run.
     */
    private LinkedList queue = new LinkedList();
    
    /**
     * A flag indicating that we are available
     */
    private boolean available = false;
    
    /**
     * The name for this processor
     */
    private String name = null;
    
    /**
     * A reference back to the ProcessorManager
     */
    private ProcessorManager processorManager = null;
    
    /**
     * The default constructor
     */
    ProcessorThread(ProcessorManager pm, int procNumber) {
        processorManager = pm;
        processorNumber = procNumber;
        name = "Processor " + procNumber;
    }
    
    /**
     * This starts the processor, and waits to be notified when the
     * thread is ready.
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
     * Initialize the processor
     */
    private synchronized void initProcessor() {
        // For now, just notify the controller that we are ready
        available = true;
        notify();
    }
    
    public void run() {
        ProcessorComponent pc = null;
        
        initProcessor();
        while (!done) {            
            // This synchonized method will block until there's something to do.
            pc = getNextProcessorComponent();
            
            // Now compute this process and all of it's chains.
            pc.compute(pc.getCurrentTriggerCollection());
            
            pc = pc.getNextInChain();
            while (pc != null) {
                pc.compute(pc.getCurrentTriggerCollection());
                pc = pc.getNextInChain();
            }
        }
    }
    
    /**
     * This method places a task on the processesor queue - if the processor
     * is waiting
     */
    synchronized boolean runTask(ProcessorComponent pc) {
        if (waiting) {
            //System.out.println("Processor " + processorNumber + " grabbing task: " + pc);
            queue.add(pc);
            waiting = false;
            notify();
            return (true);
        } else {
            return (false);
        }
    }
    
    boolean isAvailable() {
        return(available);
    }
    
    void setAvailable(boolean flag) {
        available = flag;
    }
    
        
    public String toString() {
        return (name);
    }
    
    /** 
     * 
     * @return
     */
    private synchronized ProcessorComponent getNextProcessorComponent() {
        ProcessorComponent pc = null;
           
        if (queue.isEmpty()) {
            waiting = true;
            processorManager.notifyDone(this);
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }         
        pc = (ProcessorComponent) queue.removeFirst();
        return (pc);
    } 

}
