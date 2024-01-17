/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.testerina.test.utils.AssertionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * Test class to test predeclared langlibs using a ballerina project.
 * @since 2.0.0
 */
public class ImportTest extends BaseTestCase {
    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass()
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
    }

    @Test()
    public void testImportTest() throws BallerinaTestException, IOException {
        String[] args = mergeCoverageArgs(new String[]{"predeclared-import-tests"});
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, true);
        AssertionUtils.assertOutput("ImportTest-testImportTest.txt", output);
    }
}
