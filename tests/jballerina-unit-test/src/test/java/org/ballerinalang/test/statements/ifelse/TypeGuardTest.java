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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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

    @Test(groups = { "brokenOnNewParser" })
    public void testTypeGuardNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/statements/ifelse/type-guard-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string' will not be matched to 'int'", 20, 27);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 29, 13);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'string' will not be matched to 'int'", 33, 13);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(float|boolean)' will not be matched to 'string'", 53, 16);
        BAssertUtil.validateError(negativeResult, i++, "unreachable code", 84, 5);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(string|int)' will not be matched to 'float'", 91, 23);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(int|boolean)' will not be matched to 'float'", 99, 63);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(boolean|float)' will not be matched to 'int'", 108, 30);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: 'string' will not be matched to 'int'", 117, 25);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: 'string' will not be matched to 'float'", 117, 37);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: 'string' will not be matched to 'float'", 126, 25);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(Person|Student)' will not be matched to 'string'", 147, 10);
        BAssertUtil.validateError(negativeResult, i++,
                                  "unnecessary condition: expression will always evaluate to 'true'", 147, 25);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(Person|Student)' will not be matched to 'float'", 147, 40);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: '(Person|Student)' will not be matched to 'boolean'", 147, 56);
        BAssertUtil.validateError(negativeResult, i++,
                                  "incompatible types: 'any' will not be matched to 'error'", 157, 18);

        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(groups = { "brokenOnNewParser" })
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

        Assert.assertEquals(negativeResult.getErrorCount(), i);
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
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardInElse_7");
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

    @Test(groups = { "brokenOnJBallerina", "brokenOnNewParser" })
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
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForGlobalVarsUsingLocalAssignment");
        Assert.assertEquals(returns[0].stringValue(), "e1");
        Assert.assertEquals(returns[1].stringValue(), "e2");
    }

    @Test(dataProvider = "finiteTypeAsBroaderTypesFunctions")
    public void testFiniteTypeAsBroaderTypes(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "finiteTypeAsBroaderTypesAndFiniteTypeFunctions")
    public void testFiniteTypeAsBroaderTypesAndFiniteType(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "typeNarrowingForIntersectingUnions")
    public void testTypeNarrowingForIntersectingUnions(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "valueTypesAsFiniteTypesFunctions")
    public void testValueTypesAsFiniteTypesFunctions(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFiniteTypeAsBroaderTypeInStructureNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAsBroaderTypeInStructureNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFiniteTypeAsFiniteTypeWithIntersectionNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAsFiniteTypeWithIntersectionNegative");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFiniteTypeReassignmentToBroaderType() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeReassignmentToBroaderType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "finiteTypeAsBroaderTypesFunctions")
    public Object[][] finiteTypeAsBroaderTypesFunctions() {
        return new Object[][]{
                {"testFiniteTypeAsBroaderTypes_1"},
                {"testFiniteTypeAsBroaderTypes_2"},
                {"testFiniteTypeAsBroaderTypes_3"},
                {"testFiniteTypeAsBroaderTypes_4"}
        };
    }

    @DataProvider(name = "finiteTypeAsBroaderTypesAndFiniteTypeFunctions")
    public Object[][] finiteTypeAsBroaderTypesAndFiniteTypeFunctions() {
        return new Object[][]{
                {"testFiniteTypeAsBroaderTypesAndFiniteType_1"},
                {"testFiniteTypeAsBroaderTypesAndFiniteType_2"},
                {"testFiniteTypeAsBroaderTypesAndFiniteType_3"},
                {"testFiniteTypeAsBroaderTypesAndFiniteType_4"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_1"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_2"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_3"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_4"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_5"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_6"},
                {"testFiniteTypeInUnionAsComplexFiniteTypes_7"},
                {"testFiniteTypeAsFiniteTypeWithIntersectionPositive"},
                {"testFiniteTypeAsBroaderTypeInStructurePositive"}
        };
    }

    @DataProvider(name = "typeNarrowingForIntersectingUnions")
    public Object[][] typeNarrowingForIntersectingUnions() {
        return new Object[][]{
                {"testTypeNarrowingForIntersectingDirectUnion_1"},
                {"testTypeNarrowingForIntersectingDirectUnion_2"},
                {"testTypeNarrowingForIntersectingAssignableUnion_1"},
                {"testTypeNarrowingForIntersectingAssignableUnion_2"}
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
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeUnionAsFiniteTypeUnionPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFiniteTypeUnionAsFiniteTypeUnionNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeUnionAsFiniteTypeUnionNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForErrorPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForErrorPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForErrorNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForErrorNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForCustomErrorPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForCustomErrorPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testTypeGuardForCustomErrorNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForCustomErrorNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForTupleDestructuringAssignmentPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForTupleDestructuringAssignmentPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForTupleDestructuringAssignmentNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForTupleDestructuringAssignmentNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForRecordDestructuringAssignmentPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForRecordDestructuringAssignmentPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeGuardForRecordDestructuringAssignmentNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForRecordDestructuringAssignmentNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test(groups = { "brokenOnJBallerina", "brokenOnNewParser"})
    public void testTypeGuardForErrorDestructuringAssignmentPositive() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForErrorDestructuringAssignmentPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(groups = { "brokenOnJBallerina", "brokenOnNewParser" })
    public void testTypeGuardForErrorDestructuringAssignmentNegative() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeGuardForErrorDestructuringAssignmentNegative");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testNarrowedTypeResetWithMultipleBranches() {
        BRunUtil.invoke(result, "testNarrowedTypeResetWithMultipleBranches");
    }

    @Test
    public void testNarrowedTypeResetWithNestedTypeGuards() {
        BRunUtil.invoke(result, "testNarrowedTypeResetWithNestedTypeGuards");
    }

    @Test
    public void testSameVarNameInDifferentScopes() {
        BRunUtil.invoke(result, "testSameVarNameInDifferentScopes");
    }

    @Test(description = "Test Typetest for TypeDefs when types are equal")
    public void testTypetestForTypedefs1() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDescTypeTest1");
        Assert.assertEquals(BBoolean.TRUE, returns[0]);
    }

    @Test(description = "Test Typetest for TypeDefs when types are not equal")
    public void testTypetestForTypedefs2() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDescTypeTest2");
        Assert.assertEquals(BBoolean.TRUE, returns[0]);
    }
}
