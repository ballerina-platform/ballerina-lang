/*
 *  Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
 * Tests the convert to query expression code action.
 *
 * @since 2201.2.1
 */
public class ConvertToQueryExpressionCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"convert_to_query_expr_both_records1.json"},
                {"convert_to_query_expr_lhs_record_type1.json"},
                {"convert_to_query_expr_lhs_non_record1.json"},
                {"convert_to_query_expr_lhs_record_field1.json"},
                {"convert_to_query_expr_lhs_record_field2.json"},
                {"convert_to_query_expr_lhs_record_field3.json"},
                {"convert_to_query_expr_lhs_class_field1.json"},
                {"convert_to_query_expr_lhs_class_field2.json"},
                {"convert_to_query_expr_lhs_record_field_incompatible_types1.json"}
                
        };
    }

    @Override
    @Test(dataProvider = "negativeDataProvider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"convert_to_query_expression_negative1.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "convert-to-query-expr";
    }
}
