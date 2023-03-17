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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        resultSemanticsNegative = BCompileUtil.compile(
                "test-src/expressions/lambda/arrow-expression-semantics-negative.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/lambda/arrow-expression-negative.bal");
        resultTypeNarrowNegative = BCompileUtil.compile("test-src/expressions/lambda/" +
                "arrow-expression-type-narrow-negative.bal");
    }

    @Test(description = "Test arrow expression that takes one input parameter")
    public void testArrowExprWithOneParam() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprWithOneParam");
        Assert.assertEquals(returns, 24L);
    }

    @Test(description = "Test arrow expression that takes two input parameter")
    public void testArrowExprWithTwoParams() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprWithTwoParams");
        Assert.assertEquals(returns.toString(), "12John");
    }

    @Test(description = "Test arrow expression inferring type from return signature")
    public void testReturnArrowExpr() {
        Object returns = BRunUtil.invoke(basic, "testReturnArrowExpr");
        Assert.assertEquals(returns.toString(), "10Adam");
    }

    @Test(description = "Test arrow expression that returns a tuple")
    public void testArrowExprReturnTuple() {
        Object arr = BRunUtil.invoke(basic, "testArrowExprReturnTuple");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "12John");
        Assert.assertEquals(returns.get(1), 12L);
    }

    @Test(description = "Test arrow expression that returns a union")
    public void testArrowExprReturnUnion() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprReturnUnion");
        Assert.assertEquals(returns.toString(), "12John");
    }

    @Test(description = "Test arrow expression with boolean type input")
    public void testBooleanParamType() {
        Object returns = BRunUtil.invoke(basic, "testBooleanParamType");
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test arrow expression accessing a closure variable")
    public void testClosure() {
        Object returns = BRunUtil.invoke(basic, "testClosure");
        Assert.assertEquals(returns, 35L);
    }

    @Test(description = "Test arrow expression accessing a closure variable with casting to float")
    public void testClosureWithCasting() {
        Object returns = BRunUtil.invoke(basic, "testClosureWithCasting");
        Assert.assertEquals(returns, 45.0);

    }

    @Test(description = "Test arrow expression with input parameter and return type record")
    public void testRecordTypeWithArrowExpr() {
        Object returns = BRunUtil.invoke(basic, "testRecordTypeWithArrowExpr");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("name")).toString(), "John");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("age")).toString(), "12");
    }

    @Test(description = "Test arrow expression that takes one nillable input parameter")
    public void testNillableParameter() {
        Object returns = BRunUtil.invoke(basic, "testNillableParameter");
        Assert.assertEquals(returns.toString(), "John");
    }

    @Test(description = "Test arrow expression that takes tuple as an input parameter")
    public void testTupleInput() {
        Object arr = BRunUtil.invoke(basic, "testTupleInput");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "Doe");
        Assert.assertEquals(returns.get(1).toString(), "Peter");
    }

    @Test(description = "Test arrow expression accessing a closure in an enclosing lambda")
    public void twoLevelTestWithEndingArrowExpr() {
        Object returns = BRunUtil.invoke(basic, "twoLevelTest");
        Assert.assertEquals(returns, 36L);
    }

    @Test(description = "Test arrow expression accessing a closure in enclosing lambdas upto 3 levels")
    public void threeLevelTestWithEndingArrowExpr() {
        Object returns = BRunUtil.invoke(basic, "threeLevelTest");
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test arrow expression inside an arrow expression")
    public void testNestedArrowExpression() {
        Object returns = BRunUtil.invoke(basic, "testNestedArrowExpression");
        Assert.assertEquals(returns.toString(), "John18");
    }

    @Test(description = "Test arrow expression chaining with 3 degrees")
    public void testNestedArrowExpression2() {
        Object returns = BRunUtil.invoke(basic, "testNestedArrowExpression2");
        Assert.assertEquals(returns.toString(), "DoReMe");
    }

    @Test(description = "Test arrow expression chaining with 4 degrees")
    public void testNestedArrowExpression3() {
        Object returns = BRunUtil.invoke(basic, "testNestedArrowExpression3");
        Assert.assertEquals(returns.toString(), "DoReMeFa");
    }

    @Test(description = "Test arrow expression chaining with 4 degrees including lambda")
    public void testNestedArrowExpression4() {
        Object returns = BRunUtil.invoke(basic, "testNestedArrowExpression4");
        Assert.assertEquals(returns.toString(), "DoReMeFa");
    }

    @Test(description = "Test arrow expression inside a record")
    public void testArrowExprInRecord() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprInRecord");
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test arrow expression inside an object")
    public void testArrowExprInObject() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprInObject");
        Assert.assertEquals(returns, 60L);
    }

    @Test(description = "Test arrow expression with no arguments")
    public void testArrowExprWithNoArguments() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprWithNoArguments");
        Assert.assertEquals((returns).toString(), "Some Text Global Text");
    }

    @Test(description = "Test arrow expression with no arguments and string template")
    public void testArrowExprWithNoArgumentsAndStrTemplate() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprWithNoArgumentsAndStrTemplate");
        Assert.assertEquals((returns).toString(), "Some Text Global Text");
    }

    @Test(description = "Test arrow expression with no arguments and closure var")
    public void testArrowExprWithNoArgumentsAndClosure() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprWithNoArgumentsAndClosure");
        Assert.assertEquals((returns).toString(), "Some Text Global Text Closure Text");
    }

    @Test(description = "Test arrow expression wrapped in braces")
    public void testArrowExprInBracedExpr() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprInBracedExpr");
        Assert.assertEquals((returns).toString(), "Some Text");
    }

    @Test(description = "Test arrow expression with nil return")
    public void testArrowExprWithNoReturn() {
        Object returns = BRunUtil.invoke(basic, "testArrowExprWithNoReturn");
        Assert.assertEquals(returns, 120L);
    }

    @Test(description = "Test compile time errors for arrow expression")
    public void testArrowExprSemanticsNegative() {
        int i = 0;
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
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "operator '+' not defined for 'string' and 'int'", 78, 38);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "operator '+' not defined for 'string' and 'int'", 81, 25);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "operator '+' not defined for 'int' and 'string'", 84, 25);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "operator '+' not defined for 'string' and 'int'", 87, 25);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "operator '+' not defined for 'int' and 'string'", 90, 25);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "cannot define a variable of type 'never' or equivalent to type 'never'", 92, 16);
        BAssertUtil.validateError(resultSemanticsNegative, i++,
                                  "operator '+' not defined for 'never' and 'int'", 92, 21);
//        https://github.com/ballerina-platform/ballerina-lang/issues/26191
//        BAssertUtil.validateError(resultSemanticsNegative, i++,
//                                  "cannot define a variable of type 'never' or equivalent to type 'never'", 95, 22);
//        BAssertUtil.validateError(resultSemanticsNegative, i++,
//                                  "operator '+' not defined for 'int' and 'never'", 95, 27);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i);
    }

    @Test(description = "Test compile time errors for arrow expression")
    public void testNegativeArrowExpr() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        Assert.assertEquals(resultNegative.getWarnCount(), 1);
        BAssertUtil.validateWarning(resultNegative, 0, "unused variable 'addFunc1'", 20, 5);
        BAssertUtil.validateError(resultNegative, 1, "variable 'i' is not initialized", 20, 54);
        BAssertUtil.validateError(resultNegative, 2, "variable 'm' is not initialized", 20, 58);
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

    @Test
    public void testExpressionBodiedFunctionWithBinaryExpr() {
        BRunUtil.invoke(basic, "testExpressionBodiedFunctionWithBinaryExpr");
    }

    @AfterClass
    public void tearDown() {
        basic = null;
        resultNegative = null;
        resultTypeNarrowNegative = null;
        resultSemanticsNegative = null;
    }
}
