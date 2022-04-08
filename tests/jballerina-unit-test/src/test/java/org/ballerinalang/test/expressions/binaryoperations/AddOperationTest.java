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
                "testStringXmlSubtypesAddition"
        };
    }

    @Test(description = "Test binary statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 15);
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
    }
}
