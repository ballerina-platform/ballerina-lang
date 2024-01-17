/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test cases for Extract to Constant code action with quick pick.
 * 
 * @since 2201.4.0
 */
public class ExtractToConstantWithQuickPickAndPositionalRenameTest extends AbstractCodeActionTest {
    
    @Override 
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_QUICKPICK_SUPPORT, true);
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
    }

    @Override
    protected Path getConfigJsonPath(String configFilePath) {
        return FileUtils.RES_DIR.resolve("codeaction")
                .resolve(getResourceDir())
                .resolve("config-positional-rename-and-quick-pick-capability")
                .resolve(configFilePath);
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"extractExprToConstantWithQuickPickAndPositionalRename1.json"},
                {"extractExprToConstantWithQuickPickAndPositionalRename2.json"},
                {"extractExprToConstantWithQuickPickAndPositionalRename3.json"},
                {"extractExprToConstantWithQuickPickAndPositionalRename4.json"},
                {"extractExprToConstantWithQuickPickAndPositionalRename5.json"},
                {"extractExprToConstantWithQuickPickAndPositionalRename6.json"},
                {"extractExprToConstantWithQuickPickAndPositionalRename7.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-constant";
    }
}
