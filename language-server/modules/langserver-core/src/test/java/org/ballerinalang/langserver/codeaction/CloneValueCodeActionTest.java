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
 * Test class to test clone value code action.
 *
 * @since 2201.10.0
 */
public class CloneValueCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"clone_value_config1.json"},
                {"clone_value_config2.json"},
                {"clone_value_config3.json"},
                {"clone_value_config4.json"},
                {"clone_value_config5.json"},
                {"clone_value_config6.json"},
                {"clone_value_config7.json"},
                {"clone_value_config8.json"},
                {"clone_value_config9.json"},
                {"clone_value_config10.json"},
                {"clone_value_config11.json"},
                {"clone_value_config12.json"},
                {"clone_value_config13.json"},
                {"clone_value_config14.json"},
                {"clone_value_config15.json"},
                {"clone_value_config16.json"},
                {"clone_value_config17.json"},
                {"clone_value_config18.json"},
                {"clone_value_config19.json"},
                {"clone_value_config20.json"},
                {"clone_value_config21.json"},
                {"clone_value_config22.json"},
                {"clone_value_config23.json"},
                {"clone_value_config24.json"},
                {"clone_value_config25.json"},
                {"clone_value_config26.json"},
                {"clone_value_config27.json"},
                {"clone_value_config28.json"},
                {"clone_value_config29.json"},
                {"clone_value_config30.json"},
                {"clone_value_config31.json"},
                {"clone_value_config32.json"},
                {"clone_value_config33.json"},
                {"clone_value_config34.json"},
                {"clone_value_config35.json"},
                {"clone_value_config36.json"},
                {"clone_value_config37.json"},
                {"clone_value_config38.json"},
                {"clone_value_config39.json"},
                {"clone_value_config40.json"},
                {"clone_value_config41.json"},
                {"clone_value_config42.json"},
                {"clone_value_config43.json"},
                {"clone_value_config44.json"},
                {"clone_value_config45.json"},
                {"clone_value_config46.json"},
                {"clone_value_config47.json"},
                {"clone_value_config48.json"},
                {"clone_value_config49.json"},
                {"clone_value_config50.json"},
                {"clone_value_config51.json"},
                {"clone_value_config52.json"},
                {"clone_value_config53.json"},
                {"clone_value_config54.json"},
                {"clone_value_config55.json"}
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
                {"clone_value_negative_config1.json"},
                {"clone_value_negative_config2.json"},
                {"clone_value_negative_config3.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "clone-value";
    }
}
