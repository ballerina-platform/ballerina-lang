/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.stream.input;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.Event;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 11/28/14.
 */
public class SingleThreadEntryValve implements InputProcessor {

    private Lock lock;
    private InputProcessor inputProcessor;


    public SingleThreadEntryValve(ExecutionPlanContext executionPlanContext, InputProcessor inputProcessor) {
        this.lock = executionPlanContext.getSharedLock();
        if (lock == null) {
            lock = new ReentrantLock();
        }
        this.inputProcessor = inputProcessor;
    }

    @Override
    public void send(Event event, int streamIndex) {
        lock.lock();
        try {
            inputProcessor.send(event, streamIndex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void send(Event[] events, int streamIndex) {
        lock.lock();
        try {
            inputProcessor.send(events, streamIndex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void send(long timeStamp, Object[] data, int streamIndex) {
        lock.lock();
        try {
            inputProcessor.send(timeStamp, data, streamIndex);
        } finally {
            lock.unlock();
        }
    }
}
