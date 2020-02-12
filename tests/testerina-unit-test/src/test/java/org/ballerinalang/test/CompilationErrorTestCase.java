/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Negative test cases for before,after attribute.
 */
public class CompilationErrorTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
    }

    @Test
    public void testSingleFileCompilationError() throws BallerinaTestException {

        projectPath = singleFilesProjectPath.resolve("compilation-error").toString();
        String errMsg = "error: .::unreachable_code.bal:12:9: unreachable code";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"unreachable_code.bal"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testMultiModuleProjectCompilationError() throws BallerinaTestException {
        String errMsg = "error: intg-tests/foo:0.0.0::hello_world_negative.bal:3:1: mismatched input '}'. " +
                "expecting {'is', ';', '?', '+', '-', '*', '/', '%', '==', '!=', '>', '<', '>=', '<=', '&&', '||', " +
                "'===', '!==', '&', '^', '...', '|', '?:', '->>', '..<'}";
        LogLeecher clientLeecher = new LogLeecher(errMsg, ERROR);
        balClient.runMain("test", new String[]{"--all"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, multiModulesProjectPath.toString());
        clientLeecher.waitForText(20000);
    }


}
