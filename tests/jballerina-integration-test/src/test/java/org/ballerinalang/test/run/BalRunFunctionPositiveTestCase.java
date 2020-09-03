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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class tests invoking a main function in a bal file via the Ballerina Run Command and the data binding
 * functionality.
 */
public class BalRunFunctionPositiveTestCase extends BaseTest {

    private static final int LOG_LEECHER_TIMEOUT = 10000;

    @Test(enabled = false)
    public void testNoArg() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[] { "test_main_with_no_params.bal" },
                                                           (new File("src" + File.separator + "test" + File.separator
                                                                             + "resources" + File.separator + "run" +
                                                                             File.separator + "file"))
                                                                   .getAbsolutePath());
        Assert.assertTrue(output.endsWith("1"));
    }

    @Test(enabled = false)
    public void testMultipleParam() throws BallerinaTestException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String[] args = new String[] {
                "test_main_with_multiple_typed_params.bal", "1000", "1.0", "Hello Ballerina", "255", "true",
                "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>", "{ \"name\": \"Em\" }", "just", "the", "rest"
        };
        String balFile = (new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                                           "run" + File.separator + "file")).getAbsolutePath();
        String output = bMainInstance.runMainAndReadStdOut("run", args, balFile);
        Assert.assertTrue(output.endsWith("integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, boolean: " +
                                          "true, JSON Name Field: Maryam, XML Element Name: book, Employee Name " +
                                          "Field: Em, string rest args: just the rest "));
    }
}
