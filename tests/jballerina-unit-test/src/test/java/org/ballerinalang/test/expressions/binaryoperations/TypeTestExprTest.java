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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of type test expressions.
 */
public class TypeTestExprTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/type-test-expr.bal");
    }

    @Test
    public void testTypeTestExprSemanticsNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/expressions/binaryoperations/type-test-expr-semantics-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "unknown type 'C'", 35, 14);
    }

    @Test
    public void testTypeTestExprNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/expressions/binaryoperations/type-test-expr-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 37);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 19, 9);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'int' will not be matched to 'float'", 28,
                9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 37, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'int' will not be matched to '(string|float)'", 46, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 55, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 64, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(int|string)' will not be matched to '(boolean|float)'", 73, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 91, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 93, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 118, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 120, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 131, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 131, 32);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'int[]' will not be matched to 'float[]'",
                132, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 133, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 134, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 135, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 141, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '[int,string]' will not be matched to '[float,boolean]'", 142, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 143, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 144, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 150, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 151, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 157, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 158, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 159, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 160, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 161, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 177, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 187, 13);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 187, 23);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 225, 8);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 229, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 241, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 246, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'foo|bar' will not be matched to 'baz|2'", 255, 9);
        BAssertUtil.validateError(negativeResult, i,
                "incompatible types: '(string|int)' will not be matched to '(float|boolean)'",
                262, 9);
    }

    @Test
    public void testValueTypeInUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test
    public void testUnionTypeInUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "numeric");
    }

    @Test
    public void testSimpleRecordTypes_1() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is A1");
    }

    @Test
    public void testSimpleRecordTypes_2() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSimpleRecordTypes_3() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_3");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testNestedRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedRecordTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSealedRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testSealedRecordTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testNestedTypeCheck() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedTypeCheck");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "boolean");
        Assert.assertEquals(returns[1].stringValue(), "int");
        Assert.assertEquals(returns[2].stringValue(), "string");
    }

    @Test
    public void testTypeInAny() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeInAny");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string value: This is working");
    }

    @Test
    public void testNilType() {
        BValue[] returns = BRunUtil.invoke(result, "testNilType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "nil");
    }

    @Test
    public void testTypeChecksWithLogicalAnd() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeChecksWithLogicalAnd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string and boolean");
    }

    @Test
    public void testTypeCheckInTernary() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeCheckInTernary");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "An integer");
    }

    @Test
    public void testJSONTypeCheck() {
        BValue[] returns = BRunUtil.invoke(result, "testJSONTypeCheck");
        Assert.assertEquals(returns.length, 7);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertSame(returns[5].getClass(), BString.class);
        Assert.assertSame(returns[6].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json int");
        Assert.assertEquals(returns[1].stringValue(), "json float");
        Assert.assertEquals(returns[2].stringValue(), "json string: hello");
        Assert.assertEquals(returns[3].stringValue(), "json boolean");
        Assert.assertEquals(returns[4].stringValue(), "json array");
        Assert.assertEquals(returns[5].stringValue(), "json object");
        Assert.assertEquals(returns[6].stringValue(), "json null");
    }

    @Test
    public void testRecordsWithFunctionType_1() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordsWithFunctionType_1");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is not a man");
        Assert.assertEquals(returns[1].stringValue(), "Human: Piyal");
    }

    @Test
    public void testRecordsWithFunctionType_2() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordsWithFunctionType_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Man: Piyal");
        Assert.assertEquals(returns[1].stringValue(), "Human: Piyal");
    }

    @Test
    public void testObjectWithUnorderedFields() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithUnorderedFields");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "I am a person in order: John");
        Assert.assertEquals(returns[1].stringValue(), "I am a person not in order: John");
        Assert.assertEquals(returns[2].stringValue(), "I am a person in order: Doe");
        Assert.assertEquals(returns[3].stringValue(), "I am a person not in order: Doe");
    }

    @Test
    public void testPublicObjectEquivalency() {
        BValue[] returns = BRunUtil.invoke(result, "testPublicObjectEquivalency");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "values: 5, foo");
        Assert.assertEquals(returns[1].stringValue(), "values: 5, foo, 6.7");
        Assert.assertEquals(returns[2].stringValue(), "n/a");
    }

    @Test
    public void testPrivateObjectEquivalency() {
        BValue[] returns = BRunUtil.invoke(result, "testPrivateObjectEquivalency");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "values: 5, foo");
        Assert.assertEquals(returns[1].stringValue(), "values: 5, foo, 6.7");
        Assert.assertEquals(returns[2].stringValue(), "n/a");
    }

    @Test
    public void testAnonymousObjectEquivalency() {
        BValue[] returns = BRunUtil.invoke(result, "testAnonymousObjectEquivalency");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "values: 5, foo, 6.7");
        Assert.assertEquals(returns[1].stringValue(), "values: 5, foo, 6.7, true");
        Assert.assertEquals(returns[2].stringValue(), "n/a");
    }

    @Test
    public void testObjectWithSameMembersButDifferentAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithSameMembersButDifferentAlias");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "I am same as person: John");
        Assert.assertEquals(returns[1].stringValue(), "I am a person: John");
        Assert.assertEquals(returns[2].stringValue(), "I am same as person: Doe");
        Assert.assertEquals(returns[3].stringValue(), "I am a person: Doe");
    }

    @Test
    public void testObjectIsCheckWithCycles() {
        BRunUtil.invoke(result, "testObjectIsCheckWithCycles");
    }

    @Test
    public void testSimpleArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleArrays");
        Assert.assertEquals(returns.length, 5);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testRestType() {
        BRunUtil.invoke(result, "testRestType");
    }

    @Test
    public void testUnionType() {
        BRunUtil.invoke(result, "testUnionType");
    }

    @Test
    public void testInferredArrayType() {
        BRunUtil.invoke(result, "testInferredArrayType");
    }

    @Test
    public void testClosedArrayType() {
        BRunUtil.invoke(result, "testClosedArrayType");
    }

    @Test
    public void testEmptyArrayType() {
        BRunUtil.invoke(result, "testEmptyArrayType");
    }

    @Test
    public void testRecordArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordArrays");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testSimpleTuples() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleTuples");
        Assert.assertEquals(returns.length, 5);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testTupleWithAssignableTypes_1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleWithAssignableTypes_1");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testTupleWithAssignableTypes_2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleWithAssignableTypes_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleUnconstrainedMap_1() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleUnconstrainedMap_1");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSimpleUnconstrainedMap_2() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleUnconstrainedMap_2");
        Assert.assertEquals(returns.length, 5);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testSimpleConstrainedMap() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleConstrainedMap");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testJsonArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonArrays");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testFiniteType() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testFiniteTypeInTuple() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeInTuple");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testFiniteTypeInTuplePoisoning() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeInTuplePoisoning");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "on");
        Assert.assertEquals(returns[1].stringValue(), "on");
    }

    @Test
    public void testFiniteType_1() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteType_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is a fruit");
    }

    @Test
    public void testFiniteType_2() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteType_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is an Apple");
    }

    @Test
    public void testFiniteTypeAsBroaderType_1() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAsBroaderType_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFiniteTypeAsBroaderType_2() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAsBroaderType_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testUnionWithFiniteTypeAsFiniteTypeTrue() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionWithFiniteTypeAsFiniteTypeTrue");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testUnionWithFiniteTypeAsFiniteTypeFalse() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionWithFiniteTypeAsFiniteTypeFalse");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testFiniteTypeAsFiniteTypeTrue() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAsFiniteTypeTrue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFiniteTypeAsFiniteTypeFalse() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAsFiniteTypeFalse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntersectingUnionTrue() {
        BValue[] returns = BRunUtil.invoke(result, "testIntersectingUnionTrue");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testIntersectingUnionFalse() {
        BValue[] returns = BRunUtil.invoke(result, "testIntersectingUnionFalse");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testValueTypeAsFiniteTypeTrue() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeAsFiniteTypeTrue");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testValueTypeAsFiniteTypeFalse() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeAsFiniteTypeFalse");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test(groups = { "brokenOnNewParser" })
    public void testError_1() {
        BValue[] returns = BRunUtil.invoke(result, "testError_1");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testError_2() {
        BValue[] returns = BRunUtil.invoke(result, "testError_2");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testClosedArrayAsOpenArray() {
        BValue[] returns = BRunUtil.invoke(result, "testClosedArrayAsOpenArray");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFunctions1() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctions1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testFunctions2() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctions2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testFuture() {
        BValue[] returns = BRunUtil.invoke(result, "testFutureTrue");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        returns = BRunUtil.invoke(result, "testFutureFalse");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testMapAsRecord() {
        BRunUtil.invoke(result, "testMapAsRecord");
    }
}
