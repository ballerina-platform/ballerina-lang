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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
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
        Assert.assertEquals(negativeResult.getErrorCount(), 51);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 22, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 25, 20);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'b' in record 'A'", 45, 16);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'c' in record 'A'", 45, 28);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'a' in record 'B'", 47, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'int'", 56,
                27);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string|int'", 65,
                20);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'a'", 71, 8);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'a'", 72, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 84, 13);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'int'", 88,
                13);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'float|boolean' will not be matched to 'string'", 108, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 133, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'int|string|boolean', found 'float'", 135, 13);
        BAssertUtil.validateError(negativeResult, i++, "invalid literal for type 'int|string|boolean'", 139, 9);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 139, 5);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string|int' will not be matched to 'float'", 146, 23);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string'", 156,
                17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string'", 169,
                21);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'int|boolean' will not be matched to 'float'", 182, 63);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|boolean'", 183,
                17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'float|string|int|boolean'", 185, 20);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'boolean|float' will not be matched to 'int'", 191, 30);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'boolean|float'", 192,
                17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'boolean|int|string'", 194, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|boolean'", 201,
                17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'float|string'",
                203, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'int'", 209,
                25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'float'",
                209, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string|boolean'",
                210, 17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'int|float|string'", 212, 20);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'T'", 218, 30);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'string' will not be matched to 'float'",
                218, 35);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string|boolean'",
                219, 17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'int|float|string'", 221, 20);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'Person|Student' will not be matched to 'string'", 239, 10);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'Person' will not be matched to 'float'",
                239, 40);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'Person|boolean'",
                240, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'any' will not be matched to 'error'", 249,
                18);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                259, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string|boolean'",
                264, 24);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                276, 17);

        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                295, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                298, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                300, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                302, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'int|string|boolean'", 303, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int|string|boolean'",
                305, 21);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'int|string|boolean'", 306, 24);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'int|string', found 'int|string|boolean'", 309, 24);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'int|string|boolean'", 310, 20);
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

    @Test
    public void testTypeGuardInElse_5() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_5");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "x is int: 5");
    }

    @Test
    public void testTypeGuardInElse_6() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_6");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "int: 5");
    }

    @Test
    public void testTypeGuardInElse_7() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_6");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "int: 5");
    }

    @Test
    public void testComplexTernary_1() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTernary_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test
    public void testComplexTernary_2() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTernary_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test
    public void testArray() {
        BValue[] returns = BRunUtil.invoke(result, "testArray");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
    }

    @Test
    public void testUpdatingGuardedVar_1() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdatingGuardedVar_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "BALLERINA - updated");
    }

    @Test
    public void testUpdatingGuardedVar_2() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdatingGuardedVar_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "BALLERINA - updated once - updated via function");
    }

    @Test
    public void testFuncPtrTypeInferenceInElseGuard() {
        BValue[] returns = BRunUtil.invoke(result, "testFuncPtrTypeInferenceInElseGuard");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 100);
    }

    @Test
    public void testTypeGuardNegation() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardNegation", new BValue[] { new BInteger(4) });
        Assert.assertEquals(returns[0].stringValue(), "int: 4");

        returns = BRunUtil.invoke(result, "testTypeGuardNegation", new BValue[] { new BBoolean(true) });
        Assert.assertEquals(returns[0].stringValue(), "boolean: true");

        returns = BRunUtil.invoke(result, "testTypeGuardNegation", new BValue[] { new BString("Hello") });
        Assert.assertEquals(returns[0].stringValue(), "string: Hello");
    }

    @Test
    public void testTypeGuardsWithBinaryOps() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new BValue[] { new BInteger(4) });
        Assert.assertEquals(returns[0].stringValue(), "int: 4");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new BValue[] { new BBoolean(true) });
        Assert.assertEquals(returns[0].stringValue(), "boolean: true");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new BValue[] { new BString("Hello") });
        Assert.assertEquals(returns[0].stringValue(), "string: Hello");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new BValue[] { new BFloat(4.5) });
        Assert.assertEquals(returns[0].stringValue(), "float: 4.5");
    }

    @Test
    public void testTypeGuardsWithRecords_1() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardsWithRecords_1");
        Assert.assertEquals(returns[0].stringValue(), "John");
    }

    @Test
    public void testTypeGuardsWithRecords_2() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardsWithRecords_2");
        Assert.assertEquals(returns[0].stringValue(), "student: John");
    }

    @Test
    public void testTypeGuardsWithError() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardsWithError");
        Assert.assertEquals(returns[0].stringValue(), "status: 500");
    }

    @Test
    public void testTypeGuardsWithErrorInmatch() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardsWithErrorInmatch");
        Assert.assertEquals(returns[0].stringValue(), "some error");
    }

    @Test
    public void testTypeNarrowingWithClosures() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeNarrowingWithClosures");
        Assert.assertEquals(returns[0].stringValue(), "int: 8");
    }

    @Test
    public void testTypeGuardsWithBinaryAnd() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryAnd", new BValue[] { new BInteger(2) });
        Assert.assertEquals(returns[0].stringValue(), "int: 2 is < 5");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryAnd", new BValue[] { new BInteger(6) });
        Assert.assertEquals(returns[0].stringValue(), "int: 6 is >= 5");
    }

    @Test
    public void testTypeGuardsWithBinaryOpsInTernary() {
        BValue[] returns =
                BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new BValue[] { new BInteger(4) });
        Assert.assertEquals(returns[0].stringValue(), "int: 4");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new BValue[] { new BBoolean(true) });
        Assert.assertEquals(returns[0].stringValue(), "boolean: true");

        returns =
                BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new BValue[] { new BString("Hello") });
        Assert.assertEquals(returns[0].stringValue(), "string: Hello");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new BValue[] { new BFloat(4.5) });
        Assert.assertEquals(returns[0].stringValue(), "float: 4.5");
    }

    @Test
    public void testUpdatingTypeNarrowedVar_1() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedVar_1");
        Assert.assertEquals(returns[0].stringValue(), "string: hello");
    }

    @Test
    public void testUpdatingTypeNarrowedVar_2() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedVar_2", new BValue[] { new BInteger(2) });
        Assert.assertEquals(returns[0].stringValue(), "int: 2");

        returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedVar_2", new BValue[] { new BInteger(8) });
        Assert.assertEquals(returns[0].stringValue(), "int: -1");
    }

    @Test
    public void testUpdatingTypeNarrowedVar_3() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedVar_3");
        Assert.assertEquals(returns[0].stringValue(), "string: hello");
    }

    @Test
    public void testTypeGuardForGlobalVars() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForGlobalVars");
        Assert.assertEquals(returns[0].stringValue(), "e1");
        Assert.assertEquals(returns[1].stringValue(), "e2");
    }

    @Test
    public void testUpdatingTypeNarrowedGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedGlobalVar");
        Assert.assertEquals(returns[0].stringValue(), "string: hello");
    }
}
