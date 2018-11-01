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

package org.ballerinalang.test.builtinmethods;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
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

public class CloneTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/builtinmethods/clone.bal");
    }

    @Test
    public void testCloneInt() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneInt");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(((BInteger)outputEmployeeEvents[0]).intValue(), 12);
        Assert.assertEquals(((BInteger)outputEmployeeEvents[1]).intValue(), 10);
        Assert.assertEquals(((BInteger)outputEmployeeEvents[2]).intValue(), 13);
    }

    @Test
    public void testCloneFloat() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneFloat");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(((BFloat)outputEmployeeEvents[0]).floatValue(), 12.01);
        Assert.assertEquals(((BFloat)outputEmployeeEvents[1]).floatValue(), 10.01);
        Assert.assertEquals(((BFloat)outputEmployeeEvents[2]).floatValue(), 13.01);
    }

    @Test
    public void testCloneBoolean() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneBoolean");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertFalse(((BBoolean) outputEmployeeEvents[0]).booleanValue());
        Assert.assertTrue(((BBoolean) outputEmployeeEvents[1]).booleanValue());
        Assert.assertTrue(((BBoolean) outputEmployeeEvents[2]).booleanValue());
    }

    @Test
    public void testCloneString() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneString");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals((outputEmployeeEvents[0]).stringValue(), "BBBB");
        Assert.assertEquals((outputEmployeeEvents[1]).stringValue(), "AAAA");
        Assert.assertEquals((outputEmployeeEvents[2]).stringValue(), "CCCC");
    }

    @Test
    public void testCloneXML() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneXML");

        testCloneOnXMLs((BXMLItem) outputEmployeeEvents[0], "Charlos", 123, 21);
        testCloneOnXMLs((BXMLItem) outputEmployeeEvents[1], "Alex", 123, 21);
        testCloneOnXMLs((BXMLItem) outputEmployeeEvents[2], "Alex", 5000, 21);
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
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneJSON");
        testValuesInJSON((BMap) outputEmployeeEvents[0], "Charlos", 21, 123, new Object[]{1, "EE", 12.3});
        testValuesInJSON((BMap) outputEmployeeEvents[1], "Alex", 21, 123, new Object[]{1, "EE", 12.3});
        testValuesInJSON((BMap) outputEmployeeEvents[2], "Alex", 21, 5000, new Object[]{1, "EE", 12.3});
    }

    private void testValuesInJSON(BMap outputEmployeeEvent, String name, int age, int id, Object[] arr) {
        Assert.assertEquals(outputEmployeeEvent.get("name").stringValue(), name);
        Assert.assertEquals(((BInteger)outputEmployeeEvent.get("age")).intValue(), age);
        Assert.assertEquals(((BInteger)outputEmployeeEvent.get("id")).intValue(), id);

        BRefValueArray array = (BRefValueArray) outputEmployeeEvent.get("otherData");
        testJSONArray(arr, array);
    }

    @Test
    public void testCloneJSONArray() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneJSONArray");
        testJSONArray(new Object[] {100, "EE", 12.3}, (BRefValueArray) outputEmployeeEvents[0]);
        testJSONArray(new Object[] {1, "EE", 12.3}, (BRefValueArray) outputEmployeeEvents[1]);
        testJSONArray(new Object[] {1, "EE", 300.5}, (BRefValueArray) outputEmployeeEvents[2]);
    }

    private void testJSONArray(Object[] arr, BRefValueArray array) {
        testRefArray(arr, array);
    }

    private void testRefArray(Object[] arr, BRefValueArray array) {
        Assert.assertEquals(((BInteger)array.getBValue(0)).intValue(), ((Integer)arr[0]).intValue());
        Assert.assertEquals((array.getBValue(1)).stringValue(), arr[1]);
        Assert.assertEquals(((BFloat)array.getBValue(2)).floatValue(), arr[2]);
    }

    @Test
    public void testCloneIntArray() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneIntArray");
        testIntArray(new long[] {100, 2, 3}, (BIntArray) outputEmployeeEvents[0]);
        testIntArray(new long[] {1, 2, 3}, (BIntArray) outputEmployeeEvents[1]);
        testIntArray(new long[] {1, 2, 300}, (BIntArray) outputEmployeeEvents[2]);
    }

    private void testIntArray(long[] arr, BIntArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneFloatArray() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneFloatArray");
        testFloatArray(new double[] {100.5, 2.0, 3.0}, (BFloatArray) outputEmployeeEvents[0]);
        testFloatArray(new double[] {1.0, 2.0, 3.0}, (BFloatArray) outputEmployeeEvents[1]);
        testFloatArray(new double[] {1.0, 2.0, 300.5}, (BFloatArray) outputEmployeeEvents[2]);
    }

    private void testFloatArray(double[] arr, BFloatArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneStringArray() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneStringArray");
        testStringArray(new String[] {"XX", "B", "C"}, (BStringArray) outputEmployeeEvents[0]);
        testStringArray(new String[] {"A", "B", "C"}, (BStringArray) outputEmployeeEvents[1]);
        testStringArray(new String[] {"A", "B", "YY"}, (BStringArray) outputEmployeeEvents[2]);
    }

    private void testStringArray(String[] arr, BStringArray array) {
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(array.get(i), arr[i]);
        }
    }

    @Test
    public void testCloneUnionArray() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneUnionArray");
        testRefArray(new Object[] {100, "EE", 12.3}, (BRefValueArray) outputEmployeeEvents[0]);
        testRefArray(new Object[] {1, "EE", 12.3}, (BRefValueArray) outputEmployeeEvents[1]);
        testRefArray(new Object[] {1, "EE", 300.5}, (BRefValueArray) outputEmployeeEvents[2]);
    }

    @Test
    public void testCloneConstrainedJSON() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneConstrainedJSON");

        testConstrainedJSON((BMap) outputEmployeeEvents[0], "Charlos", 1, 300.5);
        testConstrainedJSON((BMap) outputEmployeeEvents[1], "Jane", 1, 300.5);
        testConstrainedJSON((BMap) outputEmployeeEvents[2], "Jane", 1, 400.5);
    }

    private void testConstrainedJSON(BMap outputEmployeeEvent, String name, int id, double salary) {
        Assert.assertEquals(((BInteger) outputEmployeeEvent.get("id")).intValue(), id);
        Assert.assertEquals(outputEmployeeEvent.get("name").stringValue(), name);
        Assert.assertEquals(((BFloat) outputEmployeeEvent.get("salary")).floatValue(), salary);
    }

    @Test
    public void testCloneTable() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneTable");
        Assert.assertNotNull(outputEmployeeEvents);

        Object[][] expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                new Object[]{3, "John", 400.50},
                };
        testValuesOnTable((BTable) outputEmployeeEvents[0], expectedValues);

        expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                };
        testValuesOnTable((BTable) outputEmployeeEvents[1], expectedValues);

        expectedValues = new Object[][]{
                new Object[]{1, "Jane", 300.50},
                new Object[]{2, "Anne", 100.50},
                new Object[]{3, "John", 400.50},
                };
        testValuesOnTable((BTable) outputEmployeeEvents[2], expectedValues);
    }

    private void testValuesOnTable(BTable table, Object[][] expectedValues) {
        int i = 0;
        while (table.hasNext()) {
            BMap<String, BValue> next = table.getNext();
            Assert.assertEquals(((BInteger) next.get("id")).intValue(), ((Integer)expectedValues[i][0]).intValue());
            Assert.assertEquals(next.get("name").stringValue(), expectedValues[i][1]);
            Assert.assertEquals(((BFloat)next.get("salary")).floatValue(), expectedValues[i][2]);
            i++;
        }
        Assert.assertEquals(expectedValues.length, i);
    }

    @Test
    public void testCloneMap() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "cloneMap");
        Assert.assertNotNull(outputEmployeeEvents);

        testCloneOnMaps(outputEmployeeEvents[0], "Charlos", 123, 21);
        testCloneOnMaps(outputEmployeeEvents[1], "Alex", 123, 21);
        testCloneOnMaps(outputEmployeeEvents[2], "Alex", 5000, 21);
    }

    private void testCloneOnMaps(BValue outputEmployeeEvent, String name, int id, int age) {
        BMap mapA, mapB, mapC, jsonVal;
        String tmpName;
        long tmpId, tmpAge;
        mapA = (BMap) outputEmployeeEvent;
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
}
