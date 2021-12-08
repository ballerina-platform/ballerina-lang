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

package io.ballerina.shell.test.unit;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.classload.ClassLoadInvoker;
import io.ballerina.shell.parser.TreeParser;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.factory.BasicSnippetFactory;
import io.ballerina.shell.snippet.factory.SnippetFactory;
import io.ballerina.shell.test.TestUtils;
import io.ballerina.shell.test.unit.base.TestCase;
import io.ballerina.shell.test.unit.base.TestCases;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Optional;

/**
 * Test invoker use cases.
 *
 * @since 2.0.0
 */
public class InvokerTest {
    private static final String FINAL_KEYWORD_TESTCASE = "testcases/invoker.final.json";
    private static final String FINAL_KEYWORD_FUNCTION_TESTCASE = "testcases/invoker.final.function.json";
    private static final String FUNCTION_SAME_NAME_TESTCASE = "testcases/invoker.function.name.json";
    private static final String VARIABLE_SAME_NAME_TESTCASE = "testcases/invoker.variable.name.json";
    private static final String NO_MODULE_FOUND = "testcases/invoker.no.module.json";
    private static final String SAME_PREFIX_FOUND = "testcases/invoker.same.prefix.json";

    @Test(expectedExceptions = InvokerException.class)
    public void testFinalKeyword() throws BallerinaShellException {
        testInvoker(FINAL_KEYWORD_TESTCASE);
    }

    @Test(expectedExceptions = InvokerException.class)
    public void testFinalKeywordFunction() throws BallerinaShellException {
        testInvoker(FINAL_KEYWORD_FUNCTION_TESTCASE);
    }

    @Test(expectedExceptions = InvokerException.class)
    public void testFunctionSameName() throws BallerinaShellException {
        testInvoker(FUNCTION_SAME_NAME_TESTCASE);
    }

    @Test(expectedExceptions = InvokerException.class)
    public void testVariableSameName() throws BallerinaShellException {
        testInvoker(VARIABLE_SAME_NAME_TESTCASE);
    }

    @Test(expectedExceptions = InvokerException.class)
    public void testNoModuleFound() throws BallerinaShellException {
        testInvoker(NO_MODULE_FOUND);
    }

    @Test(expectedExceptions = InvokerException.class)
    public void testSamePrefix() throws BallerinaShellException {
        testInvoker(SAME_PREFIX_FOUND);
    }

    private void testInvoker(String fileName) throws BallerinaShellException {
        TestCases testCases = TestUtils.loadTestCases(fileName, TestCases.class);
        TreeParser treeParser = TestUtils.getTestTreeParser();
        SnippetFactory snippetFactory = new BasicSnippetFactory();
        ClassLoadInvoker invoker = new ClassLoadInvoker();
        invoker.initialize();
        for (TestCase testCase : testCases) {
            Collection<Node> nodes = treeParser.parseString(testCase.getInput());
            Collection<Snippet> snippets = snippetFactory.createSnippets(nodes);
            PackageCompilation compilation = invoker.getCompilation(snippets);
            invoker.execute(Optional.ofNullable(compilation));
        }
    }
}
