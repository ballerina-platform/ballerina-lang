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
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnydataType;
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

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Test cases for stamping Map type variables.
 *
 * @since 0.985.0
 */
public class MapStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/expressions/stamp/map-stamp-expr-test.bal");
    }

    //----------------------------- Map Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampIntMapToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampIntMapToRecord");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "IntRecord");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("a")).toString(), "1");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("b")).toString(), "2");
    }

    @Test
    public void testStampIntMapToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampIntMapToJSON");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getTag(), TypeTags.JSON_TAG);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("a")).toString(), "1");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("b")).toString(), "2");
    }

    @Test
    public void testStampIntMapToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampIntMapToAnydata");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getTag(), TypeTags.ANYDATA_TAG);
    }

    @Test
    public void testStampIntMapToIntMap() {

        Object results = BRunUtil.invoke(compileResult, "stampIntMapToIntMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals((((BMapType) mapValue.getType()).getConstrainedType().getTag()), TypeTags.INT_TAG);
    }

    @Test
    public void testStampIntMapToAnydataMap() {

        Object results = BRunUtil.invoke(compileResult, "stampIntMapToAnydataMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("a")).toString(), "1");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("b")).toString(), "2");
    }

    @Test
    public void testStampAnydataMapToIntMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToIntMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("a")).toString(), "1");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("b")).toString(), "2");
    }

    @Test
    public void testStampAnydataMapToStringMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToStringMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("firstName"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("firstName")).toString(), "mohan");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("lastName"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("lastName")).toString(), "raj");
    }

    @Test
    public void testStampAnydataToIntMapWithoutExplicitConstraintType() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToStringMapWithoutExplicitConstraintType");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("firstName"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("firstName")).toString(), "mohan");

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("lastName"))).getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("lastName")).toString(), "raj");
    }

    @Test
    public void testStampAnydataMapToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToRecord");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "Teacher");

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

    }

    @Test
    public void testStampAnydataMapToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToJSON");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BJsonType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

    }

    @Test
    public void testStampAnydataMapToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToAnydata");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

    }

    @Test
    public void testStampAnydataMapToSimilarOpenRecordMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToSimilarOpenRecordMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getName(), "Employee");
        Assert.assertEquals(getType(((Object) ((BMap) mapValue.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("age")))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(getType(((Object) ((BMap) mapValue.get(StringUtils.fromString("a"))).get(
                StringUtils.fromString("school")))).
                getClass(), BStringType.class);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Employee");
        Assert.assertEquals(getType(((Object) ((BMap) mapValue.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("age")))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(getType(((Object) ((BMap) mapValue.get(StringUtils.fromString("b"))).get(
                StringUtils.fromString("school")))).
                getClass(), BStringType.class);
    }

    @Test
    public void testStampAnydataMapToRecordMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToRecordMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getName(), "Teacher");
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("a"))).size(), 5);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Teacher");
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("b"))).size(), 5);
    }

    @Test
    public void testStampAnydataMapToJSONMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToJSONMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BJsonType.class);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getClass(), BMapType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getClass(), BMapType.class);
    }

    @Test
    public void testStampRecordMapToAnydataMap() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordMapToAnydataMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getClass(), BMapType.class);
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("a"))).size(), 5);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getClass(), BMapType.class);
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("b"))).size(), 5);
    }

    @Test
    public void testStampRecordMapToSimilarOpenRecordMap() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordMapToSimilarOpenRecordMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getName(), "Employee");
        Assert.assertEquals(
                getType(((BMap) mapValue.get(StringUtils.fromString("a"))).get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(
                getType(((BMap) mapValue.get(StringUtils.fromString("a"))).get(StringUtils.fromString("school"))).
                        getClass(), BStringType.class);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Employee");
        Assert.assertEquals(
                getType(((BMap) mapValue.get(StringUtils.fromString("b"))).get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(
                getType(((BMap) mapValue.get(StringUtils.fromString("b"))).get(StringUtils.fromString("school"))).
                        getClass(), BStringType.class);
    }

    @Test
    public void testStampRecordMapToJSONMap() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordMapToJSONMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BJsonType.class);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getClass(), BMapType.class);
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("a"))).size(), 5);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getClass(), BMapType.class);
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("b"))).size(), 5);
    }

    @Test
    public void testStampJSONMapToRecordMap() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONMapToRecordMap");
        BMap<String, Object> employee0 = (BMap<String, Object>) results;

        Assert.assertEquals(employee0.size(), 2);

        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Type constrainedType =
                ((ReferenceType) ((BMapType) employee0.getType()).getConstrainedType()).getReferredType();
        Assert.assertEquals(constrainedType.getClass(), BRecordType.class);
        Assert.assertEquals(constrainedType.getName(), "Employee");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("a"))).getName(), "Employee");
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("school"))).getClass(),
                BStringType.class);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("b"))).getName(), "Employee");
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("school"))).getClass(),
                BStringType.class);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("batch"))).getClass(),
                BStringType.class);
    }

    @Test
    public void testStampRecordTypeMultiDimensionMap() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordTypeMultiDimensionMap");
        BMap<String, Object> employee0 = (BMap<String, Object>) results;

        Assert.assertEquals(employee0.size(), 2);
        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) ((BMapType) employee0.getType()).getConstrainedType()).getConstrainedType().
                getName(), "Employee");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("aa"))).getClass(), BMapType.class);
        Type constrainedType =
                ((ReferenceType) ((BMapType) getType(
                        employee0.get(StringUtils.fromString("aa")))).getConstrainedType()).getReferredType();
        Assert.assertEquals(constrainedType.getClass(), BRecordType.class);
        Assert.assertEquals(constrainedType.getName(), "Employee");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("bb"))).getClass(), BMapType.class);
        constrainedType =
                ((ReferenceType) ((BMapType) getType(
                        employee0.get(StringUtils.fromString("bb")))).getConstrainedType()).getReferredType();
        Assert.assertEquals(constrainedType.getClass(), BRecordType.class);
        Assert.assertEquals(constrainedType.getName(), "Employee");

        Assert.assertEquals(
                ((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("a"))).getType()
                        .getName(),
                "Employee");
        Assert.assertEquals(
                getType((((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("a"))))
                        .get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(
                getType((((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("a"))))
                        .get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);
        Assert.assertEquals(
                getType((((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("a"))))
                        .get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(
                ((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("b"))).getType()
                        .getName(),
                "Employee");
        Assert.assertEquals(getType((((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(
                StringUtils.fromString("b")))).get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(
                getType((((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("b"))))
                        .get(StringUtils.fromString("school"))).getClass(), BStringType.class);
        Assert.assertEquals(
                getType((((BMap) ((BMap) employee0.get(StringUtils.fromString("bb"))).get(StringUtils.fromString("b"))))
                        .get(StringUtils.fromString("batch"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampAnydataToIntMultiDimensionMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToIntMultiDimensionMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(
                getType(((BMap) ((BMap) mapValue.get(StringUtils.fromString("a"))).get(StringUtils.fromString("aa")))
                        .get(StringUtils.fromString("aa"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(
                ((BMap) ((BMap) mapValue.get(StringUtils.fromString("a"))).get(StringUtils.fromString("aa")))
                        .get(StringUtils.fromString("aa")).toString(), "11");
    }

    @Test
    public void testStampIntToAnydataMultiDimensionMap() {

        Object results = BRunUtil.invoke(compileResult, "stampIntToAnydataMultiDimensionMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(((BMap) ((BMap) mapValue.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("aa"))).get(StringUtils.fromString("aa"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(
                ((BMap) ((BMap) mapValue.get(StringUtils.fromString("a"))).get(StringUtils.fromString("aa"))).get(
                        StringUtils.fromString("aa")).toString(), "11");
    }

    @Test
    public void testStampConstraintMapToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampConstraintMapToAnydata");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getClass(), BMapType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getClass(), BMapType.class);
    }

    @Test
    public void testStampConstraintMapToUnion() {

        Object results = BRunUtil.invoke(compileResult, "stampConstraintMapToUnion");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(
                ((ReferenceType) ((BMapType) mapValue.getType()).getConstrainedType()).getReferredType().getClass(),
                BRecordType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getName(), "Teacher");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getName(), "Teacher");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getClass(), BRecordType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Teacher");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getClass(), BRecordType.class);
    }

    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test
    public void testStampMapToRecordNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampMapToRecordNegative");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<string>' value cannot be converted to 'EmployeeClosedRecord': " +
                        "\n\t\tfield 'school' cannot be added to the closed record 'EmployeeClosedRecord'");
    }

    @AfterClass
    public void tearDown() {

        compileResult = null;
    }
}
