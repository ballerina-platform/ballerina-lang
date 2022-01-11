/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.test.command;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.command.AbstractCommandExecutionTest;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Command Execution Test Cases for compiler plugin based code actions.
 */
public class CompilerPluginCodeActionExecutionTest extends AbstractCommandExecutionTest {

    @BeforeSuite
    public void compilePlugins() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_with_codeactions");
    }

    @Test(dataProvider = "create-function-data-provider")
    public void testCreateFunction(String config, String source, String command) throws IOException {
        performTest(config, source, command);
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        return new Object[][]{
                {
                        "compiler_plugin_code_action_exec_config1.json",
                        "package_plugin_user_with_codeactions_1/main.bal",
                        "BCE2526/lstest/package_comp_plugin_with_codeactions/CREATE_VAR"
                },
                {
                        "compiler_plugin_code_action_exec_config2.json",
                        "source1.bal",
                        "BCE2526/lstest/package_comp_plugin_with_codeactions/CREATE_VAR"
                }
        };
    }

    @Override
    protected List<Object> getArgs(JsonObject argsObject) {
        return List.of(
                CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, argsObject.getAsJsonObject("node.range")),
                CommandArgument.from("var.type", argsObject.get("var.type").getAsString())
        );
    }

    @Override
    protected String getSourceRoot() {
        return "compiler-plugins";
    }
}
