/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion.latest;

import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Expression Context tests.
 *
 * @since 2.0.0
 */
public class ExpressionContextTest extends CompletionTestNew {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public Object[][] testSubset() {
        return new Object[][] {
//                {"annotation_access_ctx_config1.json", this.getTestResourceDir()},        
//                {"annotation_access_ctx_config2.json", this.getTestResourceDir()},        
//                {"annotation_access_ctx_config3.json", this.getTestResourceDir()},        
//                {"annotation_access_ctx_config4.json", this.getTestResourceDir()},        
//                {"annotation_access_ctx_config5.json", this.getTestResourceDir()},        
//                {"annotation_access_ctx_config6.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config1.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config1a.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config1b.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config2.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config3.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config4.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config5.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config6.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config7.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config8.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config9.json", this.getTestResourceDir()},        
                {"anon_func_expr_ctx_config10.json", this.getTestResourceDir()},        
                {"check_expression_ctx_config1.json", this.getTestResourceDir()},        
                {"check_expression_ctx_config2.json", this.getTestResourceDir()},        
                {"check_expression_ctx_config3.json", this.getTestResourceDir()},        
                {"check_expression_ctx_config4.json", this.getTestResourceDir()},        
                {"check_expression_ctx_config5.json", this.getTestResourceDir()},        
                {"fail_expr_ctx_config1.json", this.getTestResourceDir()},        
                {"fail_expr_ctx_config2.json", this.getTestResourceDir()},        
                {"fail_expr_ctx_config3.json", this.getTestResourceDir()},        
                {"field_access_ctx_config1.json", this.getTestResourceDir()},        
                {"field_access_ctx_config2.json", this.getTestResourceDir()},        
                {"field_access_ctx_config3.json", this.getTestResourceDir()},        
                {"field_access_ctx_config4.json", this.getTestResourceDir()},        
                {"field_access_ctx_config5.json", this.getTestResourceDir()},        
                {"field_access_ctx_config6.json", this.getTestResourceDir()},        
                {"list_constructor_ctx_config1.json", this.getTestResourceDir()},        
                {"list_constructor_ctx_config2.json", this.getTestResourceDir()},        
                {"list_constructor_ctx_config3.json", this.getTestResourceDir()},        
                {"list_constructor_ctx_config4.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config1.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config2.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config3.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config4.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config5.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config6.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config7.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config8.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config9.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config10.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config11.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config12.json", this.getTestResourceDir()},        
                {"mapping_expr_ctx_config13.json", this.getTestResourceDir()},        
                {"member_access_expr_ctx_config1.json", this.getTestResourceDir()},        
                {"member_access_expr_ctx_config2.json", this.getTestResourceDir()},        
                {"member_access_expr_ctx_config3.json", this.getTestResourceDir()},        
                {"member_access_expr_ctx_config4.json", this.getTestResourceDir()},        
                {"new_expr_ctx_config1.json", this.getTestResourceDir()},        
                {"new_expr_ctx_config1.json", this.getTestResourceDir()},        
                {"new_expr_ctx_config2.json", this.getTestResourceDir()},        
                {"new_expr_ctx_config3.json", this.getTestResourceDir()},
                {"new_expr_ctx_config4.json", this.getTestResourceDir()},
                {"new_expr_ctx_config5.json", this.getTestResourceDir()},        
                {"new_expr_ctx_config6.json", this.getTestResourceDir()},     
//                {"new_expr_ctx_config7.json", this.getTestResourceDir()},   // blocked due to the typeref issue     
//                {"new_expr_ctx_config8.json", this.getTestResourceDir()},   // blocked due to the typeref issue     
//                {"new_expr_ctx_config9.json", this.getTestResourceDir()},   // blocked due to the typeref issue     
//                {"new_expr_ctx_config10.json", this.getTestResourceDir()},  // blocked due to the typeref issue      
                {"object_constructor_expr_ctx_config1.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config2.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config3.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config4.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config5.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config7.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config8.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config9.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config10.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config12.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config13.json", this.getTestResourceDir()},   
                {"object_constructor_expr_ctx_config14.json", this.getTestResourceDir()},   
//                {"optional_field_access_ctx_config1.json", this.getTestResourceDir()},   
//                {"optional_field_access_ctx_config2.json", this.getTestResourceDir()},   
//                {"optional_field_access_ctx_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config1.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config4.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config5a.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config6.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config8.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config9.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config10.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config11.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config12.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config13.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config14.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config15.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config16.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config17.json", this.getTestResourceDir()},   
                {"query_expr_ctx_config18.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config1.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config2.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config5.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config9.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config10.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config12.json", this.getTestResourceDir()},   
                {"query_expr_ctx_join_clause_config13.json", this.getTestResourceDir()},   
                {"query_expr_ctx_limit_clause_config1.json", this.getTestResourceDir()},   
                {"query_expr_ctx_limit_clause_config2.json", this.getTestResourceDir()},   
                {"query_expr_ctx_limit_clause_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_onconflict_clause_config2.json", this.getTestResourceDir()},   
                {"query_expr_ctx_onconflict_clause_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_orderby_clause_config1.json", this.getTestResourceDir()},   
                {"query_expr_ctx_orderby_clause_config2.json", this.getTestResourceDir()},   
                {"query_expr_ctx_orderby_clause_config2a.json", this.getTestResourceDir()},   
                {"query_expr_ctx_orderby_clause_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_orderby_clause_config4.json", this.getTestResourceDir()},   
                {"query_expr_ctx_select_clause_config1.json", this.getTestResourceDir()},   
                {"query_expr_ctx_select_clause_config2.json", this.getTestResourceDir()},   
                {"query_expr_ctx_select_clause_config3.json", this.getTestResourceDir()},   
                {"query_expr_ctx_where_clause_config1.json", this.getTestResourceDir()},   
                {"query_expr_ctx_where_clause_config2.json", this.getTestResourceDir()},   
                {"table_constructor_expr_ctx_config1.json", this.getTestResourceDir()},   
                {"table_constructor_expr_ctx_config3.json", this.getTestResourceDir()},   
                {"table_constructor_expr_ctx_config4.json", this.getTestResourceDir()},   
                {"trap_expression_ctx_config1.json", this.getTestResourceDir()},   
                {"trap_expression_ctx_config2.json", this.getTestResourceDir()},   
                {"trap_expression_ctx_config3.json", this.getTestResourceDir()},   
                {"trap_expression_ctx_config4.json", this.getTestResourceDir()},   
                {"type_test_expression_ctx_config1.json", this.getTestResourceDir()},   
                {"type_test_expression_ctx_config2.json", this.getTestResourceDir()},   
                {"type_test_expression_ctx_config3.json", this.getTestResourceDir()},   
                {"type_test_expression_ctx_config4.json", this.getTestResourceDir()},   
                {"typecast_expr_ctx_config1.json", this.getTestResourceDir()},   
                {"typecast_expr_ctx_config2.json", this.getTestResourceDir()},   
                {"typecast_expr_ctx_config3.json", this.getTestResourceDir()},   
                {"typecast_expr_ctx_config4.json", this.getTestResourceDir()},   
                {"typecast_expr_ctx_config5.json", this.getTestResourceDir()},   
                {"typecast_expr_ctx_config6.json", this.getTestResourceDir()},   
                {"typeof_expression_ctx_config1.json", this.getTestResourceDir()},   
                {"typeof_expression_ctx_config2.json", this.getTestResourceDir()},   
                {"typeof_expression_ctx_config3.json", this.getTestResourceDir()},   
                {"typeof_expression_ctx_config4.json", this.getTestResourceDir()},   
                {"var_ref_ctx_config1.json", this.getTestResourceDir()},   
                {"var_ref_ctx_config2.json", this.getTestResourceDir()},   
                {"var_ref_ctx_config3.json", this.getTestResourceDir()},   
                {"var_ref_ctx_config4.json", this.getTestResourceDir()},   
                {"var_ref_ctx_config5.json", this.getTestResourceDir()},   
        };
    }

    @Override
    public String getTestResourceDir() {
        return "expression_context";
    }

    @Override
    public List<String> skipList() {
        return Arrays.asList("table_constructor_expr_ctx_config2.json",
                "query_expr_ctx_config2.json",
                "query_expr_ctx_config6a.json",
                "query_expr_ctx_config7.json",
                "query_expr_ctx_config5.json",
                "query_expr_ctx_join_clause_config4.json",
                "query_expr_ctx_join_clause_config5a.json",
                "query_expr_ctx_join_clause_config6.json",
                "query_expr_ctx_join_clause_config6a.json",
                "query_expr_ctx_join_clause_config7.json",
                "query_expr_ctx_join_clause_config7a.json",
                "query_expr_ctx_join_clause_config8.json",
                "query_expr_ctx_join_clause_config11.json", // LS fix needed
                "query_expr_ctx_orderby_clause_config4.json", // LS fix needed
                "query_expr_ctx_onconflict_clause_config1.json",
                "query_expr_ctx_onconflict_clause_config1a.json", // LS fix needed
                "object_constructor_expr_ctx_config12a.json",
                "object_constructor_expr_ctx_config6.json", // LS fix needed
                "object_constructor_expr_ctx_config11.json" // LS fix needed
        );
    }
}
