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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestAssignmentCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in function invocation statement.")
    public void testArrayLengthAccessExprFunctionInvocationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestFunctionInvocationCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Variable definition statement.")
    public void testArrayLengthAccessExprVariableDefinitionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestVariableDefinitionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Array initialization statement.")
    public void testArrayLengthAccessExprArrayInitializationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestArrayInitializerCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Map initialization statement.")
    public void testArrayLengthAccessExprMapInitializationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestMapInitializerCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Return statement.")
    public void testArrayLengthAccessExprReturnExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestReturnStatementCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in multi Return statement.")
    public void testArrayLengthAccessExprMultiReturnExpressionCase() {
        Object[] args = {(100), (5)};
        Object arr = BRunUtil.invoke(result, "arrayLengthAccessTestMultiReturnStatementCase", args);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertSame(returns.get(0).getClass(), Long.class);

        long actualFirst = (long) returns.get(0);
        long actualSecond = (long) returns.get(1);
        long actualThird = (long) returns.get(2);

        long expectedFirst = 3;
        long expectedSecond = 1;
        long expectedThird = 2;

        Assert.assertEquals(actualFirst, expectedFirst);
        Assert.assertEquals(actualSecond, expectedSecond);
        Assert.assertEquals(actualThird, expectedThird);
    }

    @Test(description = "Test length of array when present in Type cast expression.")
    public void testArrayLengthAccessExprTypeCastExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestTypeCastExpressionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in If condition.")
    public void testArrayLengthAccessExprIfConditionExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestIfConditionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Binary expression.")
    public void testArrayLengthAccessExpBinaryExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestBinaryExpressionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when present in Struct field access expression.")
    public void testArrayLengthAccessExpStructFieldAccessCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestStructFieldAccessCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when reference point to JSON array.")
    public void testArrayLengthAccessJSONArrayCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(result, "arrayLengthAccessTestJSONArrayCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 6;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of map")
    public void lengthOfMap() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfMap", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of empty map")
    public void lengthOfEmptyMap() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfEmptyMap", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of single xml element")
    public void lengthOfSingleXmlElement() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfSingleXmlElement", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of multiple xml elements")
    public void lengthOfMultipleXmlElements() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfMultipleXmlElements", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of tuple")
    public void lengthOfTuple() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfTuple", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of record")
    public void lengthOfRecord() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfRecord", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of record without any fields")
    public void lengthOfEmptyRecord() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "lengthOfEmptyRecord", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 0;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test length of array when it is null.")
    public void testArrayLengthOfNull() {
        Object[] args = new Object[0];
        BRunUtil.invoke(result, "accessLengthOfNullArray", args);
    }

    @Test(description = "Test length of map when reference point to null map.")
    public void testMapLengthOfNull() {
        Object[] args = new Object[0];
        BRunUtil.invoke(result, "accessLengthOfNullMap", args);
    }

    @Test(description = "Test length of tuple when it is null.")
    public void testTupleLengthOfNull() {
        Object[] args = new Object[0];
        BRunUtil.invoke(result, "accessLengthOfNullTuple", args);
    }

    @Test(description = "Test length of xml when it is null.")
    public void testXMLLengthOfNull() {
        Object[] args = new Object[0];
        BRunUtil.invoke(result, "accessLengthOfNullXML", args);
    }

    @Test(description = "Test length of string.")
    public void testLengthOfString() {
        Object returns = BRunUtil.invoke(result, "stringLengthAccessTestCase");
        Assert.assertEquals(returns, 6L);
    }

    // Negative test cases that fails at compilation
    @Test(description = "Test invoking length operation on an object")
    public void testNegativeTests() {
        Assert.assertEquals(resNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resNegative, 0, "incompatible types: expected 'string', found 'int'", 31, 21);
        BAssertUtil.validateError(resNegative, 1, "undefined method 'length' in object 'Person'", 36, 21);
    }

}
