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
package org.ballerinalang.test.query;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test query-action-or-expr.
 *
 * @since 2201.2.0
 */
public class QueryActionOrExprTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/query/query_action_or_expr.bal");
    }

    @Test(dataProvider = "dataToTestQueryActionOrExpr")
    public void testQueryActionOrExpr(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryActionOrExpr() {
        return new Object[]{
                "testQueryActionOrExprWithStartAction",
                "testQueryActionOrExprWithParenthesizedStartAction",
                "testQueryActionOrExprWithWaitAction",
                "testQueryActionOrExprWithParenthesizedWaitAction",
                "testQueryActionOrExprWithClientRemoteMethodCall",
                "testQueryActionOrExprWithParenthesizedClientRemoteMethodCall",
                "testQueryActionOrExprWithQueryAction",
                "testQueryActionOrExprWithParenthesizedQueryAction",
                "testQueryActionOrExprWithTypeCastActionOrExpr",
                "testQueryActionOrExprWithParenthesizedTypeCastActionOrExpr",
                "testQueryActionOrExprWithCheckingActionOrExpr",
                "testQueryActionOrExprWithParenthesizedCheckingActionOrExpr",
                "testQueryActionOrExprWithTrapActionOrExpr",
                "testQueryActionOrExprWithParenthesizedTrapActionOrExpr",
                "testQueryActionOrExprWithQueryActionOrExpr",
                "testQueryActionOrExprWithParenthesizedQueryActionOrExpr",
                "testPrecedenceOfActionsWithQueryActionOrExpr",
                "testQueryActionOrExprWithAllQueryClauses",
                "testQueryActionOrExprWithNestedQueryActionOrExpr",
                "testQueryActionOrExprWithQueryConstructingTable",
                "testQueryActionOrExprWithQueryConstructingStream",
                "testQueryActionOrExprWithClientResourceAccessAction",
                "testQueryActionOrExprWithGroupedClientResourceAccessAction",
                "testNestedQueryActionOrExprWithClientResourceAccessAction",
                "testQueryActionWithQueryExpression",
                "testQueryActionWithRegexpLangLibs",
                "testQueryExprWithRegExpLangLibs",
                "testQueryActionWithInterpolationRegexpLangLibs",
                "testQueryActionOrExpressionWithUnionRecordResultType",
                "testQueryActionOrExprWithAnyOrErrResultType",
                "testNestedQueryActionOrExprWithClientResourceAccessAction",
                "testQueryActionWithQueryExpression",
                "testQueryActionWithLetExpression"
        };
    }

    @Test
    public void testQueryingEmptyTuple() {
        BRunUtil.invoke(compileResult, "testQueryingEmptyTuple");
    }

    @Test
    public void testQueryActionOrExprSemanticsNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/query/query_action_or_expr_semantic_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: 'future<int[]>' is not an iterable collection",
                18, 23);
        validateError(negativeResult, i++, "incompatible types: 'future<int[]>' is not an iterable collection",
                21, 24);
        validateError(negativeResult, i++, "incompatible types: '(int[]|error)' is not an iterable collection",
                27, 23);
        validateError(negativeResult, i++, "incompatible types: '(int[]|error)' is not an iterable collection",
                30, 24);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 39, 23);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 42, 24);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 51, 27);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 61, 27);
        validateError(negativeResult, i++, "incompatible types: expected 'other', found 'int'", 71, 18);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 71, 27);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 82, 27);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 93, 27);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 133, 27);
        validateError(negativeResult, i++, "incompatible types: 'other' is not an iterable collection", 151, 27);
        validateError(negativeResult, i++, "receive action not supported wth 'var' type", 151, 27);
        validateError(negativeResult, i++, "receive action not supported wth 'var' type", 168, 27);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 279, 15);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 291, 18);
        validateError(negativeResult, i++, "order by not supported for complex type fields, order key should " +
                "belong to a basic type", 291, 18);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 303, 15);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 303, 15);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 315, 23);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 316, 21);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 320, 50);
        validateError(negativeResult, i++, "incompatible types: expected '(T3[]|T4[])', found '(T3|T4)[]'",
                339, 13);
        validateError(negativeResult, i++, "incompatible types: expected '(int[]|string[])', found '(int|string)[]'",
                359, 24);
        validateError(negativeResult, i++, "incompatible types: expected '(int[]|string[])', found '(int|boolean)[]'",
                363, 24);
        validateError(negativeResult, i++, 
                "incompatible types: expected '(string[]|decimal[])', found '(int|float)[]'",
                367, 28);
        validateError(negativeResult, i++, "incompatible types: expected '(table<FooType>|table<BarType>)', " +
                        "found 'table<(FooType|BarType)> key(id)'", 371, 39);
        validateError(negativeResult, i++, "ambiguous type '[string:Char, string]'", 376, 78);
        validateError(negativeResult, i++, "incompatible types: expected 'boolean', " +
                "found 'T3'", 382, 11);
        validateError(negativeResult, i++, "incompatible types: expected 'T3', " +
                "found 'T4'", 384, 13);
        validateError(negativeResult, i++, "incompatible types: expected 'T3', " +
                "found 'T2'", 386, 13);
        validateError(negativeResult, i++, "missing non-defaultable required record field 't3OrT4'", 
                386, 17);
        validateError(negativeResult, i++, "incompatible types: expected 'T3', " +
                "found 'T1'", 388, 12);
        validateError(negativeResult, i++, "missing non-defaultable required record field 't3s'",
                388, 16);
        validateError(negativeResult, i++, "incompatible types: expected '(T3[]|T4[])', " +
                "found '(T4|T2|T1)[]'", 394, 13);
        validateError(negativeResult, i++, "missing non-defaultable required record field 't3OrT4'",
                398, 17);
        validateError(negativeResult, i++, "missing non-defaultable required record field 't3s'",
                400, 16);
        validateError(negativeResult, i++, "ambiguous type '(Baz|Qux)'", 439, 16);
        validateError(negativeResult, i++, "ambiguous type '(Baz|Qux)'", 446, 16);
        validateError(negativeResult, i++, "incompatible types: expected '(boolean[]|float[])', " +
                "found 'record {| string c; |}[]'", 452, 8);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testQueryActionOrExprCodeAnalysisNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/query/query_action_or_expr_code_analysis_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 22, 25);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 23, 20);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 31, 25);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 32, 20);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 40, 25);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 42, 20);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 50, 25);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 52, 20);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 60, 25);
        validateError(negativeResult, i++, "worker send statement position not supported yet, must be a top level " +
                "statement in a worker", 62, 20);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 95, 25);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 96, 20);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 104, 25);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 105, 20);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 113, 25);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 115, 20);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 123, 25);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 125, 20);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 133, 25);
        validateError(negativeResult, i++, "invalid worker receive statement position, must be a top level " +
                "statement in a worker", 135, 20);
        validateError(negativeResult, i++, "worker send statement position not supported yet, " +
                "must be a top level statement in a worker", 164, 20);
        validateError(negativeResult, i++, "worker send statement position not supported yet, " +
                "must be a top level statement in a worker", 175, 26);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testMatchStatementInsideDoClause() {
        CompileResult result = BCompileUtil.compile("test-src/query/match-stmt-in-do-clause.bal");
        Assert.assertEquals(result.getErrorCount(), 0);

        BRunUtil.invoke(result, "testConstMatchPattern1");
        BRunUtil.invoke(result, "testConstMatchPattern2");
        BRunUtil.invoke(result, "testBindingPatternsInMatchStatement");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
