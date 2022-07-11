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
package org.ballerinalang.langserver.command;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Command Execution Test Cases for create function.
 */
public class CreateFunctionCommandExecTest extends AbstractCommandExecutionTest {

    private static final Logger log = LoggerFactory.getLogger(CreateFunctionCommandExecTest.class);

    @Test(dataProvider = "create-function-data-provider")
    public void testCreateFunction(String config) throws IOException {
        performTest(config, CreateFunctionExecutor.COMMAND);
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        log.info("Test workspace/executeCommand for command {}", CreateFunctionExecutor.COMMAND);
        return new Object[][]{
                {"createUndefinedFunction1.json"},
                {"createUndefinedFunction2.json"},
                {"createUndefinedFunction3.json"},
                {"createUndefinedFunction4.json"},
                {"createUndefinedFunction18.json"},
                // TODO Doesn't support method creation in objects
                // {"createUndefinedFunction5.json", "createUndefinedFunction3.bal"},
                // TODO Doesn't support methods inside services yet
                // {"createUndefinedFunction6.json", "createUndefinedFunction4.bal"},

                {"createUndefinedFunction7.json"},
                {"createUndefinedFunction8.json"},
                {"createUndefinedFunction9.json"},
                {"createUndefinedFunction10.json"},
                {"createUndefinedFunction11.json"},
                {"createUndefinedFunction12.json"},
                // TODO Need to fix anonymous records support
                // {"createUndefinedFunction13.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction14.json"},

                // Tests functions returning object
                {"createUndefinedFunction14.json"},
                {"createUndefinedFunction15.json"},
                {"createUndefinedFunction16.json"},
                {"createUndefinedFunction17.json"},
                {"createUndefinedFunction19.json"},
                {"createUndefinedFunction20.json"},
                {"createUndefinedFunction21.json"},
                {"createUndefinedFunction22.json"},

                {"createUndefinedFunctionInRecord.json"},
                {"createUndefinedFunctionInRecord2.json"},
                {"createUndefinedFunctionInRecordField1.json"},
                {"createUndefinedFunctionInObjectField1.json"},

                {"projectCreateUndefinedFunction1.json"},
                {"projectCreateUndefinedFunction2.json"},
                {"projectCreateUndefinedFunction3.json"},
                {"projectCreateUndefinedFunction4.json"},
                {"projectCreateUndefinedFunction5.json"},
                {"projectCreateUndefinedFunction6.json"},

                // Let Expression
                {"projectCreateUndefinedFunctionInLet.json"},
                {"projectCreateUndefinedFunctionInLet2.json"},
                {"projectCreateUndefinedFunctionInLet3.json"},
                // Module Alias
                {"projectCreateUndefinedFunctionWithModAlias.json"},
                {"projectCreateUndefinedFunctionWithModAlias2.json"},
                {"projectCreateUndefinedFunctionWithLangLib.json"},

                {"create_function_which_returns_record1.json"},
                {"create_function_which_returns_record2.json"},
                {"create_function_with_strands1.json"},
                {"create_function_with_strands2.json"},
                {"create_function_with_strands3.json"},
                {"create_function_with_strands4.json"},

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
                // {"create_function_in_var_decl14.json", "create_function_in_var_decl4.bal"}
                
                {"create_function_which_returns_error1.json"},
                {"create_function_in_fail1.json"},
                {"create_function_in_return1.json"},
                
                // Named Args
                {"create_function_with_named_args1.json"},

                {"createUndefinedFunctionInConditionalExpression1.json"},
                {"createUndefinedFunctionInConditionalExpression2.json"},
                {"createUndefinedFunctionInConditionalExpression3.json"},

                {"createUndefinedFunctionInCheckExpression1.json"},
                {"createUndefinedFunctionInCheckExpression2.json"},
                {"createUndefinedFunctionInPanicStatement.json"},
                {"createUndefinedFunctionInCheckpanicExpression1.json"},
                {"createUndefinedFunctionInCheckpanicExpression2.json"},
        };
    }

    @Override
    protected List<Object> getArgs(JsonObject argsObject) {
        return Collections.singletonList(CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE,
                argsObject.getAsJsonObject("node.range")));
    }

    @Override
    protected String getSourceRoot() {
        return "create-function";
    }
}
