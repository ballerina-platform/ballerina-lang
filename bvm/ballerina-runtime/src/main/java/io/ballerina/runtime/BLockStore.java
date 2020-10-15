/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package io.ballerina.runtime;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @since 1.2.0
 */
public class BLockStore {

    /**
     * The map of locks inferred.
     */
    private  Map<String, BLock> globalLockMap;

    public BLockStore() {
        globalLockMap = new ConcurrentHashMap<>();
    }

    public void addLockToMap(String lockName) {
        globalLockMap.put(lockName, new BLock());
    }

    public BLock getLockFromMap(String lockName) {
        return globalLockMap.computeIfAbsent(lockName, (k) -> {
            return new BLock();
        });
    }

    public void panicIfInLock(String lockName, Strand strand) {
        for (BLock lock : globalLockMap.values()) {
            if (lock.isLockFree()) {
                continue;
            }
            if (lock.lockedBySameContext(strand)) {
                throw ErrorCreator.createError(BallerinaErrorReasons.ASYNC_CALL_INSIDE_LOCK);
            }
        }
    }
}
