/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 2201.2.0
 */
public class MakeFunctionIsolatedCodeActionTest extends AbstractCodeActionTest {
    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Override
    public String getResourceDir() {
        return "make-function-isolated";
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"make_function_isolated_config1.json",
                        "isolatedFunctionCodeAction/modules/module1/make_function_isolated_source1.bal"},
                {"make_function_isolated_config2.json",
                        "isolatedFunctionCodeAction/modules/module2/make_function_isolated_source2.bal"},
                {"make_function_isolated_config3.json",
                        "isolatedFunctionCodeAction/modules/module2/make_function_isolated_source3.bal"}
        };
    }
}
