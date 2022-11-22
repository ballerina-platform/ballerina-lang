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
                "testNestedQueryActionOrExprWithClientResourceAccessAction"
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
        validateError(negativeResult, i++, "async send action not yet supported as expression", 51, 27);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 52, 25);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 53, 20);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 61, 27);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 62, 25);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 63, 20);
        validateError(negativeResult, i++, "incompatible types: expected 'other', found 'int'", 71, 18);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 71, 27);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 72, 25);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 74, 20);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 82, 27);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 83, 25);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 85, 20);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 93, 27);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 94, 25);
        validateError(negativeResult, i++, "async send action not yet supported as expression", 96, 20);
        validateError(negativeResult, i++, "incompatible types: '()' is not an iterable collection", 133, 27);
        validateError(negativeResult, i++, "incompatible types: 'other' is not an iterable collection", 151, 27);
        validateError(negativeResult, i++, "invalid usage of receive expression, var not allowed", 151, 27);
        validateError(negativeResult, i++, "incompatible types: 'other' is not an iterable collection", 168, 27);
        validateError(negativeResult, i++, "invalid usage of receive expression, var not allowed", 168, 27);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 168, 30);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 169, 28);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 170, 23);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 179, 28);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 180, 23);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 189, 28);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 191, 23);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 200, 28);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 202, 23);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 211, 28);
        validateError(negativeResult, i++, "multiple receive action not yet supported", 213, 23);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 279, 15);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 291, 18);
        validateError(negativeResult, i++, "order by not supported for complex type fields, order key should " +
                "belong to a basic type", 291, 18);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 303, 15);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 303, 15);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 315, 23);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'other'", 316, 21);
        validateError(negativeResult, i++, "action invocation as an expression not allowed here", 320, 50);
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
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
