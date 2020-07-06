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

import org.ballerinalang.core.model.util.DecimalValueKind;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeBasics1", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 12.0, "Invalid float value returned.");
        Assert.assertEquals(returns[1].stringValue(), "sameera", "Invalid string value returned.");
    }

    @Test(description = "Test basics of union types")
    public void testUnionTypeBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeBasics2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "union types", "Invalid string value returned.");
    }

    @Test(description = "Test basics of union types")
    public void testNullableTypeBasics1() {
        BValue[] returns = BRunUtil.invoke(result, "testNullableTypeBasics1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test basics of union types")
    public void testNullableTypeBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testNullableTypeBasics2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test union type arrays")
    public void testUnionTypeArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeArrays");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(description = "Test union type arrays")
    public void testUnionTypeArrayWithValueTypeArrayAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeArrayWithValueTypeArrayAssignment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test(description = "Test union type with record literal")
    public void testRecordLiteralAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordLiteralAssignment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "John");
    }

    @Test(description = "Test union type with record literal")
    public void testUnionTypeWithMultipleRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeWithMultipleRecordTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "FOO");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "BAR");
    }

    @Test
    public void testUnionTypeWithMultipleRecordTypesWithLiteralKeysInLiteral() {
        BRunUtil.invoke(result, "testUnionTypeWithMultipleRecordTypesWithLiteralKeysInLiteral");
    }

    @Test(description = "Test union type LHS with float/decimal literals")
    public void testUnionLhsWithDiscriminatedFloatDecimalLiterals() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionLhsWithDiscriminatedFloatDecimalLiterals");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.0);
        Assert.assertEquals(returns[2], new BDecimal("1.0", DecimalValueKind.OTHER));
    }

    @Test(description = "Test negative cases")
    public void testAmbiguousAssignment() {
        int i = 0;
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, i++, "ambiguous type '(ClosedBar|ClosedFoo)'", 43, 30);
        BAssertUtil.validateError(negativeResult, i++, "ambiguous type '(ClosedBar|OpenBar)'", 44, 28);
        BAssertUtil.validateError(negativeResult, i, "incompatible mapping constructor expression for type '" +
                "(ClosedFoo|Foo2)'", 47, 25);
    }

    @Test(description = "Test nullable check")
    public void testNullableCheck() {
        try {
            BRunUtil.invoke(result, "testNullableTypeArrayFill");
        } catch (Throwable e) {
            Assert.fail("Nullable check in Union type failed");
        }
    }

    @Test(description = "Test union type with a function pointer accessing")
    public void testUnionTypeWithFunctionPointerAccess() {
        BRunUtil.invoke(result, "testUnionTypeWithFunctionPointerAccess");
    }
}
