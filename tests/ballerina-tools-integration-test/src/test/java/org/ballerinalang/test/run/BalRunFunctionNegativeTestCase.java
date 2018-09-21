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

    private String fileName = "test_entry_function.bal";
    private String filePath = (new File("src/test/resources/run/file/" + fileName)).getAbsolutePath();

    @Test
    public void testEmptyEntryFunctionName() throws BallerinaTestException {
        String sourceArg = filePath + ":";
        LogLeecher errLogLeecher = new LogLeecher("ballerina: expected function name after final ':'",
                LeecherType.ERROR);
        balClient.runMain(sourceArg, new LogLeecher[]{errLogLeecher});
        errLogLeecher.waitForText(2000);
    }
}
