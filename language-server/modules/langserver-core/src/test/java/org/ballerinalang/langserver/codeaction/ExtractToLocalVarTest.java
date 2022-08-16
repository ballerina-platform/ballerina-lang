/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases for Extract to Variable code action.
 * 
 * @since 2201.2.1
 */
public class ExtractToLocalVarTest extends AbstractCodeActionTest {

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
                {"extractToVariableInBinaryExpression.json"},
                {"extractToVariableInBinaryExpression2.json"},
                {"extractToVariableInBracedExpression.json"},
                {"extractToVariableInQNameRef.json"},
                {"extractToVariableInIndexedExpression.json"},
                {"extractToVariableInFieldAccess.json"},
                {"extractToVariableInMethodCall.json"},
                {"extractToVariableInMethodCall2.json"},
                {"extractToVariableInCheckExpression.json"},
                {"extractToVariableInMappingConstructor.json"},
                {"extractToVariableInMappingConstructor2.json"},
                {"extractToVariableInMappingConstructor3.json"},
                {"extractToVariableInTypeofExpression.json"},
                {"extractToVariableInUnaryExpression.json"},
                {"extractToVariableInTypeTestExpression.json"},
                {"extractToVariableInTrapExpression.json"},
                {"extractToVariableInTrapExpression2.json"},
                {"extractToVariableInListConstructor.json"},
                {"extractToVariableInTypeCastExpression.json"},
                {"extractToVariableInTableConstructor.json"},
                {"extractToVariableInImplicitNewExpression.json"},
                {"extractToVariableInExplicitNewExpression.json"},
                {"extractToVariableInObjectConstructor.json"},
                {"extractToVariableInErrorConstructor.json"}
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"extractToVariableInFunctionCallNegative.json"},
                {"extractToVariableInTableConstructorNegative.json"},
        };
    }
    
    @Override
    public String getResourceDir() {
        return "extract-to-local-variable";
    }
}
