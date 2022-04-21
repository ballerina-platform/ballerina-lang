/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.uniontypes;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.DecimalValueKind;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains union type related test cases.
 */
public class UnionTypeTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/uniontypes/union_types_basic.bal");
        negativeResult = BCompileUtil.compile("test-src/types/uniontypes/negative_union_types_basic.bal");
    }

    @Test(description = "Test basics of union types")
    public void testUnionTypeBasics1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testUnionTypeBasics1", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Double.class);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(0), 12.0, "Invalid float value returned.");
        Assert.assertEquals(returns.get(1).toString(), "sameera", "Invalid string value returned.");
    }

    @Test(description = "Test basics of union types")
    public void testUnionTypeBasics2() {
        Object returns = BRunUtil.invoke(result, "testUnionTypeBasics2", new Object[]{});

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "union types", "Invalid string value returned.");
    }

    @Test(description = "Test basics of union types")
    public void testNullableTypeBasics1() {
        Object returns = BRunUtil.invoke(result, "testNullableTypeBasics1", new Object[]{});

        Assert.assertNull(returns);
    }

    @Test(description = "Test basics of union types")
    public void testNullableTypeBasics2() {
        Object returns = BRunUtil.invoke(result, "testNullableTypeBasics2", new Object[]{});

        Assert.assertNull(returns);
    }

    @Test(description = "Test union type arrays")
    public void testUnionTypeArrays() {
        Object returns = BRunUtil.invoke(result, "testUnionTypeArrays");

        Assert.assertEquals(returns, 2L);
    }

    @Test(description = "Test union type arrays")
    public void testUnionTypeArrayWithValueTypeArrayAssignment() {
        Object returns = BRunUtil.invoke(result, "testUnionTypeArrayWithValueTypeArrayAssignment");

        Assert.assertEquals(returns, 3L);
    }

    @Test(description = "Test union type with record literal")
    public void testRecordLiteralAssignment() {
        Object returns = BRunUtil.invoke(result, "testRecordLiteralAssignment");

        Assert.assertEquals(returns.toString(), "John");
    }

    @Test(description = "Test union type with record literal")
    public void testUnionTypeWithMultipleRecordTypes() {
        Object returns = BRunUtil.invoke(result, "testUnionTypeWithMultipleRecordTypes");

        Assert.assertEquals(((BArray) returns).getString(0), "FOO");
        Assert.assertEquals(((BArray) returns).getString(1), "BAR");
    }

    @Test
    public void testUnionTypeWithMultipleRecordTypesWithLiteralKeysInLiteral() {
        BRunUtil.invoke(result, "testUnionTypeWithMultipleRecordTypesWithLiteralKeysInLiteral");
    }

    @Test(description = "Test union type LHS with float/decimal literals")
    public void testUnionLhsWithDiscriminatedFloatDecimalLiterals() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testUnionLhsWithDiscriminatedFloatDecimalLiterals");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 1.0);
        Assert.assertEquals(returns.get(1), 1.0);
        Assert.assertEquals(returns.get(2), ValueCreator.createDecimalValue("1.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test negative cases")
    public void testAmbiguousAssignment() {
        int i = 0;
        Assert.assertEquals(negativeResult.getErrorCount(), 8);
        BAssertUtil.validateError(negativeResult, i++, "ambiguous type '(ClosedBar|ClosedFoo)'", 43, 30);
        BAssertUtil.validateError(negativeResult, i++, "ambiguous type '(ClosedBar|OpenBar)'", 44, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible mapping constructor expression for type '" +
                "(ClosedFoo|Foo2)'", 47, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(int:Signed8|object { })'," +
                " found 'int'", 54, 31);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'SomeTypes', found 'int'",
                55, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '(string:Char|int)', " +
                        "found 'string'", 56, 26);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'SomeTypes2', found 'string'",
                57, 21);
        BAssertUtil.validateError(negativeResult, i, "incompatible types: expected '(boolean|null)', found 'string'",
                61, 22);
    }

    @Test(description = "Test nullable check")
    public void testNullableCheck() {
        try {
            BRunUtil.invoke(result, "testNullableTypeArrayFill");
        } catch (Throwable e) {
            Assert.fail("Nullable check in Union type failed");
        }
    }

    @Test(dataProvider = "function-name-provider")
    public void testUnionMemberTypes(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "function-name-provider")
    public Object[] unionMemberTypesTests() {
        return new String[]{
                "testUnionTypeWithFunctionPointerAccess",
                "testCastToImmutableUnion",
                "testUnionWithIntegerSubTypes",
                "testUnionWithStringSubTypes",
                "testUnionWithDecimalFiniteTypes"
        };
    }

    @Test(dataProvider = "function-name-provider-recursive")
    public void testRecursiveUnionTypes(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "function-name-provider-recursive")
    public Object[] recursiveUnionTypesTests() {
        return new String[]{
                "testRecursiveUnionTypeWithArray",
                "testRecursiveUnionTypeWithArrayAndMap",
                "testRecursiveUnionTypeWithRecord",
                "testRecursiveUnionTypeWithTuple",
                "testRecursiveUnionWithTable"
        };
    }

    @Test
    public void testParenthesisedUnionType() {
        BRunUtil.invoke(result, "testParenthesisedSingletonUnionType");
    }
    
    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
