/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test resiliency of the parser.
 * 
 * @since 2.0.0
 */
public class ResiliencyTest extends AbstractMiscTest {

    // Recovery tests

    @Test
    public void testParsingMatchAlikeStmt() {
        testFile("resiliency/resiliency_source_01.bal", "resiliency/resiliency_assert_01.json");
    }

    @Test
    public void testParsingJavaHelloWorld() {
        testFile("resiliency/resiliency_source_02.bal", "resiliency/resiliency_assert_02.json");
    }
}
