/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.ballerinalang.testerina.test;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.test.launcher.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Testcase for validate behavior of source compilation failures.
 */
public class TesterinaCompilationNegativeTest {

    private ByteArrayOutputStream tempErrStream = new ByteArrayOutputStream();
    private PrintStream defaultErr;

    @Test(expectedExceptions = BLangCompilerException.class,
            expectedExceptionsMessageRegExp = "compilation contains errors")
    public void testSingleFileCompilationError() {
        BTestRunner testRunner = new BTestRunner();
        testRunner.runTest("src/test/resources/compilationnegative",
                           new Path[]{Paths.get("unreachable_code.bal")}, new ArrayList<>());
    }

    @Test(description = "test proper compilation failure with ballerina test when the non-zeroth module of a project " +
            "contains errors")
    public void testMultiModuleProjectCompilationError() throws IOException {
        boolean expectedExceptionThrown = false;
        String sourceRoot = "src/test/resources/compilationnegative/multi-module-project";
        try {
            defaultErr = System.err;
            System.setErr(new PrintStream(tempErrStream));
//            BTestUtil.runTestsInProject(sourceRoot, false);
        } catch (BLangCompilerException e) {
            expectedExceptionThrown = true;
            assertEquals(e.getMessage(), "compilation contains errors",
                         "excepted exception due to compilation failure");
            assertTrue(tempErrStream.toString().contains(
                         "error:testerina_test/foo:0.0.1::hello_world_negative.bal:20:1: mismatched input " +
                                 "'}'. expecting {'is', ';', '?', '+', '-', '*', '/', '%', '==', '!=', '>', '<', '>" +
                                 "=', '<=', '&&', '||', '===', '!==', '&', '^', '...', '|', '?:', '->>', '..<'}"),
                         "invalid error message");
        } finally {
            tempErrStream.close();
            System.setErr(defaultErr);
            assertTrue(expectedExceptionThrown, "expected an exception to be thrown due to compilation error");
        }
    }

    @AfterMethod
    private void cleanup() {
        TesterinaRegistry.getInstance().setTestSuites(new HashMap<>());
    }
}
