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
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class to test positive scenarios of testerina using a ballerina project.
 */
public class BasicCasesTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass()
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = basicTestsProjectPath.toString();
    }

    @Test
    public void testAssertTrue() throws BallerinaTestException {
        String msg = "2 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--groups", "p1", "assertions"}, null, new String[]{},
                new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test(dependsOnMethods = "testAssertTrue")
    public void testAssertions() throws BallerinaTestException {
        String msg = "31 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"--disable-groups", "p1", "assertions"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(dependsOnMethods = "testAssertTrue")
    public void testAssertionsErrorMessages() throws BallerinaTestException {
        String msg = "17 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"assertions-error-messages"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(dependsOnMethods = "testAssertTrue")
    public void testAssertionsBehavioralTypes() throws BallerinaTestException {
        String msg = "12 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"assertions-behavioral-types"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(dependsOnMethods = "testAssertTrue")
    public void testAssertionsSequenceTypes() throws BallerinaTestException {
        String msg = "8 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"assertions-sequence-types"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(dependsOnMethods = "testAssertTrue")
    public void testAssertionsStructuralTypes() throws BallerinaTestException {
        String msg = "36 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"assertions-structural-types"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test
    public void testAnnotationAccess() throws BallerinaTestException {
        String msg = "3 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"annotation-access"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test
    public void testJavaInterops() throws BallerinaTestException {
        String msg = "1 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"interops"}, null, new String[]{}, new LogLeecher[]{clientLeecher},
                          projectPath);
        clientLeecher.waitForText(20000);
    }

    @Test
    public void testBeforeAfter() throws BallerinaTestException {
        String msg = "2 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"beforeAfter"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test
    public void testBeforeEachAfterEach() throws BallerinaTestException {
        String msg = "3 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"beforeEachAfterEach"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(enabled = false)
    public void testConfigApiTest() throws BallerinaTestException {
        String msg = "1 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"config-api-test", "--user.name=waruna"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(dependsOnMethods = "testBeforeAfter")
    public void testDependsOn() throws BallerinaTestException {
        String msg = "8 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"dependsOn"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test(dependsOnMethods = "testDependsOn")
    public void testAnnotations() throws BallerinaTestException {
        String msg = "15 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"annotations"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

    @Test
    public void testIsolatedFunctions() throws BallerinaTestException {
        String msg = "3 passing";
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient.runMain("test", new String[]{"isolated-functions"}, null,
                new String[]{}, new LogLeecher[]{clientLeecher}, projectPath);
        clientLeecher.waitForText(40000);
    }

}
