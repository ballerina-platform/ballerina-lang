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
 * Test class to test the converting a variable to immutable code action.
 *
 * @since 2201.9.0
 */
public class MakeVariableImmutableCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"make_variable_immutable1.json"},
                {"make_variable_immutable2.json"},
                {"make_variable_immutable3.json"},
                {"make_variable_immutable4.json"},
                {"make_variable_immutable5.json"},
                {"make_variable_immutable6.json"},
                {"make_variable_immutable7.json"},
                {"make_variable_immutable8.json"},
                {"make_variable_immutable9.json"},
                {"make_variable_immutable10.json"},
                {"make_variable_immutable11.json"},
                {"make_variable_immutable12.json"},
                {"make_variable_immutable13.json"},
                {"make_variable_immutable14.json"},
                {"make_variable_immutable15.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "make-variable-immutable";
    }
}
