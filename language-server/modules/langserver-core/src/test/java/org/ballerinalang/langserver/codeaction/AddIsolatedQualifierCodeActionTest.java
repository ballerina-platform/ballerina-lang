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
 * Test Cases for Add Isolated Qualifier Code Action.
 *
 * @since 2201.1.1
 */
public class AddIsolatedQualifierCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    public String getResourceDir() {
        return "add-isolated-qualifier";
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"add_isolated_qualifier_config1.json"},
                {"add_isolated_qualifier_config2.json"},
                {"add_isolated_qualifier_config3.json"},
                {"add_isolated_qualifier_config4.json"},
                {"add_isolated_qualifier_config5.json"},
                {"add_isolated_qualifier_config6.json"},
                {"add_isolated_qualifier_config7.json"},
                {"add_isolated_qualifier_config8.json"},
                {"add_isolated_qualifier_config9.json"},
                {"add_isolated_qualifier_config10.json"},
                {"add_isolated_qualifier_config11.json"},
                {"add_isolated_qualifier_config12.json"},
                {"add_isolated_qualifier_config13.json"},
        };
    }
}
