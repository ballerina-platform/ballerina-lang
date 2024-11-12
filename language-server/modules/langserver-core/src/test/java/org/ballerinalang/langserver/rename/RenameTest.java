/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.rename;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class for Renaming.
 *
 * @since 0.982.0
 */
public class RenameTest extends AbstractRenameTest {

    @Test(description = "Test Rename", dataProvider = "testDataProvider")
    public void test(String resultJsonPath, String varName) throws IOException {
        super.performTest(resultJsonPath, varName);
    }

    @Override
    protected String configRoot() {
        return "single";
    }

    @Override
    protected String sourceRoot() {
        return "single";
    }

    @DataProvider
    public Object[][] testDataProvider() {
        return new Object[][]{
                {"rename_method_param_result.json", "newA"},
                {"rename_rec_result.json", "NewRecData"},
                {"rename_with_invalid_identifier.json", "NewRecData]"},
                {"rename_with_cursor_at_end.json", "myIntVal"},
                {"rename_with_cursor_at_end2.json", "x"},
                {"rename_with_cursor_at_end3.json", "CyclicUnionNew"},
                {"rename_with_cursor_at_end4.json", "IntegerList"},
                {"rename_on_fail.json", "myVarName"},
                {"rename_within_service1.json", "ep1"},
                {"rename_within_service2.json", "baz"},
                {"rename_within_service3.json", "baz"},
                {"rename_enum.json", "Color"},
                {"rename_enum_member.json", "DARK_RED"},
                {"rename_to_keyword1.json", "int"},
                {"rename_identifier_with_escaped_char1.json", "first\\ name"},
                {"rename_in_undefined_fn_call.json", "counter"},
                {"rename_in_fn_call_rest_args.json", "myValue1"},
                {"rename_in_mapping_binding_pattern1.json", "name2"},
                {"rename_in_mapping_binding_pattern2.json", "pName"},
                {"rename_in_mapping_binding_pattern3.json", "pName"},
                {"rename_with_compilation_error.json", "NewTypeMap"},
                {"rename_in_groupby_clause.json", "newPrice"},

                // Rename parameter documentations
                {"rename_fn_param1.json", "v1"},
                {"rename_record_field1.json", "orgName"},
                
                // Invalid rename positions tests
                {"rename_on_keyword1.json", "fn"},
                {"rename_self.json", "this"},
                {"rename_invalid_qname_ref.json", "io"},
//                {"rename_resource_method_path_segment.json", "path1"} //TODO: Fix #41041
                
                {"rename_table_row_type_parameter1.json", "Student"},
                {"rename_table_row_type_parameter2.json", "Student"},
                {"rename_table_row_type_parameter3.json", "Student"},
                {"rename_stream_type_parameter.json", "Student"},
                {"rename_map_type_parameter.json", "Student"},
        };
    }
}
