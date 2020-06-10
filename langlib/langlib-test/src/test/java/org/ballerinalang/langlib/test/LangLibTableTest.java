/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for the lang.table library.
 *
 * @since 1.0
 */
public class LangLibTableTest {

    private CompileResult compileResult, negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/tablelib_test.bal");
        negativeResult = BCompileUtil.compile("test-src/tablelib_test_negative.bal");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTableLength");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testIterator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterator");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void getKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getValueFromKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMap");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testForeach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns[0].stringValue(), "Chiran Granier ");
    }

    @Test
    public void testFilter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFilter");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testReduce() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(((BFloat) returns[0]).floatValue(), 35.5);
    }

    @Test
    public void testRemoveWithKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeWithKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void removeIfHasKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeIfHasKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testHasKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKey");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testGetKeyList() {
        BValue[] result = BRunUtil.invoke(compileResult, "testGetKeyList");
        BValueArray returns = (BValueArray) result[0];
        assertEquals(returns.size(), 4);
        assertEquals(returns.getString(0), "Chiran");
        assertEquals(returns.getString(1), "Mohan");
        assertEquals(returns.getString(2), "Gima");
        assertEquals(returns.getString(3), "Granier");
    }

    @Test
    public void testRemoveAllFromTable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeAllFromTable");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTableToArray() {
        BValue[] returns = BRunUtil.invoke(compileResult, "tableToArray");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testNextKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNextKey");
        assertEquals(((BInteger) returns[0]).intValue(), 101);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyNotFound message=cannot find key 'AAA'.*")
    public void getWithInvalidKey() {
        BRunUtil.invoke(compileResult, "getWithInvalidKey");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyNotFound message=cannot find key 'AAA'.*")
    public void removeWithInvalidKey() {
        BRunUtil.invoke(compileResult, "removeWithInvalidKey");
        Assert.fail();
    }

    @Test
    public void testCompilerNegativeCases() {
        assertEquals(negativeResult.getErrorCount(), 9);
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: expected 'table<Employee> " +
                "key(name)', found 'table<Person> key<string>'", 66, 36);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'Person'", 66, 47);
        validateError(negativeResult, index++, "incompatible types: expected " +
                        "'object { public function next () returns (record {| Employee value; |}?); }', found " +
                        "'object { public function next () returns (record {| Person value; |}?); }'",
                75, 92);
        validateError(negativeResult, index++, "incompatible types: expected 'table<(any|error)> " +
                "key<int>', found 'table<Person> key(name)'", 82, 12);
        validateError(negativeResult, index++, "incompatible types: expected 'table<(any|error)> " +
                "key<anydata>', found 'table<Person>'", 94, 12);
        validateError(negativeResult, index++, "incompatible types: expected 'table<(any|error)> " +
                "key<anydata>', found 'table<Person>'", 105, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'table<(any|error)> " +
                "key<anydata>', found 'table<Person>'", 117, 28);
        validateError(negativeResult, index++, "incompatible types: expected 'table<(any|error)> " +
                "key<anydata>', found 'table<Person>'", 126, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'table<(any|error)> " +
                "key<anydata>', found 'table<Person>'", 127, 30);
    }

    @Test
    public void testAddData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddData");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyConstraintViolation message=A value found for key '5'.*")
    public void testAddExistingMember() {
        BRunUtil.invoke(compileResult, "testAddExistingMember");
        Assert.fail();
    }

    @Test
    public void testPutData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutData");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    //todo @Chiran as per spec if the member being put is inconsistent, it should panic.
    // Here, it throws type check error
    @Test(enabled = false)
    public void testPutInconsistentData() {
        BRunUtil.invoke(compileResult, "testPutInconsistentData");
        Assert.fail();
    }

    @Test
    public void testPutWithKeyLessTbl() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutWithKeyLessTbl");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAddWithKeyLessTbl() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddWithKeyLessTbl");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError message=Table was " +
                    "mutated after the iterator was created.*")
    public void testAddNewRecordAfterIteratorCreation() {
        BRunUtil.invoke(compileResult, "testAddNewRecordAfterIteratorCreation");
    }

    @Test
    public void testRemoveAlreadyReturnedRecordFromIterator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveAlreadyReturnedRecordFromIterator");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void removeIfHasKeyReturnedRecordFromIterator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "removeIfHasKeyReturnedRecordFromIterator");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testChangeValueForAGivenKeyWhileIterating() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testChangeValueForAGivenKeyWhileIterating");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError message=Table was " +
                    "mutated after the iterator was created.*")
    public void testPutWithKeylessTableAfterIteratorCreation() {
        BRunUtil.invoke(compileResult, "testPutWithKeylessTableAfterIteratorCreation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError message=Table was " +
                    "mutated after the iterator was created.*")
    public void testAddWithKeylessTableAfterIteratorCreation() {
        BRunUtil.invoke(compileResult, "testAddWithKeylessTableAfterIteratorCreation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError message=Table was " +
                    "mutated after the iterator was created.*")
    public void testRemoveAllReturnedRecordsFromIteratorKeylessTbl() {
        BRunUtil.invoke(compileResult, "testRemoveAllReturnedRecordsFromIteratorKeylessTbl");
    }
}
