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

import com.jme.math.Ray;

/**
 * This is the object returned by a picking query.  It holds the query information
 * as well as a linked list of the results.
 * 
 * @author Doug Twilleager
 */
public class JMEPickInfo extends PickInfo {
    /**
     * The ray used to pick - in eye space
     */
    private Ray eyeRay = null;
    
    /**
     * The ray used to pick - in world space
     */
    private Ray worldRay = null;
    
    /**
     * The default constructor
     */
    JMEPickInfo(boolean geomPick, boolean interpolateData, Ray worldRay) {
        super(geomPick, interpolateData);

        this.worldRay = worldRay;
    }

    /**
     * Get the eye ray
     */
    public Ray getEyeRay() {
        return (eyeRay);
    }
    
    /**
     * Get the world ray
     */
    public Ray getWorldRay() {
        return (worldRay);
    }
    
    /**
     * Set the eye ray
     */
    void setEyeRay(Ray eyeRay) {
        this.eyeRay = eyeRay;
    }
}
