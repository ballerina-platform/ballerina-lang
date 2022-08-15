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
        int i = 0;
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'B'",
                118, 22);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'B'",
                123, 19);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'E'",
                210, 22);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'E'",
                221, 19);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'F'",
                230, 22);
        validateError(compileResult, i++, "incompatible types: expected 'F', found '(E|F)'",
                238, 20);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'F'",
                242, 22);
        validateError(compileResult, i++, "incompatible types: expected 'int', found '(Y|Z)'",
                375, 19);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'R'",
                412, 22);
        validateError(compileResult, i++, "incompatible types: expected 'int', found 'R'",
                417, 19);
        validateError(compileResult, i++, "incompatible types: expected 'R', found '(R|T)'",
                426, 20);
        validateError(compileResult, i++, "incompatible types: expected '1', found '(1|2)'",
                445, 19);
        validateError(compileResult, i++, "incompatible types: expected '()', found '1'",
                465, 26);
        validateError(compileResult, i++, "incompatible types: expected '2', found '1'",
                477, 21);
        Assert.assertEquals(compileResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
