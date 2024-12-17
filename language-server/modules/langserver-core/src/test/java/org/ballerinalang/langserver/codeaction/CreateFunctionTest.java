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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class CreateFunctionTest extends AbstractCodeActionTest {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Override
    public String getResourceDir() {
        return "create-function";
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
                {"undefinedFunctionCodeAction.json"},
                {"undefinedFunctionCodeAction2.json"},
                {"undefinedFunctionCodeAction3.json"},
                {"undefinedFunctionCodeAction4.json"},
                {"undefinedFunctionCodeAction5.json"},
                {"undefinedFunctionCodeAction6.json"},
                {"undefinedFunctionCodeAction20.json"},
                {"undefinedFunctionCodeAction24.json"},
                // TODO Doesn't support method creation in objects
                // {"undefinedFunctionCodeAction7.json", "createUndefinedFunction5.bal"},
                // TODO Doesn't support methods inside services yet
                // {"undefinedFunctionCodeAction8.json", "createUndefinedFunction6.bal"},

                {"undefinedFunctionCodeAction9.json"},
                {"undefinedFunctionCodeAction10.json"},
                {"undefinedFunctionCodeAction11.json"},
                {"undefinedFunctionCodeAction12.json"},
                {"undefinedFunctionCodeAction13.json"},
                {"undefinedFunctionCodeAction14.json"},
                // TODO Need to fix anonymous records support
                // {"undefinedFunctionCodeAction15.json", "createUndefinedFunction7.bal"},

                // Test functions returning object
                {"undefinedFunctionCodeAction16.json"},
                {"undefinedFunctionCodeAction17.json"},
                {"undefinedFunctionCodeAction18.json"},
                {"undefinedFunctionCodeAction19.json"},
                {"undefinedFunctionCodeAction21.json"},
                {"undefinedFunctionCodeAction22.json"},
                {"undefinedFunctionCodeAction23.json"},

                {"undefinedFunctionCodeActionInRecord.json"},
                {"undefinedFunctionCodeActionInRecord2.json"},
                {"undefinedFunctionCodeActionInRecordField1.json"},
                {"undefinedFunctionCodeActionInObjectField1.json"},

                {"projectCreateUndefinedFunction1.json"},
                {"projectCreateUndefinedFunction2.json"},
                {"projectCreateUndefinedFunction3.json"},
                {"projectCreateUndefinedFunction4.json"},
                {"projectCreateUndefinedFunction5.json"},
                {"projectCreateUndefinedFunction6.json"},

                // Let Expression
                {"undefinedFunctionCodeActionInLet.json"},
                {"undefinedFunctionCodeActionInLet2.json"},
                {"undefinedFunctionCodeActionInLet3.json"},
                {"undefinedFunctionCodeActionInLet4.json"},
                {"undefinedFunctionCodeActionInLet5.json"},

                // Module Alias
                {"projectCreateUndefinedFunctionWithModAlias.json"},
                {"projectCreateUndefinedFunctionWithModAlias2.json"},
                {"projectCreateUndefinedFunctionWithLangLib.json"},

                {"create_function_which_returns_record1.json"},
                {"create_function_which_returns_record2.json"},
                {"createFunctionCodeActionWithStrands.json"},
                {"createFunctionCodeActionWithStrands2.json"},
                {"createFunctionCodeActionWithStrands3.json"},
                {"createFunctionCodeActionWithStrands4.json"},

                // Create function in condition expressions like in if, while
                {"create_function_in_if_statement1.json"},
                {"create_function_in_if_statement2.json"},
                {"create_function_in_if_statement3.json"},
                {"create_function_in_if_statement4.json"},
                {"create_function_in_if_statement5.json"},
                {"create_function_in_while_statement1.json"},

                {"create_function_in_expression1.json"},
                {"create_function_in_function_call_expr1.json"},
                {"create_function_in_method_call_expr1.json"},
                {"create_function_in_implicit_new_expression1.json"},
                {"create_function_in_explicit_new_expression1.json"},
                {"create_function_in_remote_method_call_action1.json"},

                {"create_function_in_named_arg_context1.json"},
                {"create_function_in_named_arg_context2.json"},
                {"create_function_in_named_arg_context3.json"},
                {"create_function_in_named_arg_context4.json"},

                // Error constructor
                {"create_function_in_error_constructor1.json"},
                {"create_function_in_error_constructor2.json"},
                {"create_function_in_error_constructor3.json"},

                // Variable Declaration
                {"create_function_in_var_decl1.json"},
                {"create_function_in_var_decl2.json"},
                {"create_function_in_var_decl3.json"},
                // TODO Blocked by #34448
                // {"create_function_in_var_decl4.json"},
                {"create_function_in_var_decl5.json"},
                {"create_function_in_var_decl6.json"},

                {"create_function_which_returns_error1.json"},
                {"create_function_in_fail1.json"},
                {"create_function_in_return1.json"},

                // Named Args
                {"create_function_with_named_args1.json"},

                {"undefinedFunctionInConditionalExpression1.json"},
                {"undefinedFunctionInConditionalExpression2.json"},
                {"undefinedFunctionInConditionalExpression3.json"},

                {"undefinedFunctionInCheckExpression1.json"},
                {"undefinedFunctionInCheckExpression2.json"},
                {"undefinedFunctionInCheckpanicExpression1.json"},
                {"undefinedFunctionInCheckpanicExpression2.json"},
                {"undefinedFunctionInPanicStatement.json"},
                {"undefinedFunctionInReturn1.json"},
                {"create_function_in_conditional_expression.json"},
                {"create_function_in_nil_conditional_expression.json"},

                {"create_function_in_local_var1.json"},
                {"create_function_in_local_var2.json"},
                {"create_function_in_start_action1.json"},

                {"undefinedFunctionCodeAction25.json"},
                {"undefinedFunctionCodeAction26.json"},
                {"undefinedFunctionCodeAction27.json"},
                {"undefinedFunctionCodeAction28.json"},
                {"undefinedFunctionCodeAction29.json"},
                {"undefinedFunctionCodeAction30.json"},
                {"undefinedFunctionCodeAction31.json"},

                {"create_function_in_anonymous_function1.json"},
                {"create_function_in_anonymous_function2.json"},

                {"create_function_in_worker1.json"},
                {"create_function_in_worker2.json"},
                {"create_function_in_worker3.json"},
                {"create_function_in_worker4.json"},
                {"create_function_in_worker5.json"},

                {"create_function_in_explicit_anonymous_function1.json"},
                {"create_function_in_explicit_anonymous_function2.json"},
                {"create_function_in_explicit_anonymous_function3.json"},
                {"create_function_in_explicit_anonymous_function4.json"},
                {"create_function_in_explicit_anonymous_function5.json"},

                // compound assignment
                {"createUndefinedFunctionInCompoundStatement1.json"},
                {"createUndefinedFunctionInCompoundStatement2.json"},
                {"createUndefinedFunctionInCompoundStatement3.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"undefinedFunctionCodeActionNegativeTest1.json"},
                {"undefinedFunctionCodeActionNegativeTest2.json"}
        };
    }

    @Override
    protected Object convertActionData(Object actionData) {
        return CommandArgument.from(gson.toJsonTree(actionData));
    }
}
