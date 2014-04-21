/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.util.collection.queue.scheduler;

import org.wso2.siddhi.core.util.collection.queue.SiddhiQueue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerSiddhiQueue<T> extends SiddhiQueue<T> implements ISchedulerSiddhiQueue<T> {
    protected volatile AtomicBoolean isScheduledForDispatching = new AtomicBoolean(false);
    protected final SchedulerElement schedulerElement;

    public SchedulerSiddhiQueue(SchedulerElement schedulerElement) {
        this.schedulerElement = schedulerElement;
    }

    @Override
    public synchronized void put(T t) {
        super.put(t);
        if (isScheduledForDispatching.compareAndSet(false, true)) {
            this.schedulerElement.schedule();
        }
    }


    public synchronized T poll() {
        T t = super.poll();
        if (t == null) {
            isScheduledForDispatching.set(false);
            return null;
        } else {
            return t;
        }
    }

    public synchronized T peek() {
        T t = super.peek();
        if (t == null) {
            isScheduledForDispatching.set(false);
            return null;
        } else {
            return t;
        }
    }

//    public synchronized T peek() {
//        T t = queue.peek();
//        if (t == null) {
//            isScheduledForDispatching.set(false);
//            t = queue.peek();
//            if (t == null) {
//                return null;
//            } else {
//                isScheduledForDispatching.set(true);
//                return t;
//            }
//        }
//        return t;
//
//    }

    public Iterator<T> iterator() {
        return super.iterator();
    }


    public void reSchedule() {
        if (schedulerElement != null) {
            this.schedulerElement.scheduleNow();
        }
    }

    public void schedule() {
        if (schedulerElement != null) {
            this.schedulerElement.schedule();
        }
    }
}
