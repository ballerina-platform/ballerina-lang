/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadBarrier {

    private volatile Lock lock = null;

    public void pass() {
        if (lock != null) {
            lock.lock();
            lock.unlock();
        }
    }

    public synchronized void lock() {
        if (lock == null) {
            lock = new ReentrantLock();
        }
        lock.lock();

    }

    public synchronized void unlock() {
        if (lock != null) {
            try {
            } finally {
                lock.unlock();
            }
            lock = null;
        }
    }

}
