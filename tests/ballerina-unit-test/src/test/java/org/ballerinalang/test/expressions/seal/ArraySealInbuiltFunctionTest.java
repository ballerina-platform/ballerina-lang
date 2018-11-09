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
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for sealing Array type variables.
 *
 * @since 0.983.0
 */
public class ArraySealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/array-seal-expr-test.bal");
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
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testSealRecordToSimilarOpenRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToSimilarOpenRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
        Assert.assertEquals(mapValue0.getMap().get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(mapValue0.getMap().get("school").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue1.getType().getName(), "Employee");
        Assert.assertEquals(mapValue1.getMap().get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(mapValue1.getMap().get("school").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testSealConstraintArrayToJSONArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealConstraintArrayToJSONArray");
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
    public void testSealRecordToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToAnydata");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BRecordType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BRecordType.class);
    }

    @Test
    public void testSealRecordToAnydataArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealRecordToAnydataArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals((mapValue0).getType().getClass(), BAnydataType.class);
        Assert.assertEquals((mapValue1).getType().getClass(), BAnydataType.class);
    }
}

