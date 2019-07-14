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
}
