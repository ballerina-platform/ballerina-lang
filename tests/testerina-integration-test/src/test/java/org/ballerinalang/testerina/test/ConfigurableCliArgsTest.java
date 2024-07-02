/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.test;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class containing tests related to configurable cli arguments.
 *
 * @since 2201.6.0
 */

public class ConfigurableCliArgsTest extends BaseTestCase {

    private BMainInstance bMainInstance;
    private String testFileLocation;
    private String singleFileTestLocation;

    @BeforeClass
    public void setup() {
        bMainInstance = new BMainInstance(balServer);
        testFileLocation = projectBasedTestsPath.resolve("configurable-cli-args-test").toString();
        singleFileTestLocation = singleFileTestsPath.resolve("configurable-cli-arguments").toString();
    }

    @Test
    public void configurableCliArgsTest() throws BallerinaTestException {
        LogLeecher testLog1 = new LogLeecher("5 passing");
        LogLeecher testLog2 = new LogLeecher("5 passing");
        bMainInstance.runMain("test", new String[]{"-CintVar=40", "-CfloatVar=4.5",
                        "-CstringVar=main test", "-CbooleanVar=true", "-CxmlVal=<book>The Lost Symbol</book>",
                        "-CtestInt=30", "-CtestFloat=5.6", "-CtestString=cli arg", "-CtestBoolean=false"},
                null, new String[]{}, new LogLeecher[]{testLog1, testLog2},
                testFileLocation);
        testLog1.waitForText(5000);
        testLog2.waitForText(5000);
    }

    @Test
    public void configurableCliArgsForSingleFileTest() throws BallerinaTestException {
        LogLeecher testLog1 = new LogLeecher("2 passing");
        bMainInstance.runMain("test", new String[]{"configurable-cli-args.bal",
                        "-CtestString=single test", "-CtestInt=89"},
                null, new String[]{}, new LogLeecher[]{testLog1},
                singleFileTestLocation);
        testLog1.waitForText(5000);
    }
}
