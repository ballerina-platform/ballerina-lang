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
package org.ballerinalang.composer.service.workspace.langserver.compilationunit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.workspace.langserver.FileUtils;
import org.ballerinalang.composer.service.workspace.langserver.MessageUtil;
import org.ballerinalang.composer.service.workspace.langserver.ServerManager;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the suggestions for the compilation unit scope
 */
public class CompilationUnitCompletionTest {

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Gson GSON = new Gson();

    /**
     * Test the completion items suggested when the cursor is at the first line
     * @throws IOException ioException
     * @throws URISyntaxException URISyntaxException
     */
    @Test
    public void testCompilationInitialItems() throws IOException, URISyntaxException {
        String balPath = "compilationunit" + File.separator + "firstLine.bal";
        String expectedResultPath = "compilationunit" + File.separator + "firstLine.exp";
        String content = FileUtils.fileContent(balPath);
        String expectedResult = FileUtils.fileContent(expectedResultPath);
        JsonObject expectedJson = JSON_PARSER.parse(expectedResult).getAsJsonObject();
        JsonArray result = expectedJson.getAsJsonArray("result");
        List<CompletionItem> expectedList = new ArrayList<>();

        Position position = new Position();
        position.setLine(1);
        position.setCharacter(3);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MessageUtil.MESSAGE_ID);
        List<CompletionItem> responseItemList = ServerManager.getCompletions(requestMessage);
        result.forEach(jsonElement -> {
            CompletionItem completionItem = GSON.fromJson(jsonElement, CompletionItem.class);
            expectedList.add(completionItem);
        });
        GSON.fromJson(result, ArrayList.class);
        Assert.assertEquals(true, MessageUtil.listMatches(expectedList, responseItemList));
    }
}
