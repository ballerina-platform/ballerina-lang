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
package org.ballerinalang.composer.service.workspace.langserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.composer.service.workspace.langserver.definitions.FunctionDefinition;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.util.FileUtils;
import org.ballerinalang.composer.service.workspace.langserver.util.MessageUtil;
import org.ballerinalang.composer.service.workspace.langserver.util.ServerManager;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.util.List;

/**
 * Completion Test Interface
 */
public abstract class CompletionTest {

    private static final Logger LOGGER = Logger.getLogger(FunctionDefinition.class);

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) {
        String configJsonPath =  configPath + config;
        JsonObject jsonObject = null;
        jsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonArray expectedItems = jsonObject.get("items").getAsJsonArray();
        JsonObject positionObj = jsonObject.get("position").getAsJsonObject();

        String balPath = jsonObject.get("source").getAsString();
        Position position = new Position();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        String content = FileUtils.fileContent(balPath);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MessageUtil.MESSAGE_ID);
        List<CompletionItem> responseItemList = ServerManager.getCompletions(requestMessage);
        List<CompletionItem> expectedList = MessageUtil.getExpectedItemList(expectedItems);

        Assert.assertEquals(true, MessageUtil.isSubList(expectedList, responseItemList));
    }

    @DataProvider(name = "completion-data-provider")
    public abstract Object[][] dataProvider();

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            LOGGER.error("Test Failed for: [" + result.getParameters()[1] + result.getParameters()[0] + "]");
        }
    }
}
