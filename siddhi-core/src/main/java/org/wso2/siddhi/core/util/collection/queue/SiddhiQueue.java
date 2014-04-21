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
package org.wso2.siddhi.core.util.collection.queue;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SiddhiQueue<T> implements ISiddhiQueue<T> {
    private BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    public synchronized void put(T t) {
        queue.add(t);
    }

    public synchronized T poll() {
        return queue.poll();
    }

    public synchronized T peek() {
        return queue.peek();
    }

    public Iterator<T> iterator() {
        return queue.iterator();
    }

    public Object[] currentState() {
        return new Object[]{queue};
    }

    public void restoreState(Object[] objects) {
        queue = (LinkedBlockingQueue) objects[0];
    }

    public int size() {
        return queue.size();
    }
}
