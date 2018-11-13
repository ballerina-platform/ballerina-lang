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
package org.ballerinalang.test.expressions.stamp;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnydataType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for stamping Record type variables.
 *
 * @since 0.983.0
 */
public class RecordStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/record-stamp-expr-test.bal");
    }


    //------------------------Record Stamp Test cases ----------------------------------------------------

    @Test
    public void testStampOpenRecords() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampWithOpenRecords");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampOpenRecordsNonAssignable() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampWithOpenRecordsNonAssignable");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 3);
        Assert.assertEquals((employee0.getType()).toString(), "Teacher");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

    }

    @Test
    public void testStampClosedRecordWithOpenRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampClosedRecordWithOpenRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampRecordToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampRecordToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToMap");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "John");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnydataType.class);

    }

    @Test
    public void testStampRecordToMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "John");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BStringType.class);

    }

    @Test
    public void testStampRecordToMapV3() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToMapV3");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue0.get("age").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(((BMap) mapValue0.get("emp")).size(), 3);
        Assert.assertEquals(mapValue0.get("emp").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampRecordToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToAnydata");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampFunctionReferenceWithOpenRecords() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampFunctionReferenceWithOpenRecords");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampOpenRecordToTypeClosedRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampOpenRecordToTypeClosedRecord");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);


        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "NonAcademicStaff");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampExtendedRecordToOpenRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecord");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);


        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "Employee");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV2");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);


        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithMap");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType)mapValue.get("address").getType()).getConstrainedType().getClass(),
                BAnydataType.class);
    }


    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: incompatible stamp operation: 'Teacher' value " +
                    "cannot be stamped as 'map<string>'.*")
    public void testStampOpenRecordToMap() {
        BRunUtil.invoke(compileResult, "stampOpenRecordToMap");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: incompatible stamp operation: 'Teacher' value cannot be " +
                    "stamped as 'NonAcademicStaff'.*")
    public void testStampOpenRecordToTypeClosedRecordNegative() {
        BRunUtil.invoke(compileResult, "stampOpenRecordToTypeClosedRecordNegative");
    }
}

