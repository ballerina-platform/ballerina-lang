/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.values.BError;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class stores {@link WorkerChannel} reference to unique channel key.
 *
 * @since 2201.11.0
 */

public class WorkerChannelMap {

    private final Map<String, WorkerChannel> channelMap = new HashMap<>();
    private final ReentrantReadWriteLock channelMapLock = new ReentrantReadWriteLock();

    public void addChannelKeys(String[] channelKeys) {
        try {
            channelMapLock.writeLock().lock();
            for (String channelKey : channelKeys) {
                WorkerChannel workerChannel = channelMap.get(channelKey);
                if (workerChannel == null) {
                    workerChannel = new WorkerChannel(channelKey);
                    channelMap.put(channelKey, workerChannel);
                }
            }
        } finally {
            channelMapLock.writeLock().unlock();
        }
    }

    public WorkerChannel get(String channelKey) {
        try {
            channelMapLock.readLock().lock();
            return channelMap.get(channelKey);
        } finally {
            channelMapLock.readLock().unlock();
        }
    }

    public void panicSendWorkerChannels(String channelKey, BError error) {
        // System.out.println("panic send" +  channelKey + Scheduler.getStrand().getName());
        try {
            channelMapLock.writeLock().lock();
            WorkerChannel workerChannel = channelMap.get(channelKey);
            workerChannel.panicOnSend(error);
            if (workerChannel.done()) {
                channelMap.remove(channelKey);
            }
        } finally {
            channelMapLock.writeLock().unlock();
        }
    }

    public void panicReceiveWorkerChannels(String channelKey, BError error) {
        // System.out.println("panic receive" +  channelKey + Scheduler.getStrand().getName());
        try {
            channelMapLock.writeLock().lock();
            WorkerChannel workerChannel = channelMap.get(channelKey);
            workerChannel.panicOnReceive(error);
            if (workerChannel.done()) {
                channelMap.remove(channelKey);
            }
        } finally {
            channelMapLock.writeLock().unlock();
        }
    }

    public void completeSendWorkerChannels(String channelKey, Object returnValue) {
        // System.out.println("Remove send" +  channelKey + Scheduler.getStrand().getName());
        try {
            channelMapLock.writeLock().lock();
            WorkerChannel workerChannel = channelMap.get(channelKey);
            workerChannel.errorOnSend(channelKey, returnValue);
            if (workerChannel.done()) {
                channelMap.remove(channelKey);
            }
        } finally {
            channelMapLock.writeLock().unlock();
        }
    }

    public void completeReceiveWorkerChannels(String channelKey, Object returnValue) {
        // System.out.println("Remove receive" +  channelKey + Scheduler.getStrand().getName());
        try {
            channelMapLock.writeLock().lock();
            WorkerChannel workerChannel = channelMap.get(channelKey);
            workerChannel.errorOnReceive(channelKey, returnValue);
            if (workerChannel.done()) {
                channelMap.remove(channelKey);
            }
        } finally {
            channelMapLock.writeLock().unlock();
        }
    }
}
