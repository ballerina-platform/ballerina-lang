/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.birspec;

import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases to verify BIR binary against the spec.
 */
public class BIRSpecTest {

    @Test(description = "Test to verify BIR with different type of functions")
    public void functionsTest() {
        BIRTestUtils.assertFunctions();
    }

    @Test(description = "Test to verify BIR with different type of constants and values")
    public void constantValueTest() {
        BIRTestUtils.assertValues();
    }

    @Test(description = "Test to verify BIR with different type definitions")
    public void typeDefinitionTest() {
        BIRTestUtils.assertTypeDefs();
    }

    @Test(description = "Test to verify BIR with annotations")
    public void annotationsTest() {
        BIRTestUtils.assertAnnotations();
    }

    @Test(description = "Test to verify BIR spec using langlib test sources")
    public void validateBIRSpecTest() throws IOException {
        BIRTestUtils.validateBIRSpec();
    }
}
