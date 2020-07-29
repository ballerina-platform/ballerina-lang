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
package io.ballerinalang.compiler.parser.test.syntax.actions;

import org.testng.annotations.Test;

/**
 * Test parsing remote method call action.
 * 
 * @since 1.3.0
 */
public class RemoteMethodCallActionTest extends AbstractActionTest {

    // Valid source tests

    @Test
    public void testBasicRemoteMethodCall() {
        testFile("remote-method-call-action/remote_method_call_source_01.bal",
                "remote-method-call-action/remote_method_call_assert_01.json");
    }

    @Test
    public void testRemoteMethodCallWithDifferentArgs() {
        testFile("remote-method-call-action/remote_method_call_source_02.bal",
                "remote-method-call-action/remote_method_call_assert_02.json");
    }

    @Test
    public void testComplexExprInLHS() {
        testFile("remote-method-call-action/remote_method_call_source_03.bal",
                "remote-method-call-action/remote_method_call_assert_03.json");
    }

    @Test
    public void testActionUsageInDifferentPlaces() {
        testFile("remote-method-call-action/remote_method_call_source_05.bal",
                "remote-method-call-action/remote_method_call_assert_05.json");
    }

    // Recovery tests

    @Test
    public void testMissingClosingBrace() {
        testFile("remote-method-call-action/remote_method_call_source_04.bal",
                "remote-method-call-action/remote_method_call_assert_04.json");
    }

    @Test
    public void testActionInBinaryExprRhs() {
        testFile("remote-method-call-action/remote_method_call_source_06.bal",
                "remote-method-call-action/remote_method_call_assert_06.json");
    }

    @Test
    public void testActionInsideFunctionCall() {
        testFile("remote-method-call-action/remote_method_call_source_07.bal",
                "remote-method-call-action/remote_method_call_assert_07.json");
    }

    @Test
    public void testActionPrecedenceWithExpressions() {
        testFile("remote-method-call-action/remote_method_call_source_08.bal",
                "remote-method-call-action/remote_method_call_assert_08.json");
    }

    @Test
    public void testMissingRHS() {
        testFile("remote-method-call-action/remote_method_call_source_09.bal",
                "remote-method-call-action/remote_method_call_assert_09.json");
    }

    @Test
    public void testInvalidTokenInRHS() {
        testFile("remote-method-call-action/remote_method_call_source_10.bal",
                "remote-method-call-action/remote_method_call_assert_10.json");
    }
}
