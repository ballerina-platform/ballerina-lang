/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.test.negative;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.testerina.test.BaseTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test to validate adding diagnostic for invalid usages of test annotation.
 *
 * @since 2201.5.0
 */
public class InvalidTestDefinitionsTestCase extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass
    public void setup() {
        balClient = new BMainInstance(balServer);
        projectPath = singleFileTestsPath.resolve("invalid-test-definitions").toString();
    }

    @Test
    public void testClassLevelTestDefinitions() throws BallerinaTestException {
        String[] args = new String[]{"class_invalid-test-definitions.bal"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Assert.assertTrue(output.contains(
                "ERROR [class_invalid-test-definitions.bal:(20:5,20:20)] invalid usage of test annotation"));
    }

    @Test
    public void testSvcLevelTestDefinitions() throws BallerinaTestException {
        String[] args = new String[]{"svc_invalid-test-definitions.bal"};
        String output = balClient.runMainAndReadStdOut("test", args,
                new HashMap<>(), projectPath, false);
        Assert.assertTrue(output.contains(
                "ERROR [svc_invalid-test-definitions.bal:(21:5,21:20)] invalid usage of test annotation"));
    }
}
