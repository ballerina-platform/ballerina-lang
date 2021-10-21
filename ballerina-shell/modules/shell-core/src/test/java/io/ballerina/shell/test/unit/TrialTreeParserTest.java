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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.shell.exceptions.TreeParserException;
import io.ballerina.shell.parser.TreeParser;
import io.ballerina.shell.test.TestUtils;
import io.ballerina.shell.test.unit.base.TestCase;
import io.ballerina.shell.test.unit.base.TestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

/**
 * Test tree parser use cases.
 *
 * @since 2.0.0
 */
public class TrialTreeParserTest {
    private static final String STATEMENT_TESTCASES = "testcases/treeparser.statement.json";
    private static final String IMPORT_TESTCASES = "testcases/treeparser.import.json";
    private static final String EXPRESSION_TESTCASES = "testcases/treeparser.expression.json";
    private static final String MODULE_DCLN_TESTCASES = "testcases/treeparser.moduledcln.json";
    private static final String MISC_TESTCASES = "testcases/treeparser.misc.json";
    private static final String MODULE_DCLN_MAIN_TESTCASE = "testcases/treeparser.dcln.main.json";
    private static final String MODULE_DCLN_INIT_TESTCASE = "testcases/treeparser.dcln.init.json";
    private static final String MODULE_DCLN_RESERVED_TESTCASE = "testcases/treeparser.dcln.underscore.json";

    @Test
    public void testImportParse() {
        testParse(IMPORT_TESTCASES, ImportDeclarationNode.class);
    }

    @Test
    public void testModuleMemberDeclarationParse() {
        // TODO: Add test cases with Annotation Declaration
        testParse(MODULE_DCLN_TESTCASES, ModuleMemberDeclarationNode.class);
    }

    @Test
    public void testStatementParse() {
        // In this test cases, Some statements are not tested;
        //  - Expression Statement - These are caught as an expression
        //  - Local Type Definition Statement - These are caught as a module level dcln
        //  - XML Namespace Declaration Statement - These are caught as a module level dcln
        //  - Module Variable Declaration - These are caught as a module level dcln
        testParse(STATEMENT_TESTCASES, StatementNode.class);
    }

    @Test
    public void testExpressionParse() {
        testParse(EXPRESSION_TESTCASES, ExpressionNode.class);
    }

    // TODO enable testcase
//    @Test
//    public void testMiscParse() {
//        testParse(MISC_TESTCASES, Node.class);
//    }

    @Test(expectedExceptions = TreeParserException.class)
    public void testModuleDclnNameMainTest() throws TreeParserException {
        testModuleDclnName(MODULE_DCLN_MAIN_TESTCASE);
    }

    @Test(expectedExceptions = TreeParserException.class)
    public void testModuleDclnNameInitTest() throws TreeParserException {
        testModuleDclnName(MODULE_DCLN_INIT_TESTCASE);
    }

    @Test(expectedExceptions = TreeParserException.class)
    public void testModuleDclnNameReservedTest() throws TreeParserException {
        testModuleDclnName(MODULE_DCLN_RESERVED_TESTCASE);
    }

    private void testParse(String fileName, Class<?> parentClazz) {
        TestCases testCases = TestUtils.loadTestCases(fileName, TestCases.class);
        TreeParser treeParser = TestUtils.getTestTreeParser();
        for (TestCase testCase : testCases) {
            try {
                Collection<Node> nodes = treeParser.parse(testCase.getInput());
                for (Node node : nodes) {
                    String actual = node.getClass().getSimpleName();
                    Assert.assertEquals(List.of(actual), testCase.getExpected(), testCase.getName());
                    Assert.assertTrue(parentClazz.isInstance(node), testCase.getName() + " not expected instance");
                }
            } catch (TreeParserException e) {
                Assert.assertNull(testCase.getExpected(), testCase.getName() + " error: " + e.getMessage());
            }
        }
    }

    private void testModuleDclnName(String fileName) throws TreeParserException {
        TestCases testCases = TestUtils.loadTestCases(fileName, TestCases.class);
        TreeParser treeParser = TestUtils.getTestTreeParser();
        for (TestCase testCase : testCases) {
            treeParser.parse(testCase.getInput());
        }
    }
}
