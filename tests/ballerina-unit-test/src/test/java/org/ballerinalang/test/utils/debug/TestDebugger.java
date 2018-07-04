/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.test.utils.debug;

import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * {@code VMDebugManager} Manages debug sessions and handle debug related actions.
 *
 * @since 0.96
 */
public class TestDebugger extends Debugger {
    private volatile Semaphore executionSem;

    private TestDebugClientHandler clientHandler;

    public TestDebugger(ProgramFile programFile) {
        super(programFile);
        this.executionSem = new Semaphore(0);
    }

    /**
     * Method to initialize debug manager.
     */
    public void init() {
        this.setupDebugger();
        this.clientHandler = new TestDebugClientHandler();
        this.setClientHandler(clientHandler);
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

    public TestDebugClientHandler getClientHandler() {
        return clientHandler;
    }
}
