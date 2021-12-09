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
public class TypeCastTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "type-cast";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config, String source) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"typeCast1.json", "typeCast.bal"},
                {"typeCast2.json", "typeCast.bal"},
                {"typeCast3.json", "typeCast.bal"},
                {"typeCast4.json", "typeCast.bal"},
                {"typeCast5.json", "typeCast.bal"},
//                {"typeCast6.json", "typeCast.bal"}, Not supported by the subtype of API.
                {"typeCast7.json", "typeCast.bal"},
                {"typeCast8.json", "typeCast.bal"},
                {"typeCast9.json", "typeCast.bal"},
                {"typeCast10.json", "typeCast.bal"},
                {"typeCast11.json", "typeCast.bal"},
                {"nilTypeCast.json", "typeCast.bal"},
                {"type_cast_function_param_config1.json", "type_cast_function_param_config1.bal"},
                {"typeCast11.json", "typeCast.bal"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"typeCastNegative1.json", "typeCast2.bal"},
                {"typeCastNegative2.json", "typeCast.bal"},
                {"typeCastNegative3.json", "typeCast.bal"},
                {"typeCastNegative4.json", "typeCast.bal"},
        };
    }
}
