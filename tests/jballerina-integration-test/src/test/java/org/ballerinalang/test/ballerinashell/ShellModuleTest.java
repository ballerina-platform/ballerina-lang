/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.ballerinashell;

import io.ballerina.shell.Evaluator;
import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.cli.ReplShellApplication;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

/**
 * Test Ballerina shell with internal-io module.
 *
 * @since 2201.7.0
 */
public class ShellModuleTest {

    @Test
    public void testPrint() throws Exception {
        BShellConfiguration configuration = new BShellConfiguration.Builder()
                .setDumb(true).setTreeParsingTimeoutMs(10000).build();
        ReplShellApplication.execute(configuration);

        Evaluator evaluator = configuration.getEvaluator();
        evaluator.getCompilation("import ballerinai/io");
        evaluator.getCompilation("io:println(\"ballerina\");");
        assertFalse(evaluator.hasErrors());
    }

    @Test
    public void testVariableDeclarationWithIO() throws Exception {
        BShellConfiguration configuration = new BShellConfiguration.Builder()
                .setDumb(true).setTreeParsingTimeoutMs(10000).build();
        ReplShellApplication.execute(configuration);

        Evaluator evaluator = configuration.getEvaluator();
        evaluator.getCompilation("import ballerinai/io");
        evaluator.getCompilation("int x = 1;");
        evaluator.getCompilation("io:println(1);");
        assertFalse(evaluator.hasErrors());
    }
}
