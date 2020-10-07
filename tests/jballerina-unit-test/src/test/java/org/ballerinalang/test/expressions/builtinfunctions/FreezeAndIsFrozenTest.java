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
package org.ballerinalang.test.expressions.builtinfunctions;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * This class tests the freeze() and isFrozen() builtin functions.
 *
 * @since 0.985.0
 */
public class FreezeAndIsFrozenTest {

    private static final String FREEZE_ERROR_OCCURRED_ERR_MSG =
            "error occurred on freeze: 'freeze()' not allowed on 'PersonObj'";
    private static final String FREEZE_SUCCESSFUL = "freeze successful";

    private CompileResult result;
    private CompileResult semanticsNegativeResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinoperations/freeze-and-isfrozen.bal");
        semanticsNegativeResult = BCompileUtil.compile(
                "test-src/expressions/builtinoperations/freeze-and-isfrozen-semantics-negative.bal");
        negativeResult = BCompileUtil.compile(
                "test-src/expressions/builtinoperations/freeze-and-isfrozen-negative.bal");
    }

    @Test()
    public void testFreezeOnNilTypedValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFreezeOnNilTypedValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test(dataProvider = "booleanValues")
    public void testBooleanFreeze(boolean i) {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanFreeze", new BValue[]{new BBoolean(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected booleans to be readonly");
    }

    @Test(dataProvider = "intValues")
    public void testIntFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntFreeze", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected ints to be readonly");
    }

    @Test(dataProvider = "byteValues")
    public void testByteFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testByteFreeze", new BValue[]{new BByte(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected bytes to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected bytes to be readonly");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatFreeze(double i) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatFreeze", new BValue[]{new BFloat(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected floats to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected floats to be readonly");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalFreeze(BigDecimal i) {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalFreeze", new BValue[]{new BDecimal(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected decimals to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected decimals to be readonly");
    }

    @Test(dataProvider = "stringValues")
    public void testStringFreeze(String i) {
        BValue[] returns = BRunUtil.invoke(result, "testStringFreeze", new BValue[]{new BString(i)});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected strings to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected strings to be readonly");
    }

    @Test
    public void testBasicTypeNullableUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypeNullableUnionFreeze", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected values to be readonly");
    }

    @Test
    public void testBasicTypeUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypeUnionFreeze", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be the same");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected values to be readonly");
    }

    @Test
    public void testBasicTypesAsJsonFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypesAsJsonFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json values to be the same");
    }

    @Test
    public void testIsFrozenOnStructuralTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testIsFrozenOnStructuralTypes", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as not frozen");
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected values to be identified as frozen");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InvalidUpdate \\{\"message\"" +
                    ":\"modification not allowed on readonly value\".*",
            dataProvider = "frozenBasicTypeArrayModificationFunctions")
    public void testFrozenBasicTypeArrayModification(String frozenBasicTypeArrayModificationFunction) {
        BRunUtil.invoke(result, frozenBasicTypeArrayModificationFunction, new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value.*")
    public void testFrozenDecimalArrayModification() {
        BRunUtil.invoke(result, "testFrozenDecimalArrayModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InvalidUpdate \\{\"message\":\"" +
                    "modification not allowed on readonly value\"}.*")
    public void testFrozenJsonArrayModification() {
        BRunUtil.invoke(result, "testFrozenJsonArrayModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenJsonModification() {
        BRunUtil.invoke(result, "testFrozenJsonModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testAdditionToFrozenJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":\"Failed " +
                    "to remove element from map: modification not allowed on readonly value.*")
    public void testRemovalFromFrozenJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenInnerJsonModification() {
        BRunUtil.invoke(result, "testFrozenInnerJsonModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testAdditionToFrozenInnerJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenInnerJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Failed to remove element from map: modification not allowed on readonly value\".*")
    public void testRemovalFromFrozenInnerJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenInnerJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.xml\\}XMLOperationError \\{\"message\":" +
                    "\"Failed to set children to xml element: modification not allowed on readonly value\".*")
    public void testFrozenXmlSetChildren() {
        BRunUtil.invoke(result, "testFrozenXmlSetChildren", new BValue[0]);
    }


    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.xml\\}XMLOperationError \\{\"message\":" +
                    "\"Failed to set children to xml element: modification not allowed on readonly value\".*")
    public void testFrozenXmlSetChildrenDeep() {
        BRunUtil.invoke(result, "testFrozenXmlSetChildrenDeep", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":\"" +
                    "Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenMapUpdate() {
        BRunUtil.invoke(result, "testFrozenMapUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":\"" +
                    "Failed to remove element from map: modification not allowed on readonly value\".*")
    public void testFrozenMapRemoval() {
        BRunUtil.invoke(result, "testFrozenMapRemoval", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Failed to clear map: modification not allowed on readonly value\".*")
    public void testFrozenMapClear() {
        BRunUtil.invoke(result, "testFrozenMapClear", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\"" +
                    ":\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenInnerMapUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerMapUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Failed to remove element from map: modification not allowed on readonly value\".*")
    public void testFrozenInnerMapRemoval() {
        BRunUtil.invoke(result, "testFrozenInnerMapRemoval", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Failed to clear map: modification not allowed on readonly value\".*")
    public void testFrozenInnerMapClear() {
        BRunUtil.invoke(result, "testFrozenInnerMapClear", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value\".*")
    public void testFrozenAnyArrayAddition() {
        BRunUtil.invoke(result, "testFrozenAnyArrayAddition", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value\".*")
    public void testFrozenAnyArrayUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Invalid update of record field: modification not allowed on readonly value\".*")
    public void testFrozenAnyArrayElementUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayElementUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value\".*")
    public void testFrozenTupleUpdate() {
        BRunUtil.invoke(result, "testFrozenTupleUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":\"" +
                    "Invalid update of record field: modification not allowed on readonly value\".*")
    public void testFrozenRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenRecordUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}InvalidUpdate \\{\"message\":" +
                    "\"Invalid update of record field: modification not allowed on readonly value\".*")
    public void testFrozenInnerRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerRecordUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InvalidUpdate message=Failed to add " +
                    "data to the table: modification not allowed on readonly value.*", enabled = false)
    public void testFrozenTableAddition() {
        BRunUtil.invoke(result, "testFrozenTableAddition", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InvalidUpdate message=Failed to " +
                    "remove data from the table: modification not allowed on readonly value.*", enabled = false)
    public void testFrozenTableRemoval() {
        BRunUtil.invoke(result, "testFrozenTableRemoval", new BValue[0]);
    }

    @Test
    public void testSimpleUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleUnionFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected values to be identified as frozen");
    }

    @Test(description = "test a map of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexMapFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexMapFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test an array of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexArrayFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexArrayFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test a record of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexRecordFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexRecordFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test a tuple of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexTupleFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexTupleFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test a union of member type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexUnionFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexUnionFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test
    public void testValidSelfReferencingValueFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidSelfReferencingValueFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue(), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test
    public void testStructureWithErrorValueFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testStructureWithErrorValueFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFrozenValueUpdatePanicWithCheckTrap() {
        BValue[] returns = BRunUtil.invoke(result, "testFrozenValueUpdatePanicWithCheckTrap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BError.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "modification not allowed on readonly value");
    }

    @Test
    public void testXMLItemsCloneReadOnly() {
        BRunUtil.invoke(result, "testXMLItemsCloneReadOnly");
    }

    @Test
    public void testFreezeAndIsFrozenSemanticsNegativeCases() {
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), 24);
        int index = 0;
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'PersonObj', found 'anydata'", 19, 19);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 19, 19);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 19, 19);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'map<PersonObj>'", 24, 9);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'PersonObj[]'", 29, 9);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '(PersonObjTwo|PersonObj)?[]'", 32, 10);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '[(PersonObj|PersonObjTwo),PersonObjTwo]'", 39,
                9);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'Department'", 44, 9);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found 'map<(string|PersonObj)>'", 49, 32);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'map<[(string|PersonObj)," +
                        "(FreezeAllowedDepartment|float)]>'", 52, 26);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '(boolean|PersonObj|float)?[]'", 55, 39);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '(boolean|PersonObj|float)?[]'", 57, 16);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found '[(string|PersonObj),"
                + "(FreezeAllowedDepartment|float)]'", 60, 60);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found 'FreezeAllowedDepartment'", 63, 35);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found '(string|PersonObj)'", 66, 27);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'any', found 'error'", 71, 9);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found 'error'", 71, 9);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found 'map<(string|PersonObj)>'", 81, 39);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '(string|typedesc<anydata>|float)?[]'", 92, 53);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found 'FreezeAllowedDepartment2'", 100, 42);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '[int,(string|PersonObj|float),boolean]'", 106,
                      21);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'anydata', found '(int|Department|PersonObj)'", 113, 42);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'anydata', found '(anydata|error)'", 120, 19);
        validateError(semanticsNegativeResult, index,
                "incompatible types: expected 'anydata', found '(anydata|error)'", 120, 19);
    }

    @Test
    public void testFreezeAndIsFrozenNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 2);
        validateError(negativeResult, 0, "variable 'ageRec' is not initialized", 24, 10);
        validateError(negativeResult, 1, "variable 'ageRec' is not initialized", 24, 10);
    }

    @DataProvider(name = "booleanValues")
    public Object[][] booleanValues() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][]{
                {-123457},
                {0},
                {1},
                {53456032}
        };
    }

    @DataProvider(name = "byteValues")
    public Object[][] byteValues() {
        return new Object[][]{
                {0},
                {1},
                {255}
        };
    }

    @DataProvider(name = "floatValues")
    public Object[][] floatValues() {
        return new Object[][]{
                {-1234.57},
                {0.0},
                {1.1},
                {53456.032}
        };
    }

    @DataProvider(name = "decimalValues")
    public Object[][] decimalValues() {
        return new Object[][]{
                {new BigDecimal("-1234.57", MathContext.DECIMAL128)},
                {new BigDecimal("53456.032", MathContext.DECIMAL128)},
                {new BigDecimal("0.0", MathContext.DECIMAL128)},
                {new BigDecimal("1.1", MathContext.DECIMAL128)}
        };
    }

    @DataProvider(name = "stringValues")
    public Object[][] stringValues() {
        return new Object[][]{
                {"a"},
                {"Hello, from Ballerina!"}
        };
    }

    @DataProvider(name = "frozenBasicTypeArrayModificationFunctions")
    public Object[][] frozenBasicTypeArrayModificationFunctions() {
        return new Object[][]{
                {"testFrozenIntArrayModification"},
                {"testFrozenByteArrayModification"},
                {"testFrozenBooleanArrayModification"},
                {"testFrozenFloatArrayModification"},
                {"testFrozenStringArrayModification"}
        };
    }
}
