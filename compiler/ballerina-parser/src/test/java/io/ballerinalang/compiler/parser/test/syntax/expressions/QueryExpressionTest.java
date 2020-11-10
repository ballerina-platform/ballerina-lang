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
package io.ballerinalang.compiler.parser.test.syntax.expressions;

import org.testng.annotations.Test;

/**
 * Test parsing query expression.
 */
public class QueryExpressionTest extends AbstractExpressionsTest {

    // Valid syntax

    @Test
    public void testSimplestQuery() {
        test("from int a in b select c", "query-expr/query_expr_assert_01.json");
    }

    @Test
    public void testQueryWithFromIntermediateClause() {
        test("from int a in b from int c in d select e", "query-expr/query_expr_assert_02.json");
    }

    @Test
    public void testQueryWithWhereIntermediateClause() {
        test("from int a in b where c select d", "query-expr/query_expr_assert_03.json");
    }

    @Test
    public void testQueryWithLetIntermediateClause() {
        test("from int a in b let int c = d select e", "query-expr/query_expr_assert_04.json");
        test("from int a in b let @bar{A:B} int c = d, int e = f select e",
                "query-expr/query_expr_assert_05.json");
    }

    @Test
    public void testQueryWithMultipleIntermediateClauses() {
        test("from int a in b where c where d select e", "query-expr/query_expr_assert_06.json");
        test("from int a in b where c from int d in e select f", "query-expr/query_expr_assert_07.json");
        test("from int a in b from int d in e let int f = g, int h = i where j select k",
                "query-expr/query_expr_assert_08.json");
    }

    @Test
    public void testQueryWithStreamKeyword() {
        test("stream from int a in b select e", "query-expr/query_expr_assert_09.json");
    }

    @Test
    public void testQueryWithTableKeyword() {
        test("table key() from int a in b select e", "query-expr/query_expr_assert_10.json");
        test("table key(name, age) from int a in b select e", "query-expr/query_expr_assert_11.json");
    }

    @Test
    public void testQueryWithOrderByClause() {
        test("from int a in b where c let int d = e order by f select g", "query-expr/query_expr_assert_39.json");
        test("from int a in b where c let int d = e order by f ascending, g select h", "query-expr" +
                "/query_expr_assert_40.json");
        test("from int a in b order by c ascending, d descending select e", "query-expr" +
                "/query_expr_assert_41.json");
    }

    @Test
    public void testQueryWithOnConflictAndLimitClauses() {
        test("from int a in b select c on conflict d", "query-expr/query_expr_assert_51.json");
        test("from int a in b limit c select d", "query-expr/query_expr_assert_52.json");
        test("from int a in b limit c select d on conflict e", "query-expr/query_expr_assert_53.json");
    }

    @Test
    public void testQueryWithInnerJoinClause() {
        test("from int a in b join int c in d on e equals f select g", "query-expr/query_expr_assert_54.json");
    }

    @Test
    public void testQueryWithOuterJoinClause() {
        test("from int a in b outer join int c in d on e equals f select g",
                "query-expr/query_expr_assert_55.json");
    }

    // Recovery tests

    @Test
    public void testQueryWithMissingKeySpecifier() {
        test("table from int a in b select c", "query-expr/query_expr_assert_12.json");
    }

    @Test(enabled = false) // no longer valid, since "key" is not a keyword
    public void testQueryWithMissingTableKeyword() {
        test("key() from int a in b select c", "query-expr/query_expr_assert_13.json");
    }

    @Test
    public void testQueryFromClauseWithMissingFromKeyword() {
        test("int a in b select c", "query-expr/query_expr_assert_14.json");
        test("from int a in b int c in d select e", "query-expr/query_expr_assert_15.json");
        test("from int a in b where c int d in e select f", "query-expr/query_expr_assert_16.json");
        test("from int a in b let int c = d int d in e select f", "query-expr/query_expr_assert_17.json");
    }

    @Test
    public void testQueryFromClauseWithMissingVarName() {
        test("from int in b select c", "query-expr/query_expr_assert_18.json");
    }

    @Test
    public void testQueryFromClauseWithMissingInKeyword() {
        test("from int a b select c", "query-expr/query_expr_assert_19.json");
    }

    @Test
    public void testQueryFromClauseWithMissingExpression() {
        test("from int a in select c", "query-expr/query_expr_assert_20.json");
        test("from int a in b from int c in where d select e", "query-expr/query_expr_assert_21.json");
    }

    @Test
    public void testQuerySelectClauseWithMissingSelectKeyword() {
        test("from int a in b c", "query-expr/query_expr_assert_22.json");
    }

    @Test
    public void testQuerySelectClauseWithMissingExpression() {
        test("from int a in b select", "query-expr/query_expr_assert_23.json");
    }

    @Test
    public void testQueryWhereClauseWithMissingExpression() {
        test("from int a in b where select d", "query-expr/query_expr_assert_24.json");
    }

    @Test
    public void testQueryLetClauseWithMissingLetKeyword() {
        test("from int a in b int c = d select e", "query-expr/query_expr_assert_25.json");
        test("from int a in b @A{} int c = d select e", "query-expr/query_expr_assert_26.json");
    }

    @Test
    public void testQueryLetClauseWithMissingVarName() {
        test("from int a in b let int = d select e", "query-expr/query_expr_assert_27.json");
    }

    @Test
    public void testQueryLetClauseWithMissingEqualToken() {
        test("from int a in b let int c d select e", "query-expr/query_expr_assert_28.json");
    }

    @Test
    public void testQueryLetClauseWithMissingComma() {
        test("from int a in b let int c = d int e = f select g", "query-expr/query_expr_assert_29.json");
    }

    @Test
    public void testQueryWithExtraToken() {
        test("table foo key() from int a in b select g", "query-expr/query_expr_assert_30.json");
        test("table key() foo from int a in b select g", "query-expr/query_expr_assert_31.json");
    }

    @Test(enabled = false) // no longer valid, since "key" is not a keyword
    public void testQueryWithTwoKeySpecifiersWithExtraTokenInBetween() {
        test("table key(a) foo key(b) []", "query-expr/query_expr_assert_32.json");
    }

    @Test
    public void testQueryWithExtraClausesAfterSelectClause() {
        test("from int a in b select c where d", "query-expr/query_expr_assert_33.json");
        test("from int a in b select c select d", "query-expr/query_expr_assert_34.json");
    }

    @Test
    public void testQueryWithOperatorPrecedence() {
        test("from int a in b select c + from int d in e select f", "query-expr/query_expr_assert_35.json");
    }

    @Test
    public void testQueryWithOperatorPrecedenceWithExtraClauses() {
        test("from int a in b select c where e + from int d in e select f", "query-expr/query_expr_assert_36.json");
        test("from int a in b select c from int d in e + from int f in g select h",
                "query-expr/query_expr_assert_37.json");
        test("from int a in b select c let int a = b + from int f in g select h",
                "query-expr/query_expr_assert_38.json");
    }

    @Test
    public void testQueryWithOrderByClauseRecovery() {
        test("from int a in b order by select c", "query-expr/query_expr_assert_42.json");
        test("from int a in b order c select d", "query-expr/query_expr_assert_43.json");
        test("from int a in b order by c ascending d e select f", "query-expr/query_expr_assert_44.json");
        test("from int a in b by c select d", "query-expr/query_expr_assert_45.json");
        test("from int a in b order by ascending select e", "query-expr/query_expr_assert_46.json");
    }

    @Test
    public void testQueryWithOnConflictAndLimitClauseRecovery() {
        test("from int a in b select c on d", "query-expr/query_expr_assert_47.json");
        test("from int a in b select c conflict d", "query-expr/query_expr_assert_48.json");
        test("from int a in b limit select", "query-expr/query_expr_assert_49.json");
        test("from int a in b limit select conflict", "query-expr/query_expr_assert_50.json");
    }

    @Test
    public void testQueryWithJoinClauseRecovery() {
        test("from int a in b join in d on e equals f select g", "query-expr/query_expr_assert_56.json");
        test("from int a in b join int c d on e equals f select g", "query-expr/query_expr_assert_57.json");
        test("from int a in b join int c in on e equals f select g", "query-expr/query_expr_assert_59.json");
        test("from int a in b outer int c in d on e equals f select g", "query-expr/query_expr_assert_63.json");
    }

    @Test
    public void testQueryWithJoinOnConditionRecovery() {
        test("from int a in b join int c in d on select g", "query-expr/query_expr_assert_58.json");
        test("from int a in b join int c in d e equals f select g", "query-expr/query_expr_assert_60.json");
        test("from int a in b join int c in d select g", "query-expr/query_expr_assert_61.json");
        test("from int a in b outer join int c in d select g", "query-expr/query_expr_assert_62.json");
        test("from int a in b equals select g", "query-expr/query_expr_assert_64.json");
    }
}
