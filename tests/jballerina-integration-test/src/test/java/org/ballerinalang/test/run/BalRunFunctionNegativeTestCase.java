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

import java.io.File;

/**
 * This class tests invoking an entry function in a bal file via the Ballerina Run Command and the data binding
 * functionality.
 *
 * e.g., ballerina run abc.bal:nomoremain 1 "Hello World" data binding main
 *  where nomoremain is the following function
 *      public function nomoremain(int i, string s, string... args) {
 *          ...
 *      }
 */
public class BalRunFunctionNegativeTestCase extends BaseTest {

    private static final int LOG_LEECHER_TIMEOUT = 10000;

    @Test(enabled = false, description = "test insufficient arguments")
    public void testInsufficientArguments() throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("ballerina: insufficient arguments to call the 'main' function",
                                                  LeecherType.ERROR);
        String balFile = (new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                           "run" + File.separator + "file" + File.separator +
                                           "test_main_with_multiple_typed_params" + ".bal")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain(balFile, new LogLeecher[] { errLogLeecher });
        errLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test(enabled = false, description = "test too many arguments")
    public void testTooManyArguments() throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("ballerina: too many arguments to call the 'main' function",
                                                  LeecherType.ERROR);
        String balFile = (new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                           "run" + File.separator + "file" + File.separator +
                                           "test_main_with_no_params.bal")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        bMainInstance.runMain(balFile, new String[] {}, new String[] { "extra" }, new LogLeecher[] { errLogLeecher });
        errLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }
}
