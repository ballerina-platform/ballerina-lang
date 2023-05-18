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
package org.ballerinalang.langserver.test.codeaction;

import org.ballerinalang.langserver.codeaction.AbstractCodeActionTest;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Code action tests for compiler plugin based code actions.
 */
public class CompilerPluginCodeActionTests extends AbstractCodeActionTest {

    @BeforeSuite
    public void compilePlugins() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_with_codeactions");
    }

    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"compiler_plugin_code_action_config1.json"},
                {"compiler_plugin_code_action_single_file_config1.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "compiler-plugins";
    }

    @Override
    protected Object convertActionData(Object actionData) {
        return super.convertActionData(actionData);
    }
}
