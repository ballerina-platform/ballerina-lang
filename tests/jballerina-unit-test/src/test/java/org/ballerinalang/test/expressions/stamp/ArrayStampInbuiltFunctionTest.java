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
import org.ballerinalang.core.model.types.BJSONType;
import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.BRecordType;
import org.ballerinalang.core.model.types.BStringType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

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

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToAnydataArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue1.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampAnydataToRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Teacher");
        Assert.assertEquals(mapValue1.getType().getName(), "Teacher");
    }

    @Test
    public void testStampAnydataToSimilarOpenRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToSimilarOpenRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampRecordToSimilarOpenRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToSimilarOpenRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampConstraintArrayToJSONArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampConstraintArrayToJSONArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJSONType.class);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJSONType.class);
        Assert.assertEquals((mapValue1.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampRecordToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToAnydata");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) (mapValue0).getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BMapType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) (mapValue1).getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampRecordToAnydataArrayV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToAnydataArrayV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue1.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampAnydataArrayToUnion() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataArrayToUnion");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampArrayValueToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampArrayValueToTuple");
        Assert.assertEquals(results.length, 2);

        BValue tupleValue1 = results[0];
        BValue tupleValue2 = results[1];

        Assert.assertEquals(tupleValue1.getType().getClass(), BRecordType.class);
        Assert.assertEquals(tupleValue1.getType().getName(), "Employee");

        Assert.assertEquals(tupleValue2.getType().getClass(), BRecordType.class);
        Assert.assertEquals(tupleValue2.getType().getName(), "Student");

        Assert.assertEquals(((BMap) tupleValue2).size(), 4);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).stringValue(), "Raja");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).stringValue(), "single");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).stringValue(), "LK2014");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).stringValue(), "Hindu College");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue1).getMap().get("school")).stringValue(), "Royal College");
        Assert.assertEquals(((BValue) ((BMap) tupleValue1).getMap().get("school")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testStampJSONToBasicArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToBasicArray");
        BValueArray valueArray = (BValueArray) results[0];

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(((BValueArray) results[0]).elementType.getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) results[0]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) results[0]).getInt(3), 4);
    }

    @Test
    public void testStampAnydataToBasicArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToBasicArray");
        BValueArray valueArray = (BValueArray) results[0];

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(((BValueArray) results[0]).elementType.getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) results[0]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) results[0]).getInt(3), 4);
    }

    @Test
    public void testStampAnydataArrayToBasicArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataArrayToBasicArray");
        BValueArray valueArray = (BValueArray) results[0];

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(((BValueArray) results[0]).elementType.getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) results[0]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) results[0]).getInt(3), 4);
    }

    @Test
    public void testStampJSONArrayToBasicArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONArrayToBasicArray");
        BValueArray valueArray = (BValueArray) results[0];

        Assert.assertEquals(valueArray.size(), 4);
        Assert.assertEquals(((BValueArray) results[0]).elementType.getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) results[0]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) results[0]).getInt(3), 4);
    }

    @Test
    public void testStampBasicArrayToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToJSON");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 2);
        Assert.assertEquals(((BInteger) results[2]).intValue(), 3);
        Assert.assertEquals(((BInteger) results[3]).intValue(), 4);
    }

    @Test
    public void testStampBasicArrayToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToAnydata");
        Assert.assertEquals(((BValueArray) results[0]).elementType.getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) results[0]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) results[0]).getInt(3), 4);
    }

    @Test
    public void testStampBasicArrayToAnydataArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToAnydataArray");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 2);
        Assert.assertEquals(((BInteger) results[2]).intValue(), 3);
        Assert.assertEquals(((BInteger) results[3]).intValue(), 4);
    }

    @Test
    public void testStampBasicArrayToJSONArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToJSONArray");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 2);
        Assert.assertEquals(((BInteger) results[2]).intValue(), 3);
        Assert.assertEquals(((BInteger) results[3]).intValue(), 4);
    }

    @Test
    public void testStampBasicArrayToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToTuple");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 2);
    }

    @Test
    public void testStampAnydataBasicArrayToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataBasicArrayToTuple");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 2);
    }

    @Test
    public void testStampBasicArrayToAnydataTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToAnydataTuple");
        Assert.assertEquals(((BInteger) results[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).intValue(), 2);
    }

    @Test
    public void testStampBasicArrayToBasicArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicArrayToBasicArray");
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
    }

    @Test
    public void testStampBasicMapArrayToAnydataMapArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampBasicMapArrayToAnydataMapArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue1.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void teststampRecordArrayToJsonArray() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordArrayToJsonArray");
        BMap<String, BValue> jsonValue1 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> jsonValue2 = (BMap<String, BValue>) results[1];
        Assert.assertEquals(((BMapType) jsonValue1.getType()).getConstrainedType().getClass(), BJSONType.class);
        Assert.assertEquals(((BMapType) jsonValue2.getType()).getConstrainedType().getClass(), BJSONType.class);
        Assert.assertEquals(jsonValue1.get("name").stringValue(), "Waruna");
        Assert.assertEquals(jsonValue2.get("name").stringValue(), "Heshitha");
        Assert.assertEquals(((BInteger) jsonValue1.get("age")).intValue(), 10);
        Assert.assertEquals(((BInteger) jsonValue2.get("age")).intValue(), 15);
    }
}
