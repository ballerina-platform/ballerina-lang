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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStringType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

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

        Object results = BRunUtil.invoke(compileResult, "stampWithOpenRecords");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get(StringUtils.fromString("age")).toString(), "25");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampOpenRecordsNonAssignable() {

        Object results = BRunUtil.invoke(compileResult, "stampWithOpenRecordsNonAssignable");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;

        Assert.assertEquals(employee0.size(), 5);
        Assert.assertEquals((employee0.getType()).toString(), "Teacher");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");
    }

    @Test
    public void testStampClosedRecordWithOpenRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampClosedRecordWithOpenRecord");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;

        Assert.assertEquals(employee0.size(), 4);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampClosedRecordWithClosedRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampClosedRecordWithClosedRecord");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;

        Assert.assertEquals(employee0.size(), 4);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampRecordToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToJSON");
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJsonType.class);

        Assert.assertEquals((mapValue0).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0).get(StringUtils.fromString("school")).toString(),
                "Hindu College");
        Assert.assertEquals(getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);
    }

    @Test
    public void testStampRecordToMap() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToMap");
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results;

        Assert.assertEquals((mapValue0).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("name")).toString(), "John");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampRecordToMapV2() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToMapV2");
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results;

        Assert.assertEquals((mapValue0).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("name")).toString(), "John");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

    }

    @Test
    public void testStampRecordToMapV3() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToMapV3");
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results;

        Assert.assertEquals((mapValue0).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

        Assert.assertEquals(((BMap) mapValue0.get(StringUtils.fromString("emp"))).size(), 3);
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("emp"))).getClass(), BMapType.class);
        Assert.assertEquals(
                ((BMapType) getType(mapValue0.get(StringUtils.fromString("emp")))).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    @Test
    public void testStampRecordToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToAnydata");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampFunctionReferenceWithOpenRecords() {

        Object results = BRunUtil.invoke(compileResult, "stampFunctionReferenceWithOpenRecords");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get(StringUtils.fromString("age")).toString(), "25");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampFunctionReferenceWithArgs() {

        Object results = BRunUtil.invoke(compileResult, "stampFunctionReferenceWithArgs");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get(StringUtils.fromString("age")).toString(), "23");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampOpenRecordToTypeClosedRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampOpenRecordToTypeClosedRecord");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "NonAcademicStaff");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampExtendedRecordToOpenRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecord");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "Employee");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BMapType.class);
        Assert.assertEquals(
                ((BMapType) getType(mapValue.get(StringUtils.fromString("address")))).getConstrainedType().getClass(),
                BAnydataType.class);

    }

    @Test
    public void testStampExtendedRecordToOpenRecordV2() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV2");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithMap");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BMapType.class);
        Assert.assertEquals(
                ((BMapType) getType(mapValue.get(StringUtils.fromString("address")))).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV3() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV3");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithRecord");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BRecordType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getName(), "Address");
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV4() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV4");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployee");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BRecordType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getName(), "Address");
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV5() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV5");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployee");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BRecordType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getName(), "Address");
    }

    @Test
    public void testStampExtendedRecordToOpenRecordV6() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToOpenRecordV6");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithUnionRest");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BMapType.class);
    }

    @Test
    public void testStampNilTypeToOpenRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampNilTypeToOpenRecord");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get(StringUtils.fromString("age")).toString(), "25");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampRecordWithNilValues() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordWithNilValues");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertNull(employee0.get(StringUtils.fromString("school")));
    }

    @Test
    public void testStampRecordWithNilValuesV2() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordWithNilValuesV2");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertNull(employee0.get(StringUtils.fromString("school")));
    }

    @Test
    public void testStampExtendedRecordToRecordWithUnionV7() {

        Object results = BRunUtil.invoke(compileResult, "stampExtendedRecordToRecordWithUnionV7");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithRecord");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getClass(), BRecordType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("address"))).getName(), "Address");
    }

    @Test
    public void testStampRecordToRecordWithNilValues() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToRecordWithNilValues");
        BMap<BString, Object> mapValue = (BMap<BString, Object>) results;

        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "ExtendedEmployeeWithNilRecord");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertNull(mapValue.get(StringUtils.fromString("address")));
    }

    @Test
    public void testStampRecordToRecordWithOptionalFields() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordToRecordWithOptionalFields");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;

        Assert.assertEquals(employee0.size(), 3);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("name"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("name")).toString(), "Raja");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("status")).toString(), "single");
    }

    @Test
    public void testStampAnyRecordToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampAnyRecordToRecord");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(employee0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(employee0.getType().getName(), "OpenEmployee");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get(StringUtils.fromString("age")).toString(), "25");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("status")).toString(), "single");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampComplexRecordToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampComplexRecordToJSON");
        BMap<BString, Object> employee0 = (BMap<BString, Object>) results;


        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("marks"))).getClass(), BArrayType.class);
        Assert.assertEquals(
                ((BArrayType) getType(employee0.get(StringUtils.fromString("marks")))).getElementType().getTag(),
                TypeTags.ANYDATA_TAG);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("info"))).getClass(), BMapType.class);
        Assert.assertEquals(
                ((BMapType) getType(employee0.get(StringUtils.fromString("info")))).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test
    public void testStampOpenRecordToMap() {

        Object results = BRunUtil.invoke(compileResult, "stampOpenRecordToMap");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Teacher' value cannot be converted to 'StringMap': " +
                        "\n\t\tmap field 'age' should be of type 'string', found '25'");
    }

    @Test
    public void testStampOpenRecordToTypeClosedRecordNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampOpenRecordToTypeClosedRecordNegative");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Teacher' value cannot be converted to 'NonAcademicStaff': \n\t\tvalue of field 'postalCode' " +
                        "adding to the record 'NonAcademicStaff' should be of type 'string', found '600'");
    }

    @Test
    public void testStampWithOpenRecordsNonAssignableNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampWithOpenRecordsNonAssignableNegative");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Employee' value cannot be converted to 'Teacher': " +
                        "\n\t\tmissing required field 'school' of type 'string' in record 'Teacher'" +
                        "\n\t\tmissing required field 'age' of type 'int' in record 'Teacher'");
    }

    @Test
    public void testStampOpenRecordWithInvalidValues() {

        Object results = BRunUtil.invoke(compileResult, "stampOpenRecordWithInvalidValues");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results).getDetails()).get(
                        StringUtils.fromString("message")).toString(),
                "'Employee' value cannot be converted to 'Teacher': " +
                        "\n\t\tfield 'school' in record 'Teacher' should be of type 'string', found '789'");
    }

    @AfterClass
    public void tearDown() {

        compileResult = null;
    }
}
