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

import io.ballerina.runtime.internal.ErrorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class stores {@link WorkerChannel} reference to unique channel key.
 *
 * @since 2201.11.0
 */

public class WorkerChannelMap {

    private final Map<String, WorkerChannel> channelMap = new HashMap<>();
    private final Lock channelMapLock = new ReentrantLock();

    public WorkerChannel get(String channelKey) {
        try {
            channelMapLock.lock();
            WorkerChannel workerChannel = channelMap.get(channelKey);
            if (workerChannel == null) {
                workerChannel = new WorkerChannel(channelKey);
                channelMap.put(channelKey, workerChannel);
            }
            return workerChannel;
        } finally {
            channelMapLock.unlock();
        }
    }

    public void remove(String workerName, String channelKey) {
        try {
            channelMapLock.lock();
            WorkerChannel workerChannel = channelMap.get(channelKey);
            if (!workerChannel.resultFuture.isDone()) {
                workerChannel.panic(ErrorUtils.createNoMessageError(workerName));
            }
            if (workerChannel.doneCount.get() == 0) {
                channelMap.remove(channelKey);
            } else {
                workerChannel.doneCount.decrementAndGet();
            }
        } finally {
            channelMapLock.unlock();
        }
    }
}
