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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Arrow Expression.
 *
 * @since 0.982.0
 */
public class ArrowExprTest {

    private CompileResult basic, resultNegative, resultTypeNarrowNegative, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        basic = BCompileUtil.compile("test-src/expressions/lambda/arrow-expression.bal");
        resultSemanticsNegative = BCompileUtil.compile("test-src/expressions/lambda/arrow-expression-semantics-" +
                "negative.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/lambda/arrow-expression-negative.bal");
        resultTypeNarrowNegative = BCompileUtil.compile("test-src/expressions/lambda/" +
                "arrow-expression-type-narrow-negative.bal");
    }

    @Test(description = "Test arrow expression that takes one input parameter")
    public void testArrowExprWithOneParam() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithOneParam");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 24);
    }

    @Test(description = "Test arrow expression that takes two input parameter")
    public void testArrowExprWithTwoParams() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithTwoParams");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "12John");
    }

    @Test(description = "Test arrow expression inferring type from return signature")
    public void testReturnArrowExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "testReturnArrowExpr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "10Adam");
    }

    @Test(description = "Test arrow expression that returns a tuple")
    public void testArrowExprReturnTuple() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprReturnTuple");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "12John");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
    }

    @Test(description = "Test arrow expression that returns a union")
    public void testArrowExprReturnUnion() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprReturnUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "12John");
    }

    @Test(description = "Test arrow expression with boolean type input")
    public void testBooleanParamType() {
        BValue[] returns = BRunUtil.invoke(basic, "testBooleanParamType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test arrow expression accessing a closure variable")
    public void testClosure() {
        BValue[] returns = BRunUtil.invoke(basic, "testClosure");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 35);
    }

    @Test(description = "Test arrow expression accessing a closure variable with casting to float")
    public void testClosureWithCasting() {
        BValue[] returns = BRunUtil.invoke(basic, "testClosureWithCasting");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 45.0);

    }

    @Test(description = "Test arrow expression with input parameter and return type record")
    public void testRecordTypeWithArrowExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "testRecordTypeWithArrowExpr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).getMap().get("name").toString(), "John");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("age").toString(), "12");
    }

    @Test(description = "Test arrow expression that takes one nillable input parameter")
    public void testNillableParameter() {
        BValue[] returns = BRunUtil.invoke(basic, "testNillableParameter");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "John");
    }

    @Test(description = "Test arrow expression that takes tuple as an input parameter")
    public void testTupleInput() {
        BValue[] returns = BRunUtil.invoke(basic, "testTupleInput");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].toString(), "Doe");
        Assert.assertEquals(returns[1].toString(), "Peter");
    }

    @Test(description = "Test arrow expression accessing a closure in an enclosing lambda")
    public void twoLevelTestWithEndingArrowExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "twoLevelTest");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 36);
    }

    @Test(description = "Test arrow expression accessing a closure in enclosing lambdas upto 3 levels")
    public void threeLevelTestWithEndingArrowExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "threeLevelTest");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }

    @Test(description = "Test arrow expression inside an arrow expression")
    public void testNestedArrowExpression() {
        BValue[] returns = BRunUtil.invoke(basic, "testNestedArrowExpression");
        Assert.assertEquals(returns[0].stringValue(), "John18");
    }

    @Test(description = "Test arrow expression chaining with 3 degrees")
    public void testNestedArrowExpression2() {
        BValue[] returns = BRunUtil.invoke(basic, "testNestedArrowExpression2");
        Assert.assertEquals(returns[0].stringValue(), "DoReMe");
    }

    @Test(description = "Test arrow expression chaining with 4 degrees")
    public void testNestedArrowExpression3() {
        BValue[] returns = BRunUtil.invoke(basic, "testNestedArrowExpression3");
        Assert.assertEquals(returns[0].stringValue(), "DoReMeFa");
    }

    @Test(description = "Test arrow expression chaining with 4 degrees including lambda")
    public void testNestedArrowExpression4() {
        BValue[] returns = BRunUtil.invoke(basic, "testNestedArrowExpression4");
        Assert.assertEquals(returns[0].stringValue(), "DoReMeFa");
    }

    @Test(description = "Test arrow expression inside a record")
    public void testArrowExprInRecord() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprInRecord");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }

    @Test(description = "Test arrow expression inside an object")
    public void testArrowExprInObject() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprInObject");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 60);
    }

    @Test(description = "Test arrow expression with no arguments")
    public void testArrowExprWithNoArguments() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithNoArguments");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "Some Text Global Text");
    }

    @Test(description = "Test arrow expression with no arguments and string template")
    public void testArrowExprWithNoArgumentsAndStrTemplate() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithNoArgumentsAndStrTemplate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "Some Text Global Text");
    }

    @Test(description = "Test arrow expression with no arguments and closure var")
    public void testArrowExprWithNoArgumentsAndClosure() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithNoArgumentsAndClosure");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "Some Text Global Text Closure Text");
    }

    @Test(description = "Test arrow expression wrapped in braces")
    public void testArrowExprInBracedExpr() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprInBracedExpr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "Some Text");
    }

    @Test(description = "Test arrow expression with nil return")
    public void testArrowExprWithNoReturn() {
        BValue[] returns = BRunUtil.invoke(basic, "testArrowExprWithNoReturn");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 120);
    }

    @Test(description = "Test compile time errors for arrow expression")
    public void testArrowExprSemanticsNegative() {
        int i = 0;
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 12);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "operator '/' not defined for 'string' and 'int'", 18, 54);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "incompatible types: expected 'boolean', found 'string'", 24, 19);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '2' but found '1'", 29, 58);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '1' but found '2'", 31, 50);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "incompatible types: expected 'int', found 'boolean'", 35, 56);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "cannot infer types of the arrow expression with unknown invokable type", 39, 19);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "cannot infer types of the arrow expression with unknown invokable type", 40, 19);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "undefined symbol 'param1'", 45, 5);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "redeclared symbol 'param1'", 50, 50);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "undefined symbol 'closureVar'", 54, 61);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "undefined symbol 'm'", 60, 58);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '0' but found '1'", 68, 40);
    }

    @Test(description = "Test compile time errors for arrow expression")
    public void testNegativeArrowExpr() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "variable 'i' is not initialized", 20, 54);
        BAssertUtil.validateError(resultNegative, 1, "variable 'm' is not initialized", 20, 58);
    }

    @Test(description = "Test compile time errors for arrow expression with type narrowing scenario")
    public void testNegativeArrowExprWithTypeNarrowing() {
        Assert.assertEquals(resultTypeNarrowNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultTypeNarrowNegative, 0,
                "operator '+' not defined for 'string' and '(string|int)'", 21, 45);
    }

    @Test(description = "Test arrow expression chaining with one parameter")
    public void testNestedArrowExpressionWithOneParameter() {
        BRunUtil.invoke(basic, "testNestedArrowExpressionWithOneParameter");
    }

    @Test(description = "Test global arrow expression with closures")
    public void testGlobalArrowExpressionsWithClosure() {
        BRunUtil.invoke(basic, "testGlobalArrowExpressionsWithClosure");
    }

    @Test(description = "Test type narrowing in arrow expression")
    public void testTypeNarrowingInArrowExpression() {
        BRunUtil.invoke(basic, "testTypeNarrowingInArrowExpression");
    }

}
