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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.core.model.values.BXMLSequence;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        Assert.assertEquals(((BByte) results[0]).byteValue(), 234);
        Assert.assertEquals(((BByte) results[1]).byteValue(), 100);
        Assert.assertEquals(((BByte) results[2]).byteValue(), 133);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneBoolean() {
        BValue[] results = BRunUtil.invoke(result, "cloneBoolean");
        Assert.assertNotNull(results);
        Assert.assertFalse(((BBoolean) results[0]).booleanValue());
        Assert.assertTrue(((BBoolean) results[1]).booleanValue());
        Assert.assertTrue(((BBoolean) results[2]).booleanValue());
        Assert.assertTrue(results[1] == results[2] && results[0] != results[1] && results[0] != results[2]);
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
        testCloneOnXMLs((BXMLItem) results[0], "Charlos");
        testCloneOnXMLs((BXMLItem) results[1], "Alex");
        Assert.assertNotSame(results[0], results[1]);
    }

    private void testCloneOnXMLs(BXMLItem bxmlItem, String name) {
        BXMLSequence sequence = (BXMLSequence) bxmlItem.children("name");
        BXMLItem firstItem = (BXMLItem) sequence.value().getRefValue(0);
        BString textValue = firstItem.getTextValue();
        Assert.assertEquals(textValue.stringValue(), name);
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

        BValueArray array = (BValueArray) bMap.get("otherData");
        testJSONArray(arr, array);
    }

    @Test
    public void testCloneJSONArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneJSONArray");
        testJSONArray(new Object[]{100, "EE", 12.3}, (BValueArray) results[0]);
        testJSONArray(new Object[]{1, "EE", 12.3}, (BValueArray) results[1]);
        testJSONArray(new Object[]{1, "EE", 300.5}, (BValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testJSONArray(Object[] arr, BValueArray array) {
        testRefArray(arr, array);
    }

    private void testRefArray(Object[] arr, BValueArray array) {
        Assert.assertEquals(((BInteger) array.getBValue(0)).intValue(), ((Integer) arr[0]).intValue());
        Assert.assertEquals((array.getBValue(1)).stringValue(), arr[1]);
        Assert.assertEquals(((BFloat) array.getBValue(2)).floatValue(), arr[2]);
    }

    @Test
    public void testCloneIntArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneIntArray");
        testIntArray(new long[]{100, 2, 3}, (BValueArray) results[0]);
        testIntArray(new long[]{1, 2, 3}, (BValueArray) results[1]);
        testIntArray(new long[]{1, 2, 300}, (BValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testIntArray(long[] arr, BValueArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.getInt(i), arr[i]);
        }
    }

    @Test
    public void testCloneDecimalArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneDecimalArray");
        testDecimalArray(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(30),
                                          BigDecimal.valueOf(4)}, (BValueArray) results[0]);
        testDecimalArray(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                                          BigDecimal.valueOf(4)}, (BValueArray) results[1]);
        testDecimalArray(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(300),
                                          BigDecimal.valueOf(4)}, (BValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testDecimalArray(BigDecimal[] arr, BValueArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.getRefValue(i).value(), arr[i]);
        }
    }

    @Test
    public void testCloneByteArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneByteArray");
        testByteArray(new byte[]{100, 2, 3}, (BValueArray) results[0]);
        testByteArray(new byte[]{1, 2, 3}, (BValueArray) results[1]);
        testByteArray(new byte[]{1, 2, (byte) 234}, (BValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testByteArray(byte[] arr, BValueArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.getByte(i), arr[i]);
        }
    }

    @Test
    public void testCloneFloatArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneFloatArray");
        testFloatArray(new double[]{100.5, 2.0, 3.0}, (BValueArray) results[0]);
        testFloatArray(new double[]{1.0, 2.0, 3.0}, (BValueArray) results[1]);
        testFloatArray(new double[]{1.0, 2.0, 300.5}, (BValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testFloatArray(double[] arr, BValueArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.getFloat(i), arr[i]);
        }
    }

    @Test
    public void testCloneStringArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneStringArray");
        testStringArray(new String[]{"XX", "B", "C"}, (BValueArray) results[0]);
        testStringArray(new String[]{"A", "B", "C"}, (BValueArray) results[1]);
        testStringArray(new String[]{"A", "B", "YY"}, (BValueArray) results[2]);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testStringArray(String[] arr, BValueArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.getString(i), arr[i]);
        }
    }

    @Test
    public void testCloneUnionArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneUnionArray");
        testRefArray(new Object[]{100, "EE", 12.3}, (BValueArray) results[0]);
        testRefArray(new Object[]{1, "EE", 12.3}, (BValueArray) results[1]);
        testRefArray(new Object[]{1, "EE", 300.5}, (BValueArray) results[2]);
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
    public void testCloneTable() {
        BValue[] results = BRunUtil.invoke(result, "cloneTable");
        Assert.assertNotNull(results);

        Object[][] expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                new Object[]{3, "John", 400.50},
        };
        testValuesOnTable((BValueArray) results[0], expectedValues);

        expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
        };
        testValuesOnTable((BValueArray) results[1], expectedValues);

        expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                new Object[]{3, "John", 400.50},
        };
        testValuesOnTable((BValueArray) results[2], expectedValues);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testValuesOnTable(BValueArray arr, Object[][] expectedValues) {
        int i = 0;
        while (i < arr.size()) {
            BMap<String, BValue> next = (BMap<String, BValue>) arr.getRefValue(i);
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
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, 0, "too many arguments in call to 'clone()'", 19, 13);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'anydata', found 'typedesc<int>'",
                                  24, 23);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'anydata', found 'error'", 29, 15);

        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to untainted parameter 'intArg'", 12, 22);

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
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 100);
        Assert.assertEquals(((BValueArray) results[1]).getInt(0), 20);
        Assert.assertEquals(((BValueArray) results[2]).getInt(0), 1000);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneArrayOfArrays() {
        BValue[] results = BRunUtil.invoke(result, "cloneArrayOfArrays");
        Assert.assertNotNull(results);
        Assert.assertEquals(((BValueArray) ((BValueArray) results[0]).getRefValue(0)).getInt(0), 400);
        Assert.assertEquals(((BValueArray) ((BValueArray) results[1]).getRefValue(0)).getInt(0), 200);
        Assert.assertEquals(((BValueArray) ((BValueArray) results[2]).getRefValue(0)).getInt(0), 500);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    @Test
    public void testCloneTuples() {
        BValue[] results = BRunUtil.invoke(result, "cloneTuple");
        Assert.assertNotNull(results);
        testTupleValues((BValueArray) results[0], 100, 400);
        testTupleValues((BValueArray) results[1], 100, 200);
        testTupleValues((BValueArray) results[2], 100, 500);
        Assert.assertTrue(results[1] != results[2] && results[0] != results[1] && results[0] != results[2]);
    }

    private void testTupleValues(BValueArray result, int mapValue, int arrValue) {
        BMap bMap = (BMap) result.getRefValue(0);
        BValueArray intArr = (BValueArray) result.getRefValue(1);
        Assert.assertEquals(((BInteger) bMap.get("one")).intValue(), mapValue);
        Assert.assertEquals(intArr.getInt(0), arrValue);
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
        Assert.assertNotSame(((BValueArray) results[0]).getRefValue(0), ((BValueArray) results[1]).getRefValue(0));
        Assert.assertNotSame(((BValueArray) results[0]).getRefValue(1), ((BValueArray) results[1]).getRefValue(1));
    }

    @Test
    public void testCloneCyclicRecord() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicRecord");
        Assert.assertNotNull(results);

        BMap record = (BMap)  results[0];
        BMap fieldA = (BMap) record.get("a");

        BValueArray arr = (BValueArray) fieldA.get("arr");
        BMap fieldB = (BMap) record.get("b");
        BMap fieldAOfB = (BMap) fieldB.get("aa");
        BValueArray arrOfAA = (BValueArray) fieldAOfB.get("arr");

        Assert.assertEquals(arr.getInt(0), 10);
        Assert.assertEquals(arrOfAA.getInt(0), 10);

        record = (BMap)  results[1];
        BMap fieldA1 = (BMap) record.get("a");

        arr = (BValueArray) fieldA1.get("arr");
        fieldB = (BMap) record.get("b");
        BMap fieldAOfB1 = (BMap) fieldB.get("aa");
        arrOfAA = (BValueArray) fieldAOfB1.get("arr");

        Assert.assertEquals(arr.getInt(0), 1);
        Assert.assertEquals(arrOfAA.getInt(0), 1);

        Assert.assertSame(fieldA, fieldAOfB);
        Assert.assertSame(fieldA1, fieldAOfB1);

        Assert.assertNotSame(fieldA, fieldA1);
        Assert.assertNotSame(fieldAOfB, fieldAOfB1);
    }

    @Test
    public void testCloneCyclicArray() {
        BValue[] results = BRunUtil.invoke(result, "cloneCyclicArray");
        Assert.assertNotNull(results);

        BValueArray[] arr = new BValueArray[2];
        arr[0] = (BValueArray) results[0];
        arr[1] = (BValueArray) results[1];

        BMap record1 = (BMap) arr[0].getBValue(2);
        BMap record2 = (BMap) arr[0].getBValue(3);

        BMap record3 = (BMap) arr[1].getBValue(2);
        BMap record4 = (BMap) arr[1].getBValue(3);

        BValueArray intArr1 = (BValueArray) record1.get("arr");
        BValueArray intArr2 = (BValueArray) record2.get("arr");

        BValueArray intArr3 = (BValueArray) record3.get("arr");
        BValueArray intArr4 = (BValueArray) record4.get("arr");

        Assert.assertSame(intArr1, intArr2);
        Assert.assertSame(intArr3, intArr4);

        Assert.assertNotSame(intArr1, intArr3);
        Assert.assertNotSame(intArr2, intArr4);
    }

    @Test
    public void testCloneFrozenAnydata() {
        BValue[] results = BRunUtil.invoke(result, "cloneFrozenAnydata");
        Assert.assertNotNull(results);
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
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

    @Test
    public void testCloneArrayWithError() {
        BValue[] results = BRunUtil.invoke(result, "testCloneArrayWithError");
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testCloneMapWithError() {
        BValue[] results = BRunUtil.invoke(result, "testCloneMapWithError");
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }
}
