/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.builtinoperations;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of the builtin length() operation.
 *
 * @version 0.983
 */
public class LengthOperationTest {

    private CompileResult result;
    private CompileResult resNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinoperations/length-operation.bal");
        resNegative = BCompileUtil.compile("test-src/expressions/builtinoperations/length-operation-negative.bal");
    }

    @Test(description = "Test length of array when present in an assignment statement.")
    public void testArrayLengthAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestAssignmentCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in function invocation statement.")
    public void testArrayLengthAccessExprFunctionInvocationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestFunctionInvocationCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Variable definition statement.")
    public void testArrayLengthAccessExprVariableDefinitionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestVariableDefinitionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Array initialization statement.")
    public void testArrayLengthAccessExprArrayInitializationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestArrayInitializerCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Map initialization statement.")
    public void testArrayLengthAccessExprMapInitializationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestMapInitializerCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Return statement.")
    public void testArrayLengthAccessExprReturnExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestReturnStatementCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in multi Return statement.")
    public void testArrayLengthAccessExprMultiReturnExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestMultiReturnStatementCase", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actualFirst = (int) ((BInteger) returns[0]).intValue();
        int actualSecond = (int) ((BInteger) returns[1]).intValue();
        int actualThird = (int) ((BInteger) returns[2]).intValue();

        int expectedFirst = 3;
        int expectedSecond = 1;
        int expectedThird = 2;

        Assert.assertEquals(actualFirst, expectedFirst);
        Assert.assertEquals(actualSecond, expectedSecond);
        Assert.assertEquals(actualThird, expectedThird);
    }

    @Test(description = "Test length of array when present in Type cast expression.")
    public void testArrayLengthAccessExprTypeCastExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestTypeCastExpressionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in If condition.")
    public void testArrayLengthAccessExprIfConditionExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestIfConditionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Binary expression.")
    public void testArrayLengthAccessExpBinaryExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestBinaryExpressionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Struct field access expression.")
    public void testArrayLengthAccessExpStructFieldAccessCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestStructFieldAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when reference point to JSON array.")
    public void testArrayLengthAccessJSONArrayCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(result, "arrayLengthAccessTestJSONArrayCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 6;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of map")
    public void lengthOfMap() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfMap", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of empty map")
    public void lengthOfEmptyMap() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfEmptyMap", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of single xml element")
    public void lengthOfSingleXmlElement() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfSingleXmlElement", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of multiple xml elements")
    public void lengthOfMultipleXmlElements() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfMultipleXmlElements", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of tuple")
    public void lengthOfTuple() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfTuple", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of record")
    public void lengthOfRecord() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfRecord", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of record without any fields")
    public void lengthOfEmptyRecord() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "lengthOfEmptyRecord", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when it is null.")
    public void testArrayLengthOfNull() {
        BValue[] args = new BValue[0];
        BRunUtil.invoke(result, "accessLengthOfNullArray", args);
    }

    @Test(description = "Test length of map when reference point to null map.")
    public void testMapLengthOfNull() {
        BValue[] args = new BValue[0];
        BRunUtil.invoke(result, "accessLengthOfNullMap", args);
    }

    @Test(description = "Test length of tuple when it is null.")
    public void testTupleLengthOfNull() {
        BValue[] args = new BValue[0];
        BRunUtil.invoke(result, "accessLengthOfNullTuple", args);
    }

    @Test(description = "Test length of xml when it is null.")
    public void testXMLLengthOfNull() {
        BValue[] args = new BValue[0];
        BRunUtil.invoke(result, "accessLengthOfNullXML", args);
    }

    @Test(description = "Test length of string.")
    public void testLengthOfString() {
        BValue[] returns = BRunUtil.invoke(result, "stringLengthAccessTestCase");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);
    }

    // Negative test cases that fails at compilation
    @Test(description = "Test invoking length operation on an object")
    public void testNegativeTests() {
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0, "incompatible types: expected 'string', found 'int'", 31, 21);
        BAssertUtil.validateError(resNegative, 1, "undefined method 'length' in object 'Person'", 36, 21);
    }

}
