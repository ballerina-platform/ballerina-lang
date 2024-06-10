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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnydataType;
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
 * Test cases for stamping Array type variables.
 *
 * @since 0.985.0
 */
public class ArrayStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/expressions/stamp/array-stamp-expr-test.bal");
    }

    //----------------------------- Array Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampRecordToAnydataArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampRecordToAnydataArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(getType((mapValue0)).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals(getType((mapValue1)).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue1.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampAnydataToRecordArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataToRecordArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Teacher");
        Assert.assertEquals(mapValue1.getType().getName(), "Teacher");
    }

    @Test
    public void testStampAnydataToSimilarOpenRecordArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataToSimilarOpenRecordArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(getType(mapValue1.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(getType(mapValue1.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampRecordToSimilarOpenRecordArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampRecordToSimilarOpenRecordArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(getType(mapValue1.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(getType(mapValue1.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampConstraintArrayToJSONArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampConstraintArrayToJSONArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(getType(mapValue0).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJsonType.class);
        Assert.assertEquals((mapValue0).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(
                getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(getType(mapValue1).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJsonType.class);
        Assert.assertEquals((mapValue1).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(
                getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

    }

    @Test
    public void testStampRecordToAnydata() {

        Object arr = BRunUtil.invoke(compileResult, "stampRecordToAnydata");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(getType((mapValue0)).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) (mapValue0).getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals(getType((mapValue1)).getClass(), BMapType.class);
        Assert.assertEquals(getType((mapValue1)).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) (mapValue1).getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampRecordToAnydataArrayV2() {

        Object arr = BRunUtil.invoke(compileResult, "stampRecordToAnydataArrayV2");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(getType(mapValue0).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals(getType(mapValue1).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue1.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampAnydataArrayToUnion() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataArrayToUnion");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(getType(mapValue1.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(getType(mapValue1.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampArrayValueToTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampArrayValueToTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);

        Object tupleValue1 = results.get(0);
        Object tupleValue2 = results.get(1);

        Assert.assertEquals(getType(tupleValue1).getClass(), BRecordType.class);
        Assert.assertEquals(getType(tupleValue1).getName(), "Employee");

        Assert.assertEquals(getType(tupleValue2).getClass(), BRecordType.class);
        Assert.assertEquals(getType(tupleValue2).getName(), "Student");

        Assert.assertEquals(((BMap) tupleValue2).size(), 4);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("name"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("status"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue1).get(StringUtils.fromString("school")).toString(), "Royal College");
        Assert.assertEquals(getType(((BMap) tupleValue1).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);
    }

    @Test
    public void testStampJSONToBasicArray() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToBasicArray");
        BArray valueArray = (BArray) results;

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(valueArray.getElementType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(valueArray.getInt(0), 1);
        Assert.assertEquals(valueArray.getInt(1), 2);
        Assert.assertEquals(valueArray.getInt(2), 3);
        Assert.assertEquals(valueArray.getInt(3), 4);
    }

    @Test
    public void testStampAnydataToBasicArray() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToBasicArray");
        BArray valueArray = (BArray) results;

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(valueArray.getElementType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(valueArray.getInt(0), 1);
        Assert.assertEquals(valueArray.getInt(1), 2);
        Assert.assertEquals(valueArray.getInt(2), 3);
        Assert.assertEquals(valueArray.getInt(3), 4);
    }

    @Test
    public void testStampAnydataArrayToBasicArray() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataArrayToBasicArray");
        BArray valueArray = (BArray) results;

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(valueArray.getElementType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(valueArray.getInt(0), 1);
        Assert.assertEquals(valueArray.getInt(1), 2);
        Assert.assertEquals(valueArray.getInt(2), 3);
        Assert.assertEquals(valueArray.getInt(3), 4);
    }

    @Test
    public void testStampJSONArrayToBasicArray() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONArrayToBasicArray");
        BArray valueArray = (BArray) results;

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(valueArray.getElementType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(valueArray.getInt(0), 1);
        Assert.assertEquals(valueArray.getInt(1), 2);
        Assert.assertEquals(valueArray.getInt(2), 3);
        Assert.assertEquals(valueArray.getInt(3), 4);
    }

    @Test
    public void testStampBasicArrayToJSON() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicArrayToJSON");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
        Assert.assertEquals(results.get(2), 3L);
        Assert.assertEquals(results.get(3), 4L);
    }

    @Test
    public void testStampBasicArrayToAnydata() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicArrayToAnydata");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
        Assert.assertEquals(results.get(2), 3L);
        Assert.assertEquals(results.get(3), 4L);
    }

    @Test
    public void testStampBasicArrayToAnydataArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicArrayToAnydataArray");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
        Assert.assertEquals(results.get(2), 3L);
        Assert.assertEquals(results.get(3), 4L);
    }

    @Test
    public void testStampBasicArrayToJSONArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicArrayToJSONArray");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
        Assert.assertEquals(results.get(2), 3L);
        Assert.assertEquals(results.get(3), 4L);
    }

    @Test
    public void testStampBasicArrayToTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicArrayToTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
    }

    @Test
    public void testStampAnydataBasicArrayToTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataBasicArrayToTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
    }

    @Test
    public void testStampBasicArrayToAnydataTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicArrayToAnydataTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
    }

    @Test
    public void testStampBasicArrayToBasicArray() {

        Object results = BRunUtil.invoke(compileResult, "stampBasicArrayToBasicArray");
        Assert.assertEquals(((BArray) results).getInt(0), 1);
        Assert.assertEquals(((BArray) results).getInt(1), 2);
    }

    @Test
    public void testStampBasicMapArrayToAnydataMapArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampBasicMapArrayToAnydataMapArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> mapValue0 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> mapValue1 = (BMap<BString, Object>) results.get(1);

        Assert.assertEquals(getType(mapValue0).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals(getType(mapValue1).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue1.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void teststampRecordArrayToJsonArray() {
        Object arr = BRunUtil.invoke(compileResult, "stampRecordArrayToJsonArray");
        BArray results = (BArray) arr;
        BMap<BString, Object> jsonValue1 = (BMap<BString, Object>) results.get(0);
        BMap<BString, Object> jsonValue2 = (BMap<BString, Object>) results.get(1);
        Assert.assertEquals(((BMapType) jsonValue1.getType()).getConstrainedType().getClass(), BJsonType.class);
        Assert.assertEquals(((BMapType) jsonValue2.getType()).getConstrainedType().getClass(), BJsonType.class);
        Assert.assertEquals(jsonValue1.get(StringUtils.fromString("name")).toString(), "Waruna");
        Assert.assertEquals(jsonValue2.get(StringUtils.fromString("name")).toString(), "Heshitha");
        Assert.assertEquals((jsonValue1.get(StringUtils.fromString("age"))), 10L);
        Assert.assertEquals((jsonValue2.get(StringUtils.fromString("age"))), 15L);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
