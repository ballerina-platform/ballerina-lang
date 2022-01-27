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
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
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
        Object[] returns = JvmRunUtil.invoke(compileResult, "testTableLength");
        assertEquals(returns[0], 4L);
    }

    @Test
    public void testIterator() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testIterator");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void getKey() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "getValueFromKey");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testMap() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testMap");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testForeach() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns[0].toString(), "Chiran Granier ");
    }

    @Test
    public void testFilter() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testFilter");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testReduce() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testReduce");
        assertEquals(returns[0], 35.5d);
    }

    @Test
    public void testRemoveWithKey() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "removeWithKey");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void removeIfHasKey() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "removeIfHasKey");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testHasKey() {
        JvmRunUtil.invoke(compileResult, "testHasKey");
    }

    @Test
    public void testGetValue() {
        JvmRunUtil.invoke(compileResult, "testGetValue");
    }

    @Test
    public void testHashCollisionHandlingScenarios() {
        JvmRunUtil.invoke(compileResult, "testHashCollisionHandlingScenarios");
    }

    @Test
    public void testGetKeyList() {
        Object[] result = JvmRunUtil.invoke(compileResult, "testGetKeyList");
        BArray returns = (BArray) result[0];
        assertEquals(returns.size(), 4);
        assertEquals(returns.getString(0), "Chiran");
        assertEquals(returns.getString(1), "Mohan");
        assertEquals(returns.getString(2), "Gima");
        assertEquals(returns.getString(3), "Granier");

        Object[] unionReturns = JvmRunUtil.invoke(compileResult, "testGetKeysFromUnionConstrained");
        BArray unionResult = (BArray) unionReturns[0];
        assertEquals(unionResult.size(), 2);
        assertEquals(unionResult.getString(0), "Adam");
        assertEquals(unionResult.getString(1), "Mark");
    }

    @Test
    public void testRemoveAllFromTable() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "removeAllFromTable");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testTableToArray() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "tableToArray");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testNextKey() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testNextKey");
        assertEquals(returns[0], 101L);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyNotFound \\{\"message\":\"cannot find key 'AAA'\"\\}.*")
    public void getWithInvalidKey() {
        JvmRunUtil.invoke(compileResult, "getWithInvalidKey");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyNotFound \\{\"message\":\"cannot find key 'AAA'\"\\}.*")
    public void removeWithInvalidKey() {
        JvmRunUtil.invoke(compileResult, "removeWithInvalidKey");
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
        Object[] returns = JvmRunUtil.invoke(compileResult, "testAddData");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: \\{ballerina/lang.table\\}KeyConstraintViolation \\{\"message\":\"a value found for key " +
                            "'5'\"\\}.*")
    public void testAddExistingMember() {
        JvmRunUtil.invoke(compileResult, "testAddExistingMember");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type 'table<Engineer> "
                    + "key\\(name\\)'\"\\}.*")
    public void testAddInconsistentData() {
        JvmRunUtil.invoke(compileResult, "testAddInconsistentData");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type 'table<Engineer>"
                    + " key\\(name\\)'\"\\}.*")
    public void testAddInconsistentData2() {
        JvmRunUtil.invoke(compileResult, "testAddInconsistentData2");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testAddInconsistentDataWithMapConstrTbl() {
        JvmRunUtil.invoke(compileResult, "testAddInconsistentDataWithMapConstrTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'map<string>' inconsistent with the inherent table type " +
                    "'table<Teacher>'\"\\}.*")
    public void testAddInconsistentDataWithMapConstrTbl2() {
        JvmRunUtil.invoke(compileResult, "testAddInconsistentDataWithMapConstrTbl2");
        Assert.fail();
    }

    @Test
    public void testAddValidData() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testAddValidData");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testAddValidData2() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testAddValidData2");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testAddValidDataWithMapConstrTbl() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testAddValidDataWithMapConstrTbl");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testPutData() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPutData");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type 'table<Engineer> "
                    + "key\\(name\\)'\"\\}.*")
    public void testPutInconsistentData() {
        JvmRunUtil.invoke(compileResult, "testPutInconsistentData");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type 'table<Engineer>"
                    + " key\\(name\\)'\"\\}.*")
    public void testPutInconsistentData2() {
        JvmRunUtil.invoke(compileResult, "testPutInconsistentData2");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testPutInconsistentDataWithMapConstrTbl() {
        JvmRunUtil.invoke(compileResult, "testPutInconsistentDataWithMapConstrTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'map<string>' inconsistent with the inherent table type " +
                    "'table<Teacher>'\"\\}.*")
    public void testPutInconsistentDataWithMapConstrTbl2() {
        JvmRunUtil.invoke(compileResult, "testPutInconsistentDataWithMapConstrTbl2");
        Assert.fail();
    }

    @Test
    public void testPutValidData() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPutValidData");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testPutValidData2() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPutValidData2");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testPutValidDataWithMapConstrTbl() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPutValidDataWithMapConstrTbl");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testPutWithKeyLessTbl() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPutWithKeyLessTbl");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testAddWithKeyLessTbl() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testAddWithKeyLessTbl");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testAddNewRecordAfterIteratorCreation() {
        JvmRunUtil.invoke(compileResult, "testAddNewRecordAfterIteratorCreation");
    }

    @Test
    public void testRemoveAlreadyReturnedRecordFromIterator() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testRemoveAlreadyReturnedRecordFromIterator");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void removeIfHasKeyReturnedRecordFromIterator() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "removeIfHasKeyReturnedRecordFromIterator");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testChangeValueForAGivenKeyWhileIterating() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testChangeValueForAGivenKeyWhileIterating");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testRemoveThenIterate() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testRemoveThenIterate");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testPutWithKeylessTableAfterIteratorCreation() {
        JvmRunUtil.invoke(compileResult, "testPutWithKeylessTableAfterIteratorCreation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testAddWithKeylessTableAfterIteratorCreation() {
        JvmRunUtil.invoke(compileResult, "testAddWithKeylessTableAfterIteratorCreation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}IteratorMutabilityError \\{\"message\":\"Table "
                    + "was mutated after the iterator was created\"\\}.*")
    public void testRemoveAllReturnedRecordsFromIteratorKeylessTbl() {
        JvmRunUtil.invoke(compileResult, "testRemoveAllReturnedRecordsFromIteratorKeylessTbl");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testAddInconsistentDataToKeylessTbl() {
        JvmRunUtil.invoke(compileResult, "testAddInconsistentDataToKeylessTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testAddInconsistentDataToKeylessTbl2() {
        JvmRunUtil.invoke(compileResult, "testAddInconsistentDataToKeylessTbl2");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Person' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testPutInconsistentDataToKeylessTbl() {
        JvmRunUtil.invoke(compileResult, "testPutInconsistentDataToKeylessTbl");
        Assert.fail();
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.table\\}InherentTypeViolation " +
                    "\\{\"message\":\"value type 'Student' inconsistent with the inherent table type " +
                    "'table<Engineer>'\"\\}.*")
    public void testPutInconsistentDataToKeylessTbl2() {
        JvmRunUtil.invoke(compileResult, "testPutInconsistentDataToKeylessTbl2");
        Assert.fail();
    }

    @Test
    public void testAddValidDataToKeylessTbl() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testAddValidDataToKeylessTbl");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testPutValidDataToKeylessTbl() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPutValidDataToKeylessTbl");
        Assert.assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testReadOnlyTableFilter() {
        JvmRunUtil.invoke(compileResult, "testReadOnlyTableFilter");
    }

    @Test
    public void testKeylessTableForeach() {
        JvmRunUtil.invoke(compileResult, "testKeylessTableForeach");
    }

    @Test
    public void testKeylessReadOnlyTableForeach() {
        JvmRunUtil.invoke(compileResult, "testKeylessReadOnlyTableForeach");
    }
    
    @Test
    public void testReduceForKeylessTables() {
        JvmRunUtil.invoke(compileResult, "testReduceForKeylessTables");
    }

    @Test
    public void testReduceForKeylessReadOnlyTables() {
        JvmRunUtil.invoke(compileResult, "testReduceForKeylessReadOnlyTables");
    }
}
