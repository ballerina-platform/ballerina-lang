/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core.types;

import org.testng.annotations.Test;

/**
 * Test formatting for type descriptors.
 *
 * @since 2.0.0
 */
public class TypeTestImpl extends AbstractTypeTest {

    @Test(description = "Test the formatting of behavioural type descriptors")
    public void testBehaviouralTypes() {
        testFile("source/behavioural-type-descriptors.bal", "expected/behavioural-type-descriptors.bal");
    }

    @Test(description = "Test the formatting of other type descriptors")
    public void testOtherTypes() {
        testFile("source/other-type-descriptors.bal", "expected/other-type-descriptors.bal");
    }

    @Test(description = "Test the formatting of sequence type descriptors")
    public void testSequenceTypes() {
        testFile("source/sequence-type-descriptors.bal", "expected/sequence-type-descriptors.bal");
    }

    @Test(description = "Test the formatting of simple type descriptors")
    public void testSimpleTypes() {
        testFile("source/simple-type-descriptors.bal", "expected/simple-type-descriptors.bal");
    }

    @Test(description = "Test the formatting of structured type descriptors")
    public void testStructuredTypes() {
        testFile("source/structured-type-descriptors.bal", "expected/structured-type-descriptors.bal");
    }
}
