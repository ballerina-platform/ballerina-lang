/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completion.util.CompletionTestUtil;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Completion Test Interface.
 */
public abstract class CompletionTest {

    private static final Logger LOGGER = Logger.getLogger(CompletionTest.class);

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException {
        String configJsonPath = "completion" + File.separator + configPath + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        List<CompletionItem> responseItemList = getResponseItemList(configJsonObject);
        List<CompletionItem> expectedList = getExpectedList(configJsonObject);
        Assert.assertTrue(CompletionTestUtil.isSubList(expectedList, responseItemList));
    }

    protected List<CompletionItem> getResponseItemList(JsonObject configJsonObject) throws WorkspaceDocumentException {
        JsonObject positionObj = configJsonObject.get("position").getAsJsonObject();
        String balPath = "completion" + File.separator + configJsonObject.get("source").getAsString();
        Position position = new Position();
        String content = FileUtils.fileContent(balPath);
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());

        Path filePath = FileUtils.RES_DIR.resolve(balPath);
        TextDocumentPositionParams positionParams =
                CompletionTestUtil.getPositionParams(position, filePath.toString());
        WorkspaceDocumentManager documentManager =
                CompletionTestUtil.prepareDocumentManager(filePath, content);
        LSCompiler lsCompiler = new LSCompiler(documentManager);
        List<CompletionItem> completions = CompletionTestUtil.getCompletions(lsCompiler, documentManager,
                                                                             positionParams);
        CompletionTestUtil.clearDocumentManager(documentManager, filePath);
        return completions;
    }

    protected List<CompletionItem> getExpectedList(JsonObject configJsonObject) {
        JsonArray expectedItems = configJsonObject.get("items").getAsJsonArray();
        return CompletionTestUtil.getExpectedItemList(expectedItems);
    }

    @DataProvider(name = "completion-data-provider")
    public abstract Object[][] dataProvider();

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            LOGGER.error("Test Failed for: [" + result.getParameters()[1] + "/" + result.getParameters()[0] + "]");
        }
    }
}
