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

import com.jme.scene.Geometry;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.math.Matrix3f;

/**
 * This class contains all attributes for a geometry - as specified by 
 * the config files
 * 
 * @author Doug Twilleager
 */
public class Portal {
    /**
     * The geometry of the Portal
     */
    private Geometry geometry = null;
    
    /**
     * The Entry coordinate system of the portal
     */
    private Vector3f enterLoc = new Vector3f();
    private Vector3f enterDir = new Vector3f();
    private Vector3f enterUp = new Vector3f();
    private Vector3f enterLeft = new Vector3f();

    /**
     * The Exit coordinate system of the portal
     */
    private Vector3f exitLoc = new Vector3f();
    private Vector3f exitDir = new Vector3f();
    private Vector3f exitUp = new Vector3f();
    private Vector3f exitLeft = new Vector3f();

    /**
     * The transform from enter to exit coordinates
     */
    private Quaternion enterExitRotation = new Quaternion();
    private Vector3f enterExitTranslation = new Vector3f();
    private Matrix3f rotation = new Matrix3f();

    /**
     * The Sector this portal leads to
     */
    private Sector nextSector = null;

    /**
     * The Sector this portal is in
     */
    private Sector mySector = null;

    /**
     * The default constructor
     */
    public Portal(Geometry geo, Vector3f enterLoc, Vector3f enterDir, Vector3f enterUp,
                  Vector3f enterLeft, Vector3f exitLoc, Vector3f exitDir, Vector3f exitUp,
                  Vector3f exitLeft, Sector mySector, Sector nextSector) {
        geometry = geo;
        this.enterLoc.set(enterLoc);
        this.enterDir.set(enterDir);
        this.enterUp.set(enterUp);
        this.enterLeft.set(enterLeft);
        this.exitLoc.set(exitLoc);
        this.exitDir.set(exitDir);
        this.exitUp.set(exitUp);
        this.exitLeft.set(exitLeft);
        this.mySector = mySector;
        this.nextSector = nextSector;
        updateEnterExitTransform();
    }

    public Vector3f getEnterLocation() {
        return (enterLoc);
    }

    public Vector3f getExitLocation() {
        return (exitLoc);
    }

    public void getEnterCoordinate(Vector3f loc, Vector3f dir, Vector3f up, Vector3f left) {
        if (loc != null) {
            loc.set(enterLoc);
        }
        if (dir != null) {
            dir.set(enterDir);
        }
        if (up != null) {
            up.set(enterUp);
        }
        if (left != null) {
            left.set(enterLeft);
        }
    }

    public void setEnterCoordinate(Vector3f loc, Vector3f dir, Vector3f up, Vector3f left) {
        if (loc != null) {
            enterLoc.set(loc);
        }
        if (dir != null) {
            enterDir.set(dir);
        }
        if (up != null) {
            enterUp.set(up);
        }
        if (left != null) {
            enterLeft.set(left);
        }
        updateEnterExitTransform();
    }

    public void getExitCoordinate(Vector3f loc, Vector3f dir, Vector3f up, Vector3f left) {
        if (loc != null) {
            loc.set(exitLoc);
        }
        if (dir != null) {
            dir.set(exitDir);
        }
        if (up != null) {
            up.set(exitUp);
        }
        if (left != null) {
            left.set(exitLeft);
        }
    }

    public void setExitCoordinate(Vector3f loc, Vector3f dir, Vector3f up, Vector3f left) {
        if (loc != null) {
            exitLoc.set(loc);
        }
        if (dir != null) {
            exitDir.set(dir);
        }
        if (up != null) {
            exitUp.set(up);
        }
        if (left != null) {
            exitLeft.set(left);
        }
        updateEnterExitTransform();
    }

    public void updateEnterExitTransform() {
        Matrix3f mEnter = new Matrix3f();
        Matrix3f mExit = new Matrix3f();
        mEnter.fromAxes(enterLeft, enterUp, enterDir);
        mExit.fromAxes(exitLeft, exitUp, exitDir);

        mEnter.invertLocal();
        mExit.multLocal(mEnter);
        rotation.copy(mExit);
        enterExitRotation.fromRotationMatrix(mExit);
        enterExitTranslation.set(exitLoc.x - enterLoc.x, exitLoc.y - enterLoc.y, exitLoc.z - enterLoc.z);
    }

//    public Quaternion getEnterExitRotation() {
//        return (enterExitRotation);
//    }

    public Matrix3f getEnterExitRotation() {
        return (rotation);
    }

    public Vector3f getEnterExitTranslation() {
        return (enterExitTranslation);
    }

    public Sector getMySector() {
        return mySector;
    }

    public void setMySector(Sector mySector) {
        this.mySector = mySector;
    }

    public Sector getNextSector() {
        return nextSector;
    }

    public void setNextSector(Sector nextSector) {
        this.nextSector = nextSector;
    }
    
    /**
     * get the geometry name
     */
    public Geometry getGeometry() {
        return (geometry);
    }
}
