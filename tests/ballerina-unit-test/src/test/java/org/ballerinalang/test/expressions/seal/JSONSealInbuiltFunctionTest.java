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
import org.ballerinalang.model.types.BAnydataType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for sealing JSON type variables.
 *
 * @since 0.983.0
 */
public class JSONSealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/json-seal-expr-test.bal");
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
                BAnydataType.class);

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
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealJSONToMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToMapV2");
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
    public void testSealConstraintJSONToConstraintMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintJSONToConstraintMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getName(), "any");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BAnyType.class);

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

    @Test
    public void testSealJSONToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealJSONToAnydata");
        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(results[0].getType().getClass(), BAnydataType.class);
    }

    //----------------------------------- Negative Test cases ----------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: incompatible seal operation: 'json' value " +
                    "cannot be sealed as 'Student'.*")
    public void testSealJSONToRecordNegative() {
        BRunUtil.invoke(compileResult, "sealJSONToRecordNegative");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: incompatible seal operation: 'json' value " +
                    "cannot be sealed as 'map<string>'.*")
    public void testSealJSONToMapNegative() {
        BRunUtil.invoke(compileResult, "sealJSONToMapNegative");
    }
}
