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
package org.ballerinalang.bre.nonblocking;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents the Result of a Ballerina Program execution.
 */
public class BLangExecution implements Future<Integer> {

    private final CountDownLatch latch;
    private final BLangAbstractExecutionVisitor executionVisitor;

    BLangExecution(BLangAbstractExecutionVisitor executionVisitor) {
        this.latch = new CountDownLatch(1);
        this.executionVisitor = executionVisitor;
    }

    protected void notifyComplete() {
        latch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (executionVisitor.isExecutionCompleted()) {
            return false;
        }
        executionVisitor.cancelExecution();
        latch.countDown();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return executionVisitor.isExecutionCanceled();
    }

    @Override
    public boolean isDone() {
        return executionVisitor.isExecutionCompleted();
    }

    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        latch.await();
        return 0;
    }

    @Override
    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (latch.await(timeout, unit)) {
            return 0;
        } else {
            executionVisitor.cancelExecution();
            return -1;
        }
    }
}
