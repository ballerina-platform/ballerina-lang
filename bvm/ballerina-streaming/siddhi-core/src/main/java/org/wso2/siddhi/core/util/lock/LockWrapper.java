/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.util.lock;


import java.util.concurrent.locks.ReentrantLock;

/**
 * A wrapper class for {@link ReentrantLock} which is used to share and sync locks across various
 * components of Siddhi. LockWrapper creates a mutable instance where the internal lock can be
 * assigned and reassigned to new instances.
 * However, internal lock is not intended to be replaced after initialization.
 */
public class LockWrapper {
    /**
     * Id of the lock which is supposed to be unique inside EventWindows.
     * It is recommended to use the {@link org.wso2.siddhi.core.window.Window} id as lock id.
     * For query lock, an empty String can be used as lock id since it will not be used in LockSyn
     */
    private final String lockId;

    /**
     * The actual lock which is used to lock and unlock.
     */
    private ReentrantLock lock;

    /**
     * Construct an empty LockWrapper object with no lock inside.
     *
     * @param lockId id of the lock. Cannot be null.
     */
    public LockWrapper(String lockId) {
        if (lockId == null) {
            throw new NullPointerException("Window id cannot be null");
        }
        this.lockId = lockId;
    }

    /**
     * Return the internal lock of the {@link LockWrapper}
     *
     * @return the actual lock which is used by this instance
     */
    public ReentrantLock getLock() {
        return lock;
    }

    /**
     * Set the internal lock to this {@link LockWrapper}
     *
     * @param lock the actual lock to be used
     */
    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    /**
     * Acquires the lock.
     */
    public void lock() {
        if (this.lock != null) {
            this.lock.lock();
        }
    }

    /**
     * Release the lock.
     */
    public void unlock() {
        if (this.lock != null && this.lock.isHeldByCurrentThread()) {
            this.lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LockWrapper that = (LockWrapper) o;

        return lockId.equals(that.lockId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return lockId.hashCode();
    }
}
