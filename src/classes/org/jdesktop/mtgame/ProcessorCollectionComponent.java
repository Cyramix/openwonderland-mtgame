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
 * This entity component consists of a collection of processor componants
 * 
 * @author Doug Twilleager
 */
public class ProcessorCollectionComponent extends EntityComponent {
    /**
     * The list of ProcessorComponent's
     */
    private ArrayList processors = new ArrayList();
    
    /**
     * The default constructor
     */
    public ProcessorCollectionComponent() {
    }
    
    /**
     * Add a processor
     */
    public void addProcessor(ProcessorComponent pc) {
        processors.add(pc);
        Entity e = getEntity();
        if (e != null) {
            WorldManager wm = e.getWorldManager();
            if (wm != null) {
                wm.getProcessorManager().addComponent(pc);
            }
        }
    }
    
    /**
     * Get the processors
     * @return
     */
    public ProcessorComponent[] getProcessors() {
        ProcessorComponent[] procs = new ProcessorComponent[0];
        
        procs = (ProcessorComponent[]) processors.toArray(procs);
        return(procs);
    }
    
    /**
     * Remove a processor from the collection
     */
    public void removeProcessor(ProcessorComponent pc) {
        Entity e = getEntity();
        if (e != null) {
            WorldManager wm = e.getWorldManager();
            if (wm != null) {
                wm.getProcessorManager().removeComponent(pc);
            }
        }
        processors.remove(pc);
    }
    
    /**
     * Remove all processors from this collection
     */
    public void removeAllProcessors() {
        int len = processors.size();
        
        for (int i=0; i<len; i++) {
            // This relies on the fast that removeProcessor shrinks
            // the processor array list
            removeProcessor((ProcessorComponent)processors.get(0));
        }
    }
}
