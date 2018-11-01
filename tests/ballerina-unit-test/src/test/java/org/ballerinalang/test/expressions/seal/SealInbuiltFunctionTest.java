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
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
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


    //------------------------Record Seal Test cases ----------------------------------------------------

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
    public void testSealRecordToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToAny");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BAnyType.class);
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
    public void testSealRecordToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToMap");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "John");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnyType.class);

    }

    @Test
    public void testSealRecordToMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToMapV2");
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
    public void testSealRecordToMapV3() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToMapV3");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue0.get("age").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnyType.class);

        Assert.assertEquals(((BMap) mapValue0.get("emp")).size(), 3);
        Assert.assertEquals(mapValue0.get("emp").getType().getClass(), BAnyType.class);
    }

    //----------------------------- JSON Seal Test cases ------------------------------------------------------

    @Test
    public void testSealJSONToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToAny");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "3");
        Assert.assertEquals(anyValue.getType().getClass(), BAnyType.class);
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
    public void testSealJSONToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
    }

    @Test
    public void testSealJSONToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToMap");
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
    public void testSealJSONToMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue0.get("age").getType().getName(), "int");

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BStringType.class);

        Assert.assertEquals(((BMap) mapValue0.get("emp")).size(), 3);
        Assert.assertEquals(mapValue0.get("emp").getType().getClass(), BJSONType.class);
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
    public void testSealConstraintJSONToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintJSONToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

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

    @Test
    public void testSealConstraintJSONToConstraintMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintJSONToConstraintMap");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getName(), "Student");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testSealConstraintJSONToConstraintMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintJSONToConstraintMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getName(), "any");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }


    @Test
    public void testSealJSONArrayToConstraintArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONArrayToConstraintArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Student");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue1.getType().getName(), "Student");

        Assert.assertEquals((mapValue1.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testSealJSONArrayToPrimitiveTypeArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONArrayToPrimitiveTypeArray");
        Assert.assertEquals(results.length, 4);
        Assert.assertEquals(results[0].stringValue(), "1");
        Assert.assertEquals(results[0].getType().getName(), "int");
        Assert.assertEquals(results[1].stringValue(), "2");
        Assert.assertEquals(results[1].getType().getName(), "int");
        Assert.assertEquals(results[2].stringValue(), "3");
        Assert.assertEquals(results[2].getType().getName(), "int");
        Assert.assertEquals(results[3].stringValue(), "4");
        Assert.assertEquals(results[3].getType().getName(), "int");
    }

    @Test
    public void testSealJSONArrayToAnyTypeArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONArrayToAnyTypeArray");
        Assert.assertEquals(results.length, 4);
        Assert.assertEquals(results[0].stringValue(), "1");
        Assert.assertEquals(results[0].getType().getClass(), BAnyType.class);
        Assert.assertEquals(results[1].stringValue(), "false");
        Assert.assertEquals(results[1].getType().getClass(), BAnyType.class);
        Assert.assertEquals(results[2].stringValue(), "foo");
        Assert.assertEquals(results[2].getType().getClass(), BAnyType.class);
        Assert.assertEquals(((LinkedHashMap) ((BMap) results[3]).getMap()).size(), 2);
        Assert.assertEquals(results[3].getType().getClass(), BAnyType.class);
    }

    //----------------------------- XML Seal Test cases ------------------------------------------------------

    @Test
    public void testSealXMLToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealXMLToAny");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anyValue.getType().getClass(), BAnyType.class);
    }

    //----------------------------- Object Seal Test cases ------------------------------------------------------


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
    public void testSealObjectToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealObjectsToAny");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.getMap().get("age").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("age").stringValue(), "10");

        Assert.assertEquals(mapValue.get("year").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("year").stringValue(), "2014");

        Assert.assertEquals(mapValue.get("month").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("month").stringValue(), "february");
    }


    //----------------------------- Map Seal Test cases ------------------------------------------------------


    @Test
    public void testSealIntMapToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealIntMapToRecord");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "IntRecord");

        Assert.assertEquals(mapValue.get("a").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("a").stringValue(), "1");

        Assert.assertEquals(mapValue.get("b").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("b").stringValue(), "2");
    }

    @Test
    public void testSealIntMapToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealIntMapToJSON");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BJSONType.class);

        Assert.assertEquals(mapValue.get("a").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("a").stringValue(), "1");

        Assert.assertEquals(mapValue.get("b").getType().getName(), "int");
        Assert.assertEquals(mapValue.get("b").stringValue(), "2");
    }

    @Test
    public void testSealIntMapToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealIntMapToAny");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BAnyType.class);
    }


    @Test
    public void testSealIntMapToIntMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealIntMapToIntMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals((((BMapType) mapValue.getType()).getConstrainedType().getName()), "int");
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
    public void testSealAnyMapToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToRecord");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue.getType().getName(), "Teacher");

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getName(), "int");

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue.get("school").getType().getClass(), BStringType.class);

    }

    @Test
    public void testSealAnyMapToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToJSON");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BJSONType.class);

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getName(), "int");

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue.get("school").getType().getClass(), BStringType.class);

    }

    @Test
    public void testSealAnyMapToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToAny");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getName(), "int");

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BStringType.class);

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
    public void testSealAnyMapToRecordMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToRecordMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Teacher");
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Teacher");
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
    }

    @Test
    public void testSealAnyMapToJSONMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyMapToJSONMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BJSONType.class);

        Assert.assertEquals(mapValue.get("a").getType().getClass(), BJSONType.class);
        Assert.assertEquals(mapValue.get("b").getType().getClass(), BJSONType.class);
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
    public void testSealRecordMapToJSONMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordMapToJSONMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BJSONType.class);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getClass(), BJSONType.class);
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getClass(), BJSONType.class);
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
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

    //----------------------------- Array Seal Test cases ------------------------------------------------------

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
    public void testSealAnyToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnyToJSON");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "3");
        Assert.assertEquals(anyValue.getType().getClass(), BJSONType.class);
    }

}

