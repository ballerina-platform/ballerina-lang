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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
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
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string' will not be matched to 'int'", 20, 27);
        BAssertUtil.validateHint(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 29, 13);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 33, 9);
        BAssertUtil.validateHint(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true' for variable of type 'never'",
                33, 13);
        BAssertUtil.validateError(negativeResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 34, 30);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 38, 5);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(float|boolean)' will not be matched to 'string'", 53, 16);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 84, 5);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(int|boolean)' will not be matched to 'float'", 90, 63);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(boolean|float)' will not be matched to 'int'", 99, 30);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string' will not be matched to 'int'", 108, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string' will not be matched to 'float'", 108, 37);
        BAssertUtil.validateWarning(negativeResult, i++, "unused variable 'y'", 109, 9);
        BAssertUtil.validateWarning(negativeResult, i++, "unused variable 's'", 111, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string' will not be matched to 'float'", 117, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(Person|Student)' will not be matched to 'string'", 138, 10);
        BAssertUtil.validateHint(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 138, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(Person|Student)' will not be matched to 'float'", 138, 40);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(Person|Student)' will not be matched to 'boolean'", 138, 56);
//        BAssertUtil.validateError(negativeResult, i++,
//                "incompatible types: '(Baz|int)' will not be matched to 'Bar'", 150, 15);
//        BAssertUtil.validateError(negativeResult, i++,
//                "incompatible types: '(Baz|int)' will not be matched to 'Qux'", 156, 15);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'record {| int i; boolean b; |}' will not be matched to 'ClosedRec'", 187, 8);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'record {| int i; boolean s; boolean...; |}' will not be matched to 'ClosedRec'",
                191, 8);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'RecordWithDefaultValue' will not be " +
                "matched to 'RecordWithNoDefaultValue'", 207, 8);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'map<(int|string)>' will not be matched to 'record {| int i; float f; |}'", 214,
                8);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'map<(int|string)>' will not be matched " +
                "to 'map<boolean>'", 221, 8);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'CyclicComplexUnion' will not" +
                " be matched to 'float'", 232, 8);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'CyclicComplexUnion' will not" +
                " be matched to 'floatUnion'", 239, 8);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'CyclicComplexUnion' will not" +
                " be matched to 'float[]'", 245, 8);

        Assert.assertEquals(negativeResult.getDiagnostics().length, i);
    }

    @Test
    public void testTypeGuardSemanticsNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/ifelse/type-guard-semantics-negative.bal");

        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 22, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 25, 20);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'b' in 'A'", 43, 16);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'c' in 'A'", 43, 31);
        BAssertUtil.validateError(negativeResult, i++, "undefined field 'a' in 'B'", 45, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found '(string|int)'",
                63, 20);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'a'", 69, 8);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'a'", 70, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 131, 22);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int|string|boolean)', found 'float'", 133, 13);
        BAssertUtil.validateError(negativeResult, i++,
                "a type compatible with mapping constructor expressions not found in " +
                        "type '(int|string|boolean)'", 137, 9);
        BAssertUtil.validateWarning(negativeResult, i++, "pattern will not be matched", 144, 9);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|string)'",
                154, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|string)'",
                167, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|boolean)'",
                181, 17);
        // TODO : Fix me : #21609
//        BAssertUtil.validateError(negativeResult, i++,
//                                  "incompatible types: expected 'string', found '(float|string|int|boolean)'", 183,
//                                  20);
        // TODO : Fix me : #21609
//        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(boolean|float)'",
//                190, 17);
//        BAssertUtil.validateError(negativeResult, i++,
//                "incompatible types: expected 'string', found '(boolean|int|string)'", 192, 20);
                BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found 'boolean'", 192, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|boolean)'",
                199, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found '(float|string)'",
                201, 20);
        // TODO : Fix me : #21609
//        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(string|boolean)'",
//                208, 17);
//        BAssertUtil.validateError(negativeResult, i++,
//                "incompatible types: expected 'string', found '(int|float|string)'", 210, 20);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'T'", 216, 30);
        // TODO : Fix me : #21609
//        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(string|boolean)'",
//                217, 17);
//        BAssertUtil.validateError(negativeResult, i++,
//                "incompatible types: expected 'string', found '(int|float|string)'", 219, 20);
//        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(Person|boolean)'",
//                238, 17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '(Person|Student)'",
                240, 20);
        BAssertUtil.validateWarning(negativeResult, i++, "pattern will not be matched", 247, 9);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 257, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found '" +
                "(string|boolean)'", 262, 24);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 274, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 293, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 296, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 298, 29);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 300, 25);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '(int|string|boolean)'", 301, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found " +
                "'(int|string|boolean)'", 303, 21);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '(int|string|boolean)'", 304, 24);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int|string)', found '(int|string|boolean)'", 307, 24);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '(int|string|boolean)'", 308, 20);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'string', found '(string|int)'", 318, 21);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'int', found '(string|int)'", 320, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'string?'",
                328, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 343, 22);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 355, 22);
        BAssertUtil.validateError(negativeResult, i++, "undefined symbol 'j'", 377, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte...; |}', found " +
                "'record {| byte i?; boolean b; |}'", 398, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte...; |}', found " +
                "'record {| byte i?; boolean b; |}'", 403, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte...; |}', found " +
                "'record {| byte i; boolean b; |}'", 407, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte...; |}', found " +
                "'record {| byte i?; boolean b; |}'", 411, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte...; |}', found " +
                "'record {| byte i?; boolean b; |}'", 416, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte...; |}', found " +
                "'record {| byte i; boolean b; |}'", 420, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'record {| byte i?;" +
                " boolean b; |}'", 429, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected " +
                        "'RecordWithReadOnlyFieldAndNonReadOnlyField', found 'record {| readonly int i; |} & " +
                        "readonly'", 452, 56);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'readonly', found 'record {| " +
                "readonly int i; string s; |}'", 456, 22);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'record {| byte i; |}', found " +
                "'record {| byte i; boolean b?; |}'", 474, 37);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<stream<int>>', found " +
                "'map<int>'", 480, 30);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean[]', found '" +
                "(string|boolean)[]'", 484, 23);
        BAssertUtil.validateError(negativeResult, i++, "invalid operation: type '(Bar & readonly)' does not support " +
                "optional field access for field 't'", 498, 17);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean', found '(record {| " +
                "string s; |}? & readonly)'", 499, 21);
        BAssertUtil.validateError(negativeResult, i++, "field access cannot be used to access an optional field of " +
                "a type that includes nil, use optional field access or member access", 500, 50);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '\"r\"|\"g\"', found " +
                "'\"r\"|\"g\"|\"b\"'", 512, 22);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '1', found '1|2'", 518, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '2|3', found '1|2'", 524, 18);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '\"r\"', found " +
                "'\"r\"|\"g\"|\"b\"'", 530, 18);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected '\"r\"|\"g\"|\"x\"', " +
                "found '\"r\"|\"g\"|\"b\"'", 536, 26);
        BAssertUtil.validateError(negativeResult, i++, "missing non-defaultable required record field 'b'", 556, 16);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'byte', found 'int'", 556, 20);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '" +
                "(int|string|boolean)'", 566, 17);
        Assert.assertEquals(negativeResult.getErrorCount(), i - 2);
        Assert.assertEquals(negativeResult.getWarnCount(), 2);
    }

    @Test
    public void testTypeTestExprTypeNarrowingNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/statements/ifelse/type_test_type_narrowing_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'SomeRecord', found 'SomeRecord?'", 49, 17);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(SomeRecord|int)', found '(SomeRecord|int)?'", 52, 21);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '()', found 'SomeRecord'", 62, 13);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(SomeRecord|int)', found '(SomeRecord|int)?'", 68, 13);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int|string)', found '(int|string)?'", 105, 24);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int|string)', found '(int|string)?'", 106, 24);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test
    public void testValueTypeInUnion() {
        Object returns = BRunUtil.invoke(result, "testValueTypeInUnion");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "int: 5");
    }

    @Test
    public void testSimpleRecordTypes_1() {
        Object returns = BRunUtil.invoke(result, "testSimpleRecordTypes_1");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "foo");
    }

    @Test
    public void testSimpleRecordTypes_2() {
        Object returns = BRunUtil.invoke(result, "testSimpleRecordTypes_2");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "foo-bar");
    }

    @Test
    public void testSimpleTernary() {
        Object returns = BRunUtil.invoke(result, "testSimpleTernary");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hello");
    }

    @Test
    public void testMultipleTypeGuardsWithAndOperator() {
        Object returns = BRunUtil.invoke(result, "testMultipleTypeGuardsWithAndOperator");

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 12L);
    }

    @Test
    public void testMultipleTypeGuardsWithAndOperatorInTernary() {
        Object returns = BRunUtil.invoke(result, "testMultipleTypeGuardsWithAndOperatorInTernary");

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 12L);
    }

    @Test
    public void testTypeGuardInElse_1() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_1");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "int: 5");
    }

    @Test
    public void testTypeGuardInElse_2() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_2");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "boolean: true");
    }

    @Test
    public void testTypeGuardInElse_3() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_3");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "x is boolean and y is boolean: false");
    }

    @Test
    public void testTypeGuardInElse_4() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_4");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "1st round: x is boolean and y is boolean: false | " +
                "2nd round: x is boolean and y is boolean: false");
    }

    @Test
    public void testTypeGuardInElse_5() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_5");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "x is int: 5");
    }

    @Test
    public void testTypeGuardInElse_6() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_6");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "int: 5");
    }

    @Test
    public void testTypeGuardInElse_7() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardInElse_7");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "int: 5");
    }

    @Test
    public void testComplexTernary_1() {
        Object returns = BRunUtil.invoke(result, "testComplexTernary_1");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "string");
    }

    @Test
    public void testComplexTernary_2() {
        Object returns = BRunUtil.invoke(result, "testComplexTernary_2");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "string");
    }

    @Test
    public void testArray() {
        Object returns = BRunUtil.invoke(result, "testArray");

        Assert.assertSame(returns.getClass(), Long.class);
        Assert.assertEquals(returns, 20L);
    }

    @Test
    public void testUpdatingGuardedVar_1() {
        Object returns = BRunUtil.invoke(result, "testUpdatingGuardedVar_1");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "BALLERINA - updated");
    }

    @Test
    public void testUpdatingGuardedVar_2() {
        Object returns = BRunUtil.invoke(result, "testUpdatingGuardedVar_2");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "BALLERINA - updated once - updated via function");
    }

    @Test
    public void testFuncPtrTypeInferenceInElseGuard() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testFuncPtrTypeInferenceInElseGuard");
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertEquals(returns.get(1), 100L);
    }

    @Test
    public void testTypeGuardNegation() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardNegation", new Object[]{(4)});
        Assert.assertEquals(returns.toString(), "int: 4");

        returns = BRunUtil.invoke(result, "testTypeGuardNegation", new Object[]{(true)});
        Assert.assertEquals(returns.toString(), "boolean: true");

        returns = BRunUtil.invoke(result, "testTypeGuardNegation", new Object[]{StringUtils.fromString("Hello")});
        Assert.assertEquals(returns.toString(), "string: Hello");
    }

    @Test
    public void testTypeGuardsWithBinaryOps() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new Object[]{(4)});
        Assert.assertEquals(returns.toString(), "int: 4");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new Object[]{(true)});
        Assert.assertEquals(returns.toString(), "boolean: true");

        returns =
                BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new Object[]{StringUtils.fromString("Hello")});
        Assert.assertEquals(returns.toString(), "string: Hello");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOps", new Object[]{(4.5)});
        Assert.assertEquals(returns.toString(), "float: 4.5");
    }

    @Test
    public void testTypeGuardsWithRecords_1() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardsWithRecords_1");
        Assert.assertEquals(returns.toString(), "John");
    }

    @Test
    public void testTypeGuardsWithRecords_2() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardsWithRecords_2");
        Assert.assertEquals(returns.toString(), "student: John");
    }

    @Test
    public void testTypeGuardsWithError() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardsWithError");
        Assert.assertEquals(returns.toString(), "status: 500");
    }

    @Test
    public void testTypeGuardsWithErrorInmatch() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardsWithErrorInmatch");
        Assert.assertEquals(returns.toString(), "some error");
    }

    @Test
    public void testTypeNarrowingWithClosures() {
        Object returns = BRunUtil.invoke(result, "testTypeNarrowingWithClosures");
        Assert.assertEquals(returns.toString(), "int: 8");
    }

    @Test
    public void testTypeGuardsWithBinaryAnd() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryAnd", new Object[]{(2)});
        Assert.assertEquals(returns.toString(), "int: 2 is < 5");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryAnd", new Object[]{(6)});
        Assert.assertEquals(returns.toString(), "int: 6 is >= 5");
    }

    @Test
    public void testTypeGuardsWithBinaryOpsInTernary() {
        Object returns =
                BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new Object[]{(4)});
        Assert.assertEquals(returns.toString(), "int: 4");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new Object[]{(true)});
        Assert.assertEquals(returns.toString(), "boolean: true");

        returns =
                BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary",
                        new Object[]{StringUtils.fromString("Hello")});
        Assert.assertEquals(returns.toString(), "string: Hello");

        returns = BRunUtil.invoke(result, "testTypeGuardsWithBinaryOpsInTernary", new Object[]{(4.5)});
        Assert.assertEquals(returns.toString(), "float: 4.5");
    }

    @Test
    public void testUpdatingTypeNarrowedVar_1() {
        Object returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedVar_1");
        Assert.assertEquals(returns.toString(), "string: hello");
    }

    @Test
    public void testUpdatingTypeNarrowedVar_3() {
        Object returns = BRunUtil.invoke(result, "testUpdatingTypeNarrowedVar_3");
        Assert.assertEquals(returns.toString(), "string: hello");
    }

    @Test
    public void testTypeGuardForGlobalVars() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTypeGuardForGlobalVarsUsingLocalAssignment");
        Assert.assertEquals(returns.get(0).toString(), "e1");
        Assert.assertEquals(returns.get(1).toString(), "e2");
    }

    @Test(dataProvider = "finiteTypeAsBroaderTypesFunctions")
    public void testFiniteTypeAsBroaderTypes(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(dataProvider = "finiteTypeAsBroaderTypesAndFiniteTypeFunctions")
    public void testFiniteTypeAsBroaderTypesAndFiniteType(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(dataProvider = "typeNarrowingForIntersectingUnions")
    public void testTypeNarrowingForIntersectingUnions(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @Test(dataProvider = "valueTypesAsFiniteTypesFunctions")
    public void testValueTypesAsFiniteTypesFunctions(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFiniteTypeAsBroaderTypeInStructureNegative() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypeAsBroaderTypeInStructureNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testFiniteTypeAsFiniteTypeWithIntersectionNegative() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypeAsFiniteTypeWithIntersectionNegative");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFiniteTypeReassignmentToBroaderType() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypeReassignmentToBroaderType");
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "finiteTypeAsBroaderTypesFunctions")
    public Object[] finiteTypeAsBroaderTypesFunctions() {
        return new String[]{
                "testFiniteTypeAsBroaderTypes_1",
                "testFiniteTypeAsBroaderTypes_2",
                "testFiniteTypeAsBroaderTypes_3",
                "testFiniteTypeAsBroaderTypes_4"
        };
    }

    @DataProvider(name = "finiteTypeAsBroaderTypesAndFiniteTypeFunctions")
    public Object[] finiteTypeAsBroaderTypesAndFiniteTypeFunctions() {
        return new String[]{
                "testFiniteTypeAsBroaderTypesAndFiniteType_1",
                "testFiniteTypeAsBroaderTypesAndFiniteType_2",
                "testFiniteTypeAsBroaderTypesAndFiniteType_3",
                "testFiniteTypeAsBroaderTypesAndFiniteType_4",
                "testFiniteTypeInUnionAsComplexFiniteTypes_1",
                "testFiniteTypeInUnionAsComplexFiniteTypes_2",
                "testFiniteTypeInUnionAsComplexFiniteTypes_3",
                "testFiniteTypeInUnionAsComplexFiniteTypes_4",
                "testFiniteTypeInUnionAsComplexFiniteTypes_5",
                "testFiniteTypeInUnionAsComplexFiniteTypes_6",
                "testFiniteTypeInUnionAsComplexFiniteTypes_7",
                "testFiniteTypeAsFiniteTypeWithIntersectionPositive",
                "testFiniteTypeAsBroaderTypeInStructurePositive"
        };
    }

    @DataProvider(name = "typeNarrowingForIntersectingUnions")
    public Object[][] typeNarrowingForIntersectingUnions() {
        return new Object[][]{
                {"testTypeNarrowingForIntersectingDirectUnion_1"},
                {"testTypeNarrowingForIntersectingDirectUnion_2"},
                {"testTypeNarrowingForIntersectingAssignableUnion_1"},
                {"testTypeNarrowingForIntersectingAssignableUnion_2"},
                {"testTypeNarrowingForIntersectingUnionWithRecords"},
                {"testTypeNarrowingForIntersectingCyclicUnion"},
                {"testTypeNarrowingForIntersectingCyclicUnionNegative"}
        };
    }

    @DataProvider(name = "valueTypesAsFiniteTypesFunctions")
    public Object[][] valueTypesAsFiniteTypesFunctions() {
        return new Object[][]{
                {"testTypeNarrowingForValueTypeAsFiniteType_1"},
                {"testTypeNarrowingForValueTypeAsFiniteType_2"}
        };
    }

    @Test
    public void testFiniteTypeUnionAsFiniteTypeUnionPositive() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypeUnionAsFiniteTypeUnionPositive");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFiniteTypeUnionAsFiniteTypeUnionNegative() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypeUnionAsFiniteTypeUnionNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testTypeGuardForErrorPositive() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForErrorPositive");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeGuardForErrorNegative() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForErrorNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testTypeGuardForCustomErrorPositive() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTypeGuardForCustomErrorPositive");
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test
    public void testTypeGuardForCustomErrorNegative() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForCustomErrorNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testTypeGuardForTupleDestructuringAssignmentPositive() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForTupleDestructuringAssignmentPositive");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeGuardForTupleDestructuringAssignmentNegative() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForTupleDestructuringAssignmentNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testTypeGuardForRecordDestructuringAssignmentPositive() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForRecordDestructuringAssignmentPositive");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeGuardForRecordDestructuringAssignmentNegative() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForRecordDestructuringAssignmentNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testTypeGuardForErrorDestructuringAssignmentPositive() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForErrorDestructuringAssignmentPositive");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeGuardForErrorDestructuringAssignmentNegative() {
        Object returns = BRunUtil.invoke(result, "testTypeGuardForErrorDestructuringAssignmentNegative");
        Assert.assertFalse((Boolean) returns);
    }

    @Test(description = "Test Typetest for TypeDefs when types are equal")
    public void testTypetestForTypedefs1() {
        Object returns = BRunUtil.invoke(result, "testTypeDescTypeTest1");
        Assert.assertEquals(returns, Boolean.TRUE);
    }

    @Test(description = "Test Typetest for TypeDefs when types are not equal")
    public void testTypetestForTypedefs2() {
        Object returns = BRunUtil.invoke(result, "testTypeDescTypeTest2");
        Assert.assertEquals(returns, Boolean.TRUE);
    }

    @Test(dataProvider = "functionNamesProvider")
    public void testInvokeFunctions(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "functionNamesProvider")
    public Object[] getFunctionNames() {
        return new String[]{"testCustomErrorType", "testIntersectionWithIntersectionType", "testJsonIntersection",
                "testMapIntersection", "testIntersectionReadOnlyness", "testClosedRecordAndMapIntersection",
                "testRecordIntersectionWithDefaultValues",
                "testRecordIntersectionWithClosedRecordAndRecordWithOptionalField2",
                "testRecordIntersectionWithClosedRecordAndRecordWithOptionalField", "testSameVarNameInDifferentScopes",
                "testNarrowedTypeResetWithNestedTypeGuards", "testNarrowedTypeResetWithMultipleBranches",
                "testIntersectionOfBuiltInSubTypeWithFiniteType", "testTypeDefinitionForNewTypeCreatedInTypeGuard"};
    }

    @Test
    public void testTypeGuardRuntimeWithAlwaysTrueHint() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/type_guard_with_always_true_hint.bal");

        Assert.assertEquals(result.getHintCount(), 2);
        BAssertUtil.validateHint(result, 0, "unnecessary condition: expression will always evaluate to 'true'", 23, 8);
        BAssertUtil.validateHint(result, 1, "unnecessary condition: expression will always evaluate to 'true'", 33, 8);
    }

    @Test
    public void testIfElseWithTypeTest() {
        BRunUtil.invoke(result, "testIfElseWithTypeTest");
    }

    @Test
    public void testIfElseWithTypeTestMultipleVariables() {
        BRunUtil.invoke(result, "testIfElseWithTypeTestMultipleVariables");
    }

    @Test
    public void testIfElseWithTypeTestMultipleVariablesInMultipleBlocks() {
        BRunUtil.invoke(result, "testIfElseWithTypeTestMultipleVariablesInMultipleBlocks");
    }

    @Test
    public void testIfElseWithTypeTestMultipleVariablesInNestedBlocks() {
        BRunUtil.invoke(result, "testIfElseWithTypeTestMultipleVariablesInNestedBlocks");
    }

    @Test
    public void testTypeTestingInReadonlyRecord() {
        BRunUtil.invoke(result, "testTypeTestingInReadonlyRecord");
    }

    @Test(description = "Test is condition with circular tuples inside a if block")
    public void testCustomCircularTupleTypeWithIsCheck() {
        BRunUtil.invoke(result, "testCustomCircularTupleTypeWithIsCheck");
    }

    @Test
    public void testSingletonTypeNarrowedTypeDesc() {
        BRunUtil.invoke(result, "testSingletonTypeNarrowedTypeDesc");
    }

    @Test
    public void testTypeGuardsAccountingForSemTypes1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_sem_types_1.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'B', found '(A|B)'", 29, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'B', found '(A|B)'", 37, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'D', found 'E'", 54, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(X|Y)', found '(W|X|Y)'", 204, 17);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[int]', found '([int]|[string])'",
                222, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[string]', " +
                "found '([int]|[string])'", 230, 22);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '([int] & readonly)'," +
                " found '[string] & readonly'", 241, 30);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[string]', found '[int]'", 253, 22);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '([int] & readonly)', found '[int]'",
                260, 30);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '([string] & readonly)', " +
                "found '[int]'", 261, 33);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[int]', " +
                "found '[string] & readonly'", 272, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[int]', found '[string] & " +
                "readonly'", 278, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[int]', found '[string] & " +
                "readonly'", 292, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string[]', " +
                "found '(int[]|string[])'", 300, 22);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(int[] & readonly)', " +
                "found 'string[] & readonly'", 313, 30);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int[]', found 'string[] & " +
                "readonly'", 323, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int[]', found 'string[] & " +
                "readonly'", 333, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string[]', " +
                "found '((int|string)[] & readonly)'", 344, 22);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(string[] & readonly)', " +
                "found '((int|string)[] & readonly)'", 345, 33);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int[]', " +
                "found '((int|string)[] & readonly)'", 354, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(int[] & readonly)', " +
                "found '((int|string)[] & readonly)'", 355, 30);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'json', found '(Z|json)'", 379, 18);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'Z', found '(Z|json)'", 385, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'record {| stream<int> s; |}', " +
                "found '(anydata|record {| stream<int> s; |})'", 393, 41);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'anydata', " +
                "found '(anydata|record {| stream<int> s; |})'", 399, 21);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(json|stream<int>)', " +
                "found '(Z|json|stream<int>)'", 425, 30);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(Z|stream<int>)', found '" +
                "(Z|json|stream<int>)'", 431, 27);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'record {| stream<int> s; |}', " +
                "found '(anydata|record {| stream<int> s; |})'", 439, 41);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(anydata|future<string>)', " +
                "found '(anydata|record {| stream<int> s; |}|future<string>)'", 445, 36);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(map<int>|xml)', found '" +
                "(Z|map<int>|xml)'", 462, 26);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'A2', found '(A|A2)'", 478, 16);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(A2|A3)', found '(A|A2|A3)'", 486,
                19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'A5', found '(A4|A5)'", 508, 16);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'xml', found '(json|xml)'", 543, 17);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'json', found '(json|xml)'", 550, 18);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'stream<string[],error?>', found '" +
                "(xml|stream<string[],error?>)'", 557, 38);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'boolean[]', found '" +
                "(int[]|boolean[])'", 567, 23);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'boolean[]', found '" +
                "(int[]|boolean[])'", 575, 23);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(boolean[]|xml)', found '" +
                "(int[]|boolean[]|xml)'", 585, 27);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found " +
                "'record {| anydata...; |}'", 602, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(byte[]|Utc)', found 'record {| " +
                "anydata...; |}'", 609, 24);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', found " +
                "'[int,decimal] & readonly'", 615, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(byte[]|Utc)', " +
                "found '([int,decimal] & readonly|record {| anydata...; |})'", 622, 24);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(Utc|record {| anydata...; |})', " +
                "found '(record {| anydata...; |}|byte[])'", 630, 27);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'byte[]', " +
                "found '[int,decimal] & readonly'", 639, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'record {| anydata...; |}', found '" +
                "([int,decimal]|record {| anydata...; |}|byte[])'", 649, 23);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[int,decimal]', found '" +
                "(byte[]|[int,decimal])'", 656, 32);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(byte[]|[int,decimal])', found " +
                "'record {| anydata...; |}'", 660, 35);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '[int,decimal]', found '([int," +
                "decimal]|byte[])'", 666, 28);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '([int,decimal]|record {| anydata.." +
                ".; |})', found '([int,decimal]|record {| anydata...; |}|byte[])'", 672, 38);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '(byte[]|record {| anydata...; |})'," +
                " found '([int,decimal]|record {| anydata...; |}|byte[])'", 678, 30);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardsAccountingForSemTypes2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_sem_types_2.bal");
        int index = 0;
        BAssertUtil.validateHint(result, index++, "unnecessary condition: expression will always evaluate to 'true'",
                30, 8);
        BAssertUtil.validateError(result, index++, "unreachable code", 33, 9);
        BAssertUtil.validateError(result, index++,
                "expression of type 'never' or equivalent to type 'never' not allowed here",
                33, 13);
        BAssertUtil.validateHint(result, index++, "unnecessary condition: expression will always evaluate to 'true'",
                38, 8);
        BAssertUtil.validateError(result, index++, "unreachable code", 41, 9);
        BAssertUtil.validateError(result, index++,
                "expression of type 'never' or equivalent to type 'never' not allowed here",
                41, 35);
        BAssertUtil.validateHint(result, index++, "unnecessary condition: expression will always evaluate to 'true'",
                69, 15);
        BAssertUtil.validateError(result, index++, "unreachable code", 72, 5);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardsAccountingForSemTypes3() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_sem_types_3.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testTypeGuardsAccountingForSemTypes4() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_sem_types_4.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'OtherExpr', found '" +
                "(VarRefExpr|OtherExpr)'", 81, 23);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardsAccountingForSemTypes5() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_sem_types_5.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testTypeGuardsAccountingForSemTypes6() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_sem_types_6.bal");
        int index = 0;
        BAssertUtil.validateHint(result, index++, "unnecessary condition: expression will always evaluate to 'true'",
                22, 15);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardTypeNarrowing1() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_type_narrow_1.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 24, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string|boolean)'",
                36, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string|boolean)?'",
                48, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string|boolean)'",
                69, 13); // issue #34307 TODO: clean all, once the issue is fixed.
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string|boolean)?'",
                81, 13); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'boolean', found 'boolean?'",
                101, 17); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected '()', found 'boolean?'",
                112, 16); // issue #30598, #33217
        BAssertUtil.validateError(result, index++, "incompatible types: expected '()', found 'boolean?'",
                115, 12); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected '()', found '(false|true)?'",
                129, 12); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|boolean|string)?'",
                141, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|boolean)'",
                152, 17);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'string?'", 160, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|boolean|string)?'",
                163, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '(int|string)?', found '(int|boolean|string)?'", 185, 21); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found '(PosInts|Zero|NegInts)?'", 205, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found 'Ints?'", 217, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '-1', found '-1|-2'",
                254, 20); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected '-3', found '(-4|-3)'", 264, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '0', found 'Ints'", 270, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string?', found '(int|string)?'",
                283, 17); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(int|string|boolean)?'", 302, 16); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected '()', found '(int|boolean|string)?'",
                336, 12); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found '((int[] & readonly)|string[])?'", 346, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string[]?', found '(int[]|string[])?'", 353, 23);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int[]|string[])?'",
                356, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'M', found '(L|M)'", 444, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'M', found '(L|N)'", 459, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'L', found '(L|N)'", 462, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'N', found '(L|N)'", 470, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'N', found '(M|N)'", 480, 11);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found '(int|string)'",
                544, 16);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found '(int|string)'",
                556, 16);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardTypeNarrowing2() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_type_narrow_2.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "incompatible types: expected '1', found 'int'", 21, 15);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '1', found 'int'", 28, 15);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found '3|\"foo\"|false'", 43, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3', found '3|\"foo\"|false'", 47, 15); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3', found '3|\"foo\"|false'", 48, 16); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3', found '3|\"foo\"|false'", 49, 18); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3.0f', found '3|\"foo\"|false'", 50, 17); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3.0f', found '3|\"foo\"|false'", 51, 18); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3.0d', found '3|\"foo\"|false'", 52, 18); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"foo\"', found '3|\"foo\"|false'", 55, 19); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"foo\"', found '3|\"foo\"|false'", 56, 21); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"foo\"', found '3|\"foo\"|false'", 57, 23); // issue #34928
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'false', found '3|\"foo\"|false'", 60, 19); // issue #34928
        BAssertUtil.validateError(result, index++, "incompatible types: expected '1', found '1?'", 80, 15);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found '(false|1|\"foo\")?'", 84, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '3', found '3?'", 130, 11); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"baz\"', found '\"foo\"|\"bar\"|\"baz\"'", 142, 15); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"foo\"', found '(\"baz\"|\"foo\"|\"bar\")?'", 154, 15);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"foo\"?', found '(\"baz\"|\"foo\"|\"bar\")?'", 155, 16); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected '\"c\"', found '\"a\"'", 166, 17);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '(\"c\"|\"b\")?', found '(\"c\"|\"a\"|\"b\")?'", 170, 18); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '()', found 'boolean?'", 181, 16); // issue #30598, #33217
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '()', found 'boolean?'", 184, 12); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '()', found '(false|true)?'", 198, 12); // issue #34307
        BAssertUtil.validateError(result, index++, "incompatible types: expected '3', found '2.0f'", 208, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected '2.3f', found '2.0f'", 214, 21);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(3.3f|1)'", 217, 21);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found '(3.3f|1|2.0f)?'", 222, 13);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardTypeNarrowing3() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_type_narrow_3.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|float)'", 19, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|float)'", 28, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|float|boolean)'", 38, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(float|boolean)'", 42, 24);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '(int|string|float)', found '(int|string|float|boolean)'",
                52, 26); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|float)'", 57, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 78, 17);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 81, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 90, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 97, 17);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 100, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 109, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 117, 17);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 120, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 131, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '(int|float)', found '(int|string|float)'", 141, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(float|int)'", 150, 13);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(string|int)'", 158, 17);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '(int|string)', found '(int|string|float)'", 162, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(string|int)'", 169, 17);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '(int|string)', found '(int|string|float)'", 173, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(int|float)'", 196, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(int|float)'", 206, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 220, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'boolean', found '(boolean|float)'", 221, 21);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'float', found '(int|float)'", 222, 19);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 234, 16);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'boolean', found '(boolean|float)'", 235, 17);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'float', found '(int|float)'", 236, 15);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 243, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 252, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found 'other'", 263, 17); // issue #34965
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 265, 20); // issue #34965
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 271, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'int'", 274, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 280, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'string', found 'int'", 284, 16);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 289, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 294, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 299, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 310, 20);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 316, 13);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(string|int)'", 323, 20);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(int|string|float)'", 349, 16); // issue #34307
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(int|string|float)'", 359, 16);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'string', found '(int|string|float)'", 369, 16);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 392, 17);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'float', found '(float|boolean)'", 395, 19);
        BAssertUtil.validateError(result, index++, "incompatible types: expected 'int', found '(int|string)'", 405, 17);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'float', found '(float|boolean)'", 408, 19);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"foo\"', found '(\"foo\"|\"bar\")'", 420, 19);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected '\"bar\"', found '(\"foo\"|\"bar\")'", 421, 19);
        BAssertUtil.validateError(result, index++,
                "incompatible types: expected 'int', found '(\"bar\"|2|\"foo\")?'", 430, 13);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testTypeGuardTypeNarrowing4() {
        CompileResult result = BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_type_narrow_4.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++,
                "expression of type 'never' or equivalent to type 'never' not allowed here", 21, 19); // issue #34965
        BAssertUtil.validateError(result, index++,
                "expression of type 'never' or equivalent to type 'never' not allowed here", 27, 19); // issue #34965
        BAssertUtil.validateError(result, index++,
                "expression of type 'never' or equivalent to type 'never' not allowed here", 33, 19); // issue #34965
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test(description = "Test type guard type narrowing with no errors")
    public void testTypeGuardTypeNarrowPositive() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/ifelse/test_type_guard_type_narrow_positive.bal");
        Object returns = BRunUtil.invoke(result, "testTypeGuardTypeNarrow");
        Assert.assertTrue((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
