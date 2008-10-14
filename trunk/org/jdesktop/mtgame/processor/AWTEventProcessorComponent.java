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
 * This processor takes the given listener, and consumes it's events
 * when this processor is triggered.
 * 
 * @author Doug Twilleager
 */
public class AWTEventProcessorComponent extends ProcessorComponent {
    /**
     * The listener to grap the events from
     */
    private AWTInputComponent eventListener = null;
    
    /**
     * The default constructor
     */
    public AWTEventProcessorComponent(AWTInputComponent listener) {
        eventListener = listener;
    }
    
    /**
     * Arm for AWT Events
     */
    public void initialize() {
        setArmingCondition(new AwtEventCondition(this));
    }
    
    /**
     * Just print out the events
     * @param conditions
     */
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = eventListener.getEvents();
        
        if (events.length == 0) {
            System.out.println("No Events!!!!!");
        }
    }
    
    public void commit(ProcessorArmingCollection collection) {        
    }  
        
    /**
     * A boolean that says whether or not there are events pending
     */
    public boolean eventsPending() {
        return (eventListener.eventsPending());
    }  
        
    /**
     * This returns our currently queued events to the caller
     * @return
     */
    public Object[] getEvents() {
        return (eventListener.getEvents());
    }

}
