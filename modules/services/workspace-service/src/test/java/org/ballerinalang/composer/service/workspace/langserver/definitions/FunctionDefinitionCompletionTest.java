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

import org.ballerinalang.composer.service.workspace.langserver.FileUtils;
import org.ballerinalang.composer.service.workspace.langserver.MessageUtil;
import org.ballerinalang.composer.service.workspace.langserver.ServerManager;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test the completion items in a function definition
 */
public class FunctionDefinitionCompletionTest {

    public static final String MESSAGE_ID = "1111-1111-1111";

    /**
     * Test the completion items suggested when the cursor is at the first line
     * @throws IOException ioException
     * @throws URISyntaxException URISyntaxException
     */
    public void testFunctionBlockStatementEmptyFirstLine() throws IOException, URISyntaxException {
        String balPath = "definitions" + File.separator + "functions" + File.separator + "blockStmtEmptyFirstLine.bal";
        String expectedResultPath =  "definitions" + File.separator + "functions" + File.separator
                + "blockStmtEmptyFirstLine.exp";
        String content = FileUtils.fileContent(balPath);
        String expectedResult = FileUtils.fileContent(expectedResultPath);
        Position position = new Position();
        position.setLine(2);
        position.setCharacter(4);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MESSAGE_ID);
        Assert.assertEquals(expectedResult, ServerManager.getCompletions(requestMessage));
    }

    /**
     * Test the completion items suggested when the cursor is at the first line and the line is non empty
     * @throws IOException ioException
     * @throws URISyntaxException URISyntaxException
     */
    public void testFunctionBlockStatementNonEmptyFirstLine() throws IOException, URISyntaxException {
        String balPath = "definitions" + File.separator + "functions" + File.separator
                + "blockStmtNonEmptyFirstLine.bal";
        String expectedResultPath =  "definitions" + File.separator + "functions" + File.separator
                + "blockStmtNonEmptyFirstLine.exp";
        String content = FileUtils.fileContent(balPath);
        String expectedResult = FileUtils.fileContent(expectedResultPath);
        Position position = new Position();
        position.setLine(2);
        position.setCharacter(5);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MESSAGE_ID);
        Assert.assertEquals(expectedResult, ServerManager.getCompletions(requestMessage));
    }
}
