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

package io.ballerina.shell.test.evaluator;

import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.Evaluator;
import io.ballerina.shell.EvaluatorBuilder;
import io.ballerina.shell.ShellCompilation;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.test.TestUtils;
import io.ballerina.shell.test.evaluator.base.TestCase;
import io.ballerina.shell.test.evaluator.base.TestInvoker;
import io.ballerina.shell.test.evaluator.base.TestSession;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Base class for evaluator tests.
 * TODO: Find a way to test concurrency.
 * TODO: Add db lib/http lib support and test Transactions
 *
 * @since 2.0.0
 */
public abstract class AbstractEvaluatorTest {
    private static final String TEST_HEADER = "test.bal";

    /**
     * Tests a json file containing test case session.
     * This is a E2E test of the evaluator.
     *
     * @param fileName File containing test cases.
     */
    protected void testEvaluate(String fileName) throws BallerinaShellException {
        // Create evaluator
        TestInvoker invoker = new TestInvoker();
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .invoker(invoker).build();
        try {
            evaluator.initialize();
            evaluator.evaluateDeclarationFile(TestUtils.getPath(TEST_HEADER));
        } catch (Exception e) {
            Assert.fail(evaluator.diagnostics().toString(), e);
        }

        TestSession testSession = TestUtils.loadTestCases(fileName, TestSession.class);
        for (TestCase testCase : testSession) {
            try {
                String testCode = testCase.getCode();

                // Evaluating files
                if (testCode.startsWith("/file")) {
                    String loadFile = testCode.split("\\s")[1];
                    evaluator.evaluateDeclarationFile(TestUtils.getPath(loadFile));
                    continue;
                }
                // Removing declarations
                if (testCode.startsWith("/remove")) {
                    List<String> names = Arrays.asList(testCode.split("\\s"));
                    names.remove(0);
                    evaluator.delete(names);
                    continue;
                }

                ShellCompilation shellCompilation = evaluator.getCompilation(testCase.getCode());
                Optional<PackageCompilation> compilation = shellCompilation.getPackageCompilation();
                String expr = evaluator.getValue(compilation);
                Assert.assertEquals(invoker.getStdOut(), testCase.getStdout(), testCase.getDescription());
                Assert.assertEquals(expr, testCase.getExpr(), testCase.getDescription());
                Assert.assertNull(testCase.getError(), testCase.getDescription());
                Assert.assertFalse(evaluator.hasErrors(), testCase.getDescription());
            } catch (BallerinaShellException e) {
                Assert.assertTrue(evaluator.hasErrors(), testCase.getDescription());
                String errorClass = e.getClass().getSimpleName();
                if (testCase.getError() != null) {
                    Assert.assertEquals(invoker.getStdOut(), testCase.getStdout(), testCase.getDescription());
                    Assert.assertEquals(testCase.getError(), errorClass, testCase.getDescription());
                    continue;
                }
                Assert.fail(String.format("Exception occurred in: %s, error: %s, with diagnostics: %s",
                        testCase.getDescription(), e.getMessage(), evaluator.diagnostics()));
            } finally {
                evaluator.resetDiagnostics();
                invoker.reset();
            }
        }
    }
}
