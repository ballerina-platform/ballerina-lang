/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion.definitions;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Completion item tests for service definition.
 */
public class ServiceDefinitionCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(ServiceDefinitionCompletionTest.class);

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for Service Definitions Scope");
        return new Object[][] {
            {"serviceEndpointBind1.json", "service"},
            {"serviceEndpointBind2.json", "service"},
            // This is an invalid case
//            {"serviceEndpointBind3.json", "service"},
            {"serviceEndpointBind4.json", "service"},
            {"serviceEndpointBind5.json", "service"},
            {"serviceBodyCompletion1.json", "service"},
            {"serviceBodyCompletion2.json", "service"},
            {"serviceBodyCompletion6.json", "service"},
            {"serviceBodyCompletion7.json", "service"},
            {"serviceBodyCompletion8.json", "service"},
            {"serviceVariableAndFieldsCompletion1.json", "service"},
            {"httpServiceBodyResourceCompletion.json", "service"},
            {"websocketServiceBodyResourceCompletion1.json", "service"},
            {"websocketServiceBodyResourceCompletion2.json", "service"},
            {"websubServiceBodyResourceCompletion.json", "service"},
            {"grpcServiceBodyResourceCompletion.json", "service"},
            {"serviceDefinitionContextSuggestion1.json", "service"},
            {"completionAftercheckpanic.json", "service"},
        };
    }
}
