/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.binaryoperations;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import io.ballerina.runtime.internal.values.XmlValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of add operation.
 */
public class AddOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/add-operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/add-operation-negative.bal");
    }

    @Test(description = "Test two int add expression")
    public void testIntAddExpr() {
        Object[] args = { (2147483647), (2147483646)};

        Object returns = BRunUtil.invoke(result, "intAdd", args);
        Assert.assertTrue(returns instanceof Long);
        long actual = (long) returns;
        long expected = 4294967293L;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int add overflow expression", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}NumberOverflow \\{\"message\":\"int range " +
                    "overflow\"\\}.*")
    public void testIntOverflowByAddition() {
        BRunUtil.invoke(result, "overflowByAddition");
    }

    @Test(description = "Test two float add expression")
    public void testFloatAddExpr() {
        Object[] args = { (100.0f), (200.0f)};

        Object returns = BRunUtil.invoke(result, "floatAdd", args);
        Assert.assertTrue(returns instanceof Double);
        double actual = (double) returns;
        double expected = 300.0f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two string add expression")
    public void testStringAddExpr() {
        Object[] args = { StringUtils.fromString("WSO2"), StringUtils.fromString(" Inc.")};
        Object returns = BRunUtil.invoke(result, "stringAdd", args);

        Assert.assertTrue(returns instanceof BString);

        String actual = returns.toString();
        String expected = "WSO2 Inc.";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test adding negative values")
    public void testNegativeValues() {
        int a = -10;
        int b = -20;

        long expectedResult = a + b;

        Object[] args = {(a), (b)};

        Object returns = BRunUtil.invoke(result, "intAdd", args);
        Assert.assertTrue(returns instanceof Long);
        long actualResult = (long) returns;
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test string and int add expression")
    public void testStringAndIntAddExpr() {
        String a = "test";
        int b = 10;

        String expectedResult = a + b;

        Object[] args = {StringUtils.fromString(a), (b)};

        Object returns = BRunUtil.invoke(result, "stringAndIntAdd", args);
        Assert.assertTrue(returns instanceof BString);
        String actualResult = returns.toString();
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test xml xml add expression")
    public void testXmlXmlAddExpr() {
        Object returns = BRunUtil.invoke(result, "xmlXmlAdd");
        Assert.assertEquals(((XmlValue) returns).size(), 1);
        Assert.assertEquals(returns.toString(), "abcdef");
    }

    @Test(description = "Test xml string add expression")
    public void testXmlStringAddExpr() {
        Object returns = BRunUtil.invoke(result, "xmlStringAdd");
        Assert.assertEquals(((XmlValue) returns).size(), 1);
        Assert.assertEquals(returns.toString(), "abcdef");
    }

    @Test(description = "Test string xml add expression")
    public void testStringXmlAddExpr() {
        Object returns = BRunUtil.invoke(result, "stringXmlAdd");
        Assert.assertEquals(((XmlValue) returns).size(), 1);
        Assert.assertEquals(returns.toString(), "defabc");
    }

    @Test(dataProvider = "dataToTestAdditionWithTypes", description = "Test addition with types")
    public void testAdditionWithTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @Test(description = "Test contextually expected type of numeric literals in addition")
    public void testContextuallyExpectedTypeOfNumericLiteralInAdd() {
        BRunUtil.invoke(result, "testContextuallyExpectedTypeOfNumericLiteralInAdd");
    }

    @Test(description = "Test addition of nullable values")
    public void testAddNullable() {
        BRunUtil.invoke(result, "testAddNullable");
    }

    @DataProvider
    public Object[] dataToTestAdditionWithTypes() {
        return new Object[]{
                "testAdditionWithTypes",
                "testAddSingleton",
                "testStringCharAddition",
                "testStringXmlSubtypesAddition",
                "testStringSubtypesAddition",
                "testXmlSubtypesAddition",
                "testNullableIntAddition"
        };
    }

    @Test(dataProvider = "dataToTestShortCircuitingInAddition")
    public void testShortCircuitingInAddition(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestShortCircuitingInAddition() {
        return new Object[]{
                "testNoShortCircuitingInAdditionWithNullable",
                "testNoShortCircuitingInAdditionWithNonNullable"
        };
    }

    @Test(description = "Test binary statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 35);
        BAssertUtil.validateError(resultNegative, 0, "operator '+' not defined for 'json' and 'json'", 8, 10);
        BAssertUtil.validateError(resultNegative, 1, "operator '+' not defined for 'int' and 'string'", 14, 9);
        BAssertUtil.validateError(resultNegative, 2, "operator '+' not defined for 'C' and 'string'", 28, 14);
        BAssertUtil.validateError(resultNegative, 3, "operator '+' not defined for 'C' and '(float|int)'", 29, 14);
        BAssertUtil.validateError(resultNegative, 4, "operator '+' not defined for 'C' and 'xml'", 30, 14);
        BAssertUtil.validateError(resultNegative, 5, "operator '+' not defined for 'D' and 'int'", 47, 14);
        BAssertUtil.validateError(resultNegative, 6, "operator '+' not defined for 'F' and 'int'", 48, 14);
        BAssertUtil.validateError(resultNegative, 7, "operator '+' not defined for 'G' and 'int'", 49, 14);
        BAssertUtil.validateError(resultNegative, 8, "operator '+' not defined for 'float' and 'decimal'", 56, 14);
        BAssertUtil.validateError(resultNegative, 9, "operator '+' not defined for 'float' and 'decimal'", 57, 14);
        BAssertUtil.validateError(resultNegative, 10, "operator '+' not defined for 'float' and 'int'", 58, 14);
        BAssertUtil.validateError(resultNegative, 11, "operator '+' not defined for 'decimal' and 'int'", 59, 14);
        BAssertUtil.validateError(resultNegative, 12, "operator '+' not defined for 'int' and 'float'", 60, 18);
        BAssertUtil.validateError(resultNegative, 13, "operator '+' not defined for 'C' and 'float'", 64, 14);
        BAssertUtil.validateError(resultNegative, 14, "operator '+' not defined for 'C' and 'float'", 65, 14);
        BAssertUtil.validateError(resultNegative, 15, "incompatible types: expected 'string', found 'xml'", 72, 16);
        BAssertUtil.validateError(resultNegative, 16, "incompatible types: expected 'FO', found 'string'", 73, 12);
        BAssertUtil.validateError(resultNegative, 17, "operator '+' not defined for '(string:Char|xml)' and " +
                "'(string:Char|xml)'", 85, 13);
        BAssertUtil.validateError(resultNegative, 18, "operator '+' not defined for '(string:Char|xml)' and " +
                "'(xml:Element|Strings)'", 86, 13);
        BAssertUtil.validateError(resultNegative, 19, "operator '+' not defined for '(string:Char|xml)' and " +
                "'(xml<(xml:Comment|xml:Text)>|string)'", 87, 13);
        BAssertUtil.validateError(resultNegative, 20, "operator '+' not defined for '(xml:Element|Strings)' and " +
                "'(xml:Element|Strings)'", 88, 13);
        BAssertUtil.validateError(resultNegative, 21, "operator '+' not defined for '(xml:Element|Strings)' and " +
                "'(xml<(xml:Comment|xml:Text)>|string)'", 89, 13);
        BAssertUtil.validateError(resultNegative, 22, "operator '+' not defined for " +
                "'(xml<(xml:Comment|xml:Text)>|string)' and '(xml<(xml:Comment|xml:Text)>|string)'", 90, 13);
        BAssertUtil.validateError(resultNegative, 23, "operator '+' not defined for '(int|float)'" +
                " and '(int|float)'", 94, 13);
        BAssertUtil.validateError(resultNegative, 24, "operator '+' not defined for '(int|float)'" +
                " and '(float|int)'", 95, 13);
        BAssertUtil.validateError(resultNegative, 25, "operator '+' not defined for '(int|float)'" +
                " and '(byte|float)'", 96, 13);
        BAssertUtil.validateError(resultNegative, 26, "operator '+' not defined for '(float|int)'" +
                " and '(float|int)'", 97, 13);
        BAssertUtil.validateError(resultNegative, 27, "operator '+' not defined for '(float|int)'" +
                " and '(byte|float)'", 98, 13);
        BAssertUtil.validateError(resultNegative, 28, "operator '+' not defined for '(byte|float)'" +
                " and '(byte|float)'", 99, 13);
        BAssertUtil.validateError(resultNegative, 29, "operator '+' not defined for '(decimal|float)'" +
                " and '(decimal|float)'", 103, 17);
        BAssertUtil.validateError(resultNegative, 30, "operator '+' not defined for '(decimal|float)'" +
                " and '(decimal|float)'", 104, 17);
        BAssertUtil.validateError(resultNegative, 31, "operator '+' not defined for '(decimal|float)'" +
                " and '(decimal|float)'", 105, 17);
        BAssertUtil.validateError(resultNegative, 32, "operator '+' not defined for '(int|float)'" +
                " and '(int|float)'", 111, 13);
        BAssertUtil.validateError(resultNegative, 33, "operator '+' not defined for '(int|float)'" +
                " and '(ints|float)'", 112, 13);
        BAssertUtil.validateError(resultNegative, 34, "operator '+' not defined for '(ints|float)'" +
                " and '(ints|float)'", 113, 13);
    }
}
