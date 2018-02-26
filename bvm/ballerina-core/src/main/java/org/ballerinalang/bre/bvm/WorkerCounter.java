/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atomic counter for counting spawned workers for given context.
 *
 * @since 0.94
 */
public class WorkerCounter {

    private final Semaphore lock = new Semaphore(0);
    private final AtomicInteger count = new AtomicInteger(0);
    private Context resourceContext;

    /**
     * Count up worker count.
     */
    public void countUp() {
        count.incrementAndGet();
    }

    /**
     * Count down worker count.
     */
    public void countDown() {
        if (count.decrementAndGet() == 0) {
            lock.release();
            // FIXME
//            if (resourceContext != null && resourceContext.getConnectorFuture() != null) {
//                // Asynchronously notify the resource.
//                resourceContext.getConnectorFuture().notifySuccess();
//            }
        }
    }

    /**
     * Wait until all spawned workers are completed.
     */
    public void await() {
        try {
            lock.acquire();
            lock.release();
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    /**
     * Wait until all spawned worker are completed within the given waiting time.
     *
     * @param timeout time out duration in seconds.
     * @return {@code true} if a all workers are completed within the given waiting time, else otherwise.
     */
    public boolean await(int timeout) {
        boolean success = false;
        try {
            success = lock.tryAcquire(timeout, TimeUnit.SECONDS);
            if (success) {
                lock.release();
            }
        } catch (InterruptedException e) {
            // Ignore.
        }
        return success;
    }

    public void setResourceContext(Context resourceContext) {
        this.resourceContext = resourceContext;
    }
}
