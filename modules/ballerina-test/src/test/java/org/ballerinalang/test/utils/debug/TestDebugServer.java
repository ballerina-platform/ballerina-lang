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
package org.ballerinalang.test.utils.debug;

import org.ballerinalang.util.debugger.DebugServer;
import org.ballerinalang.util.debugger.VMDebugManager;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Test debug server implementation for tests.
 *
 * @since 0.95.4
 */
public class TestDebugServer implements DebugServer {
    private volatile Semaphore executionSem;

    public TestDebugServer() {
        this.executionSem = new Semaphore(0);
    }

    public boolean tryAcquireLock(long timeout) {
        try {
            return this.executionSem.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void releaseLock() {
        this.executionSem.release();
    }

    @Override
    public void startServer(VMDebugManager debugManager) {
    }
}
