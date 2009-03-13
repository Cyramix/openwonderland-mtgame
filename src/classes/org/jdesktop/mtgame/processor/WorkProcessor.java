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

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.*;

/**
 * This processor listens for post events
 * 
 * @author Doug Twilleager
 */
public class WorkProcessor extends ProcessorComponent {

    /**
     * The world manager
     */
    private WorldManager worldManager = null;
    /**
     * The arming condition for this processor
     */
    PostEventCondition condition = null;
    /**
     * A name
     */
    private String name = null;
    /**
     * The list of events which this processor is waiting for
     */
    private LinkedList<WorkRecord> workCommitList = new LinkedList();
    private LinkedList<WorkRecord> workComputeList = new LinkedList();
    private long postCommit;
    private long postCompute;

    /**
     * The constructor
     */
    public WorkProcessor(String name, WorldManager wm) {
        worldManager = wm;
        postCommit = worldManager.allocateEvent();
        postCompute = worldManager.allocateEvent();
        this.name = name;
        condition = new PostEventCondition(this, new long[] {postCommit, postCompute});
    }

    @Override
    public void compute(ProcessorArmingCollection arg0) {
        synchronized (this) {
            for (WorkRecord job : workComputeList) {
                ((WorkCompute) job.worker).compute();
                if (job.listener != null) {
                    job.listener.workDone(job.worker);
                }
            }
            workComputeList.clear();
        }
    }

    @Override
    public void commit(ProcessorArmingCollection arg0) {
        // Clear the triggering events

        synchronized (this) {
            if (arg0.size() != 0) {
                PostEventCondition pec = (PostEventCondition) arg0.get(0);
            }

            for (WorkRecord job : workCommitList) {
                ((WorkCommit) job.worker).commit();
                if (job.listener != null) {
                    job.listener.workDone(job.worker);
                }
            }
            workCommitList.clear();
        }
    }

    @Override
    public void initialize() {
        setArmingCondition(condition);
    }

    public void addWorker(final WorkCommit worker, boolean wait) {
        final Semaphore semaphore = new Semaphore(0);

        if (wait) {
            addWorker(worker, new WorkDoneListener() {
                public void workDone(Object o) {
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(WorkProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            addWorker(worker);
        }
    }

    public void addWorker(WorkCommit worker) {
        addWorker(worker, null);
    }

    public void addWorker(WorkCommit worker, WorkDoneListener listener) {
        synchronized (this) {
            workCommitList.add(new WorkRecord(worker, listener));
            worldManager.postEvent(postCommit);
        }
    }

    public void addWorker(final WorkCompute worker, boolean wait) {
        final Semaphore semaphore = new Semaphore(0);

        if (wait) {
            addWorker(worker, new WorkDoneListener() {
                public void workDone(Object o) {
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(WorkProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            addWorker(worker);
        }
    }

    public void addWorker(WorkCompute worker) {
        addWorker(worker, null);
    }

    public void addWorker(WorkCompute worker, WorkDoneListener listener) {
        synchronized (this) {
            workComputeList.add(new WorkRecord(worker, listener));
            worldManager.postEvent(postCompute);
        }
    }

    class WorkRecord {

        public Object worker;
        public WorkDoneListener listener;

        public WorkRecord(Object worker, WorkDoneListener listener) {
            this.listener = listener;
            this.worker = worker;
        }
    }

    public interface WorkCommit {

        public void commit();
    }

    public interface WorkCompute {

        public void compute();
    }

    public interface WorkDoneListener {
        public void workDone(Object o);
    }

}
