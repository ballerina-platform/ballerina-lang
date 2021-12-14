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
 * Tests {@link org.ballerinalang.langserver.codeaction.providers.AddCheckCodeAction}'s functionality.
 */
public class AddCheckCodeActionTest extends AbstractCodeActionTest {

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config, String source) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"add_check_codeaction_config1.json", "add_check_codeaction_source1.bal"},
                {"add_check_codeaction_config2.json", "add_check_codeaction_source2.bal"},
                {"add_check_codeaction_config3.json", "add_check_codeaction_source3.bal"},
                {"add_check_codeaction_config4.json", "add_check_codeaction_source4.bal"},
                {"add_check_codeaction_config5.json", "add_check_codeaction_source4.bal"},
                {"add_check_with_parantheses_config1.json", "add_check_with_parantheses_source1.bal"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negative_add_check_codeaction_config1.json", "negative_add_check_codeaction_source1.bal"},
        };
    }

    @Override
    public String getResourceDir() {
        return "add-check";
    }
}
