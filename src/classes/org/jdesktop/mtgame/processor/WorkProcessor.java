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

package org.jdesktop.mtgame.processor;

import org.jdesktop.mtgame.*;

/**
 * This processor listens for post events
 * 
 * @author Doug Twilleager
 */
public class WorkProcessor extends ProcessorComponent {
    public interface WorkDoneListener {
        public void workDone(Object o);
    }

    /**
     * The world manager
     */
    private WorldManager worldManager = null;

    /**
     * The arming condition for this processor
     */
    PostEventCondition condition = null;
    
    /**
     * A name
     */
    private String name = null;
    
    /**
     * The list of events which this processor is waiting for
     */
    private long[] event = new long[1];

    /**
     * A boolean indicating that the work is done
     */
    private boolean done = false;

    /**
     * The listener to use when done doing work
     */
    private WorkDoneListener listener = null;

    /**
     * The argument to doing work
     */
    private Object arg = null;
    
    /**
     * The constructor
     */
    public WorkProcessor(String name, WorldManager wm) {
        worldManager = wm;
        event[0] = worldManager.allocateEvent();
        this.name = name;
        condition = new PostEventCondition(this, event);
    }

    /**
     * Tell the WorkProcess to do some work
     */
    public void startWork(WorkDoneListener l, Object arg, boolean wait) {
        listener = l;
        this.arg = arg;
        done = false;
        worldManager.postEvent(event[0]);
        if (wait) {
            while (!done) {
                try {
                    Thread.currentThread().sleep(10);
                } catch (java.lang.InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
    }

    public String toString() {
        return (name);
    }
    
    /**
     * The initialize method
     */
    public void initialize() {
        setArmingCondition(condition);
    }
    
    /**
     * The Calculate method
     */
    public void compute(ProcessorArmingCollection collection) {
        PostEventCondition pec = (PostEventCondition)collection.get(0);
        long[] e = pec.getTriggerEvents();

        doWork(arg);
        listener.workDone(arg);
        done = true;
    }

    /**
     * Subclassed will implement this method to do the work
     */
    public void doWork(Object arg) {
        System.out.println("Doing Work....");
    }

    /**
     * The commit method
     */
    public void commit(ProcessorArmingCollection collection) {
    }
}
