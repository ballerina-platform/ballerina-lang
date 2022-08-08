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
                {"extract_to_function_statements_list_without_return.json"},
                {"extract_to_function_statements_list_with_return.json"},
                {"extract_to_function_statements_list_with_param_without_return.json"},
                {"extract_to_function_statements_list_with_param_with_return.json"},
                {"extract_to_function_statements_list_with_const_with_return.json"},
                {"extract_to_function_statements_list_with_moduleVar_with_return.json"},
                {"extract_to_function_statements_list_assigned_twice.json"},
                {"extract_to_function_statements_list_multipleLocalVarDefsInRange_with_single_return.json"},
                {"extract_to_function_statements_list_varDeclOnlyInRange_return_single.json"},
                {"extract_to_function_statements_list_with_ifElseStatement.json"},
                {"extract_to_function_statements_list_with_compound_assignments.json"},
                {"extract_to_function_statements_list_with_asgnmntAndCompAsgnmnt_sameVar.json"},
                {"extract_to_function_statements_list_with_moduleVar_assignmentStatement.json"},
                {"extract_to_function_statements_list_with_moduleVar_comAssignmentStatement.json"},
//                {"extract_to_function_stmts_list_with_action_stmt_start_and_wait.json"},

                {"extract_to_function_stmts_block_stmt_inside_block.json"},
//                {"extract_to_function_stmts_block_stmt_with_block.json"}, // todo (support this)

                {"extract_to_function_stmts_local_var_decl_with_init.json"},
                {"extract_to_function_stmts_local_var_decl_with_init_range_till_closing_bracket.json"},
                {"extract_to_function_stmts_local_var_decl_without_init.json"}, //todo check this for other statements as well
//                {"extract_to_function_stmts_local_var_decl_with_start_action.json"},  // todo conform with spec for every case to allow these
//                {"extract_to_function_stmts_local_var_decl_with_wait_action_without_start_action.json"},
//                {"extract_to_function_stmts_local_var_decl_with_wait_action_with_future_param.json"},

                {"extract_to_function_stmts_assignment_stmt_moduleVar.json"},
                {"extract_to_function_stmts_com_assignment_stmt_moduleVar.json"},

                {"extract_to_function_stmts_if_else_stmt_with_if_only.json"},
                {"extract_to_function_stmts_if_else_stmt_with_if_and_else.json"}, // todo (support this)
//                {"extract_to_function_stmts_if_else_stmt_with_else_if.json"},
                {"extract_to_function_stmts_if_else_stmt_with_nested_if.json"},

                {"extract_to_function_stmts_while_stmt.json"},
                {"extract_to_function_stmts_while_stmt_with_local_var_referred.json"},

                {"extract_to_function_stmts_return_stmt.json"},

                {"extract_to_function_stmts_lock_stmt.json"},

                {"extract_to_function_stmts_foreach_stmt.json"},
                {"extract_to_function_stmts_foreach_stmt_with_range_expr.json"},
                {"extract_to_function_stmts_foreach_stmt_without_iterable_declared_inside.json"},
// todo
//                {"extract_to_function_stmts_xml_namespace_decl_stmt.json"},
//
                {"extract_to_function_stmts_match_stmt_without_return_stmt.json"},

                {"extract_to_function_stmts_do_stmt_without_onfail_stmt.json"},
//                {"extract_to_function_stmts_do_stmt_with_onfail_stmt.json"}, // todo add this support
//
//                {"extract_to_function_stmts_fail_stmt.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negative_extract_to_function_statements_list_with_multiple_assignments.json"},
                {"negative_extract_to_function_statements_list_with_multiple_comAssignments.json"},
                {"negative_extract_to_function_statements_list_with_multiple_varDecls_referredAfterRange.json"},
                {"negative_extract_to_function_statements_list_with_compoundAss_without_varDecl.json"},
                {"negative_extract_to_function_statements_list_with_assignment_and_compAssignment.json"},
                {"negative_extract_to_function_statements_list_with_multiple_moduleVar_assignments.json"},
                {"negative_extract_to_function_statements_list_with_multiple_moduleVar_and_localVar_assignments.json"},
                {"negative_extract_to_function_stmts_list_with_while_loop_with_return_inside.json"},
                {"negative_extract_to_function_stmts_list_with_while_loop_with_panic_inside.json"},

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

                {"negative_extract_to_function_stmts_panic_statement.json"},

                {"negative_extract_to_function_stmts_return_stmt_without_action_or_expr.json"},

                {"negative_extract_to_function_stmts_lock_stmt.json"},

                {"negative_extract_to_function_stmts_foreach_stmt.json"},
                {"negative_extract_to_function_stmts_foreach_stmt_with_return_without_expr.json"},
                {"negative_extract_to_function_stmts_foreach_stmt_with_return_with_expr.json"},

                {"negative_extract_to_function_stmts_match_stmt_match_clause.json"},
                {"negative_extract_to_function_stmts_match_stmt_without_varDecl.json"},
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-function";
    }
}
