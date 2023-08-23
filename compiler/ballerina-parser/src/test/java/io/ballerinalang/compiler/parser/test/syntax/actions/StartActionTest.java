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
 * Test parsing start action.
 * 
 * @since 1.3.0
 */
public class StartActionTest extends AbstractActionTest {

    // Valid source tests

    @Test
    public void testStartAction() {
        testFile("start-action/start_action_source_01.bal", "start-action/start_action_assert_01.json");
    }

    @Test
    public void testStartActionWithAnnots() {
        testFile("start-action/start_action_source_03.bal", "start-action/start_action_assert_03.json");
    }

    // Recovery tests

    @Test
    public void testStartActionRecovery() {
        testFile("start-action/start_action_source_02.bal", "start-action/start_action_assert_02.json");
    }

    @Test
    public void testStartActionWithAnnotsRecovery() {
        testFile("start-action/start_action_source_04.bal", "start-action/start_action_assert_04.json");
    }

    @Test
    public void testStartActionWithInvalidExpr() {
        testFile("start-action/start_action_source_05.bal", "start-action/start_action_assert_05.json");
    }

    @Test
    public void testStartActionWithRemoteMtdRecovery() {
        testFile("start-action/start_action_source_06.bal", "start-action/start_action_assert_06.json");
    }
}
