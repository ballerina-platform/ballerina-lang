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
 * @since 2201.0.4
 */
public class MakeConstructPublicCodeActionTest extends AbstractCodeActionTest {
    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Override
    public String getResourceDir() {
        return "make-construct-public";
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"convert_to_public_class_config1.json",
                        "publicCodeAction/modules/module1/convert_to_public_class_source1.bal"},
                {"convert_to_public_record_config2.json",
                        "publicCodeAction/modules/module2/convert_to_public_record_source2.bal"}
        };
    }
}
