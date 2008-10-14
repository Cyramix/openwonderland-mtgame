/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.scene;

/**
 * Anyone who wishes to be updated when geometric state changes
 * can implement this interface
 * 
 * @author Doug Twilleager
 */
public interface GeometricUpdateListener {
    public void geometricDataChanged(Spatial spatial);
}
