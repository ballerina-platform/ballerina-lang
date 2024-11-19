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

package io.ballerina.runtime.internal.lock;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that keep Ballerina locks based on lock name.
 *
 * @since 1.2.0
 */
public class BLockStore {

    /**
     * The map of locks inferred.
     */
    private final Map<String, ReentrantLock> globalLockMap;

    private final ReentrantReadWriteLock storeLock;

    public BLockStore() {
        this.globalLockMap = new HashMap<>();
        this.storeLock = new ReentrantReadWriteLock();
    }

    /*
        This is code generated method to get Ballerina lock and lock.
    */
    @SuppressWarnings("unused")
    public void lock(Strand strand, String lockName) {
        try {
            strand.yield();
            getLockFromMap(lockName).lock();
            strand.acquiredLockCount++;
        } finally {
            strand.resume();
        }
    }

    /*
        This is code generated method to get Ballerina lock and unlock.
    */
    @SuppressWarnings("unused")
    public void unlock(Strand strand, String lockName) {
        try {
            strand.yield();
            getLockFromMap(lockName).unlock();
            strand.acquiredLockCount--;
        } finally {
            strand.resume();
        }
    }


    /*
        This is code generated method check and panic before async call if strand is in lock
    */
    @SuppressWarnings("unused")
    public void panicIfInLock(Strand strand) {
        if (strand.acquiredLockCount > 0) {
            throw ErrorCreator.createError(ErrorReasons.ASYNC_CALL_INSIDE_LOCK);
        }
    }

    private ReentrantLock getLockFromMap(String lockName) {
        ReentrantLock lock;
        try {
            storeLock.readLock().lock();
            lock = globalLockMap.get(lockName);
        } finally {
            storeLock.readLock().unlock();
        }
        if (lock != null) {
            return lock;
        }
        return addLockToMap(lockName);
    }

    private ReentrantLock addLockToMap(String lockName) {
        try {
            storeLock.writeLock().lock();
            return globalLockMap.computeIfAbsent(lockName, k -> new ReentrantLock());
        } finally {
            storeLock.writeLock().unlock();
        }
    }
}
