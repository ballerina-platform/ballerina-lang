/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Tests cases for {@link org.ballerinalang.langserver.codeaction.providers.AddToConfigTomlCodeAction}.
 *
 * @since 2201.10.0
 */
public class AddToConfigTomlTest extends AbstractCodeActionTest {

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"with_config_toml_default_module1.json"},
                {"with_config_toml_default_module2.json"},
                {"with_config_toml_default_module3.json"},
                {"with_config_toml_default_module4.json"},
                {"with_config_toml_default_module5.json"},
                {"with_config_toml_default_module6.json"},
                {"with_config_toml_non_default_module1.json"},
                {"with_config_toml_non_default_module2.json"},
                {"with_config_toml_non_default_module3.json"},
                {"with_config_toml_non_default_module4.json"},
                {"with_config_toml_non_default_module5.json"},
                {"with_config_toml_non_default_module6.json"},
                {"without_config_toml_default_module1.json"},
                {"without_config_toml_default_module2.json"},
                {"without_config_toml_default_module3.json"},
                {"without_config_toml_non_default_module1.json"},
                {"without_config_toml_non_default_module2.json"},
                {"without_config_toml_non_default_module3.json"}
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"add_to_config_toml_negative1.json"},
                {"add_to_config_toml_negative2.json"},
                {"add_to_config_toml_negative3.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "add-to-config-toml";
    }
}
