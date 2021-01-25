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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Test class to test positive scenarios of testerina using a ballerina project.
 */
public class BasicCasesTest extends BaseTestCase {

    private BMainInstance balClient;
    private String projectPath;

    @BeforeClass()
    public void setup() throws BallerinaTestException {
        balClient = new BMainInstance(balServer);
        projectPath = projectBasedTestsPath.toString();
    }

    @Test
    public void testAssertions() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"assertions"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to assertion failure in test framework");
        }
    }

    @Test
    public void testAssertDiffError() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"assertions-diff-error"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to assertion diff message failure in test framework");
        }
    }

    @Test
    public void testAssertionErrorMessage() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"assertions-error-messages"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to assertion error message failure in test framework");
        }
    }

    @Test
    public void testAssertBehavioralTypes() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"assertions-behavioral-types"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to assertion failure for behavioral data types " +
                    "in test framework");
        }
    }

    @Test
    public void testAssertStructuralTypes() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"assertions-structural-types"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to assertion failure for structural data types " +
                    "in test framework");
        }
    }

    @Test
    public void testAssertSequenceTypes() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"assertions-sequence-types"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to assertion failure for sequence data types " +
                    "in test framework");
        }
    }

    @Test
    public void testAnnotationAccess() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"annotation-access"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to test annotation access failure in test framework");
        }
    }

    @Test
    public void testJavaInterops() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"interops"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to interops failure in test framework");
        }
    }

    @Test
    public void testBeforeAfter() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"before-after"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to before-after annotation attribute failure in test " +
                    "framework");
        }
    }

    @Test
    public void testBeforeEachAfterEach() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"before-each-after-each"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to before-each-after-each annotation attribute failure " +
                    "in test framework");
        }
    }

    @Test(dependsOnMethods = "testBeforeAfter")
    public void testDependsOn() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"depends-on"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to depends-on annotation attribute failure in test " +
                    "framework");
        }
    }

    @Test(dependsOnMethods = "testDependsOn")
    public void testAnnotations() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"annotations"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to annotations failure in test " +
                    "framework");
        }
    }

    @Test
    public void testIsolatedFunctions() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"isolated-functions"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to isolated functions failure in test " +
                    "framework");
        }
    }

    @Test
    public void testIntersectionTypes() throws BallerinaTestException {
        String errorOutput = balClient.runMainAndReadStdOut("test", new String[]{"intersection-type-test"},
                new HashMap<>(), projectPath, true);
        if (errorOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to intersection type failure in test " +
                    "framework");
        }
    }

}
