/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
 * Test Cases for Fill Record Fields code action.
 *
 * @since 2201.2.3
 */
public class FillRecordFieldsCodeActionTest extends AbstractCodeActionTest {
    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    public String getResourceDir() {
        return "fill-record-fields";
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"fill_record_fields_config1.json"},
                {"fill_record_fields_config2.json"},
                {"fill_record_fields_config3.json"},
                {"fill_record_fields_config4.json"},
                {"fill_record_fields_config5.json"},
                {"fill_record_fields_config6.json"},
                {"fill_record_fields_config7.json"},
                {"fill_record_fields_config8.json"},
                {"fill_record_fields_config9.json"},
                {"fill_record_fields_config10.json"},
                {"fill_record_fields_config11.json"},
                {"fill_record_fields_config12.json"}
        };
    }
}
