/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases for ModVarToListenerDeclCodeAction.
 *
 * @since 2.0.0
 */
public class ModVarToListenerDeclCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Test(dataProvider = "negative-test-data-provider")
    @Override
    public void negativeTest(String config, String source) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"modVarToListenerDecl1.json", "modVarToListenerDecl1.bal"},
                {"modVarToListenerDecl2.json", "project/main.bal"},
                {"modVarToListenerDecl3.json", "modVarToListenerDecl3.bal"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeTestDataProvider() {
        return new Object[][]{
                {"negative_modVarToListnerDecl1.json", "negative_modVarToListnerDecl1.bal"},
                {"negative_modVarToListnerDecl2.json", "negative_modVarToListnerDecl2.bal"}
        };
    }

    @Override
    public String getResourceDir() {
        return "modulevar-listenerdecl";
    }
}
