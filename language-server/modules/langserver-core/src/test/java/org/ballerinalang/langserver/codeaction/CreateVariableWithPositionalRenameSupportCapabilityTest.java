/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test cases for create variable code action with positional rename support capability.
 *
 * @since 2201.4.0
 */
public class CreateVariableWithPositionalRenameSupportCapabilityTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
    }

    @Override
    protected Path getConfigJsonPath(String configFilePath) {
        return FileUtils.RES_DIR.resolve("codeaction")
                .resolve(getResourceDir())
                .resolve("config-rename-positional-capability")
                .resolve(configFilePath);
    }

    @Override
    public String getResourceDir() {
        return "create-variable";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"variableAssignmentRequiredCodeAction1.json"},
                {"variableAssignmentRequiredCodeAction2.json"},
                {"variableAssignmentRequiredCodeAction3.json"},
                {"variableAssignmentRequiredCodeAction4.json"},
                {"variableAssignmentRequiredCodeAction5.json"},
                {"variableAssignmentRequiredCodeAction6.json"},
                {"variableAssignmentRequiredCodeAction7.json"},
                {"variableAssignmentRequiredCodeAction8.json"},
                {"variableAssignmentRequiredCodeAction9.json"},
                {"variableAssignmentRequiredCodeAction10.json"},
                {"variableAssignmentRequiredCodeAction11.json"},
                {"variableAssignmentRequiredCodeAction12.json"},
                {"variableAssignmentRequiredCodeAction13.json"},
                {"variableAssignmentRequiredCodeAction14.json"},
                {"variableAssignmentRequiredCodeAction15.json"},
                {"variableAssignmentRequiredCodeAction16.json"},
                {"variableAssignmentRequiredCodeAction17.json"},
                {"variableAssignmentRequiredCodeAction18.json"},
                {"variableAssignmentRequiredCodeAction19.json"},
                {"variableAssignmentRequiredCodeAction20.json"},
                {"variableAssignmentRequiredCodeAction21.json"},
                {"variableAssignmentRequiredCodeAction22.json"},
                {"variableAssignmentRequiredCodeAction23.json"},
                {"variableAssignmentRequiredCodeAction24.json"},
                {"variableAssignmentRequiredCodeAction25.json"},
                {"variableAssignmentRequiredCodeAction26.json"},
                {"variableAssignmentRequiredCodeAction27.json"},
                {"variableAssignmentRequiredCodeAction28.json"},
                {"variableAssignmentRequiredCodeAction29.json"},
                {"variableAssignmentRequiredCodeAction30.json"},
                {"variableAssignmentRequiredCodeAction31.json"},
                {"variableAssignmentRequiredCodeAction32.json"},
                {"variableAssignmentRequiredCodeAction33.json"},
                {"variableAssignmentRequiredCodeAction34.json"},
                {"variableAssignmentRequiredCodeAction35.json"},
                {"variableAssignmentRequiredCodeAction36.json"},
                {"variableAssignmentRequiredCodeAction37.json"},
                {"variableAssignmentRequiredCodeAction38.json"},
                {"variableAssignmentRequiredCodeAction39.json"},
                {"variableAssignmentRequiredCodeAction40.json"},
                {"variableAssignmentRequiredCodeAction41.json"},
                {"variableAssignmentRequiredCodeAction42.json"},
                {"variableAssignmentRequiredCodeAction43.json"},
                {"variableAssignmentRequiredCodeAction44.json"},
                {"variableAssignmentRequiredCodeAction45.json"},
                {"ignoreReturnValueCodeAction.json"},
                {"projectVariableAssignmentRequiredCodeAction1.json"},
                {"projectVariableAssignmentRequiredCodeAction2.json"},
                {"projectVariableAssignmentRequiredCodeAction3.json"},
                {"createVariableInClassMethod.json"},
                {"createVariableInServiceMethod.json"},
                {"createVariableInServiceRemoteMethod.json"},
                {"createVariableWithUnionType.json"},
                {"createVariableWithIntersectionType.json"},
                {"createVariableWithIntersectionType2.json"},
                {"createVariableForOptionalFieldAccess1.json"},
                {"createVariableForOptionalFieldAccess2.json"},
                {"createVariableWithTypeDesc.json"},

                // Tuple related
                {"createVariableWithTuple1.json"},

                // Create variables of function/invocable type
                {"createVariableWithFunctionType1.json"},
                {"createVariableWithFunctionType2.json"},

                {"createVariableWithFunctionCall1.json"},
                {"createVariableWithFunctionCall2.json"},
                {"createVariableWithRemoteMethodInvocation.json"},

                // Async send action
                {"createVarInSendAction1.json"},

                // Create variable with check
                {"createVariableWithCheck1.json"},
                {"createVariableWithCheck2.json"},
                {"createVariableWithCheck3.json"},
                {"createVariableWithCheck4.json"},
                {"createVariableWithCheck5.json"},
                {"createVariableWithCheck6.json"}
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"createVariableNegative1.json"},
                {"createVariableNegative2.json"},
                {"createVariableNegative3.json"},
                {"createVariableNegative4.json"}
        };
    }
}
