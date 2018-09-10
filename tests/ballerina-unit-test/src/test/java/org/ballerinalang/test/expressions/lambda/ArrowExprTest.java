/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.lambda;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Arrow Expression.
 *
 * @since 0.982.0
 */
public class ArrowExprTest {

    private CompileResult basic, resultNegative;

    @BeforeClass
    public void setup() {
        basic = BCompileUtil.compile("test-src/expressions/lambda/arrow-expression.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/lambda/arrow-expression-negative.bal");
    }

    @Test
    public void testArrowExprWithOneParam() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithOneParam");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 24);
    }

    @Test
    public void testArrowExprWithTwoParams() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithTwoParams");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "12John");
    }

    @Test
    public void testReturnArrowExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "testReturnArrowExpr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "10Adam");
    }

    @Test
    public void testArrowExprReturnTuple() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprReturnTuple");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "12John");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
    }

    @Test
    public void testArrowExprReturnUnion() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprReturnUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "12John");
    }

    @Test
    public void testBooleanParamType() {
        BValue[] returns = BRunUtil.invoke(basic, "testBooleanParamType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testClosureAccess() {
        BValue[] returns = BRunUtil.invoke(basic, "testClosureAccess");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 50.0);

    }

    @Test
    public void testRecordTypeWithArrowExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "testRecordTypeWithArrowExpr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).getMap().get("name").toString(), "John");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("age").toString(), "12");
    }

    @Test
    public void testNillableParameter() {
        BValue[] returns = BRunUtil.invoke(basic, "testNillableParameter");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "John");
    }

    @Test
    public void testTupleInput() {
        BValue[] returns = BRunUtil.invoke(basic, "testTupleInput");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].toString(), "Doe");
        Assert.assertEquals(returns[1].toString(), "Peter");
    }

    @Test
    public void testNegativeArrowExpr() {
        int i = 0;
        Assert.assertEquals(resultNegative.getErrorCount(), 9);
        BAssertUtil.validateError(resultNegative, i++,
                "operator '/' not defined for 'string' and 'int'", 18, 54);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found 'string'", 24, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '2' but found '1'", 29, 58);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '1' but found '2'", 31, 50);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found 'boolean'", 35, 56);
        BAssertUtil.validateError(resultNegative, i++,
                "arrow expression can only be defined with known invokable types", 39, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "arrow expression can only be defined with known invokable types", 40, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "undefined symbol 'param1'", 45, 5);
        BAssertUtil.validateError(resultNegative, i++,
                "redeclared symbol 'param1'", 50, 50);
    }
}
