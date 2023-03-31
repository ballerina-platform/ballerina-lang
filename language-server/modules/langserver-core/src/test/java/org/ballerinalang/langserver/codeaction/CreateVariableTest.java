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
package org.ballerinalang.langserver.codeaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.TextEdit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class CreateVariableTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_RENAME_SUPPORT, true);
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

    @Override
    protected boolean validateAndModifyArguments(JsonObject actualCommand,
                                                 JsonArray actualArgs,
                                                 JsonArray expArgs,
                                                 Path sourceRoot,
                                                 Path sourcePath,
                                                 List<TextEdit> actualEdits,
                                                 TestConfig testConfig) {
        if (CommandConstants.RENAME_COMMAND.equals(actualCommand.get("command").getAsString())) {
            if (actualArgs.size() == 2) {
                Optional<String> actualFilePath =
                        PathUtil.getPathFromURI(actualArgs.get(0).getAsString())
                                .map(path -> path.toUri().toString().replace(sourceRoot.toUri().toString(), ""));
                int actualRenamePosition = actualArgs.get(1).getAsInt();
                String expectedFilePath = expArgs.get(0).getAsString();
                int expectedRenamePosition = expArgs.get(1).getAsInt();
                if (actualFilePath.isPresent()) {
                    String actualPath = actualFilePath.get();
                    if (actualFilePath.get().startsWith("/") || actualFilePath.get().startsWith("\\")) {
                        actualPath = actualFilePath.get().substring(1);
                    }
                    if (sourceRoot.resolve(actualPath).equals(sourceRoot.resolve(expectedFilePath))) {
                        if (System.lineSeparator().equals("\r\n")) {
                            int newImportCount = (int) actualEdits.stream()
                                    .filter(edit -> edit.getNewText().startsWith("import ")).count();
                            int noOfNewLinesBeforeCursorPos = testConfig.position.getLine() + newImportCount;
                            if (actualRenamePosition == expectedRenamePosition + noOfNewLinesBeforeCursorPos) {
                                return true;
                            }
                        } else if (actualRenamePosition == expectedRenamePosition) {
                            return true;
                        }
                    }

                    JsonArray newArgs = new JsonArray();
                    newArgs.add(actualPath);
                    newArgs.add(actualRenamePosition);

                    //Replace the args of the actual command to update the test config
                    actualCommand.add("arguments", newArgs);
                }
            }
        }
        return false;
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
                {"variableAssignmentRequiredCodeAction46.json"},
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
                {"createVariableWithCheck6.json"},
                {"createVariableWithCheck7.json"}
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"createVariableNegative1.json"},
                {"createVariableNegative2.json"},
                {"createVariableNegative3.json"},
                {"createVariableNegative4.json"},
                {"createVariableNegative5.json"},
                {"createVariableNegative6.json"},
                {"createVariableNegative7.json"}
        };
    }
}
