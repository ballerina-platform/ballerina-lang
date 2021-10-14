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

import io.ballerina.shell.Evaluator;
import io.ballerina.shell.EvaluatorBuilder;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.test.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Misc functionality testing of evaluator.
 *
 * @since 2.0.0
 */
public class EvaluatorMiscTest {
    @Test
    public void testInitialization() throws BallerinaShellException {
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .build();
        evaluator.initialize();
        Assert.assertFalse(evaluator.hasErrors());
    }

    @Test
    public void testEvaluatorReset() throws BallerinaShellException {
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .build();
        Assert.assertTrue(evaluator.diagnostics().isEmpty());
        evaluator.initialize();
//        String assignment = evaluator.evaluate("int i = 4");
//        String result = evaluator.evaluate("i");
//        Assert.assertEquals(result, "4");
        Assert.assertFalse(evaluator.diagnostics().isEmpty());
        evaluator.reset();
        Assert.assertTrue(evaluator.diagnostics().isEmpty());
    }

    @Test
    public void testEvaluatorImportList() throws BallerinaShellException {
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .build();
        evaluator.initialize();
        evaluator.evaluate("import ballerina/jballerina.java");
        evaluator.evaluate("import ballerina/lang.'int as prefix");
        evaluator.evaluate("import ballerina/lang.'float as prefix2");
        Assert.assertEquals(new HashSet<>(evaluator.availableImports()),
                Set.of(
                        "(java) import ballerina/jballerina.java;",
                        "(prefix) import ballerina/lang.'int as prefix;",
                        "(prefix2) import ballerina/lang.'float as prefix2;"
                )
        );
        Assert.assertTrue(evaluator.availableVariables().isEmpty());
        Assert.assertEquals(evaluator.availableImports().size(), 3);
        Assert.assertTrue(evaluator.availableModuleDeclarations().isEmpty());
    }

    @Test
    public void testEvaluatorLangImportList() throws BallerinaShellException {
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .build();
        evaluator.initialize();
        evaluator.evaluate("import ballerina/lang.'int");
        evaluator.evaluate("import ballerina/lang.'float");
        evaluator.evaluate("import ballerina/lang.'object");
        evaluator.evaluate("import ballerina/lang.'boolean");
        Assert.assertEquals(new HashSet<>(evaluator.availableImports()),
                Set.of(
                        "('int) import ballerina/lang.'int;",
                        "('float) import ballerina/lang.'float;",
                        "('object) import ballerina/lang.'object;",
                        "('boolean) import ballerina/lang.'boolean;"
                )
        );
        Assert.assertTrue(evaluator.availableVariables().isEmpty());
        Assert.assertEquals(evaluator.availableImports().size(), 4);
        Assert.assertTrue(evaluator.availableModuleDeclarations().isEmpty());
    }

    @Test
    public void testEvaluatorVarDclns() throws BallerinaShellException {
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .build();
        evaluator.initialize();
        evaluator.evaluate("int i = 23");
        evaluator.evaluate("string? k = ()");
        evaluator.evaluate("string t = \"Hello\"");
        evaluator.evaluate("var f = function () returns int {return 1;}");
        evaluator.evaluate("int a = 1; string b = \"World\"");
        Assert.assertEquals(new HashSet<>(evaluator.availableVariables()),
                Set.of(
                        "(a) int a = 1",
                        "(k) string? k = ()",
                        "(t) string t = \"Hello\"",
                        "(f) function () returns int f = function isolated function () returns (int)",
                        "(i) int i = 23",
                        "(b) string b = \"World\""
                )
        );
        Assert.assertEquals(evaluator.availableImports().size(), 0);
        Assert.assertTrue(evaluator.availableModuleDeclarations().isEmpty());
    }

    @Test
    public void testEvaluatorModuleDclns() throws BallerinaShellException {
        Evaluator evaluator = new EvaluatorBuilder()
                .treeParser(TestUtils.getTestTreeParser())
                .build();
        evaluator.initialize();
        evaluator.evaluate("function a() {}");
        evaluator.evaluate("const t = 100");
        evaluator.evaluate("class A{}");
        evaluator.evaluate("enum B{C, D}");
        Assert.assertEquals(new HashSet<>(evaluator.availableModuleDeclarations()),
                Set.of(
                        "(a) function a() {}",
                        "(t) const t = 100;",
                        "(A) class A{}",
                        "(B) enum B{C, D}"
                )
        );
        Assert.assertEquals(evaluator.availableImports().size(), 0);
        Assert.assertTrue(evaluator.availableVariables().isEmpty());
    }
}
