/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for remove import code action.
 *
 * @since 2201.1.1
 */
public class RemoveImportCodeActionTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "remove-import";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"remove_unused_import_config1.json"},
                {"remove_unused_import_config2.json"},
                {"remove_unused_import_config3.json"},
                {"remove_unused_import_config4.json"},
                {"remove_unused_import_config5.json"},
                {"remove_unused_import_config6.json"},
                {"remove_unused_import_config7.json"},
                {"remove_unused_import_config8.json"},
                {"remove_unused_import_config9.json"},
                {"remove_redeclared_import_config.json"}
        };
    }
}
