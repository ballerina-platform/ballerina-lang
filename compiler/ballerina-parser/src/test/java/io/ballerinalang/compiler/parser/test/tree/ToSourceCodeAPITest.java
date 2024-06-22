/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains test case to validate the Node.toSourceCode() method.
 *
 * @since 2.0.0
 */
public class ToSourceCodeAPITest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testToSourceCodeAPIBasic() {
        String expectedSourceCode = """
                public function add() {
                    int z = x + y;
                }

                public function multiply() {
                    int z = x * (z - x) + x / (y + (a+b));
                }
                """;
        assertSourceCode(expectedSourceCode);
    }

    @Test
    public void testWithMissingTokens() {
        String expectedSourceCode = """
                public function add() {
                    int z = x + ;
                }

                public function multiply() {
                    int z = * (z - x) + x / (y + (a+b));
                }
                """;
        assertSourceCode(expectedSourceCode);
    }

    @Test
    public void testWithInvalidNodeMinutia() {
        String expectedSourceCode = """
                public function add(int ...a, int b, int c = 10, int k) {
                    int z = x + y;
                }
                """;
        assertSourceCode(expectedSourceCode);
    }

    @Test
    public void testWithWindowNewLineChars() {
        String expectedSourceCode = """
                public function add() {\r
                    int z = x + y;\r
                }\r
                \r
                public function multiply() {\r
                    int z = x * (z - x) + x / (y + (a+b));\r
                }\r
                """;
        assertSourceCode(expectedSourceCode);
    }

    @Test
    public void testWithMixedNewLineChars() {
        String expectedSourceCode = """
                public function add() {\r
                    int z = x + y;
                }\r
                \r
                public function multiply() {\r
                    int z = x * (z - x) + x / (y + (a+b));
                }\r
                """;
        assertSourceCode(expectedSourceCode);
    }

    private void assertSourceCode(String expectedSourceCode) {
        SyntaxTree syntaxTree = parseString(expectedSourceCode);
        String actualSourceCode = syntaxTree.toSourceCode();
        Assert.assertEquals(actualSourceCode, expectedSourceCode);
    }
}
