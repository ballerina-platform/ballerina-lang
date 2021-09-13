/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;


/**
 * Test performance of completions feature in language server.
 *
 *  * @since 2.0.0
 */
public class CompletionPerformanceTest extends CompletionTest {

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @Override
    public String getResponse(Path sourcePath, Position position, String triggerChar) throws IOException {
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        long start = System.currentTimeMillis();
        String responseString = TestUtil.getCompletionResponse(sourcePath.toString(), position,
                this.serviceEndpoint, triggerChar);
        long end = System.currentTimeMillis();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        long actualResponseTime = end - start;
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold")) / 2;
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
        return responseString;
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "performance_completion";
    }
}
