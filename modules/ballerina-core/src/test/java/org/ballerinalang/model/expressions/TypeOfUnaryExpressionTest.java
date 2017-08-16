/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TypeOfUnaryExpressionTest {
    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/expressions/typeof-unary-expression.bal");
    }

    @Test(description = "Test reference type access expression trivial equality positive case")
    public void testRefTypeAccessExprTrivialEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestTrivialEqualityPositiveCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression trivial equality with type declared as variables")
    public void testRefTypeAccessExprTrivialEqualityCaseWithTypeDeclared() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclared", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression trivial equality with type declared " +
            "as variables with var")
    public void testRefTypeAccessExprTrivialEqualityCaseWithTypeDeclaredWithVar() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestTrivialEqualityPositiveCaseWithTypeDeclaredWithVar", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression trivial equality negative case")
    public void testRefTypeAccessExprTrivialEqualityNegativeCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestTrivialEqualityNegativeCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression trivial not equality case")
    public void testRefTypeAccessExprTrivialNotEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestTrivialNotEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test reference type access expression Any type negative case")
    public void testRefTypeAccessExprAnyTypeNegativeCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestAnyTypeNegativeCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Any type positive case")
    public void testRefTypeAccessExprAnyTypePositiveCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestAnyTypePositiveCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Map access case")
    public void testRefTypeAccessExprMapAccessCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestMapAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test reference type access expression Array access case")
    public void testRefTypeAccessExprArrayAccessCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestArrayAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Array equality case")
    public void testRefTypeAccessTestArrayEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestArrayEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Array equality positive case")
    public void testRefTypeAccessTestArrayEqualityPositiveCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestArrayEqualityPositiveCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct access case")
    public void testRefTypeAccessExprStructAccessCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestStructAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct equality case")
    public void testRefTypeAccessTestStructTypeEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestStructTypeEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct not equality case")
    public void testRefTypeAccessTestStructTypeNotEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "refTypeAccessTestStructTypeNotEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct equality negative case")
    public void testRefTypeAccessTestStructTypeNegativeEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions
                .invokeNew(bLangProgram, "refTypeAccessTestStructTypeNegativeEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct not equality negative case")
    public void testRefTypeAccessTestStructTypeNegativeNotEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestStructTypeNegativeNotEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct field equality case")
    public void testRefTypeAccessTestStructFieldTypeEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestStructFieldTypeEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression Struct field not equality case")
    public void testRefTypeAccessTestStructFieldTypeNotEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestStructFieldTypeNotEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression JSON equality case")
    public void testRefTypeAccessTestJSONEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestJSONEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression type as return value")
    public void testRefTypeAccessTestTypeAsReturnValue() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestTypeAsReturnValue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression type of multi dimensional array negative case")
    public void testRefTypeAccessTestMultiArrayNegativeCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestMultiArrayNegativeCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression type of multi dimensional array positive case")
    public void testRefTypeAccessTestMultiArrayPositiveCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestMultiArrayPositiveCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression type of different dimensional arrays case")
    public void testRefTypeAccessTestMultiArrayDifferentDimensionCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestMultiArrayDifferentDimensionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression type of different dimensional two type arrays case")
    public void testRefTypeAccessTestMultiArrayDifferentDimensionCaseTwo() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestMultiArrayDifferentDimensionCaseTwo", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test reference type access expression type of different dimensional two type arrays " +
            "check not quality case")
    public void testRefTypeAccessTestMultiArrayDifferentDimensionNotEqualityCase() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "refTypeAccessTestMultiArrayDifferentDimensionNotEqualityCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test type to any implicit cast.")
    public void testTypeToAnyImplicitCast() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "typeToAnyImplicitCast", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BTypeValue.class);
        Assert.assertEquals(returns[0], returns[1]);
    }

    @Test(description = "Test type to any explicit cast.")
    public void testTypeToAnyExplicitCast() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "typeToAnyExplicitCast", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BTypeValue.class);
        Assert.assertSame(returns[2].getClass(), BTypeValue.class);
        Assert.assertEquals(returns[0], returns[1]);
        Assert.assertEquals(returns[1], returns[2]);
    }

    @Test(description = "Test any to type explicit cast.")
    public void testAnyToTypeExplicitCast() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "anyToTypeExplicitCast", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);
        Assert.assertSame(returns[1].getClass(), BTypeValue.class);

        Assert.assertEquals(returns[0], returns[1]);

    }

    @Test(description = "Test access string value of a type")
    public void testTypeStringValue() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getTypeStringValue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);

        String typeString = returns[0].stringValue();
        String typeStringExpected = "int";
        Assert.assertEquals(typeString, typeStringExpected);
    }

    @Test(description = "Test access string value of a Struct type")
    public void getStructTypeStringValue() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getStructTypeStringValue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BTypeValue.class);

        String typeString = returns[0].stringValue();
        String typeStringExpected = "Person";
        Assert.assertEquals(typeString, typeStringExpected);
    }
}


