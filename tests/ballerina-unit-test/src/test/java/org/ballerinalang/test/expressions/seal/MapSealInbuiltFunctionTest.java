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
 * Test cases for sealing Map type variables.
 *
 * @since 0.983.0
 */
public class MapSealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/map-seal-expr-test.bal");
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
    public void testSealJSONMapToRecordMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONMapToRecordMap");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 2);

        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BRecordType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getName(), "Employee");

        Assert.assertEquals(employee0.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BValue)((BMap)employee0.getMap().get("a")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)((BMap)employee0.getMap().get("a")).getMap().get("school")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)((BMap)employee0.getMap().get("a")).getMap().get("batch")).getType().getClass(),
                BStringType.class);


        Assert.assertEquals(employee0.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BValue)((BMap)employee0.getMap().get("b")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)((BMap)employee0.getMap().get("b")).getMap().get("school")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)((BMap)employee0.getMap().get("b")).getMap().get("batch")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testSealRecordTypeMultiDimensionMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordTypeMultiDimensionMap");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 2);
        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType)employee0.getType()).getConstrainedType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) ((BMapType)employee0.getType()).getConstrainedType()).getConstrainedType().
                getName(), "Employee");

        Assert.assertEquals(employee0.getMap().get("aa").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getMap().get("aa").getType()).getConstrainedType().getClass(),
                BRecordType.class);
        Assert.assertEquals(((BMapType) employee0.getMap().get("aa").getType()).getConstrainedType().getName(),
                "Employee");

        Assert.assertEquals(employee0.getMap().get("bb").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getMap().get("bb").getType()).getConstrainedType().getClass(),
                BRecordType.class);
        Assert.assertEquals(((BMapType) employee0.getMap().get("bb").getType()).getConstrainedType().getName(),
                "Employee");


        Assert.assertEquals(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("a")).getType().getName(),
                "Employee");
        Assert.assertEquals(((BValue)(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("a"))).getMap().
                        get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("a"))).getMap().
                        get("school")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("a"))).getMap().
                        get("batch")).getType().getClass(),
                BStringType.class);


        Assert.assertEquals(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("b")).getType().getName(),
                "Employee");
        Assert.assertEquals(((BValue)(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("b"))).getMap().
                        get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("b"))).getMap().
                        get("school")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue)(((BMap)((BMap)employee0.getMap().get("bb")).getMap().get("b"))).getMap().
                        get("batch")).getType().getClass(),
                BStringType.class);
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

}

