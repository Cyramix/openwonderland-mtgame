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
import java.awt.event.*;
import java.awt.AWTEvent;
import java.awt.Canvas;

/**
 * This is the default input manager.  It listens to mouse and 
 * keyboard events via jME.  It currently routes events to all
 * Entities listening.
 * 
 * @author Doug Twilleager
 */
class AWTInputManager extends InputManager implements KeyListener, 
        MouseListener, MouseMotionListener, MouseWheelListener {
    /**
     * The list of all InputComponents
     */
    private ArrayList inputComponents = new ArrayList();
    
    /**
     * The list of entities interested in key events
     */
    private ArrayList keyListeners = new ArrayList();
    
    /**
     * The list of entities interested in mouse events
     */
    private ArrayList mouseListeners = new ArrayList();

    /**
     * A state variable tracking whether or not we are listening for mouse events
     */
    private boolean mouseListening = false;
    
    /**
     * A state variable tracking whether or not we are listening for key events
     */
    private boolean keyListening = false;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    /**
     * The default constructor
     */
    AWTInputManager () {
    }
    
    /**
     * The initialize method
     */
    void initialize(WorldManager wm) {
        worldManager = wm;
    }
    
    /**
     * Create an AWTInputComponent.
     */
    public InputComponent createInputComponent(Canvas c, int events) {
        AWTInputComponent ic = new AWTInputComponent(c, events);
        if ((events & KEY_EVENTS) != 0) {
            addAWTKeyListener(c, ic);
        }
        if ((events & MOUSE_EVENTS) != 0) {
            addAWTMouseListener(c, ic);
        }
        return (ic);
    }
    
        
    /**
     * Create an AWTInputComponent.
     */
    public void removeInputComponent(InputComponent ic) {
        AWTInputComponent aic = (AWTInputComponent)ic;
        int events = aic.getEventMask();
        if ((events & KEY_EVENTS) != 0) {
            removeAWTKeyListener(aic.getCanvas(), aic);
        }
        if ((events & MOUSE_EVENTS) != 0) {
            removeAWTMouseListener(aic.getCanvas(), aic);
        }
    }
    
    /**
     * This method adds an entity to the list of those tracking key events
     * @param e The interested entity
     */
    void addAWTKeyListener(Canvas c, AWTEventListener listener) {
        synchronized (keyListeners) {
            keyListeners.add(listener);
            if (!keyListening) {
                worldManager.trackKeyInput(c, this);
                keyListening = true;
            }
        }
    }

    /**
     * This method removes an entity from the list of those tracking key events
     * @param e The uinterested entity
     */    
    void removeAWTKeyListener(Canvas c, AWTEventListener listener) {
        synchronized (keyListeners) {
            keyListeners.remove(listener);
            if (keyListeners.size() == 0) {
                worldManager.untrackKeyInput(c, this);
                keyListening = false;
            }
        }
    }
    
    /**
     * This method adds an entity to the list of those tracking mouse events
     * @param e The interested entity
     */    
    void addAWTMouseListener(Canvas c, AWTEventListener listener) {
        synchronized (mouseListeners) {
            mouseListeners.add(listener);
            if (!mouseListening) {
                worldManager.trackMouseInput(c, this);
                mouseListening = true;
            }
        }
    }

    /**
     * This method removes an entity from the list of those tracking mouse events
     * @param e The uinterested entity
     */        
    void removeAWTMouseListener(Canvas c, AWTEventListener listener) {
        synchronized (mouseListeners) {
            mouseListeners.remove(listener);
            if (mouseListeners.size() == 0) {
                worldManager.untrackMouseInput(c, this);
                mouseListening = false;
            }
        }
    }

    /**
     * An internal method to make dispatching easier
     * @param e
     */
    private void dispatchKeyEvent(AWTEvent e) {
        AWTEventListener l = null;
        
        synchronized (keyListeners) {
            for (int i=0; i<keyListeners.size(); i++) {
                l = (AWTEventListener) keyListeners.get(i);
                l.eventDispatched(e);
            }
        }
        worldManager.triggerAWTEvent();        
    }
    
    
    /**
     * An internal method to make dispatching easier
     * @param e
     */
    private void dispatchMouseEvent(AWTEvent e) {
        AWTEventListener l = null;
        
        synchronized (mouseListeners) {
            for (int i=0; i<mouseListeners.size(); i++) {
                l = (AWTEventListener) mouseListeners.get(i);
                l.eventDispatched(e);
            }
        }
        worldManager.triggerAWTEvent();        
    }
    
    /**
     * The methods used by AWT to notify us of mouse events
     */
    public void keyPressed(KeyEvent e) {
        dispatchKeyEvent(e);
        //System.out.println("keyPressed: " + e);
    }

    public void keyReleased(KeyEvent e) {
        dispatchKeyEvent(e);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.out.println("USING THE ESCAPE HATCH!");
            System.exit(0);
        }
        //System.out.println("keyReleased: " + e);
    }

    
    public void keyTyped(KeyEvent e) {
        dispatchKeyEvent(e);
        //System.out.println("keyTyped: " + e);
    }

    public void mouseClicked(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseClicked: " + e);
    }

    public void mouseEntered(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseEntered: " + e);
    }

    public void mouseExited(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseExited: " + e);
    }

    public void mousePressed(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mousePressed: " + e);
    }

    public void mouseReleased(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseReleased: " + e);
    }

    public void mouseDragged(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseDragged: " + e);
    }

    public void mouseMoved(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseMoved: " + e);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseWheelMoved: " + e);
    }
}
