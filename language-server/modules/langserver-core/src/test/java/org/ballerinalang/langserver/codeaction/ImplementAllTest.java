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
public class ImplementAllTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "implement-all";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"implementAllInObjConstructorExpr1.json", "implementAllInObjConstructorExpr.bal"},
                {"implementAllInObjConstructorExpr2.json", "implementAllInObjConstructorExpr.bal"},
                {"implementAllInService1.json", "implementAllInService.bal"},
                {"implementAllInService2.json", "implementAllInService.bal"},
                {"implementAllObj1.json", "implementAllObj.bal"},
                {"implementAllObj2.json", "implementAllObj.bal"},
        };
    }
}
