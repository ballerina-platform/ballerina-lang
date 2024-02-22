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
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeHelper;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStringType;
import org.ballerinalang.test.BAssertUtil;
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

        BAssertUtil.assertTypeClass(mapValue.getType(), BRecordType.class);
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

        BAssertUtil.assertTypeClass(mapValue.getType(), BMapType.class);
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

        BAssertUtil.assertTypeClass(mapValue.getType(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getTag(), TypeTags.ANYDATA_TAG);
    }

    @Test
    public void testStampIntMapToIntMap() {
        BRunUtil.invoke(compileResult, "stampIntMapToIntMap");
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

        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("firstName"))), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("firstName")).toString(), "mohan");

        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("lastName"))), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("lastName")).toString(), "raj");
    }

    @Test
    public void testStampAnydataToIntMapWithoutExplicitConstraintType() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToStringMapWithoutExplicitConstraintType");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("firstName"))), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("firstName")).toString(), "mohan");

        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("lastName"))), BStringType.class);
        Assert.assertEquals(mapValue.get(StringUtils.fromString("lastName")).toString(), "raj");
    }

    @Test
    public void testStampAnydataMapToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToRecord");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        BAssertUtil.assertTypeClass(mapValue.getType(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "Teacher");

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("name"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("status"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("batch"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("school")).toString(), "Hindu College");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("school"))), BStringType.class);
    }

    @Test
    public void testStampAnydataMapToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToJSON");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        BAssertUtil.assertTypeClass(mapValue.getType(), BMapType.class);
        BAssertUtil.assertTypeClass(TypeHelper.typeConstraint(mapValue.getType()), BJsonType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("name"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("status"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("batch")).toString(), "LK2014");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("batch"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("school")).toString(), "Hindu College");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("school"))), BStringType.class);
    }

    @Test
    public void testStampAnydataMapToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToAnydata");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        BAssertUtil.assertTypeClass(mapValue.getType(), BMapType.class);
        BAssertUtil.assertTypeClass(TypeHelper.typeConstraint(mapValue.getType()), BAnydataType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("name"))), BStringType.class);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("status"))), BStringType.class);
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
        BAssertUtil.assertTypeClass(getType(((BMap) mapValue.get(StringUtils.fromString("a"))).get(
                StringUtils.fromString("school"))), BStringType.class);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Employee");
        Assert.assertEquals(getType(((Object) ((BMap) mapValue.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("age")))).getTag(),
                TypeTags.INT_TAG);
        BAssertUtil.assertTypeClass(getType(((Object) ((BMap) mapValue.get(StringUtils.fromString("b"))).get(
                StringUtils.fromString("school")))), BStringType.class);
    }

    @Test
    public void testStampAnydataMapToRecordMap() {
        BRunUtil.invoke(compileResult, "stampAnydataMapToRecordMap");
    }

    @Test
    public void testStampAnydataMapToJSONMap() {
        BRunUtil.invoke(compileResult, "stampAnydataMapToJSONMap");
    }

    @Test
    public void testStampRecordMapToAnydataMap() {

        Object results = BRunUtil.invoke(compileResult, "stampRecordMapToAnydataMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("a"))), BMapType.class);
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("a"))).size(), 5);
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("b"))), BMapType.class);
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
        BAssertUtil.assertTypeClass(getType(((BMap) mapValue.get(StringUtils.fromString("a"))).get(
                StringUtils.fromString("school"))), BStringType.class);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Employee");
        Assert.assertEquals(
                getType(((BMap) mapValue.get(StringUtils.fromString("b"))).get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        BAssertUtil.assertTypeClass(getType(((BMap) mapValue.get(StringUtils.fromString("b"))).get(
                StringUtils.fromString("school"))), BStringType.class);
    }

    @Test
    public void testStampRecordMapToJSONMap() {
        BRunUtil.invoke(compileResult, "stampRecordMapToJSONMap");
    }

    @Test
    public void testStampJSONMapToRecordMap() {
        BRunUtil.invoke(compileResult, "stampJSONMapToRecordMap");
    }

    @Test
    public void testStampRecordTypeMultiDimensionMap() {
        BRunUtil.invoke(compileResult, "stampRecordTypeMultiDimensionMap");
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

        BAssertUtil.assertTypeClass(mapValue.getType(), BMapType.class);
        BAssertUtil.assertTypeClass(TypeHelper.typeConstraint(mapValue.getType()), BAnydataType.class);
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("a"))), BMapType.class);
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("b"))), BMapType.class);
    }

    @Test
    public void testStampConstraintMapToUnion() {

        Object results = BRunUtil.invoke(compileResult, "stampConstraintMapToUnion");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        BAssertUtil.assertTypeClass(mapValue.getType(), BMapType.class);
        BAssertUtil.assertTypeClass(TypeUtils.getImpliedType(TypeHelper.typeConstraint(mapValue.getType())),
                BRecordType.class);
        Assert.assertEquals(TypeHelper.typeConstraint(mapValue.getType()).getName(), "Teacher");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getName(), "Teacher");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("a"))), BRecordType.class);
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Teacher");
        BAssertUtil.assertTypeClass(getType(mapValue.get(StringUtils.fromString("b"))), BRecordType.class);
    }

    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test
    public void testStampMapToRecordNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampMapToRecordNegative");
        Object error = results;

        BAssertUtil.assertTypeClass(getType(error), BErrorType.class);
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
