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
package org.ballerinalang.langserver.completion;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Tests for {@link org.ballerinalang.langserver.completions.providers.context.VariableDeclarationNodeContext}
 * completion provider.
 */
public class VariableDeclarationContextTest extends CompletionTest {

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "variable-declaration";
    }

    @Override
    public List<String> skipList() {
        return List.of(
                // expected type cases
                "var_def_ctx_config16.json" // issue #38711
        );
    }
}
