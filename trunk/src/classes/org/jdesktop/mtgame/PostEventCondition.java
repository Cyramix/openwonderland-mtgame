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

import java.util.ArrayList;

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
    private ArrayList triggerEvents = new ArrayList();

    /**
     * The list of frozen events.
     */
    private ArrayList frozenEvents = new ArrayList();
            
    /**
     * The default constructor
     */
    public PostEventCondition(ProcessorComponent pc, long[] events) {
        super(pc);
        armEvents = events;
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
     * Returns whether or not there are any post events pending
     */
    boolean eventsPending() {
        boolean pending = false;
        synchronized (triggerEvents) {
            if (triggerEvents.size() > 0) {
                pending = true;
            }
        }
        return (pending);
    }
    
    /**
     * Add the event to the trigger event
     */
    void addTriggerEvent(long event) {
        synchronized (triggerEvents) {
            triggerEvents.add(new Long(event));
        }
    }
    
    /**
     * Return the events which triggered this condition
     */
    public long[] getTriggerEvents() {
        long ret[] = null;
        
        synchronized (triggerEvents) {
            int length = frozenEvents.size();
            ret = new long[length];

            for (int i = 0; i < length; i++) {
                Long l = (Long)frozenEvents.get(i);
                ret[i] = l.longValue();
            }
        }
        
        return (ret);
    }

    /**
     * Freeze the events - copy trigger events into the freeze events
     * and empty the trigger events
     */
    void freezeEvents() {
        synchronized (triggerEvents) {
            for (int i=0; i<triggerEvents.size(); i++) {
                frozenEvents.add(triggerEvents.get(i));
            }
            triggerEvents.clear();
        }
    }

    /**
     * Unfreeze the events.  This clears the frozen events
     */
    void unfreezeEvents() {
        synchronized (triggerEvents) {
            frozenEvents.clear();
        }
    }
}
