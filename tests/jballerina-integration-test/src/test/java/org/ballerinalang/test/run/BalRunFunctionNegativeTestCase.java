/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.run;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.LogLeecher.LeecherType;
import org.testng.annotations.Test;

import java.nio.file.Path;


/**
 * This class tests invoking an entry function in a bal file via the Ballerina Run Command.
 */
public class BalRunFunctionNegativeTestCase extends BaseTest {
    @Test
    public void testInsufficientArguments() throws BallerinaTestException {
        executeTest("error: missing operand arguments for parameter 'i' of type 'int'",
                    "test_main_with_multiple_typed_params.bal");
    }

    @Test
    public void testTooManyArguments() throws BallerinaTestException {
        executeTest("error: all operand arguments are not matched", "test_main_with_no_params.bal", "extra");
    }

    @Test
    public void testErrorReturnFromMain() throws BallerinaTestException {
        executeTest("error: Returning an error", "main_error_return.bal");
    }

    @Test
    public void testErrorPanicInMain() throws BallerinaTestException {
        executeTest("error: Returning an error", "main_error_panic.bal");
    }

    private void executeTest(String message, String fileName, String... args) throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher(message, LeecherType.ERROR);
        String balFile = Path.of("src/test/resources/run/file", fileName).toAbsolutePath().toString();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain(balFile, new String[0], args, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(10000);
    }
}
