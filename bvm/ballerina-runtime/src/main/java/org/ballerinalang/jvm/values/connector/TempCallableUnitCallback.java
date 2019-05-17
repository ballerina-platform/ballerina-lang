/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm.values.connector;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Temporary callback implementation to handle non-blocking function behaviour.
 * TODO : Remove this class once strand non-blocking support is provided.
 *
 * @since 0.995.0
 */
public class TempCallableUnitCallback {

    private static final Logger log = LoggerFactory.getLogger(TempCallableUnitCallback.class);
    private Strand strand = null;
    private volatile Semaphore executionWaitSem;
    private Object returnValue;

    public TempCallableUnitCallback(Strand strand) {
        // Thread is not blocked(released to the pool) but the ballerina execution is blocked until the
        // returnValues are retrieved.
        strand.yield = true;
        this.strand = strand;
        executionWaitSem = new Semaphore(0);
    }

    public TempCallableUnitCallback() {
        executionWaitSem = new Semaphore(0);
    }

    public void notifySuccess() {
        this.executionWaitSem.release();
        //TODO : Replace following with callback.notifySuccess() once strand non-blocking support is given
        if (this.strand != null) {
            this.strand.resume();
        }
    }

    public void notifyFailure(ErrorValue error) {
        this.returnValue = error;
        this.executionWaitSem.release();
        //TODO : Replace following with callback.notifyFailure() once strand non-blocking support is given
        if (this.strand != null) {
            this.strand.setReturnValues(returnValue);
            this.strand.resume();
        }
    }

    public void sync() {
        try {
            if (!executionWaitSem.tryAcquire(120, TimeUnit.SECONDS)) {
                log.debug("Failed to acquire");
            }
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public void setReturnValues(Object returnValue) {
        this.returnValue = returnValue;
        //TODO : Replace following with callback.setReturnValues() once strand non-blocking support is given
        if (this.strand != null) {
            this.strand.setReturnValues(returnValue);
        }
    }
}
