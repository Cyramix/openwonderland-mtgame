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
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.event.KeyEvent;


/**
 * This is simple camera control which mimics the typical first person shooter
 * camera control
 * 
 * @author Doug Twilleager
 */
public class TransScaleProcessor extends AWTEventProcessorComponent {
    /**
     * The arming conditions for this processor
     */
    private ProcessorArmingCollection collection = null;
    
    /**
     * The increment to use
     */
    private float increment = 1.0f;
    
    /**
     * This current scale and translation
     */
    private float scale = 1.0f;
    private Vector3f trans = new Vector3f();
    
    /**
     * The Node to modify
     */
    private Node target = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The default constructor
     */
    public TransScaleProcessor(AWTInputComponent listener, WorldManager wm,
            Node target) {
        super(listener);
        this.target = target;
        worldManager = wm;
        
        collection = new ProcessorArmingCollection(this);
        collection.addCondition(new AwtEventCondition(this));
        collection.addCondition(new NewFrameCondition(this));
    }
    
    public void initialize() {
        setArmingCondition(collection);
    }
    
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = getEvents();

        for (int i=0; i<events.length; i++) {
            if (events[i] instanceof KeyEvent) {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
    }
    
    private void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_TYPED) {
            switch (ke.getKeyChar()) {
                case 'c':
                    scale -= increment;
                    break;
                case 'C':
                    scale += increment;
                    break;
                case 'x':
                    trans.x -= increment;
                    break;
                case 'X':
                    trans.x += increment;
                    break;
                case 'y':
                    trans.y -= increment;
                    break;
                case 'Y':
                    trans.y += increment;
                    break;
                case 'z':
                    trans.z -= increment;
                    break;
                case 'Z':
                    trans.z += increment;
                    break;
                case '+':
                    increment += 0.1f;
                    break;
                case '-':
                    increment -= 0.1f;
                    break;
                case 'p':
                    System.out.println("Translation: " + trans.x + "f, " + trans.y + "f, " +
                            trans.z + "f");
                    System.out.println("Scale: " + scale);
                    break;
            }
        }
    }
    
    /**
     * The commit methods
     */
    public void commit(ProcessorArmingCollection collection) {
        target.setLocalTranslation(trans);
        target.setLocalScale(scale);
        worldManager.addToUpdateList(target);
    }

}
