/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.values.BmpStringValue;
import io.ballerina.runtime.values.HandleValue;
import io.ballerina.runtime.values.ObjectValue;

/**
 * This class contains a set of utility instance methods required for interoperability testing.
 *
 * @since 1.0.0
 */
public class InstanceMethods {

    private Integer counter = 0;

    public void increaseCounterByOne() {
        this.counter = this.counter + 1;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounterValue(Integer newValue) {
        this.counter = newValue;
    }

    public Integer setAndGetCounterValue(Integer newValue) {
        this.counter = newValue;
        return this.counter;
    }

    public Integer setTwiceAndGetCounterValue(Integer newValue1, Integer newValue2) {
        this.counter = newValue1 + newValue2;
        return this.counter;
    }

    public Integer setAndGetCounterValueWhichThrowsCheckedException(Integer newValue)
    throws JavaInteropTestCheckedException {
        this.counter = newValue;
        return this.counter;
    }

    public Integer setAndGetCounterValueWhichThrowsUncheckedException(Integer newValue)
            throws UnsupportedOperationException {
        this.counter = newValue;
        return this.counter;
    }

    public long setGetCounterValueWhichThrowsCheckedException(float newValue) throws JavaInteropTestCheckedException {
        this.counter = (int) newValue;
        return this.counter;
    }

    public long setGetCounterValueWhichThrowsUncheckedException(float newValue) throws UnsupportedOperationException {
        this.counter = (int) newValue;
        return this.counter;
    }

    public void testThrowsWithVoid() throws InterruptedException {
        throw new InterruptedException();
    }

    public void testThrowsWithVoidReturn() throws InterruptedException {
        if (false) {
            throw new InterruptedException();
        }
    }

    public Integer handleOrErrorReturn() throws InterruptedException {
        if (false) {
            throw new InterruptedException();
        }
        return 70;
    }

    public Integer handleOrErrorReturnThrows() throws InterruptedException {
        if (true) {
            throw new InterruptedException();
        }
        return 70;
    }

    public Object handleOrErrorWithObjectReturn() throws InterruptedException {
        if (false) {
            throw new InterruptedException();
        }
        return new HandleValue(70);
    }

    public Object handleOrErrorWithObjectReturnThrows() throws InterruptedException {
        if (true) {
            throw new InterruptedException();
        }
        return 70;
    }

    public float primitiveOrErrorReturn() throws InterruptedException {
        if (false) {
            throw new InterruptedException();
        }
        return 55;
    }

    public float primitiveOrErrorReturnThrows() throws InterruptedException {
        if (true) {
            throw new InterruptedException();
        }
        return 55;
    }

    public byte unionWithErrorReturnByte() throws InterruptedException {
        if (false) {
            throw new InterruptedException();
        }
        return '5';
    }

    public byte unionWithErrorReturnThrows() throws InterruptedException {
        if (true) {
            throw new InterruptedException();
        }
        return '5';
    }

    public String unionWithErrorReturnHandle() throws InterruptedException {
        if (false) {
            throw new InterruptedException();
        }
        return "handle ret";
    }

    public void errorDetail() throws JavaInteropTestCheckedException {
        JavaInteropTestCheckedException e = new JavaInteropTestCheckedException("Custom error");
        e.initCause(new Throwable("Interop Throwable"));
        throw e;
    }

    public int uncheckedErrorDetail() {
        RuntimeException ex = new RuntimeException("Unchecked Exception", new Throwable("Unchecked cause"));
        if (true) {
            throw ex;
        }
        return 10;
    }

    public int getObjectValueField(ObjectValue objectValue) {
        return ((Long) objectValue.get(new BmpStringValue("age"))).intValue();
    }

    public int getInt(ObjectValue objectValue, int x) {
        return ((Long) objectValue.get(new BmpStringValue("age"))).intValue();
    }

    public int getRandomInt() {
        return 123;
    }
}
