/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.conversion;

import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.BTupleType;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for converting types which can be stamped.
 *
 * @since 0.985.0
 */
public class NativeConversionWithStampTypesTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion-stampable-values.bal");
    }

    @Test(description = "Test converting a record which can be stamp, into a another record and check previous values "
            + "are not changed")
    public void testConvertStampRecordToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "testConvertStampRecordToRecord");
        BMap<String, BValue> person = (BMap<String, BValue>) results[0];
        BMap<String, BValue> employee = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(person.get("name").stringValue(), "Watson");
        Assert.assertEquals(employee.get("name").stringValue(), "Waruna");

        Assert.assertEquals(person.get("age").stringValue(), "25");
        Assert.assertEquals(employee.get("age").stringValue(), "30");

        Assert.assertEquals(person.get("school").stringValue(), "ABC College");
        Assert.assertEquals(employee.get("school").stringValue(), "ABC College");
    }

    @Test(description = "Test converting a record into a json and check previous values are not changed")
    public void testConvertStampRecordToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "testConvertStampRecordToJSON");
        BMap<String, BValue> employee = (BMap<String, BValue>) results[0];
        BMap<String, BValue> json = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);
        Assert.assertEquals(json.getType().getClass(), BMapType.class);

        Assert.assertEquals((employee.getMap()).size(), 4);
        Assert.assertEquals((json.getMap()).size(), 4);

        Assert.assertEquals(employee.get("name").stringValue(), "John");
        Assert.assertEquals(json.get("name").stringValue(), "Waruna");

        Assert.assertEquals(employee.get("school").stringValue(), "DEF College");
        Assert.assertEquals(json.get("school").stringValue(), "ABC College");
    }

    @Test(description = "Test converting a record into a map and check previous values are not changed")
    public void testConvertStampRecordToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "testConvertStampRecordToMap");
        BMap<String, BValue> employee = (BMap<String, BValue>) results[0];
        BMap<String, BValue> map = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);
        Assert.assertEquals(map.getType().getClass(), BMapType.class);

        Assert.assertEquals((employee.getMap()).size(), 4);
        Assert.assertEquals((map.getMap()).size(), 4);

        Assert.assertEquals(employee.get("name").stringValue(), "Mike");
        Assert.assertEquals(map.get("name").stringValue(), "Waruna");
    }

    @Test(description = "Test converting a tuple into a map and check previous values are not changed")
    public void testConvertStampTupleToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "testConvertStampTupleToMap");
        BValueArray original = (BValueArray) results[0];
        BValueArray converted = (BValueArray) results[1];

        Assert.assertEquals(results.length, 2);
        Assert.assertEquals(original.getValues().length, 2);
        Assert.assertEquals(converted.getValues().length, 2);

        ((BMap) ((BValueArray) results[0]).getValues()[1]).getMap();
        Assert.assertEquals(converted.getType().getClass(), BTupleType.class);

        Assert.assertEquals(original.getValues()[0].stringValue(), "Vinod");
        Assert.assertEquals(converted.getValues()[0].stringValue(), "Chathura");

        BMap<String, BValue> originalMap = ((BMap<String, BValue>) original.getValues()[1]);
        BMap<String, BValue> convertedMap = ((BMap<String, BValue>) converted.getValues()[1]);

        Assert.assertEquals(originalMap.get("school").toString(), "ABC College");
        Assert.assertEquals(convertedMap.get("school").stringValue(), "ABC College");
    }
}
