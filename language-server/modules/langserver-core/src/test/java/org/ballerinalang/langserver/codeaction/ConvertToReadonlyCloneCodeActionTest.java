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
 * Tests {@link org.ballerinalang.langserver.codeaction.providers.ConvertToReadonlyCloneCodeAction}'s functionality.
 */
public class ConvertToReadonlyCloneCodeActionTest extends AbstractCodeActionTest {

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"convert_to_readonly_clone_config1.json", "convert_to_readonly_clone_source1.bal"},
                {"convert_to_readonly_clone_config2.json", "convert_to_readonly_clone_source2.bal"},
                {"convert_to_readonly_clone_config3.json", "convert_to_readonly_clone_source3.bal"},
                {"convert_to_readonly_clone_config4.json", "convert_to_readonly_clone_source4.bal"},
                {"convert_to_readonly_clone_config5.json", "convert_to_readonly_clone_source5.bal"},
                {"convert_to_readonly_clone_config6.json", "convert_to_readonly_clone_source6.bal"},
                {"convert_to_readonly_clone_config7.json", "convert_to_readonly_clone_source7.bal"},
        };
    }

    @Override
    public String getResourceDir() {
        return "convert-readonly-clone";
    }
}
