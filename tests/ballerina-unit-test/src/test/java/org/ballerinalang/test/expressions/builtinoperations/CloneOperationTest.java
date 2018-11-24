/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.expressions.builtinoperations;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * This class contains the test cases to clone operation.
 * @version 0.983.0
 */

public class CloneOperationTest {

    private CompileResult result;
    private CompileResult negativeResult;
    private CompileResult taintCheckResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinoperations/clone-operation.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/builtinoperations/clone-operation-negative.bal");
        taintCheckResult =
                BCompileUtil.compile("test-src/expressions/builtinoperations/clone-operation-taint-negative.bal");
    }

    @Test
    public void testCloneInt() {
        BValue[] results = BRunUtil.invoke(result, "cloneInt");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 12);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 10);
        Assert.assertEquals(((BInteger) results[2]).intValue(), 13);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneFloat() {
        BValue[] results = BRunUtil.invoke(result, "cloneFloat");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BFloat) results[0]).floatValue(), 12.01);
        Assert.assertEquals(((BFloat) results[1]).floatValue(), 10.01);
        Assert.assertEquals(((BFloat) results[2]).floatValue(), 13.01);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneDecimal() {
        BValue[] results = BRunUtil.invoke(result, "cloneDecimalValue");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BDecimal) results[0]).decimalValue(), new BigDecimal("20.0"));
        Assert.assertEquals(((BDecimal) results[1]).decimalValue(), new BigDecimal("10.000"));
        Assert.assertEquals(((BDecimal) results[2]).decimalValue(), new BigDecimal("30.0"));
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneByte() {
        BValue[] results = BRunUtil.invoke(result, "cloneByte");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BByte) results[0]).byteValue(), (byte) 234);
        Assert.assertEquals(((BByte) results[1]).byteValue(), (byte) 100);
        Assert.assertEquals(((BByte) results[2]).byteValue(), (byte) 133);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneBoolean() {
        BValue[] results = BRunUtil.invoke(result, "cloneBoolean");
        Assert.assertNotNull(results);
        Assert.assertFalse(((BBoolean) results[0]).booleanValue());
        Assert.assertTrue(((BBoolean) results[1]).booleanValue());
        Assert.assertTrue(((BBoolean) results[2]).booleanValue());
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneString() {
        BValue[] results = BRunUtil.invoke(result, "cloneString");
        Assert.assertNotNull(results);
        Assert.assertEquals((results[0]).stringValue(), "BBBB");
        Assert.assertEquals((results[1]).stringValue(), "AAAA");
        Assert.assertEquals((results[2]).stringValue(), "CCCC");
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneXML() {
        BValue[] results = BRunUtil.invoke(result, "cloneXML");

        testCloneOnXMLs((BXMLItem) results[0], "Charlos", 123, 21);
        testCloneOnXMLs((BXMLItem) results[1], "Alex", 123, 21);
        testCloneOnXMLs((BXMLItem) results[2], "Alex", 5000, 21);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testCloneOnXMLs(BXMLItem bxmlItem, String name, int id, int age) {

        BXMLSequence sequence = (BXMLSequence) bxmlItem.children("name");
        BXMLItem firstItem = (BXMLItem) sequence.value().get(0);
        BString textValue = firstItem.getTextValue();
        Assert.assertEquals(textValue.stringValue(), name);

        sequence = (BXMLSequence) bxmlItem.children("id");
        firstItem = (BXMLItem) sequence.value().get(0);
        textValue = firstItem.getTextValue();
        Assert.assertEquals(textValue.intValue(), id);

        sequence = (BXMLSequence) bxmlItem.children("age");
        firstItem = (BXMLItem) sequence.value().get(0);
        textValue = firstItem.getTextValue();
        Assert.assertEquals(textValue.intValue(), age);
    }

    @Test
    public void testCloneJSON() {
        BValue[] results = BRunUtil.invoke(result, "cloneJSON");
        testValuesInJSON((BMap) results[0], "Charlos", 21, 123, new Object[]{1, "EE", 12.3});
        testValuesInJSON((BMap) results[1], "Alex", 21, 123, new Object[]{1, "EE", 12.3});
        testValuesInJSON((BMap) results[2], "Alex", 21, 5000, new Object[]{1, "EE", 12.3});
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testValuesInJSON(BMap bMap, String name, int age, int id, Object[] arr) {
        Assert.assertEquals(bMap.get("name").stringValue(), name);
        Assert.assertEquals(((BInteger) bMap.get("age")).intValue(), age);
        Assert.assertEquals(((BInteger) bMap.get("id")).intValue(), id);

        BRefValueArray array = (BRefValueArray) bMap.get("otherData");
        testJSONArray(arr, array);
    }

    @Test
    public void testCloneJSONArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneJSONArray");
        testJSONArray(new Object[]{100, "EE", 12.3}, (BRefValueArray) results[0]);
        testJSONArray(new Object[]{1, "EE", 12.3}, (BRefValueArray) results[1]);
        testJSONArray(new Object[]{1, "EE", 300.5}, (BRefValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testJSONArray(Object[] arr, BRefValueArray array) {
        testRefArray(arr, array);
    }

    private void testRefArray(Object[] arr, BRefValueArray array) {
        Assert.assertEquals(((BInteger) array.getBValue(0)).intValue(), ((Integer) arr[0]).intValue());
        Assert.assertEquals((array.getBValue(1)).stringValue(), arr[1]);
        Assert.assertEquals(((BFloat) array.getBValue(2)).floatValue(), arr[2]);
    }

    @Test
    public void testCloneIntArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneIntArray");
        testIntArray(new long[]{100, 2, 3}, (BIntArray) results[0]);
        testIntArray(new long[]{1, 2, 3}, (BIntArray) results[1]);
        testIntArray(new long[]{1, 2, 300}, (BIntArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testIntArray(long[] arr, BIntArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneDecimalArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneDecimalArray");
        testDecimalArray(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(30),
                                          BigDecimal.valueOf(4)}, (BRefValueArray) results[0]);
        testDecimalArray(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                                          BigDecimal.valueOf(4)}, (BRefValueArray) results[1]);
        testDecimalArray(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(300),
                                          BigDecimal.valueOf(4)}, (BRefValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testDecimalArray(BigDecimal[] arr, BRefValueArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i).value(), arr[i]);
        }
    }

    @Test
    public void testCloneByteArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneByteArray");
        testByteArray(new byte[]{100, 2, 3}, (BByteArray) results[0]);
        testByteArray(new byte[]{1, 2, 3}, (BByteArray) results[1]);
        testByteArray(new byte[]{1, 2, (byte) 234}, (BByteArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testByteArray(byte[] arr, BByteArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneFloatArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneFloatArray");
        testFloatArray(new double[]{100.5, 2.0, 3.0}, (BFloatArray) results[0]);
        testFloatArray(new double[]{1.0, 2.0, 3.0}, (BFloatArray) results[1]);
        testFloatArray(new double[]{1.0, 2.0, 300.5}, (BFloatArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testFloatArray(double[] arr, BFloatArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneStringArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneStringArray");
        testStringArray(new String[]{"XX", "B", "C"}, (BStringArray) results[0]);
        testStringArray(new String[]{"A", "B", "C"}, (BStringArray) results[1]);
        testStringArray(new String[]{"A", "B", "YY"}, (BStringArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testStringArray(String[] arr, BStringArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneUnionArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneUnionArray");
        testRefArray(new Object[]{100, "EE", 12.3}, (BRefValueArray) results[0]);
        testRefArray(new Object[]{1, "EE", 12.3}, (BRefValueArray) results[1]);
        testRefArray(new Object[]{1, "EE", 300.5}, (BRefValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneUnion() {
        BValue[] results = BRunUtil.invoke(result, "cloneUnion");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 1);
        Assert.assertEquals(((BFloat) results[2]).floatValue(), 300.5);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneConstrainedJSON() {
        BValue[] results = BRunUtil.invoke(result, "cloneConstrainedJSON");

        testConstrainedJSON((BMap) results[0], "Charlos", 1, 300.5);
        testConstrainedJSON((BMap) results[1], "Jane", 1, 300.5);
        testConstrainedJSON((BMap) results[2], "Jane", 1, 400.5);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testConstrainedJSON(BMap bMap, String name, int id, double salary) {
        Assert.assertEquals(((BInteger) bMap.get("id")).intValue(), id);
        Assert.assertEquals(bMap.get("name").stringValue(), name);
        Assert.assertEquals(((BFloat) bMap.get("salary")).floatValue(), salary);
    }

    @Test
    public void testCloneTable() {
        BValue[] results = BRunUtil.invoke(result, "cloneTable");
        Assert.assertNotNull(results);

        Object[][] expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                new Object[]{3, "John", 400.50},
                };
        testValuesOnTable((BTable) results[0], expectedValues);

        expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                };
        testValuesOnTable((BTable) results[1], expectedValues);

        expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                new Object[]{3, "John", 400.50},
                };
        testValuesOnTable((BTable) results[2], expectedValues);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testValuesOnTable(BTable table, Object[][] expectedValues) {
        int i = 0;
        while (table.hasNext()) {
            BMap<String, BValue> next = table.getNext();
            Assert.assertEquals(((BInteger) next.get("id")).intValue(), ((Integer) expectedValues[i][0]).intValue());
            Assert.assertEquals(next.get("name").stringValue(), expectedValues[i][1]);
            Assert.assertEquals(((BFloat) next.get("salary")).floatValue(), expectedValues[i][2]);
            i++;
        }
        Assert.assertEquals(expectedValues.length, i);
    }

    @Test
    public void testCloneMap() {
        BValue[] results = BRunUtil.invoke(result, "cloneMap");
        Assert.assertNotNull(results);
        testCloneOnMaps(results[0], "Charlos", 123, 21);
        testCloneOnMaps(results[1], "Alex", 123, 21);
        testCloneOnMaps(results[2], "Alex", 5000, 21);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 4);
        BAssertUtil.validateError(negativeResult, 0, "too many arguments in call to 'clone()'", 19, 13);
        BAssertUtil.validateError(negativeResult, 1, "function invocation on type 'typedesc' is not supported", 24, 18);
        BAssertUtil.validateError(negativeResult, 2, "function invocation on type '()' is not supported", 29, 12);
        BAssertUtil.validateError(negativeResult, 3, "function invocation on type 'error' is not supported", 35, 15);

        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to sensitive parameter 'intArg'", 12, 22);

    }

    private void testCloneOnMaps(BValue val, String name, int id, int age) {
        BMap mapA, mapB, mapC, jsonVal;
        String tmpName;
        long tmpId, tmpAge;
        mapA = (BMap) val;
        mapB = (BMap) mapA.get("zzz");
        mapC = (BMap) mapB.get("yyy");
        jsonVal = (BMap) mapC.get("xxx");
        tmpName = jsonVal.get("name").stringValue();
        tmpId = ((BInteger) jsonVal.get("id")).intValue();
        tmpAge = ((BInteger) jsonVal.get("age")).intValue();
        Assert.assertEquals(tmpName, name);
        Assert.assertEquals(tmpId, id);
        Assert.assertEquals(tmpAge, age);
    }

    @Test
    public void testCloneNilableInt() {
        BValue[] results = BRunUtil.invoke(result, "cloneNilableInt");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 4);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 10);
        Assert.assertEquals(((BInteger) results[2]).intValue(), 5);
        Assert.assertEquals(((BInteger) results[3]).intValue(), 4);
        Assert.assertNull(results[4]);
        Assert.assertEquals(((BInteger) results[5]).intValue(), 5);
        Assert.assertTrue(results[1] != results[2] &&
                           results[0] != results[1] &&
                           results[0] != results[2]);
        Assert.assertNotSame(results[3], results[5]);
    }

    @Test
    public void testReturnValues() {
        BValue[] results = BRunUtil.invoke(result, "cloneReturnValues");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BIntArray) results[0]).get(0), 100);
        Assert.assertEquals(((BIntArray) results[1]).get(0), 20);
        Assert.assertEquals(((BIntArray) results[2]).get(0), 1000);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneArrayOfArrays() {
        BValue[] results = BRunUtil.invoke(result, "cloneArrayOfArrays");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BIntArray) ((BRefValueArray) results[0]).get(0)).get(0), 400);
        Assert.assertEquals(((BIntArray) ((BRefValueArray) results[1]).get(0)).get(0), 200);
        Assert.assertEquals(((BIntArray) ((BRefValueArray) results[2]).get(0)).get(0), 500);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneTuples() {
        BValue[] results = BRunUtil.invoke(result, "cloneTuple");
        Assert.assertNotNull(results);
        testTupleValues((BRefValueArray) results[0], 100, 400);
        testTupleValues((BRefValueArray) results[1], 100, 200);
        testTupleValues((BRefValueArray) results[2], 100, 500);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testTupleValues(BRefValueArray result, int mapValue, int arrValue) {
        BMap bMap = (BMap) result.get(0);
        BIntArray intArr = (BIntArray) result.get(1);
        Assert.assertEquals(((BInteger) bMap.get("one")).intValue(), mapValue);
        Assert.assertEquals(intArr.get(0), arrValue);
    }

    @Test
    public void testCloneAnyDataRecord() {
        BValue[] results = BRunUtil.invoke(result, "cloneAnydataRecord");
        Assert.assertNotNull(results);
        testCloneRecordValues((BMap) results[0], 100, "Charlos", 300.5);
        testCloneRecordValues((BMap) results[1], 100, "Alex", 300.5);
        testCloneRecordValues((BMap) results[2], 100, "Alex", 400.5);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneAnyData() {
        BValue[] results = BRunUtil.invoke(result, "cloneAnydata");
        Assert.assertNotNull(results);
        testCloneRecordValues((BMap) results[0], 100, "Charlos", 300.5);
        testCloneRecordValues((BMap) results[1], 100, "Alex", 300.5);
        testCloneRecordValues((BMap) results[2], 100, "Alex", 400.5);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testCloneRecordValues(BMap bMap, int id, String name, double salary) {
        Assert.assertEquals(((BInteger) bMap.get("id")).intValue(), id);
        Assert.assertEquals(bMap.get("name").stringValue(), name);
        Assert.assertEquals(((BFloat) bMap.get("salary")).floatValue(), salary);
    }

    @Test
    public void testCloneCyclicMapsArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicMapsArray");
        Assert.assertNotNull(results);
        Assert.assertNotSame(((BRefValueArray) results[0]).get(0), ((BRefValueArray) results[1]).get(0));
        Assert.assertNotSame(((BRefValueArray) results[0]).get(1), ((BRefValueArray) results[1]).get(1));
    }

    @Test
    public void testCloneCyclicRecord() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicRecord");
        Assert.assertNotNull(results);

        BMap record = (BMap)  results[0];
        BMap fieldA = (BMap) record.get("a");

        BIntArray arr = (BIntArray) fieldA.get("arr");
        BMap fieldB = (BMap) record.get("b");
        BMap fieldAOfB = (BMap) fieldB.get("aa");
        BIntArray arrOfAA = (BIntArray) fieldAOfB.get("arr");

        Assert.assertEquals(arr.get(0), 10);
        Assert.assertEquals(arrOfAA.get(0), 10);

        record = (BMap)  results[1];
        BMap fieldA1 = (BMap) record.get("a");

        arr = (BIntArray) fieldA1.get("arr");
        fieldB = (BMap) record.get("b");
        BMap fieldAOfB1 = (BMap) fieldB.get("aa");
        arrOfAA = (BIntArray) fieldAOfB1.get("arr");

        Assert.assertEquals(arr.get(0), 1);
        Assert.assertEquals(arrOfAA.get(0), 1);

        Assert.assertSame(fieldA, fieldAOfB);
        Assert.assertSame(fieldA1, fieldAOfB1);

        Assert.assertNotSame(fieldA, fieldA1);
        Assert.assertNotSame(fieldAOfB, fieldAOfB1);
    }

    @Test
    public void testCloneCyclicArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicArray");
        Assert.assertNotNull(results);

        BRefValueArray[] arr = new BRefValueArray[2];
        arr[0] = (BRefValueArray) results[0];
        arr[1] = (BRefValueArray) results[1];

        BMap record1 = (BMap) arr[0].getBValue(2);
        BMap record2 = (BMap) arr[0].getBValue(3);

        BMap record3 = (BMap) arr[1].getBValue(2);
        BMap record4 = (BMap) arr[1].getBValue(3);

        BIntArray intArr1 = (BIntArray) record1.get("arr");
        BIntArray intArr2 = (BIntArray) record2.get("arr");

        BIntArray intArr3 = (BIntArray) record3.get("arr");
        BIntArray intArr4 = (BIntArray) record4.get("arr");

        Assert.assertSame(intArr1, intArr2);
        Assert.assertSame(intArr3, intArr4);

        Assert.assertNotSame(intArr1, intArr3);
        Assert.assertNotSame(intArr2, intArr4);
    }

    @Test
    public void testCloneFrozenAnydata() {
        BValue[] results = BRunUtil.invoke(result, "cloneFrozenAnydata");
        Assert.assertNotNull(results);
        Assert.assertSame(results[0], results[1]);
        Assert.assertSame(results[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
    }

    @Test
    public void testCloneNonAnydata() {
        BValue[] results = BRunUtil.invoke(result, "cloneNonAnydata");
        Assert.assertNotNull(results);
        Assert.assertNotSame(results[0], results[1]);
        Assert.assertSame(results[1].getType().getTag(), TypeTags.ERROR_TAG);
        Assert.assertEquals(((BError) results[1]).reason,
                "value '({id:100, name:\"Alex\", salary:300.5}, Employee)' of type '(Employee,any)' " +
                "can not be cloned");
    }

    @Test
    public void testCloneLikeAnydata() {
        BValue[] results = BRunUtil.invoke(result, "cloneLikeAnydata");
        Assert.assertNotNull(results);
        Assert.assertNotSame(results[0], results[1]);
        BRefValueArray result1 = (BRefValueArray) results[0];
        BMap person1 = (BMap) result1.get(0);
        BIntArray arr1 = (BIntArray) result1.get(1);

        BRefValueArray result2 = (BRefValueArray) results[1];
        BMap person2 = (BMap) result2.get(0);
        BIntArray arr2 = (BIntArray) result2.get(1);
        Assert.assertEquals(person1.getMap().entrySet(), person2.getMap().entrySet());
        Assert.assertEquals(arr1.stringValue(), arr2.stringValue());
    }

    @Test
    public void testCloneNullJson() {
        BValue[] results = BRunUtil.invoke(result, "cloneNullJson");
        Assert.assertNotNull(results);
        Assert.assertNull(results[0]);
        Assert.assertNull(results[1]);
    }

    @Test
    public void testCloneNilAnydata() {
        BValue[] results = BRunUtil.invoke(result, "cloneNilAnydata");
        Assert.assertNotNull(results);
        Assert.assertNull(results[0]);
        Assert.assertNull(results[1]);
    }
}
