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
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for sealing Record type variables.
 *
 * @since 0.983.0
 */
public class RecordSealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/record-seal-expr-test.bal");
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


    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: error, message: incompatible seal operation: 'Teacher' value " +
                    "cannot be sealed as 'map'.*")
    public void testSealOpenRecordToMap() {
        BRunUtil.invoke(compileResult, "sealOpenRecordToMap");
    }
}

