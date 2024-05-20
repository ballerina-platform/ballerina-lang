/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class to test adding readonly to the type.
 *
 * @since 2201.10.0
 */
public class AddReadonlyCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"add_readonly_config1.json"},
                {"add_readonly_config2.json"},
                {"add_readonly_config3.json"},
                {"add_readonly_config4.json"},
                {"add_readonly_config5.json"},
                {"add_readonly_config6.json"},
                {"add_readonly_config7.json"},
                {"add_readonly_config8.json"},
                {"add_readonly_config9.json"},
                {"add_readonly_config10.json"},
                {"add_readonly_config11.json"},
                {"add_readonly_config12.json"},
                {"add_readonly_config13.json"},
                {"add_readonly_config14.json"},
                {"add_readonly_config15.json"},
                {"add_readonly_config16.json"},
                {"add_readonly_config17.json"},
                {"add_readonly_config18.json"},
                {"add_readonly_config19.json"},
                {"add_readonly_config20.json"},
                {"add_readonly_config21.json"},
                {"add_readonly_config22.json"},
                {"add_readonly_config23.json"},
                {"add_readonly_config24.json"},
        };
    }

    @Override
    public String getResourceDir() {
        return "add-readonly";
    }
}
