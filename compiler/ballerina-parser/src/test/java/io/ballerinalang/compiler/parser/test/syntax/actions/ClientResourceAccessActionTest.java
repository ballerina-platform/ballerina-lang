/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test parsing client resource access action.
 * 
 * @since 2201.2.0
 */
public class ClientResourceAccessActionTest extends AbstractActionTest {

    // Valid source tests

    @Test
    public void testBasicResourceMethodCall() {
        testFile("client-resource-access-action/client_resource_access_action_source_01.bal",
                "client-resource-access-action/client_resource_access_action_assert_01.json");
    }

    @Test
    public void testComplexResourceMethodCallAction() {
        testFile("client-resource-access-action/client_resource_access_action_source_02.bal",
                "client-resource-access-action/client_resource_access_action_assert_02.json");
    }

    @Test
    public void testResourceMethodCallActionInDifferentContexts() {
        testFile("client-resource-access-action/client_resource_access_action_source_03.bal",
                "client-resource-access-action/client_resource_access_action_assert_03.json");
    }
    
    // Recovery tests

    @Test
    public void testRecoveryForResourceMethodCallActionSlashToken() {
        testFile("client-resource-access-action/client_resource_access_action_source_04.bal",
                "client-resource-access-action/client_resource_access_action_assert_04.json");
    }

    @Test
    public void testRecoveryForResourceMethodCallActionRhs() {
        testFile("client-resource-access-action/client_resource_access_action_source_05.bal",
                "client-resource-access-action/client_resource_access_action_assert_05.json");
    }

    @Test
    public void testRecoveryForResourceMethodCallActionRhsInDifferentContexts() {
        testFile("client-resource-access-action/client_resource_access_action_source_06.bal",
                "client-resource-access-action/client_resource_access_action_assert_06.json");
    }

    @Test
    public void testRecoveryForResourceMethodCallActionhsInExprOnlyCtx() {
        testFile("client-resource-access-action/client_resource_access_action_source_07.bal",
                "client-resource-access-action/client_resource_access_action_assert_07.json");
    }

    @Test
    public void testResourceAccessPathSegmentOrderValidation() {
        testFile("client-resource-access-action/client_resource_access_action_source_08.bal",
                "client-resource-access-action/client_resource_access_action_assert_08.json");
    }

    @Test
    public void testRecoveryForExpressionInsideComputedResourceAccessSegment() {
        testFile("client-resource-access-action/client_resource_access_action_source_09.bal",
                "client-resource-access-action/client_resource_access_action_assert_09.json");
    }
}
