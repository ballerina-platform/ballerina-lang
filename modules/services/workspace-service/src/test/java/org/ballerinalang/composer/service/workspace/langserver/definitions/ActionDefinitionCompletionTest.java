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
package org.ballerinalang.composer.service.workspace.langserver.definitions;

import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.util.FileUtils;
import org.ballerinalang.composer.service.workspace.langserver.util.MessageUtil;
import org.ballerinalang.composer.service.workspace.langserver.util.ServerManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Test the completion items in a action definition.
 */
public class ActionDefinitionCompletionTest {

    /**
     * Test the completion items suggested when the cursor is at the first line.
     * @throws IOException ioException
     * @throws URISyntaxException URISyntaxException
     */
    @Test
    public void testActionBlockStatementEmptyFirstLine() throws IOException, URISyntaxException {
        String balPath = "definitions" + File.separator + "actions" + File.separator + "blockStmtEmptyFirstLine.bal";
        String expectedResultPath =  "definitions" + File.separator + "actions" + File.separator
                + "blockStmtEmptyFirstLine.exp";
        String content = FileUtils.fileContent(balPath);
        List<CompletionItem> expectedList = MessageUtil.getExpectedItemList(expectedResultPath);

        Position position = new Position();
        position.setLine(3);
        position.setCharacter(8);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MessageUtil.MESSAGE_ID);
        List<CompletionItem> responseItemList = ServerManager.getCompletions(requestMessage);
        Assert.assertEquals(true, MessageUtil.listMatches(expectedList, responseItemList));
    }

    /**
     * Test the completion items suggested when the cursor is at the first line and the line is non empty.
     * @throws IOException ioException
     * @throws URISyntaxException URISyntaxException
     */
    @Test
    public void testActionBlockStatementNonEmptyFirstLine() throws IOException, URISyntaxException {
        String balPath = "definitions" + File.separator + "actions" + File.separator
                + "blockStmtNonEmptyFirstLine.bal";
        String expectedResultPath =  "definitions" + File.separator + "actions" + File.separator
                + "blockStmtNonEmptyFirstLine.exp";
        String content = FileUtils.fileContent(balPath);
        List<CompletionItem> expectedList = MessageUtil.getExpectedItemList(expectedResultPath);

        Position position = new Position();
        position.setLine(3);
        position.setCharacter(9);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MessageUtil.MESSAGE_ID);
        List<CompletionItem> responseItemList = ServerManager.getCompletions(requestMessage);
        Assert.assertEquals(true, MessageUtil.listMatches(expectedList, responseItemList));
    }

    /**
     * Test the completion items suggested when the cursor is at a middle line and the line is non empty.
     * @throws IOException ioException
     * @throws URISyntaxException URISyntaxException
     */
    @Test
    public void testActionBlockStatementNonEmptyMiddleLine() throws IOException, URISyntaxException {
        String balPath = "definitions" + File.separator + "actions" + File.separator
                + "blockStmtNonEmptyMiddleLine.bal";
        String expectedResultPath =  "definitions" + File.separator + "actions" + File.separator
                + "blockStmtNonEmptyMiddleLine.exp";
        String content = FileUtils.fileContent(balPath);
        List<CompletionItem> expectedList = MessageUtil.getExpectedItemList(expectedResultPath);

        Position position = new Position();
        position.setLine(4);
        position.setCharacter(9);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MessageUtil.MESSAGE_ID);
        List<CompletionItem> responseItemList = ServerManager.getCompletions(requestMessage);
        Assert.assertEquals(true, MessageUtil.listMatches(expectedList, responseItemList));
    }
}
