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

/**
* Allocates a small integer which is different from all the other small integers which have been allocated 
* in this program run. The numbers start from 0. Numbers should be freed when they are no longer in use.
*
* @author deronj
*/
class SmallIntegerAllocator {

   /** The amount to grow the list by when the list is exhausted */
   protected static final int INCREMENT = 10;

   /** An array which indicates which integers have been allocated */
   protected boolean[] allocated = new boolean[INCREMENT];

   /** The number of integers currently allocated */
   protected int numAllocated;

   /** 
    * Construct a new instance of SmallIntegerAllocator, with no integers preallocated.
    */
   SmallIntegerAllocator () {}

   /** 
    * Construct a new instance of SmallIntegerAllocator, with the given number of integers preallocated.
    */
   SmallIntegerAllocator (int numPreAlloc) {
	for (int i = 0; i < numPreAlloc; i++) {
	    allocate();
	}
   }

   /**
    * Allocate a small integer which is unique in this program run.
    *
    * @return The integer.
    */
   int allocate () {
	int i = findInList();
	if (i >= 0) {
	    numAllocated++;
	    return i;
	}

	int prevLength = growList();
	i = findInList(prevLength);
	if (i < 0) {
	    throw new RuntimeException("Internal error: cannot allocate even after growing the list");
	}
	    
	numAllocated++;
	return i;
   }

   /**
    * Release an allocated integer
    *
    * @param i The value to release.
    */
   void free (int i) {
	if (!allocated[i]) {
	    throw new RuntimeException("Value is not allocated.");
	}
	allocated[i] = false;
	numAllocated--;
   }

   /**
    * The number of integers currently allocated.
    */
   int getNumAllocated () {
	return numAllocated;
   }

   /** 
    * Find an unallocated integer and allocate it.
    * Start the search from 0.
    */
   protected int findInList () {
	return findInList(0);
   }

   /** 
    * Find an unallocated integer and allocate it.
    * Start the search from the given index.
    */
   protected int findInList (int startIdx) {
	for (int i = startIdx; i < allocated.length; i++) {
	    if (!allocated[i]) {
		allocated[i] = true;
		return i;
	    }
	}
	return -1;
   }

   /**
    * All integers in our current array have been allocated and we must grow the array to contain new 
    * unallocated ones.
    *
    * @return The length of the arary *BEFORE* this routine was called.
    */
   protected int growList () {
	int prevLength = allocated.length;

	boolean[] allocatedOld = allocated;
	allocated = new boolean[prevLength + INCREMENT];
	    
	// Copy old contents into new list
	System.arraycopy(allocatedOld, 0, allocated, 0, prevLength);

	return prevLength;
   }
}
