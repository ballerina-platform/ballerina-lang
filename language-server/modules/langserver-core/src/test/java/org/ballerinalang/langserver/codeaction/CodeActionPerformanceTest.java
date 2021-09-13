/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Range;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test performance of code action feature in language server.
 *
 * @since 2.0.0
 */
public class CodeActionPerformanceTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "performance-codeaction";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Override
    public String getResponse(Path sourcePath, Range range, CodeActionContext codeActionContext) {
        long start = System.currentTimeMillis();
        String res = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);
        long end = System.currentTimeMillis();
        long actualResponseTime = end - start;
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold")) / 2;
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
        return res;
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"performance_codeaction.json", "performance_codeaction.bal"},
        };
    }
}
