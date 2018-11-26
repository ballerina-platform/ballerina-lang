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
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
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

        Assert.assertEquals((mapValue0).getType().getClass(), BAnydataType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BAnydataType.class);
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

        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BJSONType.class);
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

        Assert.assertEquals((mapValue0).getType().getClass(), BRecordType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BRecordType.class);
    }

    @Test
    public void testStampRecordToAnydataArrayV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampRecordToAnydataArrayV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BAnydataType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BAnydataType.class);
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
}
