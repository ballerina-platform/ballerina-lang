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
public class FixReturnTypeTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "fix-return-type";
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
                {"fixReturnType1.json"},
//                {"fixReturnType2.json"}, #36410
                {"fixReturnType3.json"},
                {"fixReturnType4.json"},
                {"fixReturnType5.json"},
                {"fixReturnTypeWithImports1.json"},
                {"fixReturnTypeWithClass1.json"},
//                {"fixReturnTypeWithClass2.json"},
                {"fixReturnTypeWithClass3.json"},
                {"fixReturnTypeWithService1.json"},
                {"fixReturnTypeWithCheckExpr1.json"},
                {"fixReturnTypeWithCheckExpr2.json"},
                {"fixReturnTypeWithCheckExpr3.json"},
                {"fixReturnTypeWithCheckExpr4.json"},
                {"fixReturnTypeInUnionContext1.json"},
                {"fixReturnTypeInUnionContext2.json"},
                {"fixReturnTypeInUnionContext3.json"},
                {"fixReturnTypeInUnionContext4.json"},
                {"fixReturnTypeInCommitAction.json"},
                {"fixReturnTypeInMain1.json"}
        };
    }

    @Test(dataProvider = "negativeDataProvider")
    @Override
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }
    
    @DataProvider
    public Object[][] negativeDataProvider() {
        return new Object[][] {
                {"negativeFixReturnTypeWithMain1.json"}      
        };
    }
}
