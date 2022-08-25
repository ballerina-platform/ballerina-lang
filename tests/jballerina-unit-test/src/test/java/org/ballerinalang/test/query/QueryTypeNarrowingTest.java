/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This contains methods to test query expressions within type guards.
 *
 * @since 2.0.0
 */
public class QueryTypeNarrowingTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-expr-type-narrowing.bal");
    }

    @Test(description = "Test query expression within a type guard")
    public void testQueryExprWithinTypeGuard() {
        BRunUtil.invoke(result, "testQueryExprWithinTypeGuard");
    }

    @Test(description = "Test query expression within a negated type guard")
    public void testQueryExprWithinNegatedTypeGuard() {
        BRunUtil.invoke(result, "testQueryExprWithinNegatedTypeGuard");
    }

    @Test(description = "Test query expression within a ternary operator")
    public void testTernaryWithinQueryExpression() {
        BRunUtil.invoke(result, "testTernaryWithinQueryExpression");
    }

    @Test(description = "Test type narrowing with where in queries")
    public void testTypeNarrowingWithinQueries() {
        BRunUtil.invoke(result, "testTypeNarrowing");
    }

    @Test
    public void testNegativeTypeNarrowing() {
        CompileResult compileResult = BCompileUtil.compile("test-src/query/query-expr-type-narrowing-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 15);
        int i = 0;
        validateError(compileResult, i++, "incompatible types: expected 'B', found 'AorB'",
                118, 20);
        validateError(compileResult, i++, "incompatible types: expected 'B', found 'AorB'",
                123, 17);
        validateError(compileResult, i++, "incompatible types: expected 'E', found 'DorE'",
                210, 20);
        validateError(compileResult, i++, "incompatible types: expected 'E', found 'DorE'",
                221, 17);
        validateError(compileResult, i++, "incompatible types: expected 'F', found 'DorF'",
                230, 20);
        validateError(compileResult, i++, "incompatible types: expected 'F', found '(E|F)'",
                238, 20);
        validateError(compileResult, i++, "incompatible types: expected 'F', found 'EorF'",
                242, 20);
        validateError(compileResult, i++, "incompatible types: expected '(Y|Z)', found '(W|Y|Z)'",
                370, 21);
        validateError(compileResult, i++, "incompatible types: expected '(Y|Z)', found '(W|Y|Z)'",
                375, 18);
        validateError(compileResult, i++, "incompatible types: expected 'R', found '(Q|R)'",
                415, 20);
        validateError(compileResult, i++, "incompatible types: expected 'R', found '(Q|R)'",
                420, 17);
        validateError(compileResult, i++, "incompatible types: expected '(R|T)', found '(Q|R|T)'",
                429, 21);
        validateError(compileResult, i++, "incompatible types: expected '1', found '(1|2)'",
                448, 19);
        validateError(compileResult, i++, "incompatible types: expected '()', found '1'",
                468, 26);
        validateError(compileResult, i, "incompatible types: expected '2', found '1'",
                480, 21);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
