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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Temp callback implementation for non-blocking service tests.
 */
public class TempCallableUnitCallback implements CallableUnitCallback{

    private final Strand strand;
    private volatile Semaphore executionWaitSem;
    private int timeOut = 120;
    private Object returnValue;

    public TempCallableUnitCallback(Strand strand) {
        strand.yield = true;
        this.strand = strand;
        executionWaitSem = new Semaphore(0);
    }

    public void notifySuccess() {
        this.executionWaitSem.release();
        //TODO : Replace following with callback.notifySuccess() once strand non-blocking support is given
        this.strand.resume();
    }

    public void notifyFailure(ErrorValue error) {
        this.returnValue = error;
        this.executionWaitSem.release();
        //TODO : Replace following with callback.notifyFailure() once strand non-blocking support is given
        strand.setReturnValues(returnValue);
        this.strand.resume();
    }

    public void sync() {
        try {
            executionWaitSem.tryAcquire(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public void setReturnValues(Object returnValue) {
        this.returnValue = returnValue;
        //TODO : Replace following with callback.setReturnValues() once strand non-blocking support is given
        strand.setReturnValues(returnValue);
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
