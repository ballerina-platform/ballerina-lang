/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.test.completion;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Completion tests for compiler plugin based completion providers.
 */
public class CompilerPluginCompletionTests extends CompletionTest {

    @BeforeSuite
    public void compilePlugins() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_with_completions");
    }

    @Override
    @Test(dataProvider = "completion-data-provider", enabled = false)
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"compiler_plugin_with_completions_config1.json", getTestResourceDir()},
                {"compiler_plugin_with_completions_config2.json", getTestResourceDir()},
                {"compiler_plugin_completion_single_file_config1.json", getTestResourceDir()}
        };
    }

    @Override
    public String getTestResourceDir() {
        return "compiler-plugins";
    }

}

