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

package org.ballerinalang.test.expressions.builtinoperations;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * This class tests the freeze() and isFrozen() builtin functions.
 *
 * @since 0.985.0
 */
public class FreezeAndIsFrozenTest {

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
        Object returns = BRunUtil.invoke(result, "testFreezeOnNilTypedValue");
        Assert.assertNull(returns);
    }

    @Test(dataProvider = "booleanValues")
    public void testBooleanFreeze(boolean i) {
        Object arr = BRunUtil.invoke(result, "testBooleanFreeze", new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected booleans to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected booleans to be readonly");
    }

    @Test(dataProvider = "intValues")
    public void testIntFreeze(int i) {
        Object arr = BRunUtil.invoke(result, "testIntFreeze", new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected ints to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected ints to be readonly");
    }

    @Test(dataProvider = "byteValues")
    public void testByteFreeze(int i) {
        Object arr = BRunUtil.invoke(result, "testByteFreeze", new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected bytes to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected bytes to be readonly");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatFreeze(double i) {
        Object arr = BRunUtil.invoke(result, "testFloatFreeze", new Object[]{(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected floats to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected floats to be readonly");
    }

    @Test(dataProvider = "decimalValues")
    public void testDecimalFreeze(BigDecimal i) {
        Object arr = BRunUtil.invoke(result, "testDecimalFreeze", new Object[]{
                ValueCreator.createDecimalValue(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected decimals to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected decimals to be readonly");
    }

    @Test(dataProvider = "stringValues")
    public void testStringFreeze(String i) {
        Object arr = BRunUtil.invoke(result, "testStringFreeze", new Object[]{StringUtils.fromString(i)});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected strings to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected strings to be readonly");
    }

    @Test
    public void testRecordWithEnumFreeze() {
        BRunUtil.invoke(result, "testRecordWithEnumFreeze");
    }

    @Test
    public void testBasicTypeNullableUnionFreeze() {
        Object arr = BRunUtil.invoke(result, "testBasicTypeNullableUnionFreeze", new Object[]{});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected values to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected values to be readonly");
    }

    @Test
    public void testBasicTypeUnionFreeze() {
        Object arr = BRunUtil.invoke(result, "testBasicTypeUnionFreeze", new Object[]{});
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(0), "Expected values to be the same");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected values to be readonly");
    }

    @Test
    public void testBasicTypesAsJsonFreeze() {
        Object returns = BRunUtil.invoke(result, "testBasicTypesAsJsonFreeze", new Object[0]);
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Expected json values to be the same");
    }

    @Test
    public void testIsFrozenOnStructuralTypes() {
        Object arr = BRunUtil.invoke(result, "testIsFrozenOnStructuralTypes", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Boolean);
        Assert.assertFalse((Boolean) returns.get(0), "Expected values to be identified as not frozen");
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected values to be identified as frozen");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InvalidUpdate \\{\"message\"" +
                    ":\"modification not allowed on readonly value\".*",
            dataProvider = "frozenBasicTypeArrayModificationFunctions")
    public void testFrozenBasicTypeArrayModification(String frozenBasicTypeArrayModificationFunction) {
        BRunUtil.invoke(result, frozenBasicTypeArrayModificationFunction, new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value.*")
    public void testFrozenDecimalArrayModification() {
        BRunUtil.invoke(result, "testFrozenDecimalArrayModification", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InvalidUpdate \\{\"message\":\"" +
                    "modification not allowed on readonly value\"}.*")
    public void testFrozenJsonArrayModification() {
        BRunUtil.invoke(result, "testFrozenJsonArrayModification", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenJsonModification() {
        BRunUtil.invoke(result, "testFrozenJsonModification", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testAdditionToFrozenJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenJson", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":\"failed " +
                    "to remove element from map: modification not allowed on readonly value.*")
    public void testRemovalFromFrozenJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenJson", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenInnerJsonModification() {
        BRunUtil.invoke(result, "testFrozenInnerJsonModification", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testAdditionToFrozenInnerJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenInnerJson", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"failed to remove element from map: modification not allowed on readonly value\".*")
    public void testRemovalFromFrozenInnerJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenInnerJson", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.xml}XMLOperationError \\{\"message\":" +
                    "\"Failed to set children to xml element: modification not allowed on readonly value\".*")
    public void testFrozenXmlSetChildren() {
        BRunUtil.invoke(result, "testFrozenXmlSetChildren", new Object[0]);
    }


    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.xml}XMLOperationError \\{\"message\":" +
            "\"Failed to set children to xml element: modification not allowed on readonly value\".*")
    public void testFrozenXmlSetChildrenDeep() {
        BRunUtil.invoke(result, "testFrozenXmlSetChildrenDeep", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":\"" +
                    "Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenMapUpdate() {
        BRunUtil.invoke(result, "testFrozenMapUpdate", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":\"" +
                    "failed to remove element from map: modification not allowed on readonly value\".*")
    public void testFrozenMapRemoval() {
        BRunUtil.invoke(result, "testFrozenMapRemoval", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"Failed to clear map: modification not allowed on readonly value\".*")
    public void testFrozenMapClear() {
        BRunUtil.invoke(result, "testFrozenMapClear", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\"" +
                    ":\"Invalid map insertion: modification not allowed on readonly value\".*")
    public void testFrozenInnerMapUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerMapUpdate", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"failed to remove element from map: modification not allowed on readonly value\".*")
    public void testFrozenInnerMapRemoval() {
        BRunUtil.invoke(result, "testFrozenInnerMapRemoval", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InvalidUpdate \\{\"message\":" +
                    "\"Failed to clear map: modification not allowed on readonly value\".*")
    public void testFrozenInnerMapClear() {
        BRunUtil.invoke(result, "testFrozenInnerMapClear", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value\".*")
    public void testFrozenAnyArrayAddition() {
        BRunUtil.invoke(result, "testFrozenAnyArrayAddition", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value\".*")
    public void testFrozenAnyArrayUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayUpdate", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'name' in record of type '\\(Employee & readonly\\)'\".*")
    public void
    testFrozenAnyArrayElementUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayElementUpdate", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array}InvalidUpdate \\{\"message\":" +
                    "\"modification not allowed on readonly value\".*")
    public void testFrozenTupleUpdate() {
        BRunUtil.invoke(result, "testFrozenTupleUpdate", new Object[0]);
    }

    @Test
    public void testFrozenRecursiveTupleUpdate() {
        BRunUtil.invoke(result, "testFrozenRecursiveTupleUpdate");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'name' in record of type '\\(DeptEmployee & readonly\\)'\".*")
    public void testFrozenRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenRecordUpdate", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map}InherentTypeViolation \\{\"message\":" +
                    "\"cannot update 'readonly' field 'code' in record of type '\\(Dept & readonly\\)'\".*")
    public void testFrozenInnerRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerRecordUpdate", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table}InvalidUpdate " +
                    "\\{\"message\":\"modification not allowed on readonly value\"}.*")
    public void testFrozenTableAddition() {
        BRunUtil.invoke(result, "testFrozenTableAddition", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table}InvalidUpdate " +
                    "\\{\"message\":\"modification not allowed on readonly value\"}.*")
    public void testFrozenTableRemoval() {
        BRunUtil.invoke(result, "testFrozenTableRemoval", new Object[0]);
    }

    @Test
    public void testSimpleUnionFreeze() {
        Object returns = BRunUtil.invoke(result, "testSimpleUnionFreeze", new Object[0]);
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns, "Expected values to be identified as frozen");
    }

    @Test(description = "test a map of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexMapFreeze() {
        Object arr = BRunUtil.invoke(result, "testValidComplexMapFreeze", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), FREEZE_SUCCESSFUL);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test
    public void testRecursiveTupleFreeze() {
        BRunUtil.invoke(result, "testRecursiveTupleFreeze");
    }

    @Test(description = "test an array of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexArrayFreeze() {
        Object arr = BRunUtil.invoke(result, "testValidComplexArrayFreeze", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), FREEZE_SUCCESSFUL);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test a record of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexRecordFreeze() {
        Object arr = BRunUtil.invoke(result, "testValidComplexRecordFreeze", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), FREEZE_SUCCESSFUL);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test a tuple of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexTupleFreeze() {
        Object arr = BRunUtil.invoke(result, "testValidComplexTupleFreeze", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), FREEZE_SUCCESSFUL);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test(description = "test a union of member type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexUnionFreeze() {
        Object arr = BRunUtil.invoke(result, "testValidComplexUnionFreeze", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), FREEZE_SUCCESSFUL);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test
    public void testValidSelfReferencingValueFreeze() {
        Object arr = BRunUtil.invoke(result, "testValidSelfReferencingValueFreeze", new Object[0]);
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), FREEZE_SUCCESSFUL);
        Assert.assertTrue(returns.get(1) instanceof Boolean);
        Assert.assertTrue((Boolean) returns.get(1), "Expected value to be readonly since no error " +
                "was encountered");
    }

    @Test
    public void testStructureWithErrorValueFreeze() {
        Object returns = BRunUtil.invoke(result, "testStructureWithErrorValueFreeze", new Object[0]);
        Assert.assertTrue(returns instanceof Boolean);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFrozenValueUpdatePanicWithCheckTrap() {
        Object returns = BRunUtil.invoke(result, "testFrozenValueUpdatePanicWithCheckTrap", new Object[0]);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(((BMap<String, BString>) ((BError) returns).getDetails()).get(StringUtils.fromString(
                "message")).toString(), "modification not allowed on readonly value");
    }

    @Test
    public void testXMLItemsCloneReadOnly() {
        BRunUtil.invoke(result, "testXMLItemsCloneReadOnly");
    }

    @Test
    public void testFreezeAndIsFrozenSemanticsNegativeCases() {
        Assert.assertEquals(semanticsNegativeResult.getErrorCount(), 20);
        int index = 0;
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'PersonObj'", 19, 39);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'map<PersonObj>'", 24, 9);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'PersonObj[]'", 29, 9);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '" +
                              "(PersonObjTwo|PersonObj)?[]'", 32, 10);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '[" +
                              "(PersonObj|PersonObjTwo),PersonObjTwo]'",
                39, 9);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'Department'", 44, 9);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'map<" +
                              "(string|PersonObj)>'", 49, 32);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'map<[(string|PersonObj)," +
                        "(FreezeAllowedDepartment|float)]>'", 52, 26);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '" +
                              "(boolean|PersonObj|float)?[]'", 55, 39);
        validateError(semanticsNegativeResult, index++,
                      "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '" +
                              "(boolean|PersonObj|float)?[]'", 57, 16);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '[(string|PersonObj),"
                + "(FreezeAllowedDepartment|float)]'", 60, 60);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found " +
                        "'FreezeAllowedDepartment'", 63, 35);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '(string|PersonObj)'", 66
                , 27);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'any', found 'error'", 71, 9);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found 'map<(string|PersonObj)" +
                        ">'", 81, 39);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '" +
                        "(string|stream<int>|float)?[]'", 92, 47);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found " +
                        "'FreezeAllowedDepartment2'", 100, 42);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '[int," +
                        "(string|PersonObj|float),boolean]'",
                106, 21);
        validateError(semanticsNegativeResult, index++,
                "incompatible types: expected 'ballerina/lang.value:0.0.0:Cloneable', found '" +
                        "(int|Department|PersonObj)'", 113, 42);
        validateError(semanticsNegativeResult, index,
                "incompatible types: expected 'anydata', found '((anydata & readonly)|error)'", 120, 19);
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

    @Test
    public void testDeprecatedWarningForIsReadOnly() {
        CompileResult result = BCompileUtil.compile(
                "test-src/expressions/builtinoperations/is_readonly_deprecated_warning.bal");
        int index = 0;
        validateWarning(result, index++, 
                "usage of construct 'ballerina/lang.value:0.0.0:isReadOnly' is deprecated", 22, 9);
        validateWarning(result, index++, 
                "usage of construct 'ballerina/lang.value:0.0.0:isReadOnly' is deprecated", 24, 17);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }
}
