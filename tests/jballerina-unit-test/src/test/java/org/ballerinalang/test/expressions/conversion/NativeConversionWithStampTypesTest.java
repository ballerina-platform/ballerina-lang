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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BTupleType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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

        Object arr = BRunUtil.invoke(compileResult, "testConvertStampRecordToRecord");
        BArray results = (BArray) arr;
        BMap<String, Object> person = (BMap<String, Object>) results.get(0);
        BMap<String, Object> employee = (BMap<String, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "Watson");
        Assert.assertEquals(employee.get(StringUtils.fromString("name")).toString(), "Waruna");

        Assert.assertEquals(person.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(employee.get(StringUtils.fromString("age")).toString(), "30");

        Assert.assertEquals(person.get(StringUtils.fromString("school")).toString(), "ABC College");
        Assert.assertEquals(employee.get(StringUtils.fromString("school")).toString(), "ABC College");
    }

    @Test(description = "Test converting a record into a json and check previous values are not changed")
    public void testConvertStampRecordToJSON() {

        Object arr = BRunUtil.invoke(compileResult, "testConvertStampRecordToJSON");
        BArray results = (BArray) arr;
        BMap<String, Object> employee = (BMap<String, Object>) results.get(0);
        BMap<String, Object> json = (BMap<String, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(json.getType().getClass(), BMapType.class);

        Assert.assertEquals(employee.size(), 4);
        Assert.assertEquals(json.size(), 4);

        Assert.assertEquals(employee.get(StringUtils.fromString("name")).toString(), "John");
        Assert.assertEquals(json.get(StringUtils.fromString("name")).toString(), "Waruna");

        Assert.assertEquals(employee.get(StringUtils.fromString("school")).toString(), "DEF College");
        Assert.assertEquals(json.get(StringUtils.fromString("school")).toString(), "ABC College");
    }

    @Test(description = "Test converting a record into a map and check previous values are not changed")
    public void testConvertStampRecordToMap() {

        Object arr = BRunUtil.invoke(compileResult, "testConvertStampRecordToMap");
        BArray results = (BArray) arr;
        BMap<String, Object> employee = (BMap<String, Object>) results.get(0);
        BMap<String, Object> map = (BMap<String, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(map.getType().getClass(), BMapType.class);

        Assert.assertEquals(employee.size(), 4);
        Assert.assertEquals(map.size(), 4);

        Assert.assertEquals(employee.get(StringUtils.fromString("name")).toString(), "Mike");
        Assert.assertEquals(map.get(StringUtils.fromString("name")).toString(), "Waruna");
    }

    @Test(description = "Test converting a tuple into a map and check previous values are not changed")
    public void testConvertStampTupleToMap() {

        Object arr = BRunUtil.invoke(compileResult, "testConvertStampTupleToMap");
        BArray results = (BArray) arr;
        BArray original = (BArray) results.get(0);
        BArray converted = (BArray) results.get(1);

        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(original.getValues().length, 2);
        Assert.assertEquals(converted.getValues().length, 2);

        Assert.assertEquals(converted.getType().getClass(), BTupleType.class);

        Assert.assertEquals(original.getValues()[0].toString(), "Vinod");
        Assert.assertEquals(converted.getValues()[0].toString(), "Chathura");

        BMap<String, Object> originalMap = ((BMap<String, Object>) original.getValues()[1]);
        BMap<String, Object> convertedMap = ((BMap<String, Object>) converted.getValues()[1]);

        Assert.assertEquals(originalMap.get(StringUtils.fromString("school")).toString(), "ABC College");
        Assert.assertEquals(convertedMap.get(StringUtils.fromString("school")).toString(), "ABC College");
    }

    @Test
    public void testConvertMapJsonWithDecimalToOpenRecord() {
        BRunUtil.invoke(compileResult, "testConvertMapJsonWithDecimalToOpenRecord");
    }

    @Test
    public void testConvertMapJsonWithDecimalUnionTarget() {
        BRunUtil.invoke(compileResult, "testConvertMapJsonWithDecimalUnionTarget");
    }

    @Test
    void testConvertToUnionWithActualType() {
        BRunUtil.invoke(compileResult, "testConvertToUnionWithActualType");
    }
}
