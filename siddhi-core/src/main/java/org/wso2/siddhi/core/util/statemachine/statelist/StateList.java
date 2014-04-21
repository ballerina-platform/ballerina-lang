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
package org.wso2.siddhi.core.util.statemachine.statelist;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StateList<T> {

    private BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    public synchronized void put(T t) throws InterruptedException {
        queue.put(t);
    }


//    public synchronized T poll() {
////        takeLock.lock();
////        try {
//        T t = queue.poll();
//        if (t == null) {
//
//            return null;
//        }
//        return t;
//
//    }
//
//
////    public synchronized T take() throws InterruptedException {
////        return linkedBlockingQueue.take();
////    }
//
//    public synchronized T peek() {
////        takeLock.lock();
////        try {
//        T t = queue.peek();
//        if (t == null) {
//            return null;
//        }
//        return t;
////        } finally {
////            takeLock.unlock();
////        }
//
//
//    }

    public Iterator<T> iterator() {
        return queue.iterator();
    }

    public Object[] currentState() {
        return new Object[]{queue};
    }

    public void restoreState(Object[] objects) {
        queue = (BlockingQueue<T>) objects[0];
    }

    public void clear() {
        queue.clear();
    }

    public void addAll(Object all) {
        queue.addAll((Collection<T>)all);
    }

    public Collection<T> getCollection(){
        return queue;
    }

    public Object getAll(){
        return queue;
    }

    @Override
    public String toString() {
        return  queue.toString() ;
    }

    public boolean isInited() {
        return false;
    }
}
