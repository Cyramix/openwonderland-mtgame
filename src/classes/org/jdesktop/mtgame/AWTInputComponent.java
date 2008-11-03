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

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.event.AWTEventListener;
import java.util.ArrayList;

/**
 * This component is used to receive AWT events from the input manager
 * It simply buffers the events, and then consumes them in the Entities
 * processor @see AWTEventProcessor
 * 
 * @author Doug Twilleager
 */
public class AWTInputComponent extends InputComponent implements AWTEventListener {
    /**
     * The buffered events
     */
    private ArrayList events = new ArrayList();
    
    /**
     * The event mask that this component is interested in.
     */
    private int eventMask = 0;
    
    /**
     * The Canvas that generates the events
     */
    private Canvas canvas = null;
    
    AWTInputComponent(Canvas c, int mask) {
        eventMask = mask;
        canvas = c;
    }
    
    /**
     * Get the event mask
     */
    int getEventMask() {
        return (eventMask);
    }
    
    /**
     * Get the event mask
     */
    Canvas getCanvas() {
        return (canvas);
    }
    
    /**
     * This method gets called when the input manager sends an event to us
     * @param e
     */
    public void eventDispatched(AWTEvent e) {
        synchronized (events) {
            // TODO: should this be a copy?
            events.add(e);
        }
    }
    
    /**
     * This returns our currently queued events to the caller
     * @return
     */
    public Object[] getEvents() {
        Object[] ret = null;
        
        synchronized (events) {
            ret = events.toArray();
            events.clear();
        }  
        return (ret);
    }
    
    /**
     * A boolean that says whether or not there are events pending
     */
    public boolean eventsPending() {
        return (!events.isEmpty());
    }
}
