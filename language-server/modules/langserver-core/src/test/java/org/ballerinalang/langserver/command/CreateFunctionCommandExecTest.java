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
    public void testCreateFunction(String config, String source) throws IOException {
        performTest(config, source, CreateFunctionExecutor.COMMAND);
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        log.info("Test workspace/executeCommand for command {}", CreateFunctionExecutor.COMMAND);
        return new Object[][]{
                {"createUndefinedFunction1.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction2.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction3.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction4.json", "createUndefinedFunction2.bal"},
                {"createUndefinedFunction18.json", "createUndefinedFunction8.bal"},
                // TODO Doesn't support method creation in objects
                // {"createUndefinedFunction5.json", "createUndefinedFunction3.bal"},
                // TODO Doesn't support methods inside services yet
                // {"createUndefinedFunction6.json", "createUndefinedFunction4.bal"},

                {"createUndefinedFunction7.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction8.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction9.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction10.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction11.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction12.json", "createUndefinedFunction5.bal"},
                // TODO Need to fix anonymous records support
                // {"createUndefinedFunction13.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction14.json", "createUndefinedFunction6.bal"},

                // Tests functions returning object
                {"createUndefinedFunction14.json", "createUndefinedFunction6.bal"},
                {"createUndefinedFunction15.json", "createUndefinedFunction6.bal"},
                {"createUndefinedFunction16.json", "createUndefinedFunction6.bal"},
                {"createUndefinedFunction17.json", "createUndefinedFunction7.bal"},

                {"createUndefinedFunctionInRecord.json", "createUndefinedFunctionInRecord.bal"},
                {"createUndefinedFunctionInRecord2.json", "createUndefinedFunctionInRecord.bal"},

                {"projectCreateUndefinedFunction1.json", "testproject/main.bal"},
                {"projectCreateUndefinedFunction2.json", "testproject/main.bal"},
                {"projectCreateUndefinedFunction3.json", "testproject/main.bal"},
                {"projectCreateUndefinedFunction4.json", "testproject/main.bal"},
                {"projectCreateUndefinedFunction5.json", "testproject/school.bal"},
                {"projectCreateUndefinedFunction6.json", "testproject/main.bal"},

                // Let Expression
                {"projectCreateUndefinedFunctionInLet.json", "testproject/school.bal"},
                {"projectCreateUndefinedFunctionInLet2.json", "testproject/school.bal"},
                {"projectCreateUndefinedFunctionInLet3.json", "testproject/school.bal"},
                // Module Alias
                {"projectCreateUndefinedFunctionWithModAlias.json", "testproject/modAlias.bal"},
                {"projectCreateUndefinedFunctionWithModAlias2.json", "testproject/modAlias.bal"},
                {"projectCreateUndefinedFunctionWithLangLib.json", "testproject/langlib.bal"},

                {"create_function_which_returns_record1.json", "create_function_which_returns_record1.bal"},
                {"create_function_which_returns_record2.json", "create_function_which_returns_record2.bal"},
                {"create_function_with_strands1.json", "create_function_with_strands1.bal"},
                {"create_function_with_strands2.json", "create_function_with_strands1.bal"},
                {"create_function_with_strands3.json", "create_function_with_strands1.bal"},
                {"create_function_with_strands4.json", "create_function_with_strands1.bal"},
                
                {"create_function_in_if_statement1.json", "create_function_in_if_statement1.bal"},
                {"create_function_in_if_statement2.json", "create_function_in_if_statement2.bal"},
                {"create_function_in_if_statement3.json", "create_function_in_if_statement3.bal"},
                {"create_function_in_if_statement4.json", "create_function_in_if_statement4.bal"},
                
                {"create_function_in_expression1.json", "create_function_in_expression1.bal"},
                {"create_function_in_function_call_expr1.json","create_function_in_function_call_expr1.bal"},
                {"create_function_in_method_call_expr1.json","create_function_in_method_call_expr1.bal"},
                {"create_function_in_implicit_new_expression1.json","create_function_in_implicit_new_expression1.bal"},
                {"create_function_in_explicit_new_expression1.json","create_function_in_explicit_new_expression1.bal"},
                {"create_function_in_remote_method_call_action1.json","create_function_in_remote_method_call_action1.bal"}
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
