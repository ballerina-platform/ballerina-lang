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
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANYDATA
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
import org.ballerinalang.core.model.types.BXMLType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for stamping Anydata type variables.
 *
 * @since 0.985.0
 */
public class AnydataStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/anydata-stamp-expr-test.bal");
    }


    //----------------------------- Anydata Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampAnydataToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToJSON");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "3");
        Assert.assertEquals(anydataValue.getType().getTag(), TypeTags.INT_TAG);
    }

    @Test
    public void testStampAnydataToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampAnydataToJSONV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToJSONV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 5);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampAnydataToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToXML");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anydataValue.getType().getClass(), BXMLType.class);
    }

    @Test
    public void testStampAnydataToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
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
    public void testStampAnydataToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToTuple");
        Assert.assertEquals(results.length, 2);

        BValue tupleValue1 = results[0];
        BValue tupleValue2 = results[1];

        Assert.assertEquals(tupleValue1.stringValue(), "Mohan");
        Assert.assertEquals(tupleValue1.getType().getClass(), BStringType.class);

        Assert.assertEquals(tupleValue2.getType().getClass(), BRecordType.class);
        Assert.assertEquals(tupleValue2.getType().getName(), "Teacher");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).stringValue(), "Raja");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).stringValue(), "25");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).getType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).stringValue(), "single");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).stringValue(), "LK2014");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).stringValue(), "Hindu College");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testStampAnydataMapToAnydataMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataMapToAnydataMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampAnydataToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataToAnydata");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampAnydataMapToUnion() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataMapToUnion");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BJSONType.class);

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BStringType.class);
    }
}
