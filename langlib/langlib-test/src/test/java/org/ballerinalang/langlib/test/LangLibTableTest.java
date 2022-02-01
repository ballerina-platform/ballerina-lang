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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
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
        BRunUtil.invoke(compileResult, "testHasKey");
    }

    @Test
    public void testHashCollisionHandlingScenarios() {
        BRunUtil.invoke(compileResult, "testHashCollisionHandlingScenarios");
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

        BValue[] unionReturns = BRunUtil.invoke(compileResult, "testGetKeysFromUnionConstrained");
        BValueArray unionResult = (BValueArray) unionReturns[0];
        assertEquals(unionResult.size(), 2);
        assertEquals(unionResult.getString(0), "Adam");
        assertEquals(unionResult.getString(1), "Mark");
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
                    "error: \\{ballerina/lang.table\\}KeyNotFound \\{\"message\":\"cannot find key 'AAA'\"\\}.*")
    public void getWithInvalidKey() {
        BRunUtil.invoke(compileResult, "getWithInvalidKey");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyNotFound \\{\"message\":\"cannot find key 'AAA'\"\\}.*")
    public void removeWithInvalidKey() {
        BRunUtil.invoke(compileResult, "removeWithInvalidKey");
        Assert.fail();
    }

    @Test
    public void testCompilerNegativeCases() {
        assertEquals(negativeResult.getErrorCount(), 25);
        int index = 0;
        validateError(negativeResult, index++, "incompatible types: expected 'EmployeeTable', " +
                "found 'table<Person> key<string>'", 68, 36);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'Person'", 68, 47);
        validateError(negativeResult, index++, "incompatible types: expected " +
                        "'object { public isolated function next () returns (record {| Employee value; |}?); }', " +
                        "found " +
                        "'object { public isolated function next () returns (record {| Person value; |}?); }'",
                77, 92);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> key<int>', found 'PersonalTable'", 84, 12);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<map<(any|error)>> key<ballerina/lang.table:0.0.0:KeyType>', found 'PersonalKeyLessTable'",
                      96, 12);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> " +
                "key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 107, 21);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> " +
                "key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 119, 28);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> " +
                "key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 128, 30);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> " +
                "key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 129, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 139, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 148, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 157, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 166, 21);
        validateError(negativeResult, index++, "cannot update 'table<Person> key(name)' with member " +
                        "access expression", 177, 5);
        validateError(negativeResult, index++, "invalid constraint type. expected subtype of 'map<any|error>' " +
                "but found 'int'", 181, 11);
        validateError(negativeResult, index++, "a type compatible with mapping constructor expressions not found in " +
                "type 'int'", 181, 38);
        validateError(negativeResult, index++, "missing ellipsis token", 181, 38);
        validateError(negativeResult, index++, "missing open brace token", 181, 38);
        validateError(negativeResult, index++, "missing close brace token", 181, 39);
        validateError(negativeResult, index++, "incompatible types: expected " +
                "'table<ballerina/lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', " +
                "found 'table<int> key(age)'", 182, 9);
        validateError(negativeResult, index++, "incompatible types: expected '[]', found 'int'", 182, 20);
        validateError(negativeResult, index++, "table with constraint of type map cannot have key specifier " +
                "or key type constraint", 188, 30);
        validateError(negativeResult, index++, "incompatible types: expected 'function (ballerina/lang.table:0.0" +
                ".0:MapType) returns (ballerina/lang.table:0.0.0:MapType1)', found 'function (Person) returns " +
                "(string)'", 195, 27);
        validateError(negativeResult, index++, "incompatible types: expected 'function (ballerina/lang.table:0.0" +
                ".0:MapType) returns (ballerina/lang.table:0.0.0:MapType1)', found 'function (Person) returns " +
                "(string)'", 199, 18);
        validateError(negativeResult, index, "incompatible types: expected 'function (ballerina/lang.table:0.0" +
                ".0:MapType) returns (ballerina/lang.table:0.0.0:MapType1)', found 'function (Person) returns " +
                "(string)'", 200, 18);
    }

    @Test
    public void testAddData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddData");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyConstraintViolation \\{\"message\":\"a value found for key " +
                            "'5'\"\\}.*")
    public void testAddExistingMember() {
        BRunUtil.invoke(compileResult, "testAddExistingMember");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type 'table<Engineer> "
                    + "key\\(name\\)'\"\\}.*")
    public void testAddInconsistentData() {
        BRunUtil.invoke(compileResult, "testAddInconsistentData");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type 'table<Engineer>"
                    + " key\\(name\\)'\"\\}.*")
    public void testAddInconsistentData2() {
        BRunUtil.invoke(compileResult, "testAddInconsistentData2");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testAddInconsistentDataWithMapConstrTbl() {
        BRunUtil.invoke(compileResult, "testAddInconsistentDataWithMapConstrTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'map<string>' inconsistent with the inherent table type " +
                    "'table<Teacher>'\"\\}.*")
    public void testAddInconsistentDataWithMapConstrTbl2() {
        BRunUtil.invoke(compileResult, "testAddInconsistentDataWithMapConstrTbl2");
        Assert.fail();
    }

    @Test
    public void testAddValidData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddValidData");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAddValidData2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddValidData2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAddValidDataWithMapConstrTbl() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddValidDataWithMapConstrTbl");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testPutData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutData");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type 'table<Engineer> "
                    + "key\\(name\\)'\"\\}.*")
    public void testPutInconsistentData() {
        BRunUtil.invoke(compileResult, "testPutInconsistentData");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type 'table<Engineer>"
                    + " key\\(name\\)'\"\\}.*")
    public void testPutInconsistentData2() {
        BRunUtil.invoke(compileResult, "testPutInconsistentData2");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testPutInconsistentDataWithMapConstrTbl() {
        BRunUtil.invoke(compileResult, "testPutInconsistentDataWithMapConstrTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'map<string>' inconsistent with the inherent table type " +
                    "'table<Teacher>'\"\\}.*")
    public void testPutInconsistentDataWithMapConstrTbl2() {
        BRunUtil.invoke(compileResult, "testPutInconsistentDataWithMapConstrTbl2");
        Assert.fail();
    }

    @Test
    public void testPutValidData() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutValidData");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testPutValidData2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutValidData2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testPutValidDataWithMapConstrTbl() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutValidDataWithMapConstrTbl");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
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

    @Test
    public void testRemoveThenIterate() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveThenIterate");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testPutWithKeylessTableAfterIteratorCreation() {
        BRunUtil.invoke(compileResult, "testPutWithKeylessTableAfterIteratorCreation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testAddWithKeylessTableAfterIteratorCreation() {
        BRunUtil.invoke(compileResult, "testAddWithKeylessTableAfterIteratorCreation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testRemoveAllReturnedRecordsFromIteratorKeylessTbl() {
        BRunUtil.invoke(compileResult, "testRemoveAllReturnedRecordsFromIteratorKeylessTbl");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testAddInconsistentDataToKeylessTbl() {
        BRunUtil.invoke(compileResult, "testAddInconsistentDataToKeylessTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testAddInconsistentDataToKeylessTbl2() {
        BRunUtil.invoke(compileResult, "testAddInconsistentDataToKeylessTbl2");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testPutInconsistentDataToKeylessTbl() {
        BRunUtil.invoke(compileResult, "testPutInconsistentDataToKeylessTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testPutInconsistentDataToKeylessTbl2() {
        BRunUtil.invoke(compileResult, "testPutInconsistentDataToKeylessTbl2");
        Assert.fail();
    }

    @Test
    public void testAddValidDataToKeylessTbl() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddValidDataToKeylessTbl");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testPutValidDataToKeylessTbl() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutValidDataToKeylessTbl");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testReadOnlyTableFilter() {
        BRunUtil.invoke(compileResult, "testReadOnlyTableFilter");
    }
}
