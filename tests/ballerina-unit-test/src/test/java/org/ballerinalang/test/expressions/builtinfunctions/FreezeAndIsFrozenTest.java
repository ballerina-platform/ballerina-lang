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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;

/**
 * This class tests the freeze() and isFrozen() builtin functions.
 *
 * @since 0.985.0
 */
public class FreezeAndIsFrozenTest {

    private static final String FREEZE_ERROR_OCCURRED_ERR_MSG =
            "error occurred on freeze: freeze not allowed on 'PersonObj'";
    private static final String FREEZE_SUCCESSFUL = "freeze successful";

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinfunctions/freeze-and-isfrozen.bal");
        negativeResult = BCompileUtil.compile(
                "test-src/expressions/builtinfunctions/freeze-and-isfrozen-negative.bal");
    }

    @Test(dataProvider = "booleanValues")
    public void testBooleanFreeze(boolean i) {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanFreeze", new BValue[]{new BBoolean(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be the same");
    }

    @Test(dataProvider = "intValues")
    public void testIntFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntFreeze", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be the same");
    }

    @Test(dataProvider = "byteValues")
    public void testByteFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testByteFreeze", new BValue[]{new BByte((byte) i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be the same");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatFreeze(double i) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatFreeze", new BValue[]{new BFloat(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected floats to be the same");
    }

    @Test(dataProvider = "stringValues")
    public void testStringFreeze(String i) {
        BValue[] returns = BRunUtil.invoke(result, "testStringFreeze", new BValue[]{new BString(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected strings to be the same");
    }

    @Test
    public void testBasicTypesAsJson() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypesAsJson", new BValue[0]);
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
            expectedExceptionsMessageRegExp = ".*error: modification not allowed on frozen value.*",
            dataProvider = "frozenBasicTypeArrayModificationFunctions")
    public void testFrozenBasicTypeArrayModification(String frozenBasicTypeArrayModificationFunction) {
        BRunUtil.invoke(result, frozenBasicTypeArrayModificationFunction, new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: failed to set element to json: modification " +
                    "not allowed on frozen value.*")
    public void testFrozenJsonArrayModification() {
        BRunUtil.invoke(result, "testFrozenJsonArrayModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: failed to set element to json: modification " +
                    "not allowed on frozen value.*")
    public void testFrozenJsonModification() {
        BRunUtil.invoke(result, "testFrozenJsonModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: failed to set element to json: modification " +
                    "not allowed on frozen value.*")
    public void testAdditionToFrozenJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove element from json: " +
                    "modification not allowed on frozen value.*")
    public void testRemovalFromFrozenJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: failed to set element to json: modification " +
                    "not allowed on frozen value.*")
    public void testFrozenInnerJsonModification() {
        BRunUtil.invoke(result, "testFrozenInnerJsonModification", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: failed to set element to json: modification " +
                    "not allowed on frozen value.*")
    public void testAdditionToFrozenInnerJson() {
        BRunUtil.invoke(result, "testAdditionToFrozenInnerJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove element from json: " +
                    "modification not allowed on frozen value.*")
    public void testRemovalFromFrozenInnerJson() {
        BRunUtil.invoke(result, "testRemovalFromFrozenInnerJson", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to add children to xml element: " +
                    "modification not allowed on frozen value.*")
    public void testFrozenXmlAppendChildren() {
        BRunUtil.invoke(result, "testFrozenXmlAppendChildren", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove children form xml element: " +
                    "modification not allowed on frozen value.*")
    public void testFrozenXmlRemoveChildren() {
        BRunUtil.invoke(result, "testFrozenXmlRemoveChildren", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove attribute: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenXmlRemoveAttribute() {
        BRunUtil.invoke(result, "testFrozenXmlRemoveAttribute", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to set attributes: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenXmlSetAttributes() {
        BRunUtil.invoke(result, "testFrozenXmlSetAttributes", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to set children to xml element: " +
                    "modification not allowed on frozen value.*")
    public void testFrozenXmlSetChildren() {
        BRunUtil.invoke(result, "testFrozenXmlSetChildren", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Invalid map insertion: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenMapUpdate() {
        BRunUtil.invoke(result, "testFrozenMapUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove element from map: " +
                    "modification not allowed on frozen value.*")
    public void testFrozenMapRemoval() {
        BRunUtil.invoke(result, "testFrozenMapRemoval", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to clear map: modification not allowed" +
                    " on frozen value.*")
    public void testFrozenMapClear() {
        BRunUtil.invoke(result, "testFrozenMapClear", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Invalid map insertion: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenInnerMapUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerMapUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove element from map: " +
                    "modification not allowed on frozen value.*")
    public void testFrozenInnerMapRemoval() {
        BRunUtil.invoke(result, "testFrozenInnerMapRemoval", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to clear map: modification not allowed" +
                    " on frozen value.*")
    public void testFrozenInnerMapClear() {
        BRunUtil.invoke(result, "testFrozenInnerMapClear", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to add element: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenAnyArrayAddition() {
        BRunUtil.invoke(result, "testFrozenAnyArrayAddition", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to add element: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenAnyArrayUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Invalid update of record field: modification " +
                    "not allowed on frozen value.*")
    public void testFrozenAnyArrayElementUpdate() {
        BRunUtil.invoke(result, "testFrozenAnyArrayElementUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to add element: modification not " +
                    "allowed on frozen value.*")
    public void testFrozenTupleUpdate() {
        BRunUtil.invoke(result, "testFrozenTupleUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Invalid update of record field: modification " +
                    "not allowed on frozen value.*")
    public void testFrozenRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenRecordUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Invalid update of record field: modification " +
                    "not allowed on frozen value.*")
    public void testFrozenInnerRecordUpdate() {
        BRunUtil.invoke(result, "testFrozenInnerRecordUpdate", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to add data to the table: modification" +
                    " not allowed on frozen value.*")
    public void testFrozenTableAddition() {
        BRunUtil.invoke(result, "testFrozenTableAddition", new BValue[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Failed to remove data from the table: " +
                    "modification not allowed on frozen value.*")
    public void testFrozenTableRemoval() {
        BRunUtil.invoke(result, "testFrozenTableRemoval", new BValue[0]);
    }

    @Test
    public void testInvalidMapFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testInvalidMapFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_ERROR_OCCURRED_ERR_MSG);
    }

    @Test
    public void testInvalidArrayFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testInvalidArrayFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "error occurred on freeze: freeze not allowed on 'typedesc'");
    }

    @Test
    public void testInvalidRecordFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testInvalidRecordFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_ERROR_OCCURRED_ERR_MSG);
    }

    @Test
    public void testInvalidTupleFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testInvalidTupleFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_ERROR_OCCURRED_ERR_MSG);
    }

    @Test(description = "test a map of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexMapFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexMapFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
    }

    @Test(description = "test an array of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexArrayFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexArrayFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
    }

    @Test(description = "test a record of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexRecordFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexRecordFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
    }

    @Test(description = "test a tuple of type not purely anydata, a combination of anydata and non-anydata")
    public void testValidComplexTupleFreeze() {
        BValue[] returns = BRunUtil.invoke(result, "testValidComplexTupleFreeze", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), FREEZE_SUCCESSFUL);
    }

    @Test
    public void testFreezeAndIsFrozenNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 21);
        validateError(negativeResult, 0, "'freeze' not allowed on value of type '()'", 19, 9);
        validateError(negativeResult, 1, "'freeze' not allowed on value of type 'PersonObj'", 24, 19);
        validateError(negativeResult, 2, "'freeze' not allowed on value of type 'stream<int>'", 27, 9);
        validateError(negativeResult, 3, "'freeze' not allowed on value of type 'future<boolean>'", 30, 9);
        validateError(negativeResult, 4, "'freeze' not allowed on value of type 'map<PersonObj>'", 35, 9);
        validateError(negativeResult, 5, "'freeze' not allowed on value of type 'map<stream|PersonObj>'", 38, 9);
        validateError(negativeResult, 6, "'freeze' not allowed on value of type 'PersonObj[]'", 43, 9);
        validateError(negativeResult, 7, "'freeze' not allowed on value of type 'PersonObjTwo|PersonObj[]'", 46,
                      9);
        validateError(negativeResult, 8, "'freeze' not allowed on value of type '(PersonObj|PersonObjTwo," +
                "PersonObjTwo)'", 51, 9);
        validateError(negativeResult, 9, "'freeze' not allowed on value of type 'Department'", 56, 9);
        validateError(negativeResult, 10, "'isFrozen' not allowed on value of type 'int'", 61, 17);
        validateError(negativeResult, 11, "'isFrozen' not allowed on value of type 'byte'", 64, 9);
        validateError(negativeResult, 12, "'isFrozen' not allowed on value of type 'float'", 67, 9);
        validateError(negativeResult, 13, "'isFrozen' not allowed on value of type 'string'", 70, 9);
        validateError(negativeResult, 14, "'isFrozen' not allowed on value of type 'boolean'", 73, 9);
        validateError(negativeResult, 15, "incompatible types: expected 'map<string|PersonObj>', found " +
                "'map<string|PersonObj>|error'", 78, 32);
        validateError(negativeResult, 16, "incompatible types: expected 'map<(any,any)>', found 'map<" +
                "(string|PersonObj,FreezeAllowedDepartment|float)>|error'", 81, 26);
        validateError(negativeResult, 17, "incompatible types: expected 'boolean|PersonObj|float[]', found " +
                              "'boolean|PersonObj|float[]|error'", 84, 38);
        validateError(negativeResult, 18, "incompatible types: expected 'any[]', found " +
                              "'boolean|PersonObj|float[]|error'", 86, 16);
        validateError(negativeResult, 19, "incompatible types: expected '(string|PersonObj," +
                              "FreezeAllowedDepartment|float)', found '(string|PersonObj," +
                              "FreezeAllowedDepartment|float)|error'", 89, 60);
        validateError(negativeResult, 20, "incompatible types: expected 'FreezeAllowedDepartment', found " +
                              "'FreezeAllowedDepartment|error'", 92, 35);
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
