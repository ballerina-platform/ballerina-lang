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

package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Test class containing negative test cases related to configurable cli arguments.
 *
 * @since 2201.6.0
 */

public class InvalidConfigurableCliArgsTestCase extends BaseTestCase {
    private BMainInstance bMainInstance;
    private String testFileLocation;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
        testFileLocation = projectBasedTestsPath.resolve("configurable-cli-args-test").toString();
    }

    @Test(enabled = false)
    public void invalidConfigurableCliArgsTest() throws BallerinaTestException {
        LogLeecher errorLeecher1 = new LogLeecher("[intVar=test] configurable variable 'intVar' is " +
                "expected to be of type 'int', but found 'test'", ERROR);
        LogLeecher errorLeecher2 = new LogLeecher("[floatVar=test1] configurable variable 'floatVar' is " +
                "expected to be of type 'float', but found 'test1'",
                ERROR);
        LogLeecher errorLeecher3 = new LogLeecher("value not provided for required configurable " +
                "variable 'booleanVar'", ERROR);
        LogLeecher errorLeecher4 = new LogLeecher("error: [xmlVal=<book>The Lost Symbol] " +
                "configurable variable 'xmlVal' is expected to be of type " +
                "'xml<((lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text) " +
                "& readonly)>', but found '<book>The Lost Symbol'", ERROR);

        bMainInstance.runMain("test", new String[]{"-CintVar=test",
                        "-CfloatVar=test1", "-CstringVar=main test", "-CxmlVal=<book>The Lost Symbol",
                        "-CtestInt=30", "-CtestFloat=5.6", "-CtestString=cli arg", "-CtestBoolean=false"},
                        null, new String[]{},
                        new LogLeecher[]{errorLeecher1, errorLeecher2, errorLeecher3, errorLeecher4},
                        testFileLocation);
        errorLeecher1.waitForText(5000);
        errorLeecher2.waitForText(5000);
        errorLeecher3.waitForText(5000);
        errorLeecher4.waitForText(5000);
    }

    @Test
    public void unusedConfigurableCliArgsTest() throws BallerinaTestException {
        LogLeecher errorLeecher1 = new LogLeecher("[extraVal=extraValue] unused command line argument", ERROR);

        bMainInstance.runMain("test", new String[]{"-CintVar=40", "-CfloatVar=4.5",
                        "-CstringVar=main test", "-CbooleanVar=true", "-CxmlVal=<book>The Lost Symbol</book>",
                        "-CextraVal=extraValue", "-CtestInt=30", "-CtestFloat=5.6",
                        "-CtestString=cli arg", "-CtestBoolean=false"}, null, new String[]{},
                        new LogLeecher[]{errorLeecher1},
                testFileLocation);
        errorLeecher1.waitForText(5000);
    }
}
