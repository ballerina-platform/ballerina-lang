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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeResult = null;
    }

    @Test
    public void testLength() {
        Object returns = BRunUtil.invoke(compileResult, "testTableLength");
        assertEquals(returns, 4L);
    }

    @Test
    public void testIterator() {
        Object returns = BRunUtil.invoke(compileResult, "testIterator");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void getKey() {
        Object returns = BRunUtil.invoke(compileResult, "getValueFromKey");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testMap() {
        Object returns = BRunUtil.invoke(compileResult, "testMap");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testForeach() {
        Object returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns.toString(), "Chiran Granier ");
    }

    @Test
    public void testFilter() {
        Object returns = BRunUtil.invoke(compileResult, "testFilter");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testReduce() {
        Object returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(returns, 35.5d);
    }

    @Test
    public void testRemoveWithKey() {
        Object returns = BRunUtil.invoke(compileResult, "removeWithKey");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void removeIfHasKey() {
        Object returns = BRunUtil.invoke(compileResult, "removeIfHasKey");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testHasKey() {
        BRunUtil.invoke(compileResult, "testHasKey");
    }

    @Test
    public void testGetValue() {
        BRunUtil.invoke(compileResult, "testGetValue");
    }

    @Test
    public void testHashCollisionHandlingScenarios() {
        BRunUtil.invoke(compileResult, "testHashCollisionHandlingScenarios");
    }

    @Test
    public void testGetKeyList() {
        Object result = BRunUtil.invoke(compileResult, "testGetKeyList");
        BArray returns = (BArray) result;
        assertEquals(returns.size(), 4);
        assertEquals(returns.getString(0), "Chiran");
        assertEquals(returns.getString(1), "Mohan");
        assertEquals(returns.getString(2), "Gima");
        assertEquals(returns.getString(3), "Granier");

        Object unionReturns = BRunUtil.invoke(compileResult, "testGetKeysFromUnionConstrained");
        BArray unionResult = (BArray) unionReturns;
        assertEquals(unionResult.size(), 2);
        assertEquals(unionResult.getString(0), "Adam");
        assertEquals(unionResult.getString(1), "Mark");
    }

    @Test
    public void testRemoveAllFromTable() {
        Object returns = BRunUtil.invoke(compileResult, "removeAllFromTable");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTableToArray() {
        Object returns = BRunUtil.invoke(compileResult, "tableToArray");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testNextKey() {
        Object returns = BRunUtil.invoke(compileResult, "testNextKey");
        assertEquals(returns, 101L);
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
        validateError(negativeResult, index++, "incompatible types: expected 'function (ballerina/lang.table:0.0" +
                ".0:MapType) returns (ballerina/lang.table:0.0.0:MapType1)', found 'function (Person) returns " +
                "(string)'", 200, 18);
        validateError(negativeResult, index++, "incompatible types: expected 'table<map<(any|error)>> " +
                "key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 210, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<map<(any|error)>> " +
                "key<ballerina/lang.table:0.0.0:KeyType>', found 'PersonEmptyKeyedTbl'", 216, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 227, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 233, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 242, 21);
        validateError(negativeResult, index++, "incompatible types: expected 'Employee', " +
                "found 'record {| string name; int age; |}'", 248, 22);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 256, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                        "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', " +
                        "found 'PersonEmptyKeyedTbl'", 262, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'EmployeeEmptyKeyedTbl'",
                270, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Employee>'", 276, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'EmployeeEmptyKeyedTbl'",
                284, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Employee>'", 290, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'", 298, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'PersonEmptyKeyedTbl'",
                304, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<int>', found 'table<Person>'", 312, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<int>', found 'PersonEmptyKeyedTbl'", 318, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'PersonEmptyKeyedTbl'",
                326, 9);
        validateError(negativeResult, index++, "incompatible types: expected 'table<ballerina/" +
                        "lang.table:0.0.0:MapType> key<ballerina/lang.table:0.0.0:KeyType>', found 'table<Person>'",
                334, 9);
        assertEquals(negativeResult.getErrorCount(), index);
    }

    @Test
    public void testAddData() {
        Object returns = BRunUtil.invoke(compileResult, "testAddData");
        Assert.assertTrue((Boolean) returns);
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
        Object returns = BRunUtil.invoke(compileResult, "testAddValidData");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAddValidData2() {
        Object returns = BRunUtil.invoke(compileResult, "testAddValidData2");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAddValidDataWithMapConstrTbl() {
        Object returns = BRunUtil.invoke(compileResult, "testAddValidDataWithMapConstrTbl");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testPutData() {
        Object returns = BRunUtil.invoke(compileResult, "testPutData");
        Assert.assertTrue((Boolean) returns);
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
        Object returns = BRunUtil.invoke(compileResult, "testPutValidData");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testPutValidData2() {
        Object returns = BRunUtil.invoke(compileResult, "testPutValidData2");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testPutValidDataWithMapConstrTbl() {
        Object returns = BRunUtil.invoke(compileResult, "testPutValidDataWithMapConstrTbl");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testPutWithKeyLessTbl() {
        Object returns = BRunUtil.invoke(compileResult, "testPutWithKeyLessTbl");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testAddWithKeyLessTbl() {
        Object returns = BRunUtil.invoke(compileResult, "testAddWithKeyLessTbl");
        Assert.assertTrue((Boolean) returns);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testAddNewRecordAfterIteratorCreation() {
        BRunUtil.invoke(compileResult, "testAddNewRecordAfterIteratorCreation");
    }

    @Test
    public void testRemoveAlreadyReturnedRecordFromIterator() {
        Object returns = BRunUtil.invoke(compileResult, "testRemoveAlreadyReturnedRecordFromIterator");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void removeIfHasKeyReturnedRecordFromIterator() {
        Object returns = BRunUtil.invoke(compileResult, "removeIfHasKeyReturnedRecordFromIterator");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testChangeValueForAGivenKeyWhileIterating() {
        Object returns = BRunUtil.invoke(compileResult, "testChangeValueForAGivenKeyWhileIterating");
        Assert.assertTrue((Boolean) returns);
    }

    @Test(dataProvider = "functionsToTestRemoveAndIterate")
    public void testRemoveAndIterateTables(String function) {
        Object returns = BRunUtil.invoke(compileResult, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider
    public Object[] functionsToTestRemoveAndIterate() {
        return new String[]{
                "testRemoveThenIterate",
                "testRemoveEmptyThenIterate",
                "testRemoveEmptyAddThenIterate",
                "testRemoveEmptyIterateThenAdd",
                "testRemoveEmptyIterateThenAddQueryExpr",
                "testRemoveEmptyIterateThenAddQueryAction"
        };
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
        Object returns = BRunUtil.invoke(compileResult, "testAddValidDataToKeylessTbl");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testPutValidDataToKeylessTbl() {
        Object returns = BRunUtil.invoke(compileResult, "testPutValidDataToKeylessTbl");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testReadOnlyTableFilter() {
        BRunUtil.invoke(compileResult, "testReadOnlyTableFilter");
    }

    @Test
    public void testKeylessTableForeach() {
        BRunUtil.invoke(compileResult, "testKeylessTableForeach");
    }

    @Test
    public void testKeylessReadOnlyTableForeach() {
        BRunUtil.invoke(compileResult, "testKeylessReadOnlyTableForeach");
    }
    
    @Test
    public void testReduceForKeylessTables() {
        BRunUtil.invoke(compileResult, "testReduceForKeylessTables");
    }

    @Test
    public void testReduceForKeylessReadOnlyTables() {
        BRunUtil.invoke(compileResult, "testReduceForKeylessReadOnlyTables");
    }

    @Test(dataProvider = "functionsToTestEmptyKeyedKeylessTbl")
    public void testEmptyKeyedKeylessTbl(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider
    public  Object[] functionsToTestEmptyKeyedKeylessTbl() {
        return new String[] {
                "testPutWithEmptyKeyedKeyLessTbl",
                "testPutWithEmptyKeyedKeyLessTblAfterIteratorCreation",
                "testAddWithEmptyKeyedKeyLessTbl",
                "testAddWithEmptyKeyedKeyLessTblAfterIteratorCreation",
                "testRemoveAllReturnedRecordsFromIteratorEmptyKeyedKeyLessTbl",
                "testAddInconsistentDataToEmptyKeyedKeyLessTbl",
                "testAddInconsistentDataToEmptyKeyedKeyLessTbl2",
                "testPutInconsistentDataToEmptyKeyedKeyLessTbl",
                "testPutInconsistentDataToEmptyKeyedKeyLessTbl2",
                "testAddValidDataToEmptyKeyedKeyLessTbl",
                "testPutValidDataToEmptyKeyedKeyLessTbl",
                "testEmptyKeyedKeyLessTblForeach",
                "testEmptyKeyedKeyLessTblReadOnlyTableForeach",
                "testReduceForEmptyKeyedKeyLessTbl",
                "testReduceForEmptyKeyedKeyLessReadOnlyTbl",
                "testMapWithEmptyKeyedKeyLessTbl",
                "testLengthWithEmptyKeyedKeyLessTbl"
        };
    }

    @Test
    public void testTableIterationAfterPut() {
        BRunUtil.invoke(compileResult, "testTableIterationAfterPut1");
        BRunUtil.invoke(compileResult, "testTableIterationAfterPut2");
        BRunUtil.invoke(compileResult, "testTableIterationAfterPut3");
        BRunUtil.invoke(compileResult, "testTableIterationAfterPut4");
    }
}
