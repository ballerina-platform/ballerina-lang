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

import org.ballerinalang.core.model.types.BAnydataType;
import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BErrorType;
import org.ballerinalang.core.model.types.BJSONType;
import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.BRecordType;
import org.ballerinalang.core.model.types.BStringType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for stamping Record type variables.
 *
 * @since 0.985.0
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

        Assert.assertEquals(employee0.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampOpenRecordsNonAssignable() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampWithOpenRecordsNonAssignable");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 5);
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

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampClosedRecordWithClosedRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampClosedRecordWithClosedRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampRecordToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJSONType.class);

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
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BStringType.class);
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
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue0.get("age").getType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("emp").size(), 3);
        Assert.assertEquals(mapValue0.get("emp").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.get("emp").getType()).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    @Test
    public void testStampRecordToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToAnydata");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampFunctionReferenceWithOpenRecords() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampFunctionReferenceWithOpenRecords");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampFunctionReferenceWithArgs() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampFunctionReferenceWithArgs");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get("age").stringValue(), "23");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
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

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.get("address").getType()).getConstrainedType().getClass(),
                BAnydataType.class);

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
        Assert.assertEquals(((BMapType) mapValue.get("address").getType()).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV3() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV3");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithRecord");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.get("address").getType().getName(), "Address");
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV4() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV4");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployee");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.get("address").getType().getName(), "Address");
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV5() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV5");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployee");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.get("address").getType().getName(), "Address");
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV6() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV6");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithUnionRest");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BMapType.class);
    }

    @Test
    public void testStampNilTypeToOpenRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampNilTypeToOpenRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampRecordWithNilValues() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordWithNilValues");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertNull(employee0.get("school"));
    }

    @Test
    public void testStampRecordWithNilValuesV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordWithNilValuesV2");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertNull(employee0.get("school"));
    }

    @Test
    public void testStampExtendedRecordToRecordWithUnionV7() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampExtendedRecordToRecordWithUnionV7");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithRecord");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("address").getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.get("address").getType().getName(), "Address");
    }

    @Test
    public void testStampRecordToRecordWithNilValues() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToRecordWithNilValues");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithNilRecord");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertNull(mapValue.get("address"));
    }

    @Test
    public void testStampRecordToRecordWithOptionalFields() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToRecordWithOptionalFields");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 3);

        Assert.assertEquals(employee0.get("name").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("status").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("status").stringValue(), "single");
    }

    @Test
    public void testStampAnyRecordToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyRecordToRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(employee0.getType().getName(), "OpenEmployee");

        Assert.assertEquals(employee0.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("status").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("status").stringValue(), "single");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampComplexRecordToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampComplexRecordToJSON");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals((employee0.get("marks").getType().getClass()), BArrayType.class);
        Assert.assertEquals(((BArrayType) ((BArrayType) (employee0).get("marks").getType()).getElementType()).
                getElementType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(employee0.get("info").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.get("info").getType()).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test
    public void testStampOpenRecordToMap() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampOpenRecordToMap");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Teacher' value cannot be converted to 'map<string>'");
    }

    @Test
    public void testStampOpenRecordToTypeClosedRecordNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampOpenRecordToTypeClosedRecordNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Teacher' value cannot be converted to 'NonAcademicStaff'");
    }

    @Test
    public void testStampWithOpenRecordsNonAssignableNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampWithOpenRecordsNonAssignableNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Employee' value cannot be converted to 'Teacher'");
    }

    @Test
    public void testStampOpenRecordWithInvalidValues() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampOpenRecordWithInvalidValues");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Employee' value cannot be converted to 'Teacher'");
    }
}
