/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.expressions.match;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for match expressions.
 *
 * @since 0.970.0
 */
public class MatchExpressionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/match/match-expr.bal");
    }

    @Test
    public void testMatchExpr() {
        BValue[] results = BRunUtil.invoke(compileResult, "testMatchExpr", new BValue[] { new BInteger(20) });
        Assert.assertEquals(results[0].stringValue(), "value1");

        results = BRunUtil.invoke(compileResult, "testMatchExpr", new BValue[] { new BFloat(3.4) });
        Assert.assertEquals(results[0].stringValue(), "value2");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testMatchExpr", results);
        Assert.assertEquals(results[0].stringValue(), "value3");

        results = BRunUtil.invoke(compileResult, "testMatchExpr", new BValue[] { new BString("John") });
        Assert.assertEquals(results[0].stringValue(), "value4");
    }

    @Test(enabled = false)
    public void testMatchExprWithMismatchingTypes() {
        BValue[] results = BRunUtil.invoke(compileResult, "testMatchExpr", new BValue[] { new BBoolean(true) });
        Assert.assertEquals(results[0].stringValue(), "value4");
    }

    @Test
    public void testMatchExprWithImplicitDefault() {
        BValue[] results =
                BRunUtil.invoke(compileResult, "testMatchExprWithImplicitDefault", new BValue[] { new BInteger(20) });
        Assert.assertEquals(results[0].stringValue(), "value1");

        results = BRunUtil.invoke(compileResult, "testMatchExprWithImplicitDefault", new BValue[] { new BFloat(3.4) });
        Assert.assertEquals(results[0].stringValue(), "value2");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testMatchExprWithImplicitDefault", results);
        Assert.assertEquals(results[0].stringValue(), "value3");

        results = BRunUtil.invoke(compileResult, "testMatchExprWithImplicitDefault",
                new BValue[] { new BString("John") });
        Assert.assertEquals(results[0].stringValue(), "John");
    }

    @Test
    public void testMatchExprInUnaryOperator() {
        BValue[] results =
                BRunUtil.invoke(compileResult, "testMatchExprInUnaryOperator", new BValue[] { new BInteger(20) });
        Assert.assertEquals(results[0].stringValue(), "HELLO value1");

        results = BRunUtil.invoke(compileResult, "testMatchExprInUnaryOperator", new BValue[] { new BFloat(3.4) });
        Assert.assertEquals(results[0].stringValue(), "HELLO value2");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testMatchExprInUnaryOperator", results);
        Assert.assertEquals(results[0].stringValue(), "HELLO value3");

        results = BRunUtil.invoke(compileResult, "testMatchExprInUnaryOperator", new BValue[] { new BString("John") });
        Assert.assertEquals(results[0].stringValue(), "HELLO John");
    }

    @Test
    public void testMatchExprInBinaryOperator() {
        BValue[] results =
                BRunUtil.invoke(compileResult, "testMatchExprInBinaryOperator", new BValue[] { new BInteger(20) });
        Assert.assertEquals(results[0].stringValue(), "HELLO value1");

        results = BRunUtil.invoke(compileResult, "testMatchExprInBinaryOperator", new BValue[] { new BFloat(3.4) });
        Assert.assertEquals(results[0].stringValue(), "HELLO value2");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testMatchExprInBinaryOperator", results);
        Assert.assertEquals(results[0].stringValue(), "HELLO value3");

        results =
                BRunUtil.invoke(compileResult, "testMatchExprInBinaryOperator", new BValue[] { new BString("John") });
        Assert.assertEquals(results[0].stringValue(), "HELLO John");
    }

    @Test
    public void testMatchExprInFuncCall() {
        BValue[] results =
                BRunUtil.invoke(compileResult, "testMatchExprInFuncCall", new BValue[] { new BInteger(20) });
        Assert.assertEquals(results[0].stringValue(), "value1");

        results = BRunUtil.invoke(compileResult, "testMatchExprInFuncCall", new BValue[] { new BFloat(3.4) });
        Assert.assertEquals(results[0].stringValue(), "value2");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testMatchExprInFuncCall", results);
        Assert.assertEquals(results[0].stringValue(), "value3");

        results = BRunUtil.invoke(compileResult, "testMatchExprInFuncCall", new BValue[] { new BString("John") });
        Assert.assertEquals(results[0].stringValue(), "John");
    }

    @Test
    public void testNestedMatchExpr() {
        BValue[] results = BRunUtil.invoke(compileResult, "testNestedMatchExpr", new BValue[] { new BInteger(20) });
        Assert.assertEquals(results[0].stringValue(), "value1");

        results = BRunUtil.invoke(compileResult, "testNestedMatchExpr", new BValue[] { new BFloat(3.4) });
        Assert.assertEquals(results[0].stringValue(), "value2");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testNestedMatchExpr", results);
        Assert.assertEquals(results[0].stringValue(), "value3");

        results = BRunUtil.invoke(compileResult, "testNestedMatchExpr", new BValue[] { new BString("John") });
        Assert.assertEquals(results[0].stringValue(), "value is string");

        results = BRunUtil.invoke(compileResult, "testNestedMatchExpr", new BValue[] { null });
        Assert.assertEquals(results[0].stringValue(), "value is null");
    }

    @Test
    public void testAssignabileTypesInPatterns() {
        BValue[] results =
                BRunUtil.invoke(compileResult, "testAssignabileTypesInPatterns", new BValue[] { new BInteger(20) });
        Assert.assertTrue(results[0] instanceof BJSON);
        Assert.assertEquals(results[0].stringValue(), "jsonStr1");

        results = BRunUtil.invoke(compileResult, "testAssignabileTypesInPatterns", new BValue[] { new BFloat(3.4) });
        Assert.assertTrue(results[0] instanceof BJSON);
        Assert.assertEquals(results[0].stringValue(), "jsonStr1");

        results = BRunUtil.invoke(compileResult, "getError");
        results = BRunUtil.invoke(compileResult, "testAssignabileTypesInPatterns", results);
        Assert.assertTrue(results[0] instanceof BJSON);
        Assert.assertEquals(results[0].stringValue(), "jsonStr2");

        results =
                BRunUtil.invoke(compileResult, "testAssignabileTypesInPatterns", new BValue[] { new BString("John") });
        Assert.assertTrue(results[0] instanceof BJSON);
        Assert.assertEquals(results[0].stringValue(), "jsonStr1");

        results = BRunUtil.invoke(compileResult, "testAssignabileTypesInPatterns", new BValue[] { null });
        Assert.assertNull(results[0]);
    }

    @Test
    public void testMatchExprNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/expressions/match/match-expr-negative.bal");
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'string', found 'int'", 8, 36);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'string', found 'float'", 9, 38);
        BAssertUtil.validateError(negativeResult, 2, "undefined symbol 's'", 21, 31);
        BAssertUtil.validateError(negativeResult, 3, "A matching pattern cannot be guaranteed for types '[error]'", 27,
                23);
        BAssertUtil.validateError(negativeResult, 4, "pattern will not be matched", 29, 21);
        BAssertUtil.validateError(negativeResult, 5,
                "A matching pattern cannot be guaranteed for types '[float, error]'", 37, 18);
        BAssertUtil.validateError(negativeResult, 6, "operator '+' not defined for 'json' and 'string'", 47, 31);
    }
}
