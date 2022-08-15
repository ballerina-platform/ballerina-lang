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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Tests the functionality of
 * {@link org.ballerinalang.langserver.codeaction.providers.docs.NodeBasedUpdateDocumentationCodeAction}.
 */
public class UpdateDocumentationTest extends AbstractCodeActionTest {

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"updateDocumentationConfig1.json"},
                {"updateDocumentationConfig2.json"},
                {"updateDocumentationConfig3.json"},
                {"updateDocumentationConfig4.json"},
                {"updateDocCodeActionWithDeprecatedConfig1.json"},
        };
    }

    @Override
    public String getResourceDir() {
        return "update-documentation";
    }
}
