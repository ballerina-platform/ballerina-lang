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
public class QueryExpressionTest extends AbstractExpressionsTest{

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
}
