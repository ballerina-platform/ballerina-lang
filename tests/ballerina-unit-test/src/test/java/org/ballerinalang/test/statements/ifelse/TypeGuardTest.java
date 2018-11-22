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
package org.ballerinalang.test.statements.ifelse;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of type guard.
 */
public class TypeGuardTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/ifelse/type-guard.bal");
    }

    @Test
    public void testTypeGuardNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/statements/ifelse/type-guard-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 11);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string'", 22,
                17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int|string'", 25,
                20);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'b' in record 'A'", 45, 16);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'c' in record 'A'", 45, 28);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'a' in record 'B'", 47, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'int'", 56,
                27);
        BAssertUtil.validateError(negativeResult, i++, "operator '>' not defined for 'int|string' and 'int'", 61, 21);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'a'", 71, 8);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'a'", 72, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 84, 13);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'int'", 88,
                13);
    }

    @Test
    public void testValueTypeInUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "int: 5");
    }

    @Test
    public void testSimpleRecordTypes_1() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "foo");
    }

    @Test
    public void testSimpleRecordTypes_2() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "foo-bar");
    }

    @Test
    public void testSimpleTernary() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleTernary");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test
    public void testMultipleTypeGuardsWithAndOperator() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleTypeGuardsWithAndOperator");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

    @Test
    public void testMultipleTypeGuardsWithAndOperatorInTernary() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleTypeGuardsWithAndOperatorInTernary");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

    @Test
    public void testTypeGuardInElse_1() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "int: 5");
    }

    @Test
    public void testTypeGuardInElse_2() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "boolean: true");
    }

    @Test
    public void testTypeGuardInElse_3() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_3");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "x is boolean and y is boolean: false");
    }

    @Test
    public void testTypeGuardInElse_4() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_4");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "1st round: x is boolean and y is boolean: false | " +
                "2nd round: x is boolean and y is boolean: false");
    }
}
