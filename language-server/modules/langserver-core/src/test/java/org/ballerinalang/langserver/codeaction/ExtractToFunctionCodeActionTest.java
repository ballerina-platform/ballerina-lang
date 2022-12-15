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
 * Test class to test the functionality of the extract to function code action.
 *
 * @since 2201.2.1
 */
public class ExtractToFunctionCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
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
                // statements and list

                // list
                {"extract_to_function_stmts_list_without_return.json"},
                {"extract_to_function_stmts_list_with_return.json"},
                {"extract_to_function_stmts_list_with_param_without_return.json"},
                {"extract_to_function_stmts_list_with_param_with_return.json"},
                {"extract_to_function_stmts_list_with_const_with_return.json"},
                {"extract_to_function_stmts_list_with_moduleVar_with_return.json"},
                {"extract_to_function_stmts_list_assigned_twice.json"},
                {"extract_to_function_stmts_list_multipleLocalVarDefsInRange_with_single_return.json"},
                {"extract_to_function_stmts_list_with_mul_modVar_asgmnts.json"},
                {"extract_to_function_stmts_list_with_mul_modVar_and_locVar_asgmts.json"},
                {"extract_to_function_stmts_list_varDeclOnlyInRange_return_single.json"},
                {"extract_to_function_stmts_list_with_ifElseStatement.json"},
                {"extract_to_function_stmts_list_with_compound_assignments.json"},
                {"extract_to_function_stmts_list_with_asgnmntAndCompAsgnmnt_sameVar.json"},
                {"extract_to_function_stmts_list_with_moduleVar_assignmentStatement.json"},
                {"extract_to_function_stmts_list_with_moduleVar_comAssignmentStatement.json"},
                {"extract_to_function_stmts_within_class_with_self_keyword.json"},

                // statements
                {"extract_to_function_stmts_block_stmt_inside_block.json"},

                {"extract_to_function_stmts_local_var_decl_with_init.json"},
                {"extract_to_function_stmts_local_var_decl_with_init_range_till_closing_bracket.json"},
                {"extract_to_function_stmts_local_var_decl_without_init.json"},

                {"extract_to_function_stmts_assignment_stmt_moduleVar.json"},
                {"extract_to_function_stmts_com_assignment_stmt_moduleVar.json"},

                {"extract_to_function_stmts_if_else_stmt_with_if_only.json"},
                {"extract_to_function_stmts_if_else_stmt_with_if_and_else.json"},
                {"extract_to_function_stmts_if_else_stmt_with_nested_if.json"},
                {"extract_to_function_stmts_if_else_stmt_inside_nested_while_stmts.json"},

                {"extract_to_function_stmts_while_stmt.json"},
                {"extract_to_function_stmts_while_stmt_with_local_var_referred.json"},

//                {"extract_to_function_stmts_return_stmt.json"}, // todo support return statements

                {"extract_to_function_stmts_lock_stmt.json"},

                {"extract_to_function_stmts_foreach_stmt.json"},
                {"extract_to_function_stmts_foreach_stmt_with_range_expr.json"},
                {"extract_to_function_stmts_foreach_stmt_without_iterable_declared_inside.json"},

                {"extract_to_function_stmts_match_stmt_without_return_stmt.json"},

                {"extract_to_function_stmts_do_stmt_without_onfail_stmt.json"},
                {"extract_to_function_stmts_do_stmt_with_onfail_stmt.json"},

                {"extract_to_function_stmts_within_isolated_function.json"},
                {"extract_to_function_stmts_within_isolated_resource_function.json"},
                {"extract_to_function_stmts_within_class_method.json"},
                {"extract_to_function_stmts_within_isolated_class_method.json"},
                {"extract_to_function_stmts_within_remote_method.json"},

                // expressions

                //  expressions by range
                {"extract_to_function_exprs_numeric_literal.json"},
                {"extract_to_function_exprs_binary_expr.json"},
                {"extract_to_function_exprs_binary_expr_selecting_plus_token.json"},
                {"extract_to_function_exprs_braced_expr.json"},
                {"extract_to_function_exprs_qual_name_ref.json"},
                {"extract_to_function_exprs_indexed_expr.json"},
                {"extract_to_function_exprs_indexed_expr_selecting_open_bracket.json"},
                {"extract_to_function_exprs_field_access_expr_record.json"},
                {"extract_to_function_exprs_field_access_expr_object.json"},
                {"extract_to_function_exprs_method_call.json"},
                {"extract_to_function_exprs_mapping_constructor_module.json"},
                {"extract_to_function_exprs_mapping_constructor_local.json"},
                {"extract_to_function_exprs_mapping_constructor_imported_module.json"},
                {"extract_to_function_exprs_computed_name_field.json"},
                {"extract_to_function_exprs_field_name_expr.json"},
                {"extract_to_function_exprs_typeof_expr.json"},
                {"extract_to_function_exprs_unary_expr.json"},
                {"extract_to_function_exprs_unary_expr_selecting_unaryOperator.json"},
                {"extract_to_function_exprs_typeTest_expr.json"},
                {"extract_to_function_exprs_list_constructor.json"},
                {"extract_to_function_exprs_typecast_expr.json"},
                {"extract_to_function_exprs_table_constructor.json"},
                {"extract_to_function_exprs_let_expr.json"},
                {"extract_to_function_exprs_let_expr_selecting_expression.json"},
                {"extract_to_function_exprs_let_expr_selecting_let_var_decl_expr.json"},
                {"extract_to_function_exprs_let_expr_with_non_let_var_decls_referred.json"},
                {"extract_to_function_exprs_binary_expr_in_let_expr.json"},
                {"extract_to_function_exprs_implicit_new_expr.json"},
                {"extract_to_function_exprs_explicit_new_expr.json"},
//                {"extract_to_function_exprs_object_constructor.json"}, // todo support later
                {"extract_to_function_exprs_error_constructor.json"},
                {"extract_to_function_exprs_within_isolated_function.json"},

                // expressions by position
                {"extract_to_function_exprs_position_numeric_literal_cur_after_literal.json"},
                {"extract_to_function_exprs_position_numeric_literal_cur_inside_literal.json"},
                {"extract_to_function_exprs_position_binary_expr_constant.json"},
                {"extract_to_function_exprs_position_binary_expr_moduleVar.json"},
                {"extract_to_function_exprs_position_binary_expr_parameter.json"},
                {"extract_to_function_exprs_position_binary_expr_localVar.json"},
                {"extract_to_function_exprs_position_binary_expr_before_plus_token.json"},
                {"extract_to_function_exprs_position_binary_expr_after_plus_token.json"},
                {"extract_to_function_exprs_position_braced_expr_end.json"},
                {"extract_to_function_exprs_position_qual_name_ref_modPrefix.json"},
                {"extract_to_function_exprs_position_qual_name_ref_identifier.json"},
                {"extract_to_function_exprs_position_indexed_expr_container_expr.json"},
                {"extract_to_function_exprs_pos_field_access_expr_record_in_expr.json"},
                {"extract_to_function_exprs_pos_field_access_expr_object_in_expr.json"},
                {"extract_to_function_exprs_pos_field_access_expr_record_in_fieldName.json"},
                {"extract_to_function_exprs_pos_method_call_object_in_methodArg.json"},
                {"extract_to_function_exprs_pos_method_call_object_in_methodName.json"},
                {"extract_to_function_exprs_pos_typeof_expr_typeof_kw.json"},
                {"extract_to_function_exprs_pos_typeof_expr_expression.json"},
                {"extract_to_function_exprs_pos_unary_expr_expression.json"},
                {"extract_to_function_exprs_pos_type_test_expr_is_kw.json"},
                {"extract_to_function_exprs_pos_type_cast_expr_expression.json"},
                {"extract_to_function_exprs_pos_type_cast_expr_typeCastParam.json"},
                {"extract_to_function_exprs_pos_table_cons_expression_table_kw.json"},
                {"extract_to_function_exprs_pos_table_cons_expr_key_spec_key_fieldName.json"},
                {"extract_to_function_exprs_pos_table_cons_expr_key_specifier_key_kw.json"},
                {"extract_to_function_exprs_pos_implicit_new_expr_new_kw.json"},
                {"extract_to_function_exprs_pos_explicit_new_expr_new_kw.json"},
//                {"extract_to_function_exprs_pos_object_cons_object_kw.json"}, // todo support later
                {"extract_to_function_exprs_pos_error_cons_error_kw.json"},
                {"extract_to_function_exprs_pos_let_expr_let_kw.json"},
                {"extract_to_function_exprs_pos_let_expr_in_kw.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                // statements and list

                {"negative_extract_to_function_stmts_list_with_mul_asgnmts.json"},
                {"negative_extract_to_function_stmts_list_with_mul_comAsgnmts.json"},
                {"neg_extract_to_function_stmts_list_with_mul_varDecls_referredAfterRange.json"},
                {"negative_extract_to_function_stmts_list_with_cmpAss_without_varDecl.json"},
                {"negative_extract_to_function_stmts_list_with_asgnmt_and_cmpAsgnmt.json"},
                {"negative_extract_to_function_stmts_list_with_while_loop_with_return_inside.json"},
                {"negative_extract_to_function_stmts_list_with_while_loop_with_panic_inside.json"},
                {"neg_extract_to_function_stmts_list_without_selecting_the_nodes_fully_single_line.json"},
                {"neg_extract_to_function_stmts_list_without_selecting_the_nodes_fully_two_lines.json"},
                {"extract_to_function_stmts_within_class_with_self_keyword_assigned_in_lhs.json"}, // todo support this

                {"negative_extract_to_function_stmts_if_else_stmt_with_varDecl_before_range.json"},
                {"negative_extract_to_function_stmts_if_else_stmt_with_selecting_only_else_block.json"},
                {"negative_extract_to_function_stmts_if_else_stmt_without_selectingTheEntireNode.json"},
                {"negative_extract_to_function_stmts_if_else_stmt_selecting_block_stmt_in_else_block.json"},
                {"negative_extract_to_function_stmts_if_else_stmt_with_and_selecting_else_if_block.json"},
                {"negative_extract_to_function_stmts_if_else_stmt_with_else_if_and_selecting_after_if.json"},

                {"negative_extract_to_function_stmts_assignment_stmt_localVar.json"},
                {"negative_extract_to_function_stmts_com_assignment_stmt_localVar.json"},

                {"negative_extract_to_function_stmts_while_statement_break_statement_inside.json"},
                {"negative_extract_to_function_stmts_while_statement_continue_statement_inside.json"},
                {"negative_extract_to_function_stmts_while_statement_varAssigns_without_varDecl.json"},
                {"negative_extract_to_function_stmts_while_statement_selected_only_block_statement.json"},
                {"negative_extract_to_function_stmts_while_statement_with_return_inside.json"},

                {"negative_extract_to_function_stmts_return_stmt_without_action_or_expr.json"},

                {"negative_extract_to_function_stmts_lock_stmt.json"},

                {"negative_extract_to_function_stmts_foreach_stmt.json"},
                {"negative_extract_to_function_stmts_foreach_stmt_with_return_without_expr.json"},
                {"negative_extract_to_function_stmts_foreach_stmt_with_return_with_expr.json"},

                {"negative_extract_to_function_stmts_match_stmt_match_clause.json"},
                {"negative_extract_to_function_stmts_match_stmt_without_varDecl.json"},

//                {"negative_extract_to_function_stmts_block_stmt.json"}, // todo support later
//                {"negative_extract_to_function_stmts_if_else_stmt_else_if.json"}, // todo support later

                {"negative_extract_to_function_stmts_call_stmt.json"},
                {"negative_extract_to_function_stmts_panic_stmt.json"},
                {"negative_extract_to_function_stmts_continue_stmt.json"},
                {"negative_extract_to_function_stmts_break_stmt.json"},

                {"negative_extract_to_function_stmts_action_stmt_start.json"},
                {"negative_extract_to_function_stmts_action_stmt_start_with_var_decl.json"},
                {"negative_extract_to_function_stmts_action_stmt_wait.json"},
                {"negative_extract_to_function_stmts_action_stmt_wait_with_var_decl.json"},

                {"negative_extract_to_function_stmts_named_worker_decl_stmt.json"},
                {"negative_extract_to_function_stmts_fork_stmt.json"},
                {"negative_extract_to_function_stmts_transaction_stmt.json"},
                {"negative_extract_to_function_stmts_rollback_stmt.json"},
                {"negative_extract_to_function_stmts_retry_stmt.json"},
                {"negative_extract_to_function_stmts_xmlns_decl_stmt.json"},
                {"negative_extract_to_function_stmts_fail_stmt.json"},

                {"negative_extract_to_function_stmts_within_class_object_fields.json"},

                 // expressions

                {"negative_extract_to_function_exprs_function_call.json"},
                {"negative_extract_to_function_exprs_field_access_expr_with_self.json"},
                {"negative_extract_to_function_exprs_field_access_expr_in_assignment_lhs.json"},
                {"negative_extract_to_function_exprs_field_access_expr_in_comp_assgnmnt_lhs.json"},
                {"negative_extract_to_function_exprs_mapping_cons_inside_table.json"},
                {"negative_extract_to_function_exprs_mapping_cons_annot_value.json"},
                {"negative_extract_to_function_exprs_mapping_cons_string_within_specific_field.json"},

                {"neg_extract_to_function_exprs_position_numeric_literal_before_literal.json"},
                {"neg_extract_to_function_exprs_pos_object_cons_function_kw_inside.json"},
                {"neg_extract_to_function_exprs_pos_in_const_declaration.json"},
                {"neg_extract_to_function_exprs_pos_in_module_xmlns_declaration.json"},
                {"neg_extract_to_function_exprs_pos_in_enum_declaration.json"},
                {"neg_extract_to_function_exprs_pos_in_type_definition.json"},
                {"neg_extract_to_function_exprs_pos_in_function_call_with_qualNameRef.json"},
                {"neg_extract_to_function_exprs_pos_mapping_cons_cur_inside_fields.json"},
                {"negative_extract_to_function_exprs_pos_function_call_in_let_expr.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-function";
    }
}
