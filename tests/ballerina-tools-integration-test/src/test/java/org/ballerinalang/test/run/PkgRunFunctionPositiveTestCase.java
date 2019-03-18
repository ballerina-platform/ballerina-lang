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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class tests invoking a main function in a package via the Ballerina Run Command and the data binding
 * functionality.
 */
public class PkgRunFunctionPositiveTestCase extends BaseTest {

    @Test
    public void testNoArg() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("run", new String[]{ "no_params" },
                                                       (new File("src/test/resources/run/package/")).getAbsolutePath());
        Assert.assertEquals(output, "1");
    }

    @Test
    public void testMultipleParam() throws BallerinaTestException {
        String output = balClient.runMainAndReadStdOut("run",
                                                       new String[]{ "multiple_params", "1000", "1.0",
                                                               "Hello Ballerina", "255", "true",
                                                               "{ \"name\": \"Maryam\" }", "<book>Harry Potter</book>",
                                                               "{ \"name\": \"Em\" }", "just", "the", "rest"},
                                                       (new File("src/test/resources/run/package/")).getAbsolutePath());
        Assert.assertEquals(output,
                            "integer: 1000, float: 1.0, string: Hello Ballerina, byte: 255, boolean: true, " +
                                    "JSON Name Field: Maryam, XML Element Name: book, Employee Name Field: Em, " +
                                    "string rest args: just the rest ");
    }
}
