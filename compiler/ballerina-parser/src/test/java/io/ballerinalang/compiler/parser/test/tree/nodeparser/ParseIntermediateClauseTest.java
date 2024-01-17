/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code parseIntermediateClause} method.
 */
public class ParseIntermediateClauseTest {

    // Valid syntax tests

    @Test
    public void testValidFromClause() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "from int i in [1, 2, 3]", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidFromClause2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "from int i in intList", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidFromClause3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "from var _ in [1, 2, 3]", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidFromClause4() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "from int _ in [1, 2, 3]", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidFromClause5() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "from var i in a -> w2", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidWhereClause() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "where true", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidWhereClause2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "where i == 1", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidWhereClause3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "where evaluateTrue()", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidWhereClause4() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "where 1 != 1", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidWhereClause5() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "where !evaluateTrue()", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLetClause() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "let int a = 1", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLetClause2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "let int _ = 1", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLetClause3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("let record" +
                " { int id; } a = {id: 1}", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLetClause4() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "let int a = 1, int b = 7", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLetClause5() {
        IntermediateClauseNode intermediateNode = NodeParser
                .parseIntermediateClause("let int a = 1 + 2, int b = 7 - 2, int c = getInt()", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidJoinClause() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("join var user in " +
                "table [{id: 1234, name: \"Keith\"}, {id: 6789, name: \"Anne\"}] " +
                "on login.userId equals user.id", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.JOIN_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidJoinClause2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("outer join var " +
                "user in table [{id: 1234, name: \"Keith\"}, {id: 6789, name: \"Anne\"}] " +
                "on login.userId equals user.id", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.JOIN_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidJoinClause3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("outer join var " +
                "user in getTable() on login.userId equals user.id", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.JOIN_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidJoinClause4() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("outer join var " +
                "user in table[] on loginUserId equals userId", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.JOIN_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidJoinClause5() {
        IntermediateClauseNode intermediateNode = NodeParser
                .parseIntermediateClause("join var user in getTable() on loginUserId equals userId", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.JOIN_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidOrderClause() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("order by name", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.ORDER_BY_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidOrderClause2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("order by employee.name", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.ORDER_BY_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidOrderClause3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("order by " +
                "getEmployee().id", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.ORDER_BY_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidOrderClause4() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("order by name " +
                "ascending, emp.id ascending", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.ORDER_BY_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidOrderClause5() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("order by name" +
                " ascending, emp.id descending", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.ORDER_BY_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLimitClause() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("limit 1", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LIMIT_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLimitClause2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("limit getName()", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LIMIT_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    @Test
    public void testValidLimitClause3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("limit emp.id", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LIMIT_CLAUSE);
        Assert.assertFalse(intermediateNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.toString(), " MISSING[where] MISSING[]");
    }

    @Test
    public void testWithIntermediateClauseRecovery1() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("from", true);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.toString(), "from MISSING[] MISSING[] MISSING[in] MISSING[]");
    }

    @Test
    public void testWithIntermediateClauseRecovery2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("from int", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.toString(), "from int MISSING[] MISSING[in] MISSING[]");
    }

    @Test
    public void testWithInvalidTokens() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("% isolated function " +
                "from int i in [1, 2, 3] +", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 15);
        Assert.assertEquals(intermediateNode.toString(), " MISSING[where] MISSING[ INVALID[%]  " +
                "INVALID[isolated]  INVALID[function]  INVALID[from]  INVALID[int]  INVALID[i] " +
                " INVALID[in]  INVALID[[] INVALID[1] INVALID[,]  INVALID[2] INVALID[,]" +
                "  INVALID[3] INVALID[]]  INVALID[+]]");
    }

    @Test
    public void testWithInvalidTokens2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("invalid!", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 2);
        Assert.assertEquals(intermediateNode.toString(), " MISSING[where] MISSING[ INVALID[invalid] INVALID[!]]");
    }

    @Test
    public void testWithInvalidTokens3() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause(
                "from var i in a -> w2", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.FROM_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 3);
        Assert.assertEquals(intermediateNode.toString(), "from var i in  MISSING[ INVALID[a] " +
                " INVALID[->]  INVALID[w2]]");
    }

    @Test
    public void testWithAStmt() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("int num = 3", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.LET_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.toString(), " MISSING[let]int num = 3");
    }

    @Test
    public void testWithAStmt2() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("a += 4", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 4);
        Assert.assertEquals(intermediateNode.toString(), " MISSING[where] MISSING[ INVALID[a]" +
                "  INVALID[+] INVALID[=]  INVALID[4]]");
    }

    @Test
    public void testWithAExpression() {
        IntermediateClauseNode intermediateNode = NodeParser.parseIntermediateClause("func()", false);
        Assert.assertEquals(intermediateNode.kind(), SyntaxKind.WHERE_CLAUSE);
        Assert.assertTrue(intermediateNode.hasDiagnostics());
        Assert.assertEquals(intermediateNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(intermediateNode.trailingInvalidTokens().size(), 3);
        Assert.assertEquals(intermediateNode.toString(), " MISSING[where] MISSING[ INVALID[func] " +
                "INVALID[(] INVALID[)]]");
    }
}
