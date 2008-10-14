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
 * This condition listens for posting of user events
 * 
 * @author Doug Twilleager
 */
public class PostEventCondition extends ProcessorArmingCondition {
    /**
     * The events that this condition is waiting for
     */
    private long[] armEvents = null;
    
    /**
     * The list of triggering events.
     */
    private long[] triggerEvents = null;
    
    /**
     * The number of trigger events
     */
    private int numTriggerEvents = 0;
            
    /**
     * The default constructor
     */
    public PostEventCondition(ProcessorComponent pc, long[] events) {
        super(pc);
        armEvents = events;
        triggerEvents = new long[events.length];
    }
    
    /**
     * This returns whether or not this condition is triggered by the
     * given event.
     */
    boolean triggers(long event) {
        for (int i=0; i<armEvents.length; i++) {
            if (armEvents[i] == event) {
                return (true);
            }
        }
        return (false);
    }
    
    /**
     * Add the event to the trigger event
     */
    void addTriggerEvent(long event) {
        synchronized (triggerEvents) {
            for (int i = 0; i < numTriggerEvents; i++) {
                if (triggerEvents[i] == event) {
                    // The event is already on the list
                    return;
                }
            }

            // Add the event to the list
            triggerEvents[numTriggerEvents++] = event;
        }
    }
    
    /**
     * Return the events which triggered this condition
     */
    public long[] getTriggerEvents() {
        long ret[] = null;
        
        synchronized (triggerEvents) {
            ret = new long[numTriggerEvents];

            for (int i = 0; i < numTriggerEvents; i++) {
                ret[i] = triggerEvents[i];
            }
        }
        
        return (ret);
    }
    
    /**
     * Clear the trigger events
     */
    void clearTriggerEvents() {
        synchronized (triggerEvents) {
            numTriggerEvents = 0;
        }
    }
}
