/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.testerina.core.entity;

/**
 * {@link TesterinaResult} represents the result of the Testerina test.
 */
public class TesterinaResult {
    private String testFunctionName;
    private boolean isPassed;
    private boolean isSkipped;
    private String assertFailureMessage;

    public TesterinaResult(String testFunctionName, boolean isPassed, boolean isSkipped, String assertFailureMessage) {
        this.testFunctionName = testFunctionName;
        this.isPassed = isPassed;
        this.isSkipped = isSkipped;
        this.assertFailureMessage = assertFailureMessage;
    }

    public String getTestFunctionName() {
        return testFunctionName;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public boolean isSkipped() {
        return isSkipped;
    }

    public String getAssertFailureMessage() {
        return assertFailureMessage;
    }
}
