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
                {"typeCast1.json"},
                {"typeCast2.json"},
                {"typeCast3.json"},
                {"typeCast4.json"},
                {"typeCast5.json"},
//                {"typeCast6.json", "typeCast.bal"}, Not supported by the subtype of API.
                {"typeCast7.json"},
                {"typeCast8.json"},
                {"typeCast9.json"},
                {"typeCast10.json"},
                {"typeCast11.json"},
                {"typeCast12.json"},
                {"typeCast13.json"},
                {"typeCast14.json"},
                {"nilTypeCast.json"},
                {"type_cast_function_param_config1.json"},
                {"typeCast11.json"},
                {"typeCastInMemberAccess1.json"},

                {"type_cast_in_binary_operation1.json"},
                {"type_cast_in_binary_operation2.json"},
                {"type_cast_in_binary_operation3.json"},
                {"type_cast_in_binary_operation4.json"},
                {"type_cast_in_binary_operation5.json"},
                {"type_cast_in_binary_operation6.json"},
                {"type_cast_in_binary_operation7.json"},
                {"type_cast_in_binary_operation8.json"},
                {"type_cast_in_binary_operation9.json"},
                {"type_cast_in_assignment1.json"},
                {"type_cast_in_conditional_expr1.json"},
                {"type_cast_in_conditional_expr2.json"},
                {"type_cast_in_conditional_expr3.json"},
                {"type_cast_numeric1.json"},
                {"type_cast_in_obj_field_config1.json"},
                {"type_cast_in_obj_field_config2.json"}
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"typeCastNegative1.json"},
                {"typeCastNegative2.json"},
                {"typeCastNegative3.json"},
                {"typeCastNegative4.json"},
                {"typeCastNegative5.json"},
                {"typeCastNegative6.json"},
                {"typeCastNegativeInLangLib.json"}
        };
    }
}
