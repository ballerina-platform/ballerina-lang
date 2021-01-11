/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.test.integration;

import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.cli.ReplShellApplication;
import io.ballerina.shell.cli.test.TestUtils;
import io.ballerina.shell.cli.test.base.TestIntegrator;
import io.ballerina.shell.cli.test.base.TestCase;
import io.ballerina.shell.cli.test.base.TestCases;
import org.jline.reader.EndOfFileException;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

/**
 * Base class for integration tests.
 */
public class AbstractIntegrationTest {
    protected void test(String fileName) throws Exception {
        List<TestCase> testCases = TestUtils.loadTestCases(fileName, TestCases.class);

        PipedOutputStream testOut = new PipedOutputStream();
        PipedInputStream shellIn = new PipedInputStream(testOut);
        PipedOutputStream shellOut = new PipedOutputStream();
        PipedInputStream testIn = new PipedInputStream(shellOut);

        TestIntegrator testIntegrator = new TestIntegrator(testIn, testOut, testCases);
        testIntegrator.start();

        try {
            BShellConfiguration configuration = new BShellConfiguration(false,
                    BShellConfiguration.EvaluatorMode.DEFAULT, shellIn, shellOut);
            ReplShellApplication.execute(configuration);
        } catch (EndOfFileException ignored) {
        }

        testIntegrator.interrupt();
    }
}
