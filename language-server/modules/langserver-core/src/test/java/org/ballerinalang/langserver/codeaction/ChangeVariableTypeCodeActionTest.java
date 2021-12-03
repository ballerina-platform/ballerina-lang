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
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class ChangeVariableTypeCodeActionTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "change-var-type";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
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
                {"changeVarType1.json", "changeVarType.bal"},
                {"changeVarType2.json", "changeVarType.bal"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeTestDataProvider() {
        return new Object[][]{
                {"negative_changeVarType1.json", "negative_changeVarType1.bal"},
                {"negative_changeVarType2.json", "negative_changeVarType2.bal"},
                {"negative_changeVarType3.json", "negative_changeVarType3.bal"},
        };
    }
}
