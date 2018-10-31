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
package org.ballerinalang.test.expressions.seal;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for seal expressions.
 *
 * @since 0.983.0
 */
public class SealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/seal-expr-test.bal");
    }

    @Test
    public void testSealOpenRecords() {

        BValue[] results = BRunUtil.invoke(compileResult, "testSealWithOpenRecords");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getClass(), BAnyType.class);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnyType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testSealOpenRecordsNonAssignable() {

        BValue[] results = BRunUtil.invoke(compileResult, "testSealWithOpenRecordsNonAssignable");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 3);
        Assert.assertEquals((employee0.getType()).toString(), "Teacher");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

    }

    @Test
    public void testSealClosedRecordWithOpenRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "testSealClosedRecordWithOpenRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnyType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testSealIntMapToAnyMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealIntMapToAnyMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.get("a").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue.get("a").stringValue(), "1");

        Assert.assertEquals(mapValue.get("b").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue.get("b").stringValue(), "2");
    }

    @Test
    public void testSealAnyMapToIntMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToIntMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.get("a").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("a").stringValue(), "1");

        Assert.assertEquals(mapValue.get("b").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("b").stringValue(), "2");
    }

    @Test
    public void testSealAnyMapToStringMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToStringMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.get("firstName").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("firstName").stringValue(), "mohan");

        Assert.assertEquals(mapValue.get("lastName").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("lastName").stringValue(), "raj");
    }

    @Test
    public void testSealAnyToIntMultiDimensionMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyToIntMultiDimensionMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);
        Assert.assertEquals(mapValue.getMap().size(), 2);

        Assert.assertEquals(((BValue) ((BMap) ((BMap) mapValue.getMap().get("a")).getMap().get("aa")).
                getMap().get("aa")).getType().getName(), "int");
        Assert.assertEquals(((BValue) ((BMap) ((BMap) mapValue.getMap().get("a")).getMap().get("aa")).
                getMap().get("aa")).stringValue(), "11");
    }

    @Test
    public void testSealIntToAnyMultiDimensionMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealIntToAnyMultiDimensionMap");
        BMap<String, BValue> mapVaue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapVaue.size(), 2);
        Assert.assertEquals(mapVaue.getMap().size(), 2);

        Assert.assertEquals(((BValue) ((BMap) ((BMap) mapVaue.getMap().get("a")).getMap().get("aa")).
                getMap().get("aa")).getType().getClass(), BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) ((BMap) mapVaue.getMap().get("a")).getMap().get("aa")).
                getMap().get("aa")).stringValue(), "11");
    }

    @Test
    public void testSealAnyToIntMapWithoutExplicitConstraintType() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToStringMapWithoutExplicitConstraintType");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.get("firstName").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("firstName").stringValue(), "mohan");

        Assert.assertEquals(mapValue.get("lastName").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("lastName").stringValue(), "raj");
    }

    @Test
    public void testSealObjectsV1() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealObjectsV1");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue.getMap().get("age").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("age").stringValue(), "10");

        Assert.assertEquals(mapValue.get("year").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue.get("year").stringValue(), "2014");

        Assert.assertEquals(mapValue.get("month").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue.get("month").stringValue(), "february");
    }

    @Test
    public void testSealRecordMapToAnyMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordMapToAnyMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getClass(), BAnyType.class);
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getClass(), BAnyType.class);
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
    }

    @Test
    public void testSealRecordMapToSimilarOpenRecordMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordMapToSimilarOpenRecordMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("a")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("a")).getMap().get("school")).getType().
                getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("b")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("b")).getMap().get("school")).getType().
                getClass(), BAnyType.class);
    }

    @Test
    public void testSealAnyMapToSimilarOpenRecordMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToSimilarOpenRecordMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("a")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("a")).getMap().get("school")).getType().
                getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("b")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) mapValue.getMap().get("b")).getMap().get("school")).getType().
                getClass(), BAnyType.class);
    }

    @Test
    public void testSealAnyToRecordMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyToRecordMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Teacher");
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Teacher");
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
    }

    @Test
    public void testSealRecordToAnyArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToAnyArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BAnyType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealAnyToRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyToRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Teacher");
        Assert.assertEquals(mapValue1.getType().getName(), "Teacher");
    }

    @Test
    public void testSealAnyToSimilarOpenRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyToSimilarOpenRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealRecordToSimilarOpenRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToSimilarOpenRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealJSONToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToAny");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "3");
        Assert.assertEquals(anyValue.getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealAnyToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyToJSON");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "3");
        Assert.assertEquals(anyValue.getType().getClass(), BJSONType.class);
    }

    @Test
    public void testSealJSONToAnyV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToAnyV2");
        Assert.assertEquals(results.length, 5);
    }

    @Test
    public void testSealJSONToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToRecord");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
    }

    @Test
    public void testSealJSONToRecordV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToRecordV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getName(), "Employee");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BAnyType.class);

    }

    @Test
    public void testSealRecordToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testSealConstraintJSONToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintJSONToAny");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BAnyType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testSealConstraintJSONToConstraintJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintJSONToConstraintJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals(((BJSONType) mapValue0.getType()).getConstrainedType().getName(), "Person");
        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

}

