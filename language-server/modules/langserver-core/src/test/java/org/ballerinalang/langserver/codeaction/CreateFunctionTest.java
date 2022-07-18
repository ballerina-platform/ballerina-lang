/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class CreateFunctionTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "create-function";
    }

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
                {"undefinedFunctionCodeAction.json"},
                {"undefinedFunctionCodeAction2.json"},
                {"undefinedFunctionCodeAction3.json"},
                {"undefinedFunctionCodeAction4.json"},
                {"undefinedFunctionCodeAction5.json"},
                {"undefinedFunctionCodeActionInRecord.json"},
                {"undefinedFunctionCodeActionInRecord2.json"},
                {"undefinedFunctionCodeActionInLet.json"},
                {"undefinedFunctionCodeActionInLet2.json"},
                {"createFunctionCodeActionWithStrands.json"},
                {"createFunctionInErrorConstructor.json"},
                {"undefinedFunctionCodeActionInObjectField1.json"},
                {"undefinedFunctionCodeActionInRecordField1.json"},
                {"undefinedFunctionInConditionalExpression1.json"},
                {"undefinedFunctionInConditionalExpression2.json"},
                {"undefinedFunctionInConditionalExpression3.json"},
                {"undefinedFunctionInCheckExpression1.json"},
                {"undefinedFunctionInCheckExpression2.json"},
                {"undefinedFunctionInCheckpanicExpression1.json"},
                {"undefinedFunctionInCheckpanicExpression2.json"},
                {"undefinedFunctionInPanicStatement.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"undefinedFunctionCodeActionNegativeTest1.json"},
                {"undefinedFunctionCodeActionNegativeTest2.json"}
        };
    }
}
