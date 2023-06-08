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

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class AddDocumentationTest extends AbstractCodeActionTest {
    @Override
    public String getResourceDir() {
        return "add-documentation";
    }

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Test(dataProvider = "negativeDataProvider")
    @Override
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"singleDocGeneration.json"},
                {"singleDocGeneration1.json"},
                {"singleDocGeneration3.json"},
                {"singleDocGeneration4.json"},
                {"singleDocGeneration5.json"},
                // Non top level node doc suggestions
                {"singleDocGeneration2.json"},
                {"singleDocGeneration6.json"},
                {"singleDocGeneration7.json"},
                {"singleDocGeneration8.json"},
                {"singleDocGeneration9.json"},
                {"singleDocGeneration10.json"},
                {"singleDocGeneration11.json"},
                {"singleDocGeneration12.json"},
                // Within Service
                {"serviceDocumentation1.json"},
                // Already documented nodes
                {"documentAlreadyDocumentedConfig1.json"},
        };
    }

    @DataProvider
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negativeDocGeneration1.json"},
        };
    }
}
