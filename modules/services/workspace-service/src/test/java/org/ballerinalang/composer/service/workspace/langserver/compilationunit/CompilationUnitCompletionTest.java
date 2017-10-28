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

import org.ballerinalang.composer.service.workspace.langserver.FileUtils;
import org.ballerinalang.composer.service.workspace.langserver.MessageUtil;
import org.ballerinalang.composer.service.workspace.langserver.ServerManager;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test the suggestions for the compilation unit scope
 */
public class CompilationUnitCompletionTest {
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
        Position position = new Position();
        position.setLine(1);
        position.setCharacter(3);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, "firstLineTest");
        Assert.assertEquals(expectedResult, ServerManager.getCompletions(requestMessage));
    }
}
