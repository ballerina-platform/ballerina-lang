/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class to test the extract to transform function code action.
 *
 * @since 2201.9.0
 */
public class ExtractToTransformFunctionCodeActionTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
    }

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"extract_to_transform_function1.json"},
                {"extract_to_transform_function2.json"},
                {"extract_to_transform_function3.json"},
                {"extract_to_transform_function4.json"},
                {"extract_to_transform_function5.json"},
                {"extract_to_transform_function6.json"},
                {"extract_to_transform_function7.json"},
                {"extract_to_transform_function8.json"},
                {"extract_to_transform_function9.json"},
                {"extract_to_transform_function10.json"},
                {"extract_to_transform_function11.json"},
                {"extract_to_transform_function12.json"},
                {"extract_to_transform_function13.json"},
                {"extract_to_transform_function14.json"},
                {"extract_to_transform_function15.json"},
                {"extract_to_transform_function16.json"},
                {"extract_to_transform_function17.json"},
                {"extract_to_transform_function18.json"},
                {"extract_to_transform_function19.json"},
                {"extract_to_transform_function20.json"},
                {"extract_to_transform_function21.json"}
        };
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"extract_to_transform_function_negative1.json"},
                {"extract_to_transform_function_negative2.json"},
                {"extract_to_transform_function_negative3.json"},
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-transform-function";
    }
}
