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
package org.ballerinalang.util.program;

import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Stack frame with ability to wait till it returns.
 *
 * @since 0.94
 */
public class SynchronizedStackFrame extends StackFrame {

    final Semaphore lock = new Semaphore(0);

    public SynchronizedStackFrame(CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, int retAddrs, int[]
            retRegIndexes) {
        super(callableUnitInfo, workerInfo, retAddrs, retRegIndexes);
    }

    @Override
    public void markedAsReturned() {
        super.markedAsReturned();
        lock.release();
    }

    /**
     * Wait until current stackframe to be returned.
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
     * Wait until current stackframe to be returned within the given waiting time.
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

}
