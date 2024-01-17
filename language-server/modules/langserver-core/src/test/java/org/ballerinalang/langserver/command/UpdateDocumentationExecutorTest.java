/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.command.executors.UpdateDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Tests the functionality of {@link UpdateDocumentationExecutor}.
 */
public class UpdateDocumentationExecutorTest extends AbstractCommandExecutionTest {

    @Test(dataProvider = "update-doc-data-provider")
    public void testUpdateDocumentation(String config) throws IOException {
        performTest(config, UpdateDocumentationExecutor.COMMAND);
    }

    @DataProvider(name = "update-doc-data-provider")
    public Object[][] addDocDataProvider() {
        return new Object[][]{
                {"updateDocumentationConfig1.json"},
                {"updateDocumentationConfig2.json"},
                {"updateDocumentationConfig3.json"},
                {"updateDocumentationConfig4.json"},
                {"updateDocumentationConfig5.json"},
                {"updateDocumentationWithDeprecatedConfig1.json"},
                {"updateDocumentationWithDeprecatedConfig2.json"},
                {"updateDocumentationWithDeprecatedConfig3.json"},
        };
    }

    @Override
    protected List<Object> getArgs(JsonObject argsObject) {
        return List.of(
                CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, argsObject.getAsJsonObject("node.range"))
        );
    }

    @Override
    protected String getSourceRoot() {
        return "update-documentation";
    }
}
