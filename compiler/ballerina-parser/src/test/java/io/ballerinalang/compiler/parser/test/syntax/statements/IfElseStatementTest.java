/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.statements;

import org.testng.annotations.Test;

/**
 * Test parsing if-else statements.
 */
public class IfElseStatementTest extends AbstractStatementTest {

    // Valid source tests

    @Test
    public void testSimpleIfElse() {
        testFile("if-else/if_else_source_01.bal", "if-else/if_else_assert_01.json");
    }

    @Test
    public void testComplexIfElse() {
        testFile("if-else/if_else_source_02.bal", "if-else/if_else_assert_02.json");
    }

    // Recovery tests

    @Test
    public void testMissingOpenAndCloseBraces() {
        testFile("if-else/if_else_source_03.bal", "if-else/if_else_assert_03.json");
    }

    @Test
    public void testMissingOpenAndCloseBracesWithStmts() {
        testFile("if-else/if_else_source_04.bal", "if-else/if_else_assert_04.json");
    }

    @Test
    public void testIfKeywordOnly() {
        testFile("if-else/if_else_source_05.bal", "if-else/if_else_assert_05.json");
    }

    @Test
    public void testMissingCloseBracesBeforeElse() {
        testFile("if-else/if_else_source_06.bal", "if-else/if_else_assert_06.json");
    }

    @Test
    public void testMissingIfKeywordInElseIf() {
        testFile("if-else/if_else_source_07.bal", "if-else/if_else_assert_07.json");
    }

    @Test
    public void testMissingBracesInNestedIf() {
        testFile("if-else/if_else_source_08.bal", "if-else/if_else_assert_08.json");
    }

    @Test
    public void testMissingIfKeyword() {
        testFile("if-else/if_else_source_09.bal", "if-else/if_else_assert_09.json");
    }
}
