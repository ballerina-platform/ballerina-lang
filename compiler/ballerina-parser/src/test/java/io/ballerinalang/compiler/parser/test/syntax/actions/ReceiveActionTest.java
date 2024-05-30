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
 * Test parsing receive action.
 * 
 * @since 2.0.0
 */
public class ReceiveActionTest extends AbstractActionTest {

    // Valid source tests

    @Test
    public void testBasicReceiveAction() {
        testFile("receive-action/receive_action_source_01.bal", "receive-action/receive_action_assert_01.json");
    }

    @Test
    public void testAlternateReceiveAction() {
        testFile("receive-action/receive_action_source_04.bal", "receive-action/receive_action_assert_04.json");
    }

    // Recover tests

    @Test
    public void testRecoveryInReceiveAction() {
        testFile("receive-action/receive_action_source_02.bal", "receive-action/receive_action_assert_02.json");
    }

    @Test
    public void testInvalidNodeInReceiveAction() {
        testFile("receive-action/receive_action_source_03.bal", "receive-action/receive_action_assert_03.json");
    }

    @Test
    public void testAlternateReceiveActionRecovery() {
        testFile("receive-action/receive_action_source_05.bal", "receive-action/receive_action_assert_05.json");
    }
}
